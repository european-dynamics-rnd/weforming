# Keys loader extension

This extension loads the public and private keys of the connector from PEM files.

In order to import this extension, add the following to the build file:

```
    implementation(project(":extensions:keys-loader"))
```

The extension should be included both in the Connector and the Identity Hub.

Also, by including this extension, new configuration settings were added:

```conf
# The path to the public key of the connector. PEM format.
connector.keys.public.path=/path/to/public-key.pem

# The path to the private key of the connector. PEM format.
connector.keys.private.path=/path/to/private-key.pem
```
