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

val mcVersion = VersionDefinition(
    "1.21.5" to VersionRange("1.21.5", "1.21.5", name = "1.21.5"),
    "1.21.8" to VersionRange("1.21.6", "1.21.8", name = "1.21.8"),
    // We need newer fabric api version
    "1.21.8-fabric" to VersionRange("1.21.8", "1.21.8", name = "1.21.8"),
    "1.21.10" to VersionRange("1.21.9", "1.21.10", name = "1.21.10"),
    "1.21.11" to VersionRange("1.21.11", "1.21.11", name = "1.21.11"),
    "26.1.2" to VersionRange("26.1.0", "26.1.2", name = "26.1.2"),

)
val parchmentVersion = VersionDefinition(
    "1.20.1" to "1.20.1:2023.09.03",
    "1.21.1" to "1.21.1:2024.11.17",
    "1.21.4" to "1.21.4:2025.03.23",
    "1.21.5" to "1.21.5:2025.06.15",
    "1.21.8" to "1.21.8:2025.09.14",
)
val fabricApiVersion = VersionDefinition(
    "1.20.1" to "0.92.3+1.20.1",
    "1.21.1" to "0.114.0+1.21.1",
    "1.21.4" to "0.118.0+1.21.4",
    "1.21.5" to "0.119.4+1.21.5",
    "1.21.8" to "0.133.4+1.21.8",
    "1.21.10" to "0.135.0+1.21.10",
    "1.21.11" to "0.141.1+1.21.11",
    "26.1.2" to "0.147.0+26.1.2",
)
val minFabricApiVersion = VersionDefinition(
    "1.21.8" to ">=0.131.0",
    default = "*"
)
val modMenuVersion = VersionDefinition(
    "1.20.1" to "7.2.2",
    "1.21.1" to "11.0.3",
    "1.21.4" to "13.0.2",
    "1.21.5" to "14.0.0-rc.2",
    "1.21.8" to "15.0.0",
    "1.21.10" to "16.0.0-rc.1",
    "1.21.11" to "17.0.0-beta.2",
    "26.1.2" to "18.0.0-alpha.8"
)
val neoForgeVersion = VersionDefinition(
    "1.21.4" to "21.4.124",
    "1.21.5" to "21.5.95",
    "1.21.8" to "21.8.47",
    "1.21.10" to "21.10.18-beta",
    "1.21.11" to "21.11.35-beta",
    "26.1.2" to "26.1.2.36-beta"
)
val yaclVersion = VersionDefinition(
    "26.1.2" to "3.9.3+26.1-${mcPlatform.loaderString}",
    "1.21.8" to "3.8.2+1.21.6-${mcPlatform.loaderString}",
    default = "3.8.2+${mcPlatform.name}",
)
val noChatReportsVersion = VersionDefinition(
    "1.21.4-fabric" to "Fabric-1.21.4-v2.11.0",
    "1.21.4-neoforge" to "NeoForge-1.21.4-v2.11.0",
    "1.21.5-fabric" to "Fabric-1.21.5-v2.12.0",
    "1.21.5-neoforge" to "NeoForge-1.21.5-v2.12.0",
    "1.21.8-fabric" to "Fabric-1.21.8-v2.15.0",
    "1.21.8-neoforge" to "NeoForge-1.21.8-v2.15.0",
    "1.21.10-fabric" to "Fabric-1.21.10-v2.16.0",
    "1.21.10-neoforge" to "NeoForge-1.21.10-v2.16.0",
    "1.21.11-fabric" to "Fabric-1.21.11-v2.18.0",
    "1.21.11-neoforge" to "NeoForge-1.21.11-v2.18.0",
    "26.1.2-fabric" to "Fabric-26.1-v2.19.0",
    "26.1.2-neoforge" to "NeoForge-26.1-v2.19.0",
)

stonecutter {
    constants["fabric"] = mcPlatform.isFabric
    constants["forge"] = mcPlatform.isForge
    constants["neoforge"] = mcPlatform.isNeoForge
    constants["forgelike"] = mcPlatform.isForgeLike
    constants["ncr"] = noChatReportsVersion.getOrNull(mcPlatform) != null

    swaps["mod_name"] = "\"$mod_name\""
    swaps["mod_id"] = "\"$mod_id\""
    swaps["mod_version"] = "\"$mod_version\""
}

dependencies {
    minecraft("com.mojang:minecraft:${mcPlatform.versionString}")

    @Suppress("UnstableApiUsage")

//    if (mcPlatform.version.ma)
//    mappings(loom.layered {
//        officialMojangMappings()
//        parchmentVersion.getOrNull(mcPlatform)?.let {
//            parchment("org.parchmentmc.data:parchment-$it@zip")
//        }
//    })

    if (mcPlatform.isFabric) {
        implementation ("net.fabricmc:fabric-loader:0.19.2")

        api  ("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion.get(mcPlatform)}")
        implementation ("com.terraformersmc:modmenu:${modMenuVersion.get(mcPlatform)}")
    } else if (mcPlatform.isNeoForge) {
        "neoForge"("net.neoforged:neoforge:${neoForgeVersion.get(mcPlatform)}")
    }

    implementation ("dev.isxander:yet-another-config-lib:${yaclVersion.get(mcPlatform)}") {
        exclude("net.neoforged.fancymodloader", "loader")
    }
    compileOnly(libs.objc)

    // Compat mods
    noChatReportsVersion.getOrNull(mcPlatform)?.let {
        compileOnly("maven.modrinth:no-chat-reports:${it}")
    }
}

val accessWidener =
    if (mcPlatform.version >= 26_1_2) "26.1.2-chatshot.accesswidener"
    else if (mcPlatform.version >= 1_21_11) "1.21.11-chatshot.accesswidener"
else if (mcPlatform.version >= 1_21_06) "1.21.8-chatshot.accesswidener"
else "chatshot.accesswidener"

val mixins = if (mcPlatform.version >= 1_21_06) "1.21.8.mixins.chatshot.json" else "1.21.5.mixins.chatshot.json"
loom {
    accessWidenerPath = rootProject.file("src/main/resources/$accessWidener")

    if (mcPlatform.isForge) forge {
        convertAccessWideners.set(true)
        mixinConfig(mixins)
    }

    runConfigs["client"].isIdeConfigGenerated = true
}

base.archivesName.set(
    "$mod_name (${
        mcVersion.get(mcPlatform).getName().replace("/", "-")
    }-${mcPlatform.loaderString})-$mod_version"
)

publishMods {
    file.set(tasks.jar.get().archiveFile)
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
    jar {
        finalizedBy("copyJar")
//        if (mcPlatform.isNeoForge) {
//            atAccessWideners.add(accessWidener)
//        }
    }
    register<Copy>("copyJar") {
        File("${project.rootDir}/jars").mkdir()
        from(jar.get().archiveFile)
        into("${project.rootDir}/jars")
    }
    clean { delete("${project.rootDir}/jars") }
    processResources {
        val properties = mapOf(
            "id" to mod_id,
            "name" to mod_name,
            "version" to mod_version,
            "aw" to accessWidener,
            "mixins" to mixins,
            "mcVersion" to mcVersion.get(mcPlatform).getLoaderRange(mcPlatform),
            "fabricApiVersion" to minFabricApiVersion.get(mcPlatform),
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
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}
