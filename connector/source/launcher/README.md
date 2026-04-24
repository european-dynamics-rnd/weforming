# EDC launcher

This is a program made to launch the Eclipse data space Connector and Identity Hub.

It performs the following tasks:

- Generates the `*.properties` files for the Connector and Identity Hub.
- Launches the Connector and Identity Hub, using the Java runtime.
- Registers the default participant in the Identity Hub, and passes the secret to the Connector, via its API.

## Configuration

The launcher can be configured with environment variables.

### Log level

| Variable    | Description                                                            |
| ----------- | ---------------------------------------------------------------------- |
| `LOG_LEVEL` | Log level. Can be `ERROR`, `WARNING`, `INFO`, `DEBUG`. Default: `INFO` |

### Java runtime

| Variable                       | Description                                                           |
| ------------------------------ | --------------------------------------------------------------------- |
| `JAVA_BINARY`                  | Name or path of the java binary to run the connector and identity Hub |
| `JAVA_RUNTIME_FLAGS_CONNECTOR` | Java runtime flags for the connector                                  |
| `JAVA_RUNTIME_FLAGS_IH`        | Java runtime flags for the identity hub                               |

### Jar files

| Variable           | Description                  |
| ------------------ | ---------------------------- |
| `CONNECTOR_JAR`    | Path to the connector JAR    |
| `IDENTITY_HUB_JAR` | Path to the identity hub JAR |

### Identity

| Variable                | Description                                                                                        |
| ----------------------- | -------------------------------------------------------------------------------------------------- |
| `PARTICIPANT_ID`        | ID of the participant (eg: `did:indy:besu:wf:0x...`)                                               |
| `PUBLIC_KEY`            | Path to the public key file (PEM format)                                                           |
| `PRIVATE_KEY`           | Path to the private key file (PEM format)                                                          |
| `IDENTITY_PROXY_URL`    | URL of the identity proxy                                                                          |
| `IDENTITY_PROXY_TOKEN`  | Authentication token for the identity proxy API                                                    |
| `VC_FOLDER`             | Folder containing the signed verified credentials                                                  |
| `TRUSTED_ISSUERS`       | List of trusted issuers, separated by commas (eg: `did:indy:besu:wf:0x...,did:indy:besu:wf:0x...`) |
| `DID_OWNER_PRIVATE_KEY` | Optional. Ethereum private key owner of the DID, in order to update the DID document.              |
| `DID_PUBLIC_KEY_TYPE`   | Public key type, in order to register into the DID document. Default: `Ed25519Signature2018`       |

### Ports (Connector)

| Variable                    | Description                                                                    | Default |
| --------------------------- | ------------------------------------------------------------------------------ | ------- |
| `CONNECTOR_PORT_BASE`       | Base port. Any endpoints without a context will be handled in this port.       | `8081`  |
| `CONNECTOR_PORT_PUBLIC`     | Public port. This port serves static assets.                                   | `8082`  |
| `CONNECTOR_PORT_CONTROL`    | Control API port. API to control the connector.                                | `8083`  |
| `CONNECTOR_PORT_MANAGEMENT` | Management API port. API to control the connector.                             | `8084`  |
| `CONNECTOR_PORT_PROTOCOL`   | Data space protocol port. Must be open to the rest of data space participants. | `8085`  |

### Ports (Identity Hub)

| Variable              | Description                                                                                           | Default |
| --------------------- | ----------------------------------------------------------------------------------------------------- | ------- |
| `IH_PORT_BASE`        | Base port. Any endpoints without a context will be handled in this port.                              | `7081`  |
| `IH_PORT_CREDENTIALS` | Credentials management API port.                                                                      | `7082`  |
| `IH_PORT_IDENTITY`    | Identity API port. This port must be exposed to other participants in order to request presentations. | `7083`  |
| `IH_PORT_DID`         | Port to serve Web DIDs.                                                                               | `7084`  |
| `IH_PORT_VERSION`     | Version port. This part exposes the version.                                                          | `7085`  |
| `IH_PORT_STS`         | STS port used by the connector to communicate with the identity hub.                                  | `7086`  |

### External URLs

| Variable                          | Description                                                                               |
| --------------------------------- | ----------------------------------------------------------------------------------------- |
| `CREDENTIAL_SERVICE_EXTERNAL_URL` | External URI for the credential service (Identity Hub). Example: `https://localhost:7082` |
| `PROTOCOL_EXTERNAL_URL`           | External URI for the connector's protocol. Example: `https://localhost:8085`              |

### Persistence

| Variable            | Description                                   |
| ------------------- | --------------------------------------------- |
| `POSTGRES_HOST`     | Postgres host                                 |
| `POSTGRES_PORT`     | Postgres port (default: `5432`)               |
| `POSTGRES_DB_NAME`  | Postgres database name (default: `edc_store`) |
| `POSTGRES_USER`     | Postgres user (default: `postgres`)           |
| `POSTGRES_PASSWORD` | Postgres password (default: `postgres`)       |

### Identity Hub super-user

| Variable               | Description                            |
| ---------------------- | -------------------------------------- |
| `IH_SUPER_USER`        | Name of the Identity Hub super user    |
| `IH_SUPER_USER_SECRET` | Secret for the Identity Hub super user |

## Compilation

In order to compile this program, run:

```sh
go build .
```

A binary with name `launcher` will be created.
