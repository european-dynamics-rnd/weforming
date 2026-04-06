/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    // Core libraries

    implementation(libs.edc.runtime.core)
    implementation(libs.edc.connector.core)
    implementation(libs.edc.control.api.configuration)
    implementation(libs.edc.control.plane.api.client)
    implementation(libs.edc.control.plane.api)
    implementation(libs.edc.control.plane.core)
    implementation(libs.edc.token.core)
    implementation(libs.edc.dsp)
    implementation(libs.edc.http)
    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.management.api)
    implementation(libs.edc.transfer.data.plane.signaling)
    implementation(libs.edc.validator.data.address.http.data)

    implementation(libs.edc.edr.cache.api)
    implementation(libs.edc.edr.store.core)
    implementation(libs.edc.edr.store.receiver)

    // Data plane libraries

    implementation(libs.edc.data.plane.selector.api)
    implementation(libs.edc.data.plane.selector.core)

    implementation(libs.edc.data.plane.self.registration)
    implementation(libs.edc.data.plane.signaling.api)
    implementation(libs.edc.data.plane.core)
    implementation(libs.edc.data.plane.http)
    implementation(libs.edc.data.plane.iam)

    // Identity / Credentials libraries

    implementation(libs.edc.did.core)
    implementation(libs.edc.identity.did)
    implementation(libs.edc.identity.trust.core)
    implementation(libs.edc.oauth2.client)
    implementation(libs.edc.api.secrets)
    implementation(libs.edc.lib.jws2020)
    implementation(libs.edc.lib.transform)

    // BOM

    implementation(libs.edc.bom.controlplane)

    // Persistence (SQL)

    implementation(libs.edc.bom.controlplane.sql)
    implementation(libs.bundles.sql.edc)

    // Extensions

    implementation(project(":extensions:keys-loader"))
    implementation(project(":extensions:trusted-issuers"))

    implementation(project(":extensions:indy-did"))

    implementation(project(":extensions:jws-suite"))

    implementation(project(":extensions:json-ld-transformer"))

    implementation(project(":extensions:policies"))
}

application {
    mainClass.set("$group.boot.system.runtime.BaseRuntime")
}

var distTar = tasks.getByName("distTar")
var distZip = tasks.getByName("distZip")

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
    dependsOn(distTar, distZip)
}
