pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.blamejared.com/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
    plugins {
        id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        version("1.21.1", "1.21.1")
        version("1.21.10", "1.21.10")
        version("1.21.11", "1.21.11")
        version("26.1", "26.1.2")
        version("26.2", "26.2")
        version("26.3", "26.3-pre-1")
        vcsVersion = "26.3"
    }
}

rootProject.name = "BuildCraftRefabricated"
