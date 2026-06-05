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
}

loom {
    mods {
        create("buildcraftrefabricated") {
            sourceSet(sourceSets["main"])
        }
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
}

// Full Fabric compile target — gameplay modules in progress; guide/script still reference excluded APIs.
val notYetOnFabric = listOf<String>()

tasks.register("generateFluidBucketAssets") {
    group = "buildcraft"
    description = "Regenerate masked bucket-fluid PNGs and Fabric fluid-bucket item JSON."
    doLast {
        val heatStill = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_still.png")
        val fluidMask = file("src/main/resources/assets/buildcraftenergy/textures/item/mask/bucket_fluid.png")
        val fluidOutDir = file("src/main/resources/assets/buildcraftenergy/textures/item/bucket_fluid")
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
            Triple("oil_heavy", 0xA08F1F, 0x423520),
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
        logger.lifecycle("Regenerated ${fluidData.size * heats.size} fluid bucket assets + icons")
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

tasks.named<JavaCompile>("compileTestJava") {
    enabled = false
}

tasks.named<Test>("test") {
    enabled = false
}
