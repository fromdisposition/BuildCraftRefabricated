import java.awt.image.BufferedImage
import javax.imageio.ImageIO

plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
}

version = providers.gradleProperty("mod_version").get()

base {
    archivesName.set("BCRefabricated")
}

repositories {
    maven("https://maven.blamejared.com")
    maven("https://maven.teamreborn.org")
}

loom {
    mods {
        create("buildcraftrefabricated") {
            sourceSet(sourceSets["main"])
        }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

// Loader-specific hooks live in buildcraft.lib.fabric.* and buildcraft.fabric.*

dependencies {
    val mc = providers.gradleProperty("minecraft_version").get()
    minecraft("com.mojang:minecraft:$mc")
    implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
    implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")

    implementation("org.jspecify:jspecify:1.0.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    compileOnly("mezz.jei:jei-$mc-fabric-api:${providers.gradleProperty("jei_version").get()}")

    val energyVersion = providers.gradleProperty("energy_version").get()
    implementation(include("teamreborn:energy:$energyVersion")!!)

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
}

// Full Fabric compile target — gameplay modules in progress; guide/script still reference excluded APIs.
val notYetOnFabric = listOf<String>()

tasks.register("generateFluidBucketAssets") {
    group = "buildcraft"
    description = "Regenerate bucket-fluid PNGs, underwater overlay PNGs (BOP-style), and fluid-bucket item JSON."
    doLast {
        val heatStill = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_still.png")
        val fluidMask = file("src/main/resources/assets/buildcraftenergy/textures/item/mask/bucket_fluid.png")
        val fluidOutDir = file("src/main/resources/assets/buildcraftenergy/textures/item/bucket_fluid")
        val underwaterOutDir = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids")
        val itemsDir = file("src/main/resources/assets/buildcraftenergy/items")
        val modelsDir = file("src/main/resources/assets/buildcraftenergy/models/item/fluid_buckets")
        require(heatStill.isFile) { "Missing ${heatStill.path} — extract from a built JAR or add the texture." }
        require(fluidMask.isFile) { "Missing ${fluidMask.path} (bucket fluid mask)." }
        fluidOutDir.mkdirs()
        modelsDir.mkdirs()

        fun recolor(basePixel: Int, light: Int, dark: Int): Int {
            val wr = (basePixel shr 16) and 0xFF
            val wg = (basePixel shr 8) and 0xFF
            val wb = basePixel and 0xFF
            val lr = (light shr 16) and 0xFF
            val lg = (light shr 8) and 0xFF
            val lb = light and 0xFF
            val dr = (dark shr 16) and 0xFF
            val dg = (dark shr 8) and 0xFF
            val db = dark and 0xFF
            val outR = (dr * (256 - wr) + lr * wr) / 256
            val outG = (dg * (256 - wg) + lg * wg) / 256
            val outB = (db * (256 - wb) + lb * wb) / 256
            return (0xFF shl 24) or (outR shl 16) or (outG shl 8) or outB
        }

        val baseImg = ImageIO.read(heatStill)
        val maskImg = ImageIO.read(fluidMask)
        val frame = baseImg.width
        check(baseImg.height >= frame) { "heat_still must be at least one ${frame}x${frame} frame" }
        check(maskImg.width == frame && maskImg.height == frame) { "bucket_fluid mask must be ${frame}x${frame}" }

        val fluidData = listOf(
            Triple("oil", 0x505050, 0x050505),
            Triple("oil_residue", 0x100F10, 0x421042),
            Triple("oil_heavy", 0xA07A9F, 0x423820),
            Triple("oil_dense", 0x876E77, 0x422424),
            Triple("oil_distilled", 0xE4AF78, 0xB47F00),
            Triple("fuel_dense", 0xFFAF3F, 0xE07F00),
            Triple("fuel_mixed_heavy", 0xF2A700, 0xC48700),
            Triple("fuel_light", 0xFFFF30, 0xE4CF00),
            Triple("fuel_mixed_light", 0xF6D700, 0xC4B700),
            Triple("fuel_gaseous", 0xFAF630, 0xE0D900),
        )
        val heats = listOf("", "_heat_1", "_heat_2")

        for ((base, light, dark) in fluidData) {
            for (heatSuffix in heats) {
                val fluid = base + heatSuffix
                val heat = when {
                    heatSuffix.isEmpty() -> 0
                    heatSuffix == "_heat_1" -> 1
                    else -> 2
                }
                val tintR = (((light shr 16) and 0xFF) + ((dark shr 16) and 0xFF)) / 2 + heat * 0x10
                val tintG = (((light shr 8) and 0xFF) + ((dark shr 8) and 0xFF)) / 2 + heat * 0x10
                val tintB = ((light and 0xFF) + (dark and 0xFF)) / 2 + heat * 0x10
                val adjLight = (0xFF shl 24) or (minOf(tintR, 0xFF) shl 16) or (minOf(tintG, 0xFF) shl 8) or minOf(tintB, 0xFF)
                val adjDark = dark

                val icon = BufferedImage(frame, frame, BufferedImage.TYPE_INT_ARGB)
                for (y in 0 until frame) {
                    for (x in 0 until frame) {
                        val maskA = (maskImg.getRGB(x, y) ushr 24) and 0xFF
                        if (maskA < 128) {
                            icon.setRGB(x, y, 0)
                            continue
                        }
                        val fluidRgb = recolor(baseImg.getRGB(x, y), adjLight, adjDark)
                        icon.setRGB(x, y, fluidRgb)
                    }
                }
                ImageIO.write(icon, "PNG", fluidOutDir.resolve("$fluid.png"))

                val underwater = BufferedImage(frame, frame, BufferedImage.TYPE_INT_ARGB)
                for (y in 0 until frame) {
                    for (x in 0 until frame) {
                        val px = recolor(baseImg.getRGB(x, y), adjLight, adjDark)
                        val alpha = (px ushr 24) and 0xFF
                        if (alpha == 0) {
                            underwater.setRGB(x, y, 0)
                        } else {
                            val dimA = alpha / 5
                            underwater.setRGB(x, y, (dimA shl 24) or (px and 0xFFFFFF))
                        }
                    }
                }
                ImageIO.write(underwater, "PNG", underwaterOutDir.resolve("${fluid}_underwater.png"))

                val bucket = "${fluid}_bucket"
                modelsDir.resolve("$bucket.json").writeText(
                    """
                    {
                        "parent": "minecraft:item/generated",
                        "textures": {
                            "layer0": "minecraft:item/bucket",
                            "layer1": "buildcraftenergy:item/bucket_fluid/$fluid"
                        }
                    }
                    """.trimIndent() + "\n"
                )
                itemsDir.resolve("$bucket.json").writeText(
                    """
                    {
                        "model": {
                            "type": "minecraft:model",
                            "model": "buildcraftenergy:item/fluid_buckets/$bucket"
                        }
                    }
                    """.trimIndent() + "\n"
                )
            }
        }
        logger.lifecycle("Regenerated ${fluidData.size * heats.size} fluid bucket + underwater overlay assets")
    }
}

tasks.register("stripUtf8Bom") {
    group = "build"
    description = "Remove UTF-8 BOM from Java sources."
    doLast {
        fileTree("src/main/java/buildcraft").matching { include("**/*.java") }.forEach { file ->
            val bytes = file.readBytes()
            if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
                file.writeBytes(bytes.copyOfRange(3, bytes.size))
            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn("stripUtf8Bom")
    options.release.set(25)
    notYetOnFabric.forEach { exclude(it) }
}

tasks.processResources {
    dependsOn("generateFluidBucketAssets")
    val props = mapOf(
        "mod_version" to version,
        "mc_dep_range" to providers.gradleProperty("mc_dep_range").get(),
        "loader_version" to providers.gradleProperty("loader_version").get(),
        "fabric_api_version" to providers.gradleProperty("fabric_api_version").get(),
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.named<Jar>("sourcesJar") {
    dependsOn("runDatagen")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

/** Unpack Mojang / Fabric API / Loom artifacts into .gradle/api-explore for local API browsing. */
tasks.register("unpackApiExplore") {
    group = "buildcraft"
    description = "Unpack Minecraft sources, Fabric API, and Fabric Loom into .gradle/api-explore/"
    dependsOn("compileJava")
    doLast {
        val explore = layout.projectDirectory.dir(".gradle/api-explore")
        val minecraftDir = explore.dir("minecraft")
        val fabricApiDir = explore.dir("fabric-api")
        val fabricModulesDir = explore.dir("fabric-api-modules")
        val loomDir = explore.dir("fabric-loom")

        val mcSources = fileTree(layout.projectDirectory.dir(".gradle/loom-cache/minecraftMaven")).matching {
            include("**/*-sources.jar")
        }.files.maxByOrNull { it.lastModified() }
            ?: error("Minecraft sources JAR not found. Run compileJava or genSources first.")

        val fapiVersion = providers.gradleProperty("fabric_api_version").get()
        val fapiJar = fileTree(gradle.gradleUserHomeDir.resolve("caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api")).matching {
            include("**/$fapiVersion/**/*.jar")
            exclude("**/*-sources.jar")
        }.singleFile

        val loomJar = fileTree(gradle.gradleUserHomeDir.resolve("caches/modules-2/files-2.1/net.fabricmc/fabric-loom")).matching {
            include("**/fabric-loom-*.jar")
            exclude("**/*-sources.jar")
        }.files.maxByOrNull { it.lastModified() }
            ?: error("fabric-loom JAR not found in Gradle cache — run any Gradle task first.")

        listOf(minecraftDir, fabricApiDir, fabricModulesDir, loomDir).forEach { it.asFile.mkdirs() }

        copy { from(zipTree(mcSources)); into(minecraftDir) }
        copy { from(zipTree(fapiJar)); into(fabricApiDir) }
        copy { from(zipTree(loomJar)); into(loomDir) }

        val modulesRoot = gradle.gradleUserHomeDir.resolve("caches/modules-2/files-2.1/net.fabricmc.fabric-api")
        if (modulesRoot.isDirectory) {
            modulesRoot.walkTopDown().maxDepth(4).filter { it.isFile && it.name.endsWith("4c.jar") }.forEach { jar ->
                val moduleDir = fabricModulesDir.dir(jar.nameWithoutExtension)
                copy { from(zipTree(jar)); into(moduleDir) }
            }
        }

        logger.lifecycle(
            "API explore unpacked to {} (minecraft={}, fabric-api={}, loom={})",
            explore.asFile.absolutePath,
            mcSources.name,
            fapiJar.name,
            loomJar.name,
        )
    }
}
