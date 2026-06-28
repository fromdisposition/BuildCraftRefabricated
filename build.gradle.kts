import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.awt.image.BufferedImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipFile
import javax.imageio.ImageIO

plugins {
    id("dev.kikugie.loom-back-compat")
    id("dev.kikugie.stonecutter")
}

// ---------------------------------------------------------------------------
// Per-node identity
// ---------------------------------------------------------------------------

val jeiVer = sc.properties.rawOrNull("deps", "jei")?.toString()
sc.constants {
    put("has_jei", jeiVer != null)
}

val mcVersion = sc.current.version
val javaRelease = if (sc.current.parsed >= "26.1") 25 else 21
val javaVer = if (javaRelease >= 25) JavaVersion.VERSION_25 else JavaVersion.VERSION_21

// Asset generators (fluid bucket baking + fabric-datagen) mutate the shared src/main resources and
// src/main/generated. They run on ONE node only — 26.1, where every generator works — and the
// committed output is then consumed as static resources by every other node. See the
// "Asset generation" section at the bottom of this file.
val isGeneratorNode = project.name == "26.1"

// Version format: <yy>.<M>.<d>+mc<mcVersion>, date without leading zeros (e.g. 26.6.18+mc1.21.11).
val buildDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yy.M.d"))
version = "$buildDate+mc$mcVersion"

base {
    // Jar name becomes <archivesName>-<version>.jar = BCRefabricated-26.6.18+mc1.21.11.jar
    archivesName.set("BCRefabricated")
}

repositories {
    maven("https://maven.blamejared.com")
    maven("https://maven.teamreborn.org")
}

// ---------------------------------------------------------------------------
// Loom / Fabric
// ---------------------------------------------------------------------------

loom {
    mods {
        create("buildcraftrefabricated") {
            sourceSet(sourceSets["main"])
        }
    }
    runs.configureEach {
        // --sun-misc-unsafe-memory-access exists only on Java 23+; the 26.x lines run on Java 25 and
        // need it (LWJGL/MC use sun.misc.Unsafe). 1.21.x runs on Java 21, where the flag is both
        // unrecognized (JVM won't start) and unnecessary (Unsafe access is not yet restricted).
        if (sc.current.parsed >= "26.1") {
            vmArg("--sun-misc-unsafe-memory-access=allow")
        }
    }
}

// Data generation is a generator task: configure it (and its runDatagen run) only on 26.1.
if (isGeneratorNode) {
    fabricApi {
        configureDataGeneration {
            client = true
        }
    }
}

// ---------------------------------------------------------------------------
// Source sets
// ---------------------------------------------------------------------------

sourceSets {
    main {
        resources {
            // Datagen output (committed) — consumed as static resources on every node.
            srcDir("src/main/generated")
        }
    }
}

// Version-specific shim sources live in versions/<mc>/src/main/java
val versionJavaSrc = projectDir.resolve("src/main/java")
if (versionJavaSrc.exists()) {
    afterEvaluate {
        tasks.named<JavaCompile>("compileJava").configure {
            source(versionJavaSrc)
        }
    }
}

