import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import gg.essential.gradle.util.noServerRunConfigs

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.blossom)
    alias(libs.plugins.minotaur)
    alias(libs.plugins.cursegradle)
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
}

val mod_name: String by project
val mod_version: String by project
val mod_id: String by project

preprocess {
    vars.put("MODERN", if (project.platform.mcMinor >= 16) 1 else 0)
}

blossom {
    replaceToken("@NAME@", mod_name)
    replaceToken("@ID@", mod_id)
    replaceToken("@VER@", mod_version)
}

version = mod_version
group = "dev.dediamondpro"
base {
    archivesName.set("$mod_name (${getPrettyVersionRange()}-${platform.loaderStr})")
}

loom.noServerRunConfigs()
loom {
    if (project.platform.isLegacyForge) runConfigs {
        "client" { programArgs("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker") }
    }
    if (project.platform.isForge) forge {
        mixinConfig("mixins.${mod_id}.json")
        mixin.defaultRefmapName.set("mixins.${mod_id}.refmap.json")
    }
    if (project.platform.isFabric && project.platform.mcVersion > 12100)  {
        accessWidenerPath.set(file("src/main/resources/chatshot.accesswidener"))
    }
}

repositories {
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://repo.essential.gg/repository/maven-public/")
    maven("https://maven.dediamondpro.dev/releases")
    maven("https://maven.isxander.dev/releases")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.neoforged.net/releases/")
    mavenCentral()
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    compileOnly(libs.objc)
    modCompileOnly("maven.modrinth:no-chat-reports:Fabric-1.20.1-v2.2.2")
    modCompileOnly("maven.modrinth:immediatelyfast:1.2.18+1.20.4-fabric")
    if (platform.isFabric) {
        val fabricApiVersion = when(project.platform.mcVersion) {
            12006 -> "0.100.4+1.20.6"
            12104 -> "0.119.2+1.21.4"
            else -> null
        }
        fabricApiVersion?.let { modImplementation("net.fabricmc.fabric-api:fabric-api:$it") }
        modCompileOnly(libs.yacl.fabric) {
            isTransitive = false
        }
        modCompileOnly(libs.modMenu)
        modRuntimeOnly(libs.devAuth.fabric)
    } else {
        modCompileOnly(libs.yacl.forge) {
            isTransitive = false
        }
        modRuntimeOnly(libs.devAuth.forge)
    }
}

tasks.processResources {
    inputs.property("id", mod_id)
    inputs.property("name", mod_name)
    inputs.property("version", mod_version)
    inputs.property("fabricMcVersion", getFabricMcVersionRange())
    inputs.property("forgeMcVersion", getForgeMcVersionRange())
    filesMatching(listOf("mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "fabric.mod.json")) {
        expand(
            mapOf(
                "id" to mod_id,
                "name" to mod_name,
                "version" to mod_version,
                "fabricMcVersion" to getFabricMcVersionRange(),
                "forgeMcVersion" to getForgeMcVersionRange(),
            )
        )
    }
}

tasks {
    withType<Jar> {
        if (project.platform.isFabric) {
            exclude("mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta")
        } else {
            exclude("fabric.mod.json")
            if (project.platform.isLegacyForge) {
                exclude("mods.toml", "META-INF/neoforge.mods.toml")
            } else if (platform.isForge) {
                exclude("mcmod.info", "META-INF/neoforge.mods.toml")
            } else if (platform.isNeoForge) {
                exclude("mcmod.info", "META-INF/mods.toml")
            }
        }
        from(rootProject.file("LICENSE"))
        from(rootProject.file("LICENSE.LESSER"))
    }
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveClassifier.set(if (platform.isForge && platform.mcVersion >= 12100) "" else "dev")
        configurations = listOf(shade)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        if (platform.isForge && platform.mcVersion >= 12100) {
            exclude("mixins.${mod_id}.refmap.json")
        }
    }
    remapJar {
        if (platform.isForge && platform.mcVersion >= 12100) {
            enabled = false
        }

        input.set(shadowJar.get().archiveFile)
        archiveClassifier.set("")
        finalizedBy("copyJar")
    }
    jar {
        if (project.platform.isLegacyForge) {
            manifest {
                attributes(
                    mapOf(
                        "ModSide" to "CLIENT",
                        "TweakOrder" to "0",
                        "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                        "ForceLoadAsMod" to true
                    )
                )
            }
        }
        dependsOn(shadowJar)
        archiveClassifier.set("")
        enabled = false
    }
    register<Copy>("copyJar") {
        File("${project.rootDir}/jars").mkdir()
        from(if (platform.isForge && platform.mcVersion >= 12100) shadowJar.get().archiveFile else remapJar.get().archiveFile)
        into("${project.rootDir}/jars")
    }
    clean { delete("${project.rootDir}/jars") }
    project.modrinth {
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set("chatshot")
        versionNumber.set(mod_version)
        versionName.set("[${getPrettyVersionRange()}-${platform.loaderStr}] ChatShot $mod_version")
        uploadFile.set(if (platform.isForge && platform.mcVersion >= 12100) shadowJar.get().archiveFile else remapJar.get().archiveFile)
        gameVersions.addAll(getSupportedVersionList())
        if (platform.isFabric) {
            loaders.add("fabric")
            loaders.add("quilt")
        } else if (platform.isForge) {
            loaders.add("forge")
        } else if (platform.isNeoForge) {
            loaders.add("neoforge")
        }
        changelog.set(file("../../changelog.md").readText())
        dependencies {
            if (platform.isFabric) required.project("fabric-api")
            required.project("yacl")
        }
    }
    project.curseforge {
        project(closureOf<CurseProject> {
            apiKey = System.getenv("CURSEFORGE_TOKEN")
            id = "908966"
            changelog = file("../../changelog.md")
            changelogType = "markdown"
            relations(closureOf<CurseRelation> {
                if (platform.isFabric) requiredDependency("fabric-api")
                requiredDependency("yacl")
            })
            gameVersionStrings.addAll(getSupportedVersionList())
            if (platform.isFabric) {
                addGameVersion("Fabric")
                addGameVersion("Quilt")
            } else if (platform.isForge) {
                addGameVersion("Forge")
            } else if (platform.isNeoForge) {
                addGameVersion("NeoForge")
            }
            releaseType = "release"
            mainArtifact(
                if (platform.isForge && platform.mcVersion >= 12100) shadowJar.get().archiveFile else remapJar.get().archiveFile,
                closureOf<CurseArtifact> {
                    displayName = "[${getPrettyVersionRange()}-${platform.loaderStr}] ChatShot $mod_version"
                })
        })
        options(closureOf<Options> {
            javaVersionAutoDetect = false
            javaIntegration = false
            forgeGradleIntegration = false
        })
    }
    register("publish") {
        dependsOn(modrinth)
        dependsOn(curseforge)
    }
}

