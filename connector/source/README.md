# Connector Source

This folder contains all the necessary files to build the connector components:

 - [Connector](./connector): The connector, handling the catalog, negotiation and transfers.
 - [Identity Hub](./identity-hub): The identity hub, storing the verifiable credentials and handling verifiable presentation requests.
 - [Launcher](./launcher): A small program to set up the configuration for the connector and the identity hub, launch them, and initialize them. 

## Building

In order to build the connector, run:

```sh
./gradlew connector:build
```

The result will be saved in the `connector/build/libs` folder, as `connector.jar`.

In order to build the identity hub, run:

```sh
./gradlew identity-hub:build
```

The result will be saved in the `identity-hub/build/libs` folder, as `identity-hub.jar`.

For build instructions on the launcher, read the [documentation present in the launcher folder](./launcher/README.md).

## Docker

In order to build the Docker image, first, make sure to build both the connector and the identity hub. The Dockerfile expects the JAR files to be available in the build folders.

Then, run the following command:

```sh
docker build --tag weforming-connector .
```

## Libraries

If you need to import Java libraries, modify the [libs.versions.toml](./gradle/libs.versions.toml) in the `graddle` folder.

Once you add them in the libraries file, you can then add libraries for the connector or the identity hub by modifying the corresponding `build.gradle.kts` files for each component:

 - [Connector build file](./connector/build.gradle.kts)
 - [Identity Hub build file](./identity-hub/build.gradle.kts)

You also add extensions this way, since they count as libraries.

## Extensions

All extensions are placed in the [extensions](./extensions) folder.

Each extension has its own documentation in the form of a `README.md` file.

| Extension                                                         | Description                                                                         |
|-------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| [Keys loader](./extensions/keys-loader/README.md)                 | Loads the public and private keys of the connector from PEM files.                  |
| [Trusted issuers](./extensions/trusted-issuers/README.md)         | Sets the list of trusted issuers.                                                   |
| [Indy DID](./extensions/indy-did/README.md)                       | Adds support for Indy-Besu DID resolution.                                          |
| [JWS Suite](./extensions/jws-suite/README.md)                     | Provides support for JWT and JWK.                                                   |
| [JSON-LD Transformer](./extensions/json-ld-transformer/README.md) | Provides support for JSON-LD.                                                       |
| [Identity Hub Super User](./extensions/ih-super-user/README.md)   | Creates a super user in the identity Hub in order for the launcher to initialize it |
| [VC Loader](./extensions/vc-loader/README.md)                     | Loads signed verifiable credentials from a folder.                                  |
| [Policies](./extensions/policies/README.md)                       | Registers rules for policies and access control.                                    |

