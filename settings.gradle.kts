pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.blamejared.com/")
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "BuildCraftRefabricated"
