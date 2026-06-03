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
    id("dev.kikugie.stonecutter") version "0.9.3"
}

val platforms = listOf(
//    "1.21.5-neoforge",
//    "1.21.5-fabric",
//    "1.21.8-neoforge",
//    "1.21.8-fabric",
//    "1.21.10-fabric",
//    "1.21.10-neoforge",
//    "1.21.11-fabric",
//    "1.21.11-neoforge",
    "26.1.2-fabric",
    "26.1.2-neoforge",
)

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    create(rootProject) {
        for (version in platforms) {
            version(version, version.split('-')[0])
        }
        vcsVersion = "26.1.2-fabric"
    }
}

val mod_name: String by settings
rootProject.name = mod_name