// Per-MC-version API converters / replacement classes live in src/main/versions/<mc>/java
// (shared root layout). Only the active version's directory is compiled into that build.
//
// OVERRIDE SEMANTICS: a class at versions/<active>/java/<pkg>/Foo.java REPLACES the same-named class in the
// shared src/main/java for that node only. The shared copy is excluded from compileJava (so no duplicate
// class), and the modern shared source stays untouched. This lets a wholly version-specific render/impl
// live as a full separate file per version instead of polluting the shared code with conditionals. Removing
// a version = deleting its versions/<mc>/java dir (the shared copy then compiles everywhere again).
// RANGE OVERRIDE DIRS (dedup sibling nodes): besides the exact versions/<mc>/java, a directory named
// versions/_ge_<X>[_lt_<Y>]/java applies to every node whose version satisfies ALL its ge_/lt_ constraints
// (component-wise numeric compare: 1.21.1 < 1.21.10 < 1.21.11 < 26.1 < 26.2). e.g. versions/_lt_26.1/java
// holds ONE copy shared by all 1.21.x nodes (no more 3× identical files). Priority: exact > range; the shared
// src/main copy is excluded when any override root provides the path.
fun bcVerCompare(a: String, b: String): Int {
    val pa = a.split('.'); val pb = b.split('.')
    for (i in 0 until maxOf(pa.size, pb.size)) {
        val d = (pa.getOrNull(i)?.toIntOrNull() ?: 0) - (pb.getOrNull(i)?.toIntOrNull() ?: 0)
        if (d != 0) return d
    }
    return 0
}
// SIMULTANEOUS-BUILD SAFETY: where an override file's SOURCE is read from depends on whether this node is
// the active one, because Stonecutter compiles them differently:
//   * the ACTIVE node compiles the raw src/main tree, which Stonecutter has chiseled IN PLACE for it, AND
//     it does NOT populate its own build/generated/stonecutter tree — so its overrides must come from RAW.
//   * a NON-ACTIVE node compiles from its own build/generated/stonecutter tree (chiseled per node), while
//     the raw tree still holds the active node's form — so its overrides must come from GENERATED.
// This split is what lets `gradlew clean build` fan out to all subprojects at once (every non-active node
// reads its correctly-chiseled generated overrides) while a plain per-node build of the active node still
// works (it reads raw). The WINNER set (which relative paths are overridden, by which dir) is always taken
// from the raw tree since it exists at configuration time, before any Stonecutter generate task has run.
val curVer = sc.current.version
val activeVer = Regex("stonecutter active \"([^\"]+)\"")
    .find(rootProject.file("stonecutter.gradle.kts").readText())?.groupValues?.get(1)
val rawVersionsRoot = rootProject.projectDir.resolve("src/main/versions")
// Active node => raw (chiseled in place); non-active => its generated tree.
val srcVersionsRoot = if (curVer == activeVer) rawVersionsRoot
    else projectDir.resolve("build/generated/stonecutter/main/versions")
val overrideDirNames = buildList {
    if (rawVersionsRoot.resolve("$curVer/java").exists()) add(curVer)
    (rawVersionsRoot.listFiles() ?: emptyArray())
        .filter { it.isDirectory && it.name.startsWith("_") }
        .filter { dir ->
            val ms = Regex("(ge|lt)_([0-9][0-9.]*)").findAll(dir.name).toList()
            ms.isNotEmpty() && ms.all { m ->
                val ver = m.groupValues[2]
                if (m.groupValues[1] == "ge") bcVerCompare(curVer, ver) >= 0 else bcVerCompare(curVer, ver) < 0
            }
        }
        .filter { it.resolve("java").exists() }
        .forEach { add(it.name) }
}
if (overrideDirNames.isNotEmpty()) {
    // winning dir per relative path (first in priority order: exact, then ranges) — from the raw tree
    val pathOwner = LinkedHashMap<String, String>()
    overrideDirNames.forEach { dn ->
        val rawRoot = rawVersionsRoot.resolve("$dn/java")
        rawRoot.walkTopDown().filter { it.isFile && it.extension == "java" }
            .forEach { pathOwner.putIfAbsent(it.relativeTo(rawRoot).invariantSeparatorsPath, dn) }
    }
    // source roots (raw for the active node, generated otherwise), keyed by dir name; prefix identifies
    // which override root a file under compilation belongs to.
    val srcRoots = overrideDirNames.associateWith { srcVersionsRoot.resolve("$it/java") }
    val prefixes = srcRoots.mapValues { it.value.path.replace('\\', '/') + "/" }
    afterEvaluate {
        tasks.named<JavaCompile>("compileJava").configure {
            srcRoots.values.forEach { source(it) }
            exclude { el ->
                if (el.isDirectory) return@exclude false
                val rel = el.relativePath.pathString.replace('\\', '/')
                val owner = pathOwner[rel] ?: return@exclude false
                val path = el.file.path.replace('\\', '/')
                val underDir = prefixes.entries.firstOrNull { path.startsWith(it.value) }?.key
                // shared src/main copy of an overridden path -> drop; a versions file -> keep only the winner
                if (underDir == null) true else underDir != owner
            }
        }
    }
}

// Loader-specific hooks live in buildcraft.lib.fabric.* and buildcraft.fabric.*

