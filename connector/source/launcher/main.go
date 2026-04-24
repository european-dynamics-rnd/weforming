package main

import (
	"github.com/AgustinSRG/glog"
	child_process_manager "github.com/AgustinSRG/go-child-process-manager"
)

func main() {
	err := child_process_manager.InitializeChildProcessManager()
	if err != nil {
		panic(err)
	}

	// Call DisposeChildProcessManager() just before exiting the main process
	defer child_process_manager.DisposeChildProcessManager()

	config := LoadLauncherConfig()

	logLevel, _ := glog.LevelFromString(config.logLevel)

	logger := glog.CreateRootLogger(glog.CreateLoggerConfigurationFromLevel(logLevel), glog.StandardLogFunction)

	launcherLogger := logger.CreateChildLogger("[Launcher] ")

	launcherLogger.Info("Initializing...")

	setupParticipantDid(config, launcherLogger)

	connectorPropertiesFile, identityHubPropertiesFile := preparePropertiesFiles(config, launcherLogger)

	wg := launchJars(config, launcherLogger, connectorPropertiesFile, identityHubPropertiesFile)

	initializeIdentityHub(config, launcherLogger)

	wg.Wait()
}
