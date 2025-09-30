pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
    }
    dependencyResolutionManagement.versionCatalogs.create("libs")
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5.2"
}

val platforms = listOf(
    "1.21.5-fabric",
)

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    create(rootProject) {
        for (version in platforms) {
            vers(version, version.split('-')[0])
        }
        vcsVersion = "1.21.5-fabric"
    }
}

val mod_name: String by settings
rootProject.name = mod_name