// ---------------------------------------------------------------------------
// Dependencies
// ---------------------------------------------------------------------------

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    loomx.applyMojangMappings()
    implementation("net.fabricmc:fabric-loader:${sc.properties.raw("deps", "loader")}")
    // Loom auto-attaches sponge-mixin/mixinextras for the 26.x targets but not on the 1.21.x
    // loom-back-compat path; add them explicitly (loader provides them at runtime).
    if (sc.current.parsed < "26.1") {
        compileOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")
        compileOnly("io.github.llamalad7:mixinextras-fabric:0.5.4")
    }
    // 1.21.x Fabric API uses intermediary class names internally; modImplementation triggers Loom remapping.
    // 26.x Fabric API already uses Mojang official names, so plain implementation works there.
    val fabricApi = "net.fabricmc.fabric-api:fabric-api:${sc.properties.raw("deps", "fabric_api")}"
    if (sc.current.parsed < "26.1") modImplementation(fabricApi) else implementation(fabricApi)

    implementation("org.jspecify:jspecify:1.0.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    if (jeiVer != null) {
        // JEI for 1.21.x (27.x) is an intermediary-mapped mod jar — modCompileOnly remaps it to Mojang
        // names, otherwise its API leaks intermediary names (class_1799, class_2960, ...). 26.x JEI (29.x)
        // already ships Mojang official names, so plain compileOnly works there.
        if (sc.current.parsed < "26.1") {
            modCompileOnly("mezz.jei:jei-$mcVersion-fabric-api:$jeiVer")
        } else {
            compileOnly("mezz.jei:jei-$mcVersion-fabric-api:$jeiVer")
        }
    }

    // energy 4.x (1.21.x) is an intermediary-mapped mod jar — it must be remapped (modCompileOnly)
    // or its API leaks intermediary names (e.g. class_2350). 26.x energy 5.0.0 already uses Mojang names.
    if (sc.current.parsed < "26.1") {
        modCompileOnly("teamreborn:energy:${sc.properties.raw("deps", "energy")}")
    } else {
        compileOnly("teamreborn:energy:${sc.properties.raw("deps", "energy")}")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
}

// ---------------------------------------------------------------------------
// Compilation & resources
// ---------------------------------------------------------------------------

// Full Fabric compile target — gameplay modules in progress; guide/script still reference excluded APIs.
val notYetOnFabric = listOf<String>()

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
    options.release.set(javaRelease)
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "2000"))
    notYetOnFabric.forEach { exclude(it) }
    if (jeiVer == null) {
        exclude("**/integration/jei/**")
    }
}

