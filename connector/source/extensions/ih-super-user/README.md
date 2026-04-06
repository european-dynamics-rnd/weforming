# Identity Hub Super User

This extension creates a superuser in the identity Hub in order for the launcher to be able to initialize it.

In order to import this extension, add the following to the build file:

```
    implementation(project(":extensions:ih-super-user"))
```

The extension should be included in the Identity Hub.

Also, by including this extension, new configuration settings were added:

```conf
# Super user client ID
edc.ih.client.id=super-user

# Super user client secret
# Must be in format: '{{BASE64(ID)}}.{{BASE64(SECRET)}}'
edc.ih.client.secret=c3VwZXItdXNlcg==.c3VwZXItc2VjcmV0LWtleQ==
```
