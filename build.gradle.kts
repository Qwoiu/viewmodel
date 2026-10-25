import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
}

val modid: String by project
val mcVersion: String by project

base {
    archivesName.set("ViewModel")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                // На macOS цей аргумент ламає запуск.
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    "forge"("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    inputs.property("version", project.version.toString())
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    filesMatching("mcmod.info") {
        expand(
            mapOf(
                "version" to project.version.toString(),
                "mcversion" to mcVersion,
                "modid" to modid
            )
        )
    }
}
