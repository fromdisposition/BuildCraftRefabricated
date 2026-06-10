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

tasks.register("generateWorldFluidTextures") {
    group = "buildcraft"
    description = "Bake world fluid still/flowing PNGs from original BC oil/fuel source textures."
    doLast {
        val fluidsDir = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids")
        val sourceDir = fluidsDir.resolve("source")
        require(fluidsDir.isDirectory) { "Missing $fluidsDir" }
        require(sourceDir.isDirectory) { "Missing $sourceDir — add oil/fuel PNGs from BuildCraft/BuildCraft" }

        fun recolor(basePixel: Int, light: Int, dark: Int): Int {
            val a = (basePixel ushr 24) and 0xFF
            if (a == 0) return 0
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

        fun blendArgb(a: Int, b: Int): Int {
            fun ch(c1: Int, c2: Int, shift: Int): Int {
                val v1 = (c1 ushr shift) and 0xFF
                val v2 = (c2 ushr shift) and 0xFF
                return (v1 + v2) / 2
            }
            return (0xFF shl 24) or (ch(a, b, 16) shl 16) or (ch(a, b, 8) shl 8) or ch(a, b, 0)
        }

        fun makeTileable(img: BufferedImage) {
            val w = img.width
            val frameH = w
            val frames = img.height / frameH
            for (f in 0 until frames) {
                val y0 = f * frameH
                repeat(3) {
                    for (x in 0 until w) {
                        val blended = blendArgb(img.getRGB(x, y0), img.getRGB(x, y0 + frameH - 1))
                        img.setRGB(x, y0, blended)
                        img.setRGB(x, y0 + frameH - 1, blended)
                    }
                    for (y in y0 until y0 + frameH) {
                        val blended = blendArgb(img.getRGB(0, y), img.getRGB(w - 1, y))
                        img.setRGB(0, y, blended)
                        img.setRGB(w - 1, y, blended)
                    }
                    for (dx in 0 until 2) {
                        for (dy in 0 until 2) {
                            val corners = listOf(
                                img.getRGB(dx, y0 + dy),
                                img.getRGB(w - 1 - dx, y0 + dy),
                                img.getRGB(dx, y0 + frameH - 1 - dy),
                                img.getRGB(w - 1 - dx, y0 + frameH - 1 - dy)
                            )
                            val avg = corners.drop(1).fold(corners[0]) { acc, px -> blendArgb(acc, px) }
                            for (px in corners.indices) {
                                val x = if (px == 0 || px == 2) dx else w - 1 - dx
                                val y = if (px < 2) y0 + dy else y0 + frameH - 1 - dy
                                img.setRGB(x, y, avg)
                            }
                        }
                    }
                }
            }
        }

        fun bakeTemplate(template: BufferedImage, light: Int, dark: Int): BufferedImage {
            val out = BufferedImage(template.width, template.height, BufferedImage.TYPE_INT_ARGB)
            for (y in 0 until template.height) {
                for (x in 0 until template.width) {
                    out.setRGB(x, y, recolor(template.getRGB(x, y), light, dark))
                }
            }
            return out
        }

        data class FluidRecipe(val base: String, val light: Int, val dark: Int, val sourceKind: String)

        val fluidData = listOf(
            FluidRecipe("oil", 0x505050, 0x050505, "oil"),
            FluidRecipe("oil_residue", 0x100F10, 0x421042, "oil"),
            FluidRecipe("oil_heavy", 0xA08F1F, 0x423520, "oil"),
            FluidRecipe("oil_dense", 0x876E77, 0x422424, "oil"),
            FluidRecipe("oil_distilled", 0xE4AF78, 0xB47F00, "oil"),
            FluidRecipe("fuel_dense", 0xFFAF3F, 0xE07F00, "fuel"),
            FluidRecipe("fuel_mixed_heavy", 0xF2A700, 0xC48700, "fuel"),
            FluidRecipe("fuel_light", 0xFFFF30, 0xE4CF00, "fuel"),
            FluidRecipe("fuel_mixed_light", 0xF6D700, 0xC4B700, "fuel"),
            FluidRecipe("fuel_gaseous", 0xFAF630, 0xE0D900, "fuel"),
        )
        val heats = listOf("" to 0, "_heat_1" to 1, "_heat_2" to 2)

        fun adjustedLight(light: Int, dark: Int, heat: Int): Int {
            val tintR = (((light shr 16) and 0xFF) + ((dark shr 16) and 0xFF)) / 2 + heat * 0x10
            val tintG = (((light shr 8) and 0xFF) + ((dark shr 8) and 0xFF)) / 2 + heat * 0x10
            val tintB = ((light and 0xFF) + (dark and 0xFF)) / 2 + heat * 0x10
            return (0xFF shl 24) or (minOf(tintR, 0xFF) shl 16) or (minOf(tintG, 0xFF) shl 8) or minOf(tintB, 0xFF)
        }

        var count = 0
        for ((base, light, dark, sourceKind) in fluidData) {
            val stillTemplate = ImageIO.read(sourceDir.resolve("${sourceKind}_still.png"))
            val flowTemplate = ImageIO.read(sourceDir.resolve("${sourceKind}_flow.png"))
            val stillMcmeta = sourceDir.resolve("${sourceKind}_still.png.mcmeta").readText()
            val flowMcmeta = sourceDir.resolve("${sourceKind}_flow.png.mcmeta").readText()
            for ((heatSuffix, heat) in heats) {
                val fluid = base + heatSuffix
                val adjLight = adjustedLight(light, dark, heat)
                val stillOut = bakeTemplate(stillTemplate, adjLight, dark)
                makeTileable(stillOut)
                ImageIO.write(stillOut, "PNG", fluidsDir.resolve("${fluid}_still.png"))
                fluidsDir.resolve("${fluid}_still.png.mcmeta").writeText(stillMcmeta)
                val flowOut = bakeTemplate(flowTemplate, adjLight, dark)
                makeTileable(flowOut)
                ImageIO.write(flowOut, "PNG", fluidsDir.resolve("${fluid}_flowing.png"))
                fluidsDir.resolve("${fluid}_flowing.png.mcmeta").writeText(flowMcmeta)
                count += 2
            }
        }
        logger.lifecycle("Generated $count world fluid textures from original BC source in ${fluidsDir.path}")
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
