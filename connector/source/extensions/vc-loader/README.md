# Verifiable credentials loader extension

This extension loads the verifiable credentials from a folder.

Each verifiable credentials must be a JSON file.

In order to import this extension, add the following to the build file:

```
    implementation(project(":extensions:vc-loader"))
```

The extension should be included in the Identity Hub.

Also, by including this extension, new configuration settings were added:

```conf
# Path where the verifiable credentials are located, as JSON files
identity.hub.credentials.path=./credentials/
```
