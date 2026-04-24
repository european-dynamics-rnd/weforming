# WeForming Connector - Docker deployment

This folder contains a deployment for the Weforming connector using Docker and Docker Compose.

## Requirements

- [Docker](https://www.docker.com/)
- Access to the WeForming connector Docker image
- Access to an identity proxy, necessary in order to resolve the DIDs of the participants.
- A role (`STEWARD` or `TRUSTEE`) in the WeForming Indy-Besu identity network.
- At least one verifiable credentials, in the form of a JSON file.

## Configuration

Copy `.env.example` into `.env` in order to configure all the necessary variables to tun the connector.

### Docker image

| Variable    | Description                                                            |
| ----------- | ---------------------------------------------------------------------- |
| `CONNECTOR_IMAGE` | Name of the WeForming connector image. Default: `weforming-connector`| 

### Log level

| Variable    | Description                                                            |
| ----------- | ---------------------------------------------------------------------- |
| `LOG_LEVEL` | Log level. Can be `ERROR`, `WARNING`, `INFO`, `DEBUG`. Default: `INFO` |

### Java runtime

| Variable                       | Description                             |
| ------------------------------ | --------------------------------------- |
| `JAVA_RUNTIME_FLAGS_CONNECTOR` | Java runtime flags for the connector    |
| `JAVA_RUNTIME_FLAGS_IH`        | Java runtime flags for the identity hub |

You can use these flags to increase the memory of the JVM. Example: `-Xms2G -Xmx2G`.

### Identity

| Variable         | Description                                          |
| ---------------- | ---------------------------------------------------- |
| `PARTICIPANT_ID` | ID of the participant (eg: `did:indy:besu:wf:0x...`) |

### Identity proxy

| Variable               | Description                                     |
| ---------------------- | ----------------------------------------------- |
| `IDENTITY_PROXY_URL`   | URL of the identity proxy                       |
| `IDENTITY_PROXY_TOKEN` | Authentication token for the identity proxy API |

### Keys

The connector needs a key pair to operate. They must be provided in PEM format, and placed in the [keys](./keys/) folder.

If you wish to generate them, you can use OpenSSL:

```sh
# Generate the private key
openssl genpkey -algorithm ed25519 -out keys/private.pem

# Generate the public key
openssl pkey -in keys/private.pem -pubout -out keys/public.pem
```

Once you placed them, you must set up 2 variables in order to indicate the name of the keys files:

| Variable           | Description                                         |
| ------------------ | --------------------------------------------------- |
| `PUBLIC_KEY_NAME`  | Name of the public key file. Example: `public.pem`  |
| `PRIVATE_KEY_NAME` | Name of the private key file. Example: `public.pem` |

Also, if you changed the algorithm of the key, you must indicate it:

| Variable              | Description                                                  |
| --------------------- | ------------------------------------------------------------ |
| `DID_PUBLIC_KEY_TYPE` | Public key type, in order to register into the DID document. |

Possible values for `DID_PUBLIC_KEY_TYPE`:

- `Ed25519VerificationKey2018` - Default. For Ed25519 keys.
- `RsaVerificationKey2018`- For RSA keys.
- `EcdsaSecp256k1VerificationKey2019` - For Secp256k1 ECDSA keys.

Also, if you want the launcher to automatically update the DID document (recommended), you must include the private key of the Ethereum wallet owner of the DID:

| Variable                | Description                                                                 |
| ----------------------- | --------------------------------------------------------------------------- |
| `DID_OWNER_PRIVATE_KEY` | Ethereum private key owner of the DID, in order to update the DID document. |

### Verifiable credentials

Place your verifiable credentials inside the [credentials](./credentials/) folder.

The credentials are JSON files, including a signature of a trusted issuer.

Make sure to set the list of trusted issuers to the official list of Trusted Issuers provided by the WeForming administration team:

| Variable          | Description                                                                                        |
| ----------------- | -------------------------------------------------------------------------------------------------- |
| `TRUSTED_ISSUERS` | List of trusted issuers, separated by commas (eg: `did:indy:besu:wf:0x...,did:indy:besu:wf:0x...`) |

### Ports (Connector)

Ports that the connector will use. They must be available in the host.

| Variable                    | Description                                                                    | Default |
| --------------------------- | ------------------------------------------------------------------------------ | ------- |
| `CONNECTOR_PORT_BASE`       | Base port. Any endpoints without a context will be handled in this port.       | `8081`  |
| `CONNECTOR_PORT_PUBLIC`     | Public port. This port serves static assets.                                   | `8082`  |
| `CONNECTOR_PORT_CONTROL`    | Control API port. API to control the connector.                                | `8083`  |
| `CONNECTOR_PORT_MANAGEMENT` | Management API port. API to control the connector.                             | `8084`  |
| `CONNECTOR_PORT_PROTOCOL`   | Data space protocol port. Must be open to the rest of data space participants. | `8085`  |

### Ports (Identity Hub)

Ports that the identity hub will use. They must be available in the host.

| Variable              | Description                                                                                           | Default |
| --------------------- | ----------------------------------------------------------------------------------------------------- | ------- |
| `IH_PORT_BASE`        | Base port. Any endpoints without a context will be handled in this port.                              | `7081`  |
| `IH_PORT_CREDENTIALS` | Credentials management API port.                                                                      | `7082`  |
| `IH_PORT_IDENTITY`    | Identity API port. This port must be exposed to other participants in order to request presentations. | `7083`  |
| `IH_PORT_DID`         | Port to serve Web DIDs.                                                                               | `7084`  |
| `IH_PORT_VERSION`     | Version port. This part exposes the version.                                                          | `7085`  |
| `IH_PORT_STS`         | STS port used by the connector to communicate with the identity hub.                                  | `7086`  |

### External URLs

External URLs to reach important services of the connector and the identity hub.

Recommended: Use a reverse proxy like NGINX to set up SSL termination and map the external services to a single port (eg: `443`), using different base paths for each service.

These external URLS must be reachable from outside the machine. Other participants will use them to communicate with your connector.

| Variable                          | Description                                                                               |
| --------------------------------- | ----------------------------------------------------------------------------------------- |
| `CREDENTIAL_SERVICE_EXTERNAL_URL` | External URI for the credential service (Identity Hub). Example: `https://localhost:7082` |
| `PROTOCOL_EXTERNAL_URL`           | External URI for the connector's protocol. Example: `https://localhost:8085`              |

### Internal secrets

You can change the internal secrets, like database or API credentials. They should be random strings of letters and numbers, long enough to be secure.

| Variable               | Description                            |
| ---------------------- | -------------------------------------- |
| `IH_SUPER_USER_SECRET` | Secret for the Identity Hub super user |
| `POSTGRES_PASSWORD`    | Postgres database password             |

## Running the connector

In order to run the connector, use the following command:

```sh
docker compose up -d
```

If you wish to update it, run:

```sh
docker compose pull
docker compose up -d
```

If you wish to stop it, run:

```sh
docker compose down
```
