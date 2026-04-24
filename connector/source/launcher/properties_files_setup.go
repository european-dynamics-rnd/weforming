package main

import (
	"encoding/base64"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/AgustinSRG/glog"
)

func preparePropertiesFiles(config *LauncherConfig, logger *glog.Logger) (connectorPropertiesFile string, identityHubPropertiesFile string) {
	connectorPropertiesFile, err := filepath.Abs("./connector.properties")

	if err != nil {
		logger.Errorf("Error: %v", err)
		os.Exit(1)
	}

	connectorProperties := ""

	// Paths

	connectorProperties += "web.http.path=/api" + "\n"
	connectorProperties += "web.http.public.path=/public" + "\n"
	connectorProperties += "web.http.control.path=/control" + "\n"
	connectorProperties += "web.http.management.path=/management" + "\n"
	connectorProperties += "web.http.protocol.path=/protocol" + "\n"

	// Keys

	connectorProperties += "\n"

	connectorProperties += "edc.transfer.proxy.token.signer.privatekey.alias=private-key" + "\n"
	connectorProperties += "edc.transfer.proxy.token.verifier.publickey.alias=public-key" + "\n"

	// Identity proxy

	connectorProperties += "\n"

	connectorProperties += "weforming.identity.proxy.url=" + config.identity.identityProxyUrl + "\n"
	connectorProperties += "weforming.identity.proxy.token=" + config.identity.identityProxyToken + "\n"

	if logger.Config.DebugEnabled {
		connectorProperties += "weforming.identity.debug=true" + "\n"
	} else {
		connectorProperties += "weforming.identity.debug=false" + "\n"
	}

	// Trusted issuers

	connectorProperties += "\n"

	connectorProperties += "connector.trusted.issuers=" + strings.Join(config.identity.trustedIssuers, ",") + "\n"

	// Participant ID

	connectorProperties += "\n"

	connectorProperties += "edc.participant.id=" + config.identity.participantId + "\n"
	connectorProperties += "edc.iam.issuer.id=" + config.identity.participantId + "\n"

	// Ports

	connectorProperties += "\n"

	connectorProperties += "web.http.port=" + fmt.Sprint(config.connectorPorts.base) + "\n"
	connectorProperties += "web.http.public.port=" + fmt.Sprint(config.connectorPorts.public) + "\n"
	connectorProperties += "web.http.control.port=" + fmt.Sprint(config.connectorPorts.control) + "\n"
	connectorProperties += "web.http.management.port=" + fmt.Sprint(config.connectorPorts.management) + "\n"
	connectorProperties += "web.http.protocol.port=" + fmt.Sprint(config.connectorPorts.protocol) + "\n"

	// Key pair

	connectorProperties += "\n"

	connectorProperties += "connector.keys.public.path=" + config.identity.publicKey + "\n"
	connectorProperties += "connector.keys.private.path=" + config.identity.privateKey + "\n"

	// Callback address

	connectorProperties += "\n"

	protocolEndpoint := concatUrl(config.externalUrls.protocolUrl, "/protocol")
	connectorProperties += "edc.dsp.callback.address=" + protocolEndpoint + "\n"

	// Identity Hub STS client

	connectorProperties += "\n"
	connectorProperties += "edc.iam.sts.oauth.client.id=" + config.identity.participantId + "\n"
	connectorProperties += "edc.iam.sts.oauth.token.url=" + "http://127.0.0.1:" + fmt.Sprint(config.identityHubPorts.sts) + "/api/sts/token" + "\n"
	connectorProperties += "edc.iam.sts.oauth.client.secret.alias=" + config.identity.participantId + "-sts-client-secret" + "\n"
	connectorProperties += "edc.iam.sts.publickey.id=" + config.identity.participantId + "#key-1" + "\n"

	// Postgres

	connectorProperties += "\n"
	connectorProperties += "edc.sql.schema.autocreate=true" + "\n"

	dataSources := []string{"default", "asset", "policy", "contractdefinition", "contractnegotiation", "transferprocess"}

	for _, source := range dataSources {
		connectorProperties += "\n"
		connectorProperties += "edc.datasource." + source + ".name=" + source + "\n"
		connectorProperties += "edc.datasource." + source + ".url=jdbc:postgresql://" + config.postgres.host + ":" + fmt.Sprint(config.postgres.port) + "/" + config.postgres.dbName + "\n"
		connectorProperties += "edc.datasource." + source + ".user=" + config.postgres.user + "\n"
		connectorProperties += "edc.datasource." + source + ".password=" + config.postgres.password + "\n"
	}

	logger.Debugf("Write %v \n%v", connectorPropertiesFile, connectorProperties)
	os.WriteFile(connectorPropertiesFile, []byte(connectorProperties), FILE_PERMISSION)

	identityHubPropertiesFile, err = filepath.Abs("./identity-hub.properties")

	if err != nil {
		logger.Errorf("Error: %v", err)
		os.Exit(1)
	}

	identityHubProperties := ""

	// Paths

	identityHubProperties += "web.http.path=/api" + "\n"
	identityHubProperties += "web.http.credentials.path=/api/credentials" + "\n"
	identityHubProperties += "web.http.identity.path=/api/identity" + "\n"
	identityHubProperties += "web.http.did.path=/" + "\n"
	identityHubProperties += "web.http.version.path=/api/version" + "\n"
	identityHubProperties += "web.http.sts.path=/api/sts" + "\n"

	// Keys

	identityHubProperties += "\n"

	identityHubProperties += "edc.iam.sts.privatekey.alias=private-key" + "\n"

	// Identity proxy

	identityHubProperties += "\n"

	identityHubProperties += "weforming.identity.proxy.url=" + config.identity.identityProxyUrl + "\n"
	identityHubProperties += "weforming.identity.proxy.token=" + config.identity.identityProxyToken + "\n"

	if logger.Config.DebugEnabled {
		identityHubProperties += "weforming.identity.debug=true" + "\n"
	} else {
		identityHubProperties += "weforming.identity.debug=false" + "\n"
	}

	// Client secret

	identityHubProperties += "\n"

	identityHubProperties += "edc.ih.client.id=" + config.identityHubSuperUser.user + "\n"

	identityHubProperties += "edc.ih.client.secret=" +
		base64.StdEncoding.EncodeToString([]byte(config.identityHubSuperUser.user)) +
		"." +
		base64.StdEncoding.EncodeToString([]byte(config.identityHubSuperUser.secret)) + "\n"

	// Ports

	identityHubProperties += "\n"

	identityHubProperties += "web.http.port=" + fmt.Sprint(config.identityHubPorts.base) + "\n"
	identityHubProperties += "web.http.credentials.port=" + fmt.Sprint(config.identityHubPorts.credentials) + "\n"
	identityHubProperties += "web.http.identity.port=" + fmt.Sprint(config.identityHubPorts.identity) + "\n"
	identityHubProperties += "web.http.did.port=" + fmt.Sprint(config.identityHubPorts.did) + "\n"
	identityHubProperties += "web.http.version.port=" + fmt.Sprint(config.identityHubPorts.version) + "\n"
	identityHubProperties += "web.http.sts.port=" + fmt.Sprint(config.identityHubPorts.sts) + "\n"

	// Key pair

	identityHubProperties += "\n"

	identityHubProperties += "connector.keys.public.path=" + config.identity.publicKey + "\n"
	identityHubProperties += "connector.keys.private.path=" + config.identity.privateKey + "\n"

	// Credentials path

	identityHubProperties += "\n"

	identityHubProperties += "identity.hub.credentials.path=" + config.identity.vcFolder + "\n"

	logger.Debugf("Write %v \n%v", identityHubPropertiesFile, identityHubProperties)
	os.WriteFile(identityHubPropertiesFile, []byte(identityHubProperties), FILE_PERMISSION)

	return connectorPropertiesFile, identityHubPropertiesFile
}
