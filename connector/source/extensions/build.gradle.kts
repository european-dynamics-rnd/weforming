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
    // Add all extensions here
    implementation(project(":extensions:keys-loader"))
    implementation(project(":extensions:trusted-issuers"))
    implementation(project(":extensions:indy-did"))
    implementation(project(":extensions:jws-suite"))
    implementation(project(":extensions:json-ld-transformer"))
    implementation(project(":extensions:ih-super-user"))
    implementation(project(":extensions:vc-loader"))
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