// ===========================================================================
// 1.21.1 DATA BACKPORT (build-time converter)
//
// The committed data/assets are authored in the modern (1.21.2+/26.x) JSON format. 1.21.1's parsers
// need the legacy shapes. Rather than maintain a duplicate resource tree, this task rewrites the affected
// JSON into a generated dir that OVERRIDES the modern copies on the 1.21.1 node only (every other node
// already speaks the modern format). The shared src/main stays the single source of truth.
//
// Phase 1 — recipes: ingredient values are bare id/tag strings in the modern format ("minecraft:slime_ball",
// "#c:ingots/iron"); 1.21.1 requires ingredient OBJECTS ({"item":...} / {"tag":...}). Also custom_model_data
// is a plain int on 1.21.1, not the {"floats":[...]}/{"strings":[...]} component of 1.21.4+.
// (The recipe folder is "recipe"/"advancement" singular on 1.21.1 already — no path rename needed.)
val bc1211DataDir = layout.buildDirectory.dir("generated/bc1211-data")
if (sc.current.parsed < "1.21.2") {
    // legacy ingredient: string -> {item|tag}; array -> array of those; object -> unchanged, EXCEPT a Fabric
    // custom ingredient (carries "fabric:type", e.g. "fabric:custom_data" used by gate recipes to match a
    // plug_gate with specific NBT) which 1.21.1's recipe parser does not understand -> reduce it to its plain
    // base item/tag. This loses the NBT/component match (a 1.21.1 gate recipe accepts any base plug_gate), but
    // makes the recipe parse and craft instead of erroring out.
    fun convIngredient(v: Any?): Any? = when (v) {
        is String -> if (v.startsWith("#")) mapOf("tag" to v.substring(1)) else mapOf("item" to v)
        is List<*> -> v.map { convIngredient(it) }
        is Map<*, *> -> {
            val base = (v["base"] ?: v["item"] ?: v["tag"]) as? String
            if (v.containsKey("fabric:type") && base != null) {
                if (base.startsWith("#")) mapOf("tag" to base.substring(1)) else mapOf("item" to base)
            } else v
        }
        else -> v
    }
    @Suppress("UNCHECKED_CAST")
    fun convertRecipe(json: Any?) {
        if (json !is MutableMap<*, *>) return
        val map = json as MutableMap<String, Any?>
        when (map["type"]) {
            "minecraft:crafting_shaped" -> (map["key"] as? MutableMap<String, Any?>)?.let { k ->
                k.keys.toList().forEach { kk -> k[kk] = convIngredient(k[kk]) }
            }
            "minecraft:crafting_shapeless" -> (map["ingredients"] as? MutableList<Any?>)?.let { list ->
                for (i in list.indices) list[i] = convIngredient(list[i])
            }
            "minecraft:smelting", "minecraft:blasting", "minecraft:smoking",
            "minecraft:campfire_cooking", "minecraft:stonecutting" ->
                map["ingredient"] = convIngredient(map["ingredient"])
        }
        // result.components custom_model_data: floats[0] -> int; strings/flags cannot map -> drop
        val components = (map["result"] as? MutableMap<String, Any?>)?.get("components") as? MutableMap<String, Any?>
        if (components != null) {
            val cmd = components["minecraft:custom_model_data"]
            if (cmd is Map<*, *>) {
                val floats = cmd["floats"] as? List<*>
                if (floats != null && floats.isNotEmpty()) {
                    components["minecraft:custom_model_data"] = (floats[0] as Number).toInt()
                } else {
                    components.remove("minecraft:custom_model_data")
                }
            }
        }
    }

    // Item models: the 1.21.4+ client-item-definition system (assets/<ns>/items/<id>.json) does not exist on
    // 1.21.1, which resolves an item's model directly from assets/<ns>/models/item/<id>.json. Translate each
    // definition into that legacy model: a plain "model" def whose target isn't the self path (buckets ->
    // item/fluid_buckets/..., block-as-item models) becomes {"parent": <target>}; a custom_model_data
    // range_dispatch becomes legacy integer "overrides" on the base model (merged onto the existing base
    // model file when present, e.g. paintbrush). Definitions that already point at their own models/item/<id>
    // need nothing (1.21.1 finds them as-is) and are skipped. Unsupported dispatch properties are skipped.
    @Suppress("UNCHECKED_CAST")
    fun build1211ItemModel(def: Map<String, Any?>, existing: File): MutableMap<String, Any?>? {
        val model = def["model"] as? Map<String, Any?> ?: return null
        val overrides = mutableListOf<Map<String, Any?>>()
        val baseRef: String? = when (model["type"]) {
            "minecraft:range_dispatch" -> {
                if (model["property"] != "minecraft:custom_model_data") return null
                (model["entries"] as? List<*>)?.forEach { e ->
                    val em = e as? Map<*, *> ?: return@forEach
                    val th = (em["threshold"] as? Number)?.toInt() ?: return@forEach
                    val mref = (em["model"] as? Map<*, *>)?.get("model") as? String ?: return@forEach
                    overrides.add(linkedMapOf("predicate" to linkedMapOf("custom_model_data" to th), "model" to mref))
                }
                (model["fallback"] as? Map<*, *>)?.get("model") as? String
            }
            "minecraft:model" -> model["model"] as? String
            else -> return null
        }
        return if (existing.exists()) {
            // base model already exists on 1.21.1; only range_dispatch needs the extra overrides merged in
            if (overrides.isEmpty()) null
            else (JsonSlurper().parse(existing) as MutableMap<String, Any?>).also { it["overrides"] = overrides }
        } else {
            if (baseRef == null) return null
            linkedMapOf<String, Any?>("parent" to baseRef).also { if (overrides.isNotEmpty()) it["overrides"] = overrides }
        }
    }

    tasks.register("convertDataFor1211") {
        group = "buildcraft"
        description = "Rewrite modern recipe + item-model JSON into the 1.21.1 legacy format."
        val srcResources = rootProject.file("src/main/resources")
        val outDir = bc1211DataDir.get().asFile
        inputs.dir(srcResources).withPropertyName("sharedResources")
        outputs.dir(outDir).withPropertyName("converted1211Data")
        doLast {
            outDir.deleteRecursively()
            var recipeCount = 0
            fileTree(srcResources) { include("data/*/recipe/**/*.json") }.forEach { file ->
                val json = JsonSlurper().parse(file)
                convertRecipe(json)
                val rel = file.relativeTo(srcResources).invariantSeparatorsPath
                val target = outDir.resolve(rel)
                target.parentFile.mkdirs()
                target.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(json)) + "\n")
                recipeCount++
            }
            var modelCount = 0
            fileTree(srcResources) { include("assets/*/items/**/*.json") }.forEach { file ->
                val parts = file.relativeTo(srcResources).invariantSeparatorsPath.split("/") // assets/<ns>/items/<id...>.json
                val ns = parts[1]
                val id = parts.drop(3).joinToString("/").removeSuffix(".json")
                val def = JsonSlurper().parse(file) as? Map<String, Any?> ?: return@forEach
                val existing = srcResources.resolve("assets/$ns/models/item/$id.json")
                val result = build1211ItemModel(def, existing) ?: return@forEach
                val outFile = outDir.resolve("assets/$ns/models/item/$id.json")
                outFile.parentFile.mkdirs()
                outFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(result)) + "\n")
                modelCount++
            }
            logger.lifecycle("Converted $recipeCount recipe + $modelCount item-model JSON(s) to 1.21.1 format -> ${outDir.path}")
        }
    }

    // Drop the modern copies the converter replaces so its 1.21.1 output wins (processResources duplicate
    // resolution otherwise keeps the modern model — that left paintbrush etc. with no integer overrides).
    // Recipes: the whole recipe/ dir. Item models: the base model of every custom_model_data range_dispatch
    // item (paintbrush/list/map_location/redstone_board/gate_copier) — the converter always regenerates these
    // with legacy integer overrides, so excluding the modern base is safe.
    val rangeDispatchModelExcludes = buildList {
        val assetsRoot = rootProject.file("src/main/resources/assets")
        (assetsRoot.listFiles() ?: emptyArray()).forEach { nsDir ->
            val itemsDir = nsDir.resolve("items")
            if (itemsDir.isDirectory) {
                itemsDir.walkTopDown().filter { it.isFile && it.extension == "json" && it.readText().contains("range_dispatch") }
                    .forEach { add("assets/${nsDir.name}/models/item/${it.relativeTo(itemsDir).invariantSeparatorsPath.removeSuffix(".json")}.json") }
            }
        }
    }
    sourceSets.named("main") {
        resources {
            exclude("data/*/recipe/**")
            rangeDispatchModelExcludes.forEach { exclude(it) }
        }
    }
}

