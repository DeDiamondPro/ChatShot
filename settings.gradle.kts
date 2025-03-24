pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://maven.dediamondpro.dev/releases")
        maven("https://repo.polyfrost.org/releases")
    }
    val egtVersion = "0.6.6"
    plugins {
        id("gg.essential.multi-version.root") version egtVersion
    }
    dependencyResolutionManagement {
        versionCatalogs {
            create("libs")
            create("egt") {
                plugin("multiversion", "gg.essential.multi-version").version(egtVersion)
                plugin("multiversionRoot", "gg.essential.multi-version.root").version(egtVersion)
                plugin("defaults", "gg.essential.defaults").version(egtVersion)
            }
        }
    }
}

val mod_name: String by settings

rootProject.name = mod_name
rootProject.buildFileName = "root.gradle.kts"

listOf(
    // Temporarily disabled to fix access widener issues
    //"1.20.1-fabric",
    //"1.20.1-forge",
    //"1.20.6-neoforge",
    //"1.20.6-fabric",
    //"1.21-forge",
    //"1.21-neoforge",
    //"1.21-fabric",
    "1.21.4-forge",
    "1.21.4-neoforge",
    "1.21.4-fabric",
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}