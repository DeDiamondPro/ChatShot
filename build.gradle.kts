import dev.dediamondpro.buildsource.Platform
import dev.dediamondpro.buildsource.VersionDefinition
import dev.dediamondpro.buildsource.VersionRange

plugins {
    alias(libs.plugins.arch.loom)
    alias(libs.plugins.publishing)
}

buildscript {
    // Set loom platform to correct loader
    extra["loom.platform"] = project.name.split('-')[1]
}

val mcPlatform = Platform.fromProject(project)

val mod_name: String by project
val mod_version: String by project
val mod_id: String by project

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
    maven("https://maven.parchmentmc.org")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.minecraftforge.net")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.dediamondpro.dev/releases")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
}

stonecutter {
    const("fabric", mcPlatform.isFabric)
    const("forge", mcPlatform.isForge)
    const("neoforge", mcPlatform.isNeoForge)
    const("forgelike", mcPlatform.isForgeLike)

    swap("mod_name", "\"$mod_name\"")
    swap("mod_id", "\"$mod_id\"")
    swap("mod_version", "\"$mod_version\"")
}

val mcVersion = VersionDefinition(
    "1.21.4" to VersionRange("1.21.3", "1.21.4", name = "1.21.4"),
)
val parchmentVersion = VersionDefinition(
    "1.20.1" to "1.20.1:2023.09.03",
    "1.21.1" to "1.21.1:2024.11.17",
    "1.21.4" to "1.21.4:2025.02.16"
)
val fabricApiVersion = VersionDefinition(
    "1.20.1" to "0.92.3+1.20.1",
    "1.21.1" to "0.114.0+1.21.1",
    "1.21.4" to "0.118.0+1.21.4",
    "1.21.5" to "0.119.4+1.21.5",
)
val modMenuVersion = VersionDefinition(
    "1.20.1" to "7.2.2",
    "1.21.1" to "11.0.3",
    "1.21.4" to "13.0.2",
    "1.21.5" to "14.0.0-rc.2",
)
val neoForgeVersion = VersionDefinition(
    "1.21.4" to "21.4.124",
)
val yaclVersion = VersionDefinition(
    "1.21.4-fabric" to "3.6.6+1.21.4-fabric",
    "1.21.4-neoforge" to "3.6.6+1.21.4-neoforge"
)
val noChatReportsVersion = VersionDefinition(
    "1.21.4-fabric" to "Fabric-1.21.4-v2.11.0",
    "1.21.4-neoforge" to "NeoForge-1.21.4-v2.11.0",
)

dependencies {
    minecraft("com.mojang:minecraft:${mcPlatform.versionString}")

    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchmentVersion.getOrNull(mcPlatform)?.let {
            parchment("org.parchmentmc.data:parchment-$it@zip")
        }
    })

    if (mcPlatform.isFabric) {
        modImplementation("net.fabricmc:fabric-loader:0.16.10")

        modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion.get(mcPlatform)}")
        modImplementation("com.terraformersmc:modmenu:${modMenuVersion.get(mcPlatform)}")
    } else if (mcPlatform.isNeoForge) {
        "neoForge"("net.neoforged:neoforge:${neoForgeVersion.get(mcPlatform)}")
    }

    modImplementation("dev.isxander:yet-another-config-lib:${yaclVersion.get(mcPlatform)}")
    compileOnly(libs.objc)

    // Compat mods
    noChatReportsVersion.getOrNull(mcPlatform)?.let {
        modCompileOnly("maven.modrinth:no-chat-reports:${it}")
    }
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/chatshot.accesswidener")

    if (mcPlatform.isForge) forge {
        convertAccessWideners.set(true)
        mixinConfig("mixins.resourcify.json")
    }

    runConfigs["client"].isIdeConfigGenerated = true
}

base.archivesName.set(
    "$mod_name (${
        mcVersion.get(mcPlatform).getName().replace("/", "-")
    }-${mcPlatform.loaderString})-$mod_version"
)

publishMods {
    file.set(tasks.remapJar.get().archiveFile)
    displayName.set("[${mcVersion.get(mcPlatform).getName()}-${mcPlatform.loaderString}] $mod_name $mod_version")
    version.set(mod_version)
    changelog.set(rootProject.file("changelog.md").readText())
    type.set(STABLE)

    modLoaders.add(mcPlatform.loaderString)
    if (mcPlatform.isFabric) modLoaders.add("quilt")

    curseforge {
        projectId.set("908966")
        accessToken.set(System.getenv("CURSEFORGE_TOKEN"))

        minecraftVersionRange {
            start = mcVersion.get(mcPlatform).startVersion
            end = mcVersion.get(mcPlatform).endVersion
        }

        if (mcPlatform.isFabric) {
            requires("fabric-api", "yacl")
        } else if (mcPlatform.isForgeLike) {
            requires("yacl")
        }
    }
    modrinth {
        projectId.set("X2Zy7Oi6")
        accessToken.set(System.getenv("MODRINTH_TOKEN"))

        minecraftVersionRange {
            start = mcVersion.get(mcPlatform).startVersion
            end = mcVersion.get(mcPlatform).endVersion
        }

        if (mcPlatform.isFabric) {
            requires("fabric-api", "yacl")
        } else if (mcPlatform.isForgeLike) {
            requires("yacl")
        }
    }
}

tasks {
    remapJar {
        finalizedBy("copyJar")
        if (mcPlatform.isNeoForge) {
            atAccessWideners.add("chatshot.accesswidener")
        }
    }
    register<Copy>("copyJar") {
        File("${project.rootDir}/jars").mkdir()
        from(remapJar.get().archiveFile)
        into("${project.rootDir}/jars")
    }
    clean { delete("${project.rootDir}/jars") }
    processResources {
        val properties = mapOf(
            "id" to mod_id,
            "name" to mod_name,
            "version" to mod_version,
            "mcVersion" to mcVersion.get(mcPlatform).getLoaderRange(mcPlatform),
        )

        properties.forEach { (k, v) -> inputs.property(k, v) }
        filesMatching(listOf("mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "fabric.mod.json")) {
            expand(properties)
        }

        if (!mcPlatform.isFabric) exclude("fabric.mod.json")
        if (!mcPlatform.isForgeLike) exclude("pack.mcmeta")
        if (!mcPlatform.isNeoForge) exclude("META-INF/neoforge.mods.toml")
    }
    withType<Jar> {
        from(rootProject.file("LICENSE"))
        from(rootProject.file("LICENSE.LESSER"))
    }
}

configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