tasks.processResources {
    // On 1.21.1, replace the modern recipe JSON with the legacy-format conversion (see convertDataFor1211)
    // and add the back-ported item models (some override an existing base model, e.g. paintbrush, so the
    // converted dir — added last — must win duplicates).
    if (sc.current.parsed < "1.21.2") {
        dependsOn("convertDataFor1211")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(bc1211DataDir)
    }
    // Generated assets (baked fluid textures/buckets + datagen output) are COMMITTED to src/main/resources
    // and src/main/generated and consumed here as plain static resources on every node. The generators are
    // deliberately NOT wired into the build graph: a clean build only packages the committed output and
    // never mutates shared source. Refresh the committed assets explicitly via `:26.1:generateAssets`.

    val mixinCompatLevel = if (javaRelease >= 25) "JAVA_25" else "JAVA_21"
    val props = mapOf(
        "mod_version" to version,
        "mc_dep_range" to sc.properties.raw("mod", "mc_dep_range").toString(),
        "loader_version" to sc.properties.raw("deps", "loader").toString(),
        "fabric_api_version" to sc.properties.raw("deps", "fabric_api").toString(),
        "energy_version" to sc.properties.raw("deps", "energy").toString(),
        "java_version" to javaRelease.toString(),
        "mixin_compat_level" to mixinCompatLevel,
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
    filesMatching("buildcraft.mixins.json") {
        expand(props)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// ---------------------------------------------------------------------------
// Java toolchain & packaging
// ---------------------------------------------------------------------------

java {
    withSourcesJar()
    sourceCompatibility = javaVer
    targetCompatibility = javaVer
    // Pin the JDK per MC line so 26.x (release 25) compiles even when Gradle itself runs on JDK 21;
    // the Foojay resolver (settings.gradle.kts) auto-downloads a missing JDK.
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaRelease))
    }
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    // Only valid/needed on Java 23+ (26.x → Java 25); 1.21.x runs on Java 21 where it is unrecognized.
    if (sc.current.parsed >= "26.1") {
        jvmArgs("--sun-misc-unsafe-memory-access=allow")
    }
}

