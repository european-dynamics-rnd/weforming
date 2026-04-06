/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

rootProject.name = "EDC"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

// Connector
include(":connector")

// Identity Hub
include(":identity-hub")

// Extensions
include(":extensions")
include(":extensions:keys-loader")
include(":extensions:trusted-issuers")
include(":extensions:indy-did")
include(":extensions:jws-suite")
include(":extensions:json-ld-transformer")
include(":extensions:ih-super-user")
include(":extensions:vc-loader")
include(":extensions:policies")