// Function to get the range of mc versions supported by a version we are building for.
// First value is start of range, second value is end of range or null to leave the range open
fun getSupportedVersionRange(): Pair<String, String?> = when (platform.mcVersion) {
    12104 -> "1.21.4" to "1.21.4"
    12100 -> "1.21" to "1.21"
    12006 -> "1.20.5" to "1.20.6"
    12001 -> "1.20" to "1.20.4"
    else -> error("Undefined version range for ${platform.mcVersion}")
}

fun getPrettyVersionRange(): String {
    val supportedVersionRange = getSupportedVersionRange()
    return when {
        platform.mcVersion == 12104 -> "1.21.4"
        platform.mcVersion == 12100 -> "1.21"
        platform.mcVersion == 12006 -> "1.20.6"
        supportedVersionRange.first == supportedVersionRange.second -> supportedVersionRange.first
        else -> "${supportedVersionRange.first}${supportedVersionRange.second?.let { "-$it" } ?: "+"}"
    }
}

fun getFabricMcVersionRange(): String {
    if (platform.mcVersion == 12104) return "1.21.4"
    if (platform.mcVersion == 12100) return "1.21"
    val supportedVersionRange = getSupportedVersionRange()
    if (supportedVersionRange.first == supportedVersionRange.second) return supportedVersionRange.first
    return ">=${supportedVersionRange.first}${supportedVersionRange.second?.let { " <=$it" } ?: ""}"
}

fun getForgeMcVersionRange(): String {
    val supportedVersionRange = getSupportedVersionRange()
    if (supportedVersionRange.first == supportedVersionRange.second) return "[${supportedVersionRange.first}]"
    return "[${supportedVersionRange.first},${supportedVersionRange.second?.let { "$it]" } ?: ")"}"
}

fun getSupportedVersionList(): List<String> {
    val supportedVersionRange = getSupportedVersionRange()
    return when (supportedVersionRange.first) {
        "1.21" -> listOf("1.21")
        "1.21.4" -> listOf("1.21.4")
        else -> {
            val minorVersion = supportedVersionRange.first.let {
                if (it.count { c -> c == '.' } == 1) it else it.substringBeforeLast(".")
            }
            val start = supportedVersionRange.first.let {
                if (it.count { c -> c == '.' } == 1) 0 else it.substringAfterLast(".").toInt()
            }
            val end = supportedVersionRange.second!!.let {
                if (it.count { c -> c == '.' } == 1) 0 else it.substringAfterLast(".").toInt()
            }
            val versions = mutableListOf<String>()
            for (i in start..end) {
                versions.add("$minorVersion${if (i == 0) "" else ".$i"}")
            }
            versions
        }
    }
}