/** Always rebuild from scratch so stale build/resources (e.g. regenerated fluid PNGs) never linger in the JAR. */
tasks.named("clean") {
    delete(layout.projectDirectory.dir("run"))
    delete(layout.projectDirectory.dir("run_server"))
}

// ===========================================================================
// Asset generation (26.1 only)
//
// These tasks regenerate committed assets from source templates: fluid bucket/block textures and
// fabric-datagen output. They mutate the shared src/main tree, so they are wired up on the 26.1 node
// only — the one node where every generator is known to work. Run `:26.1:generateAssets` to refresh
// everything at once; the output is committed and consumed as static resources by all other nodes.
// ===========================================================================

if (isGeneratorNode) {

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
        val heatStill = rootProject.file("gradle/fluid_assets/heat_still.png")
        val fluidMask = rootProject.file("src/main/resources/assets/buildcraftenergy/textures/item/mask/bucket_fluid.png")
        val fluidOutDir = rootProject.file("src/main/resources/assets/buildcraftenergy/textures/item/bucket_fluid")
        val underwaterOutDir = rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/underwater")
        val bakedOutDir = rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/baked")
        val itemsDir = rootProject.file("src/main/resources/assets/buildcraftenergy/items")
        val modelsDir = rootProject.file("src/main/resources/assets/buildcraftenergy/models/item/fluid_buckets")
        inputs.file(heatStill)
        inputs.file(fluidMask)
        inputs.files((0..2).map { rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${it}_still.png") })
        inputs.files((0..2).map { rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${it}_still.png.mcmeta") })
        inputs.files((0..2).map { rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${it}_flow.png.mcmeta") })
        outputs.dir(bakedOutDir)
        outputs.dir(underwaterOutDir)
        outputs.dir(fluidOutDir)
        outputs.files((0..2).map { rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${it}_flow.png") })
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

            // mcVersion is the build-script val (sc.current.version); on the 26.1 node it is "26.1.2",
            // which is the merged-jar folder name findMinecraftClientJar matches on.
            val mcJar = findMinecraftClientJar(rootProject.file(".gradle/loom-cache/minecraftMaven/net/minecraft"), mcVersion)
            val heatFlowTemplate = vanillaWaterToHeatFlow(loadVanillaWaterFlow(mcJar))
            for (heat in 0..2) {
                ImageIO.write(
                    heatFlowTemplate,
                    "PNG",
                    rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_flow.png")
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

                    val heatStillTemplate = rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_still.png")
                    val heatFlowTemplate = rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_flow.png")
                    require(heatStillTemplate.isFile) { "Missing ${heatStillTemplate.path}" }
                    require(heatFlowTemplate.isFile) { "Missing ${heatFlowTemplate.path}" }
                    val stillTemplate = ImageIO.read(heatStillTemplate)
                    val flowTemplate = ImageIO.read(heatFlowTemplate)
                    val bakedStill = bakeImage(stillTemplate, adjLight, adjDark, gaseous)
                    ImageIO.write(bakedStill, "PNG", bakedOutDir.resolve("$fluid.png"))
                    val stillMcmeta = rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_still.png.mcmeta")
                    val flowMcmeta = rootProject.file("src/main/resources/assets/buildcraftenergy/textures/block/fluids/heat_${heat}_flow.png.mcmeta")
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

    // The generator writes into src/main/resources, which Stonecutter's prepare/generate tasks read as
    // inputs. In a fan-out `gradlew build` (all nodes at once) Gradle schedules all three and rejects the
    // undeclared producer→consumer relationship. Order the Stonecutter tasks AFTER the generator so the
    // chiseled tree always sees fresh assets (mustRunAfter only applies when the generator is also
    // scheduled — i.e. a 26.1 build via processResources — so compile-only graphs are unaffected).
    listOf("stonecutterPrepare", "stonecutterGenerate").forEach { tn ->
        tasks.matching { it.name == tn }.configureEach { mustRunAfter("generateFluidBucketAssets") }
    }

    /** One-shot aggregate: run every generator (fluid baking + fabric-datagen) on 26.1. */
    tasks.register("generateAssets") {
        group = "buildcraft"
        description = "Run all asset generators (fluid bucket baking + data generation). 26.1 only."
        dependsOn("generateFluidBucketAssets", "runDatagen")
    }
}

// ---------------------------------------------------------------------------
// API browsing helper
// ---------------------------------------------------------------------------

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

        val fapiVersion = sc.properties.raw("deps", "fabric_api").toString()
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
