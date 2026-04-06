/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.connector.core)

    implementation(libs.edc.http)

    implementation(libs.edc.identity.did)
    implementation(libs.edc.did.core)

    implementation(libs.jakarta.rsApi)

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}