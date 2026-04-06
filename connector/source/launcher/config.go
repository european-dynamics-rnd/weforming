package main

import (
	"strings"

	"github.com/AgustinSRG/genv"
)

type JavaConfig struct {
	javaBinary              string
	runtimeFlagsConnector   string
	runtimeFlagsIdentityHub string
}

type JarConfig struct {
	connectorJar   string
	identityHubJar string
}

type IdentityConfig struct {
	participantId      string
	publicKey          string
	privateKey         string
	identityProxyUrl   string
	identityProxyToken string
	vcFolder           string
	trustedIssuers     []string
	didOwnerPrivateKey string
	didPublicKeyType   string
}

type ConnectorPortsConfig struct {
	base       int
	public     int
	control    int
	management int
	protocol   int
}

type IdentityHubPortsConfig struct {
	base        int
	credentials int
	identity    int
	did         int
	version     int
	sts         int
}

type ExternalURLsConfig struct {
	credentialServiceUrl string
	protocolUrl          string
}

type PostgresConfig struct {
	host     string
	port     int
	dbName   string
	user     string
	password string
}

type IdentityHubSuperUserConfig struct {
	user   string
	secret string
}

type LauncherConfig struct {
	logLevel string

	java                 JavaConfig
	jars                 JarConfig
	identity             IdentityConfig
	connectorPorts       ConnectorPortsConfig
	identityHubPorts     IdentityHubPortsConfig
	externalUrls         ExternalURLsConfig
	postgres             PostgresConfig
	identityHubSuperUser IdentityHubSuperUserConfig
}

func LoadLauncherConfig() *LauncherConfig {
	trustedIssuersStr := genv.GetEnvString("TRUSTED_ISSUERS", "")
	var trustedIssuers []string
	if trustedIssuersStr != "" {
		trustedIssuers = strings.Split(trustedIssuersStr, ",")
	}

	return &LauncherConfig{
		logLevel: genv.GetEnvString("LOG_LEVEL", "INFO"),

		java: JavaConfig{
			javaBinary:              genv.GetEnvString("JAVA_BINARY", "java"),
			runtimeFlagsConnector:   genv.GetEnvString("JAVA_RUNTIME_FLAGS_CONNECTOR", ""),
			runtimeFlagsIdentityHub: genv.GetEnvString("JAVA_RUNTIME_FLAGS_IH", ""),
		},

		jars: JarConfig{
			connectorJar:   genv.GetEnvString("CONNECTOR_JAR", "connector.jar"),
			identityHubJar: genv.GetEnvString("IDENTITY_HUB_JAR", "identity-hub.jar"),
		},

		identity: IdentityConfig{
			participantId:      genv.GetEnvString("PARTICIPANT_ID", ""),
			publicKey:          genv.GetEnvString("PUBLIC_KEY", ""),
			privateKey:         genv.GetEnvString("PRIVATE_KEY", ""),
			identityProxyUrl:   genv.GetEnvString("IDENTITY_PROXY_URL", ""),
			identityProxyToken: genv.GetEnvString("IDENTITY_PROXY_TOKEN", ""),
			vcFolder:           genv.GetEnvString("VC_FOLDER", ""),
			trustedIssuers:     trustedIssuers,
			didOwnerPrivateKey: genv.GetEnvString("DID_OWNER_PRIVATE_KEY", ""),
			didPublicKeyType:   genv.GetEnvString("DID_PUBLIC_KEY_TYPE", "Ed25519Signature2018"),
		},

		connectorPorts: ConnectorPortsConfig{
			base:       genv.GetEnvInt("CONNECTOR_PORT_BASE", 8081),
			public:     genv.GetEnvInt("CONNECTOR_PORT_PUBLIC", 8082),
			control:    genv.GetEnvInt("CONNECTOR_PORT_CONTROL", 8083),
			management: genv.GetEnvInt("CONNECTOR_PORT_MANAGEMENT", 8084),
			protocol:   genv.GetEnvInt("CONNECTOR_PORT_PROTOCOL", 8085),
		},

		identityHubPorts: IdentityHubPortsConfig{
			base:        genv.GetEnvInt("IH_PORT_BASE", 7081),
			credentials: genv.GetEnvInt("IH_PORT_CREDENTIALS", 7082),
			identity:    genv.GetEnvInt("IH_PORT_IDENTITY", 7083),
			did:         genv.GetEnvInt("IH_PORT_DID", 7084),
			version:     genv.GetEnvInt("IH_PORT_VERSION", 7085),
			sts:         genv.GetEnvInt("IH_PORT_STS", 7086),
		},

		externalUrls: ExternalURLsConfig{
			credentialServiceUrl: genv.GetEnvString("CREDENTIAL_SERVICE_EXTERNAL_URL", ""),
			protocolUrl:          genv.GetEnvString("PROTOCOL_EXTERNAL_URL", ""),
		},

		postgres: PostgresConfig{
			host:     genv.GetEnvString("POSTGRES_HOST", ""),
			port:     genv.GetEnvInt("POSTGRES_PORT", 5432),
			dbName:   genv.GetEnvString("POSTGRES_DB_NAME", "edc_store"),
			user:     genv.GetEnvString("POSTGRES_USER", "postgres"),
			password: genv.GetEnvString("POSTGRES_PASSWORD", "postgres"),
		},

		identityHubSuperUser: IdentityHubSuperUserConfig{
			user:   genv.GetEnvString("IH_SUPER_USER", "super-user"),
			secret: genv.GetEnvString("IH_SUPER_USER_SECRET", "super-secret-key"),
		},
	}
}
