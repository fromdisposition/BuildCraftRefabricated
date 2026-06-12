import java.awt.image.BufferedImage
import java.util.zip.ZipFile
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

sourceSets {
    main {
        resources {
            srcDir("src/main/generated")
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

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
}

// Full Fabric compile target — gameplay modules in progress; guide/script still reference excluded APIs.
val notYetOnFabric = listOf<String>()

/** Vanilla water_flow uses the UV regions FluidRenderer expects; BC heat templates were authored for legacy SpriteFluidFrozen offsets. */
fun findMinecraftClientJar(projectDir: java.io.File, mcVersion: String): java.io.File {
    val tree = projectDir.walkTopDown().maxDepth(6).filter {
        it.isFile && it.name.startsWith("minecraft-merged-") && it.name.endsWith(".jar")
            && !it.name.endsWith("-sources.jar") && !it.name.endsWith("-javadoc.jar")
            && it.parentFile.name == mcVersion
    }
    return tree.firstOrNull() ?: error("minecraft-merged jar for $mcVersion not found — run compile first")
}

fun loadVanillaWaterFlow(mcJar: java.io.File): BufferedImage =
    ZipFile(mcJar).use { zip ->
        val entry = zip.getEntry("assets/minecraft/textures/block/water_flow.png")
            ?: error("water_flow.png not found in $mcJar")
        zip.getInputStream(entry).use { stream -> ImageIO.read(stream) }
    }

fun readAnimationFrameSize(mcmeta: java.io.File): Pair<Int, Int> {
    val text = mcmeta.readText()
    val width = Regex(""""width"\s*:\s*(\d+)""").find(text)?.groupValues?.get(1)?.toInt()
        ?: error("animation width missing in ${mcmeta.path}")
    val height = Regex(""""height"\s*:\s*(\d+)""").find(text)?.groupValues?.get(1)?.toInt()
        ?: error("animation height missing in ${mcmeta.path}")
    return width to height
}

/**
 * Native still-scroll: colors from baked still, water_flow is only the shape mask.
 * Side faces map sprite U→world horizontal and V→world vertical (FluidRenderer uses flowing left half).
 * Transpose still sampling so caustics run vertically on walls (stillX←y, stillY←x).
 * Animate along bake Y (-frame on stillX) so side-face motion runs top→bottom, not left→right.
 */
fun bakeFlowFromBakedStillScroll(
    bakedStill: BufferedImage,
    flowTemplate: BufferedImage,
    stillFrameW: Int,
    stillFrameH: Int,
    flowFrameW: Int,
    flowFrameH: Int,
    gaseous: Boolean,
): BufferedImage {
    val out = BufferedImage(flowTemplate.width, flowTemplate.height, BufferedImage.TYPE_INT_ARGB)
    val stillFrames = bakedStill.height / stillFrameH
    val flowFrames = flowTemplate.height / flowFrameH
    check(stillFrames > 0) { "baked still has no animation frames" }
    for (frame in 0 until flowFrames) {
        val stillFrame = 0
        val frameY = frame * flowFrameH
        for (y in 0 until flowFrameH) {
            for (x in 0 until flowTemplate.width) {
                val fy = frameY + y
                val maskPx = flowTemplate.getRGB(x, fy)
                val shapeAlpha = (maskPx ushr 24) and 0xFF
                if (shapeAlpha == 0) {
                    out.setRGB(x, fy, 0)
                    continue
                }
                val stillX = Math.floorMod(y % stillFrameH - frame, stillFrameH)
                val stillY = x % stillFrameW
                val stillPx = bakedStill.getRGB(stillX, stillFrame * stillFrameH + stillY)
                if ((stillPx ushr 24) and 0xFF == 0) {
                    out.setRGB(x, fy, 0)
                    continue
                }
                val outA = if (gaseous) (shapeAlpha * 0.42).toInt().coerceIn(24, 255) else 0xFF
                out.setRGB(x, fy, (outA shl 24) or (stillPx and 0xFFFFFF))
            }
        }
    }
    return out
}

fun vanillaWaterToHeatFlow(water: BufferedImage): BufferedImage {
    val out = BufferedImage(water.width, water.height, BufferedImage.TYPE_INT_ARGB)
    for (y in 0 until water.height) {
        for (x in 0 until water.width) {
            val argb = water.getRGB(x, y)
            val a = argb ushr 24 and 0xFF
            if (a == 0) {
                out.setRGB(x, y, 0)
            } else {
                val r = argb shr 16 and 0xFF
                val g = argb shr 8 and 0xFF
                val b = argb and 0xFF
                val lum = (r * 77 + g * 150 + b * 29) ushr 8
                // Water alpha (~180) is only a shape mask; BC liquids bake to fully opaque flow sprites.
                out.setRGB(x, y, 0xFF000000.toInt() or (lum shl 16) or (lum shl 8) or lum)
            }
        }
    }
    return out
}

tasks.register("generateFluidBucketAssets") {
    group = "buildcraft"
    description = "Regenerate fluid block textures, bucket icons, underwater overlays, and bucket item JSON."
    val heatStill = file("gradle/fluid_assets/heat_still.png")
    val fluidMask = file("src/main/resources/assets/buildcraftenergy/textures/item/mask/bucket_fluid.png")
    val fluidOutDir = file("src/main/resources/assets/buildcraftenergy/textures/item/bucket_fluid")
    val underwaterOutDir = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/underwater")
    val bakedOutDir = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/baked")
    val itemsDir = file("src/main/resources/assets/buildcraftenergy/items")
    val modelsDir = file("src/main/resources/assets/buildcraftenergy/models/item/fluid_buckets")
    inputs.file(heatStill)
    inputs.file(fluidMask)
    inputs.files((0..2).map { file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${it}_still.png") })
    inputs.files((0..2).map { file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${it}_still.png.mcmeta") })
    inputs.files((0..2).map { file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${it}_flow.png.mcmeta") })
    outputs.dir(bakedOutDir)
    outputs.dir(underwaterOutDir)
    outputs.dir(fluidOutDir)
    outputs.files((0..2).map { file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${it}_flow.png") })
    doLast {
        require(heatStill.isFile) { "Missing ${heatStill.path} — extract from a built JAR or add the texture." }
        require(fluidMask.isFile) { "Missing ${fluidMask.path} (bucket fluid mask)." }
        fluidOutDir.mkdirs()
        bakedOutDir.mkdirs()
        underwaterOutDir.mkdirs()
        modelsDir.mkdirs()

        fun recolor(
            lumPixel: Int,
            light: Int,
            dark: Int,
            gaseous: Boolean = false,
            shapeAlpha: Int = (lumPixel ushr 24) and 0xFF,
        ): Int {
            if (shapeAlpha == 0) {
                return 0
            }
            val wr = (lumPixel shr 16) and 0xFF
            val wg = (lumPixel shr 8) and 0xFF
            val wb = lumPixel and 0xFF
            val lr = (light shr 16) and 0xFF
            val lg = (light shr 8) and 0xFF
            val lb = light and 0xFF
            val dr = (dark shr 16) and 0xFF
            val dg = (dark shr 8) and 0xFF
            val db = dark and 0xFF
            val outR = (dr * (256 - wr) + lr * wr) / 256
            val outG = (dg * (256 - wg) + lg * wg) / 256
            val outB = (db * (256 - wb) + lb * wb) / 256
            val outA = if (gaseous) (shapeAlpha * 0.42).toInt().coerceIn(24, 255) else 0xFF
            return (outA shl 24) or (outR shl 16) or (outG shl 8) or outB
        }

        fun bakeImage(src: BufferedImage, light: Int, dark: Int, gaseous: Boolean): BufferedImage {
            val out = BufferedImage(src.width, src.height, BufferedImage.TYPE_INT_ARGB)
            for (y in 0 until src.height) {
                for (x in 0 until src.width) {
                    out.setRGB(x, y, recolor(src.getRGB(x, y), light, dark, gaseous))
                }
            }
            return out
        }

        val mcVersion = providers.gradleProperty("minecraft_version").get()
        val mcJar = findMinecraftClientJar(file(".gradle/loom-cache/minecraftMaven/net/minecraft"), mcVersion)
        val heatFlowTemplate = vanillaWaterToHeatFlow(loadVanillaWaterFlow(mcJar))
        for (heat in 0..2) {
            ImageIO.write(
                heatFlowTemplate,
                "PNG",
                file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_flow.png")
            )
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
            val gaseous = base == "fuel_gaseous"
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

                val heatStillTemplate = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_still.png")
                val heatFlowTemplate = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_flow.png")
                require(heatStillTemplate.isFile) { "Missing ${heatStillTemplate.path}" }
                require(heatFlowTemplate.isFile) { "Missing ${heatFlowTemplate.path}" }
                val stillTemplate = ImageIO.read(heatStillTemplate)
                val flowTemplate = ImageIO.read(heatFlowTemplate)
                val bakedStill = bakeImage(stillTemplate, adjLight, adjDark, gaseous)
                ImageIO.write(bakedStill, "PNG", bakedOutDir.resolve("$fluid.png"))
                val stillMcmeta = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_still.png.mcmeta")
                val flowMcmeta = file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_flow.png.mcmeta")
                require(stillMcmeta.isFile) { "Missing ${stillMcmeta.path}" }
                require(flowMcmeta.isFile) { "Missing ${flowMcmeta.path}" }
                val (stillFrameW, stillFrameH) = readAnimationFrameSize(stillMcmeta)
                val (flowFrameW, flowFrameH) = readAnimationFrameSize(flowMcmeta)
                ImageIO.write(
                    bakeFlowFromBakedStillScroll(
                        bakedStill,
                        flowTemplate,
                        stillFrameW,
                        stillFrameH,
                        flowFrameW,
                        flowFrameH,
                        gaseous,
                    ),
                    "PNG",
                    bakedOutDir.resolve("${fluid}_flow.png"),
                )
                stillMcmeta.copyTo(bakedOutDir.resolve("$fluid.png.mcmeta"), overwrite = true)
                flowMcmeta.copyTo(bakedOutDir.resolve("${fluid}_flow.png.mcmeta"), overwrite = true)

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
                ImageIO.write(underwater, "PNG", underwaterOutDir.resolve("$fluid.png"))

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
        logger.lifecycle("Regenerated ${fluidData.size * heats.size} fluid block, bucket, and underwater assets")
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
    // Generator mutates src/main/resources; track its outputs so this task never copies stale PNGs.
    inputs.files(tasks.named("generateFluidBucketAssets").map { it.outputs.files })
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

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Jar>("sourcesJar") {
    dependsOn("runDatagen")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

/** Always rebuild from scratch so stale build/resources (e.g. regenerated fluid PNGs) never linger in the JAR. */
tasks.named("clean") {
    delete(layout.projectDirectory.dir("run"))
    delete(layout.projectDirectory.dir("run_server"))
}

tasks.named("build") {
    dependsOn("clean")
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
