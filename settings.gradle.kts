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
    id("dev.kikugie.stonecutter") version "0.9.6"
    id("dev.kikugie.loom-back-compat") version "0.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

extensions.configure<dev.kikugie.loomx.LoomCompatSettingsExtension>("loomx") {
    loomVersion = "1.17-SNAPSHOT"
    loomRemapPlugin = "net.fabricmc.fabric-loom-remap"
    loomUnobfPlugin = "net.fabricmc.fabric-loom"
}

stonecutter {
    create(rootProject) {
        version("1.21.10", "1.21.10")
        version("1.21.11", "1.21.11")
        version("26.1", "26.1.2")
        version("26.2", "26.2")
        vcsVersion = "26.1"
    }
}

rootProject.name = "BuildCraftRefabricated"
