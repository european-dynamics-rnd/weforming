package main

import (
	"bufio"
	"io"
	"log"
	"os"
	"os/exec"
	"sync"

	"github.com/AgustinSRG/glog"
	child_process_manager "github.com/AgustinSRG/go-child-process-manager"
	shellquote "github.com/kballard/go-shellquote"
)

func logPipe(pipe io.ReadCloser, prefix string) {
	reader := bufio.NewReader(pipe)

	var finished = false

	for !finished {
		line, err := reader.ReadString('\n')

		if err != nil {
			finished = true
		}

		if len(line) < 2 {
			continue
		}

		line = line[0 : len(line)-1] // Remove delimiter

		log.Println(prefix + line)
	}
}

func launchProcess(cmd *exec.Cmd, logger *glog.Logger, processLoggerPrefix string, wg *sync.WaitGroup) error {
	// Configure command
	err := child_process_manager.ConfigureCommand(cmd)
	if err != nil {
		return err
	}

	// Create a pipe to read StdOut
	pipeOut, err := cmd.StdoutPipe()

	if err != nil {
		return err
	}

	// Create a pipe to read StdErr
	pipeErr, err := cmd.StderrPipe()

	if err != nil {
		return err
	}

	// Start the command

	logger.Info("Running command: " + cmd.String())

	err = cmd.Start()

	if err != nil {
		return err
	}

	// Add process as a child process
	_ = child_process_manager.AddChildProcess(cmd.Process)

	go logPipe(pipeOut, processLoggerPrefix)
	go logPipe(pipeErr, processLoggerPrefix)

	go func() {
		err = cmd.Wait()

		if err != nil {
			logger.Errorf("Error: %v", err)
		}

		logger.Error(processLoggerPrefix + "Process ended prematurely")

		wg.Done()

		os.Exit(1)
	}()

	return nil
}

func launchIdentityHub(config *LauncherConfig, logger *glog.Logger, identityHubPropertiesFile string, wg *sync.WaitGroup) {
	logger.Info("Launching identity hub...")

	cmd := exec.Command(config.java.javaBinary)

	args := make([]string, 1)

	args[0] = config.java.javaBinary

	if len(config.java.runtimeFlagsConnector) > 0 {
		flags, err := shellquote.Split(config.java.runtimeFlagsIdentityHub)

		if err != nil {
			logger.Errorf("Invalid value of JAVA_RUNTIME_FLAGS_CONNECTOR: %v", err)
			os.Exit(1)
		}

		args = append(args, flags...)
	}

	args = append(args, "-jar")

	args = append(args, "-Dedc.fs.config="+identityHubPropertiesFile)

	args = append(args, config.jars.identityHubJar)

	args = append(args, "--log-level="+config.logLevel)

	cmd.Args = args

	err := launchProcess(cmd, logger, "[IDENTITY HUB] ", wg)

	if err != nil {
		logger.Errorf("Error launching identity hub: %v", err)
		os.Exit(1)
	}
}

func launchConnector(config *LauncherConfig, logger *glog.Logger, connectorPropertiesFile string, wg *sync.WaitGroup) {
	logger.Info("Launching connector...")

	cmd := exec.Command(config.java.javaBinary)

	args := make([]string, 1)

	args[0] = config.java.javaBinary

	if len(config.java.runtimeFlagsConnector) > 0 {
		flags, err := shellquote.Split(config.java.runtimeFlagsConnector)

		if err != nil {
			logger.Errorf("Invalid value of JAVA_RUNTIME_FLAGS_CONNECTOR: %v", err)
			os.Exit(1)
		}

		args = append(args, flags...)
	}

	args = append(args, "-jar")

	args = append(args, "-Dedc.fs.config="+connectorPropertiesFile)

	args = append(args, config.jars.connectorJar)

	args = append(args, "--log-level="+config.logLevel)

	cmd.Args = args

	err := launchProcess(cmd, logger, "[CONNECTOR] ", wg)

	if err != nil {
		logger.Errorf("Error launching connector: %v", err)
		os.Exit(1)
	}
}

func launchJars(config *LauncherConfig, logger *glog.Logger, connectorPropertiesFile string, identityHubPropertiesFile string) *sync.WaitGroup {
	// Check configuration

	if len(config.java.javaBinary) == 0 {
		logger.Error("Missing required configuration value: JAVA_BINARY")
		os.Exit(1)
	}

	if len(config.jars.connectorJar) == 0 {
		logger.Error("Missing required configuration value: CONNECTOR_JAR")
		os.Exit(1)
	}

	if len(config.jars.identityHubJar) == 0 {
		logger.Error("Missing required configuration value: IDENTITY_HUB_JAR")
		os.Exit(1)
	}

	wg := &sync.WaitGroup{}
	wg.Add(2)

	// Launch connector

	launchConnector(config, logger, connectorPropertiesFile, wg)

	// Launch identity Hub

	launchIdentityHub(config, logger, identityHubPropertiesFile, wg)

	return wg
}
