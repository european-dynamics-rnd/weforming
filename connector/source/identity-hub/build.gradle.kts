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

    runtimeOnly(libs.edc.bom.identityhub)
    implementation(libs.edc.ih.spi)
    implementation(libs.edc.ih.spi.credentials)

    // Extensions

    implementation(project(":extensions:keys-loader"))
    implementation(project(":extensions:indy-did"))
    implementation(project(":extensions:ih-super-user"))
    implementation(project(":extensions:vc-loader"))
}

application {
    mainClass.set("$group.boot.system.runtime.BaseRuntime")
}

var distTar = tasks.getByName("distTar")
var distZip = tasks.getByName("distZip")

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("identity-hub.jar")
    dependsOn(distTar, distZip)
}
