# Indy-DID extension

An extension to handle DID resolution for Indy-Besu, using the Identity proxy.

In order to import this extension, add the following to the build file:

```
    implementation(project(":extensions:indy-did"))
```

The extension should be included both in the Connector and the Identity Hub in order for the identity system to work properly.

Also, by including this extension, new configuration settings were added:

```conf
# Must be set to the URL of the WeForming identity proxy
weforming.identity.proxy.url=http://127.0.0.1:8080/

# Token that authorizes the access to the WeForming identity proxy API.
weforming.identity.proxy.token=change_me_1

# Set it to `true` in order to enable debug endpoints
weforming.identity.debug=true
```