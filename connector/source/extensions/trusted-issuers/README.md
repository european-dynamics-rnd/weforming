# Trusted issuers extension

This extension sets the list of trusted issuers.

The trusted issuers are the ones allowed to sign verifiable credentials for the data space participants

In order to import this extension, add the following to the build file:

```
    implementation(project(":extensions:trusted-issuers"))
```

The extension should be included in the Connector.

Also, by including this extension, new configuration settings were added:

```conf
# List of trusted issuers (DIDs), separated by commas.
connector.trusted.issuers=did:indy:besu:wf:0xed088cb405441491c0d47dd9d7935671bd12cb24
```
