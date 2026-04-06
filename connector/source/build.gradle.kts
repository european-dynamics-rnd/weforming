/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */


plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

buildscript {
    dependencies {
        classpath(libs.edc.build.plugin)
    }
}

val edcVersion = libs.versions.edc

allprojects {
    apply(plugin = "$group.edc-build")

    // configure which version of the annotation processor to use. defaults to the same version as the plugin
    configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
        processorVersion.set(edcVersion)
        outputDirectory.set(project.layout.buildDirectory.asFile.get())
    }

    configure<org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension> {
        publish.set(false)
    }

    configure<CheckstyleExtension> {
        configFile = rootProject.file("resources/edc-checkstyle-config.xml")
        configDirectory.set(rootProject.file("resources"))
    }

    tasks.test {
        testLogging {
            showStandardStreams = true
        }
    }

}


