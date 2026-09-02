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
val reiVer = sc.properties.rawOrNull("deps", "rei")?.toString()
sc.constants {
    put("has_jei", jeiVer != null)
    put("has_rei", reiVer != null)
}

val mcVersion = sc.current.version
val javaRelease = if (sc.current.parsed >= "26.1") 25 else 21
val javaVer = if (javaRelease >= 25) JavaVersion.VERSION_25 else JavaVersion.VERSION_21

// Generators mutate shared src/main; they run on ONE node and the output is committed for all the others.
val isGeneratorNode = project.name == "26.3"

val buildDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yy.M.d"))
version = "$buildDate+mc$mcVersion"

base {
    archivesName.set("BCRefabricated")
}

repositories {
    maven("https://maven.blamejared.com")
    // Scope every repo so one flaky mirror cannot abort the rest.
    maven("https://maven.fabricmc.net") {
        content { includeGroup("teamreborn") }
    }
    maven("https://maven.shedaniel.me") {
        content {
            includeGroupByRegex("me\\.shedaniel.*")
        }
    }
    maven("https://maven.architectury.dev") {
        content { includeGroup("dev.architectury") }
    }
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
        // Java 23+ only: 26.x needs it (LWJGL/MC use sun.misc.Unsafe), Java 21 refuses to start with it.
        if (sc.current.parsed >= "26.1") {
            jvmArguments.add("--sun-misc-unsafe-memory-access=allow")
        }
    }
}

if (isGeneratorNode) {
    fabricApi {
        configureDataGeneration {
            client = true
            // Must be the ROOT src/main/generated — otherwise output lands in the node's own dir and is never consumed.
            outputDirectory = rootProject.layout.projectDirectory.file("src/main/generated")
        }
    }
}

// ---------------------------------------------------------------------------
// Source sets
// ---------------------------------------------------------------------------

sourceSets {
    main {
        resources {
            srcDir("src/main/generated")
        }
    }
}

val versionJavaSrc = projectDir.resolve("src/main/java")
if (versionJavaSrc.exists()) {
    afterEvaluate {
        tasks.named<JavaCompile>("compileJava").configure {
            source(versionJavaSrc)
        }
    }
}

// OVERRIDE ROOTS: src/main/versions/<mc>/java/<pkg>/Foo.java REPLACES the shared src/main/java class of the
// same path on that node (the shared copy is dropped from compileJava). versions/_ge_<X>[_lt_<Y>]/java does
// the same for every node matching the range, so sibling nodes share one copy. Priority: exact > range.
fun bcVerCompare(a: String, b: String): Int {
    val pa = a.split('.'); val pb = b.split('.')
    for (i in 0 until maxOf(pa.size, pb.size)) {
        val d = (pa.getOrNull(i)?.takeWhile { c -> c.isDigit() }?.toIntOrNull() ?: 0) -
            (pb.getOrNull(i)?.takeWhile { c -> c.isDigit() }?.toIntOrNull() ?: 0)
        if (d != 0) return d
    }
    return 0
}
// SIMULTANEOUS-BUILD SAFETY: the ACTIVE node compiles the raw src/main tree (chiseled in place, with no
// generated tree of its own); every other node compiles its own build/generated/stonecutter tree while the
// raw tree still holds the active node's form. Reading overrides from the wrong one breaks a fan-out build.
val curVer = sc.current.version
val activeVer = Regex("stonecutter active \"([^\"]+)\"")
    .find(rootProject.file("stonecutter.gradle.kts").readText())?.groupValues?.get(1)
val rawVersionsRoot = rootProject.projectDir.resolve("src/main/versions")
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
    // Winner per relative path, resolved from the raw tree: exact dir first, then ranges.
    val pathOwner = LinkedHashMap<String, String>()
    overrideDirNames.forEach { dn ->
        val rawRoot = rawVersionsRoot.resolve("$dn/java")
        rawRoot.walkTopDown().filter { it.isFile && it.extension == "java" }
            .forEach { pathOwner.putIfAbsent(it.relativeTo(rawRoot).invariantSeparatorsPath, dn) }
    }
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

// ---------------------------------------------------------------------------
// Dependencies
// ---------------------------------------------------------------------------

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    loomx.applyMojangMappings()
    implementation("net.fabricmc:fabric-loader:${sc.properties.raw("deps", "loader")}")
    // Loom attaches these on 26.x but not on the 1.21.x loom-back-compat path; loader provides them at runtime.
    if (sc.current.parsed < "26.1") {
        compileOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")
        compileOnly("io.github.llamalad7:mixinextras-fabric:0.5.4")
    }
    // MAPPING RULE for every third-party jar below: 1.21.x artifacts are intermediary-mapped and need the
    // mod* configurations so Loom remaps them (their API otherwise leaks class_1799/class_2960 names); 26.x
    // artifacts already ship Mojang official names, so the plain configurations work.
    val fabricApi = "net.fabricmc.fabric-api:fabric-api:${sc.properties.raw("deps", "fabric_api")}"
    if (sc.current.parsed < "26.1") modImplementation(fabricApi) else implementation(fabricApi)

    implementation("org.jspecify:jspecify:1.0.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    if (jeiVer != null) {
        if (sc.current.parsed < "26.1") {
            modCompileOnly("mezz.jei:jei-$mcVersion-fabric-api:$jeiVer")
        } else {
            compileOnly("mezz.jei:jei-$mcVersion-fabric-api:$jeiVer")
        }
    }

    if (sc.current.parsed < "26.1") {
        modCompileOnly("teamreborn:energy:${sc.properties.raw("deps", "energy")}")
    } else {
        compileOnly("teamreborn:energy:${sc.properties.raw("deps", "energy")}")
    }

    if (reiVer != null) {
        if (sc.current.parsed < "26.1") {
            modCompileOnly("me.shedaniel:RoughlyEnoughItems-fabric:$reiVer")
        } else {
            // REI's 26.x pom marks architectury/cloth runtime-scope; the RUNTIME variant is what pulls them in.
            compileOnly("me.shedaniel:RoughlyEnoughItems-fabric:$reiVer") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
                }
            }
        }
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")

    // Dev runs only, never the jar: without these the transformer stack is missing from the runClient/
    // runServer classpath ("ASM not detected", MixinBootstrap CNFE). sponge-mixin must match fabric-loader
    // 0.19.3 — 0.15.x lacks IAdviceProvider and crashes at boot.
    runtimeOnly("org.ow2.asm:asm:9.7.1")
    runtimeOnly("org.ow2.asm:asm-analysis:9.7.1")
    runtimeOnly("org.ow2.asm:asm-commons:9.7.1")
    runtimeOnly("org.ow2.asm:asm-tree:9.7.1")
    runtimeOnly("org.ow2.asm:asm-util:9.7.1")
    if (sc.current.parsed < "26.1") {
        runtimeOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")
        runtimeOnly("io.github.llamalad7:mixinextras-fabric:0.4.1")
    }
}

// ---------------------------------------------------------------------------
// Compilation & resources
// ---------------------------------------------------------------------------

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
    if (reiVer == null) {
        exclude("**/integration/rei/**")
    }
}

// ===========================================================================
// 1.21.1 DATA BACKPORT (build-time converter)
//
// Data/assets are authored once in the modern (1.21.2+/26.x) JSON format; this rewrites the affected files
// into a generated dir that overrides them on the 1.21.1 node only, so shared src/main stays the one source.
// ===========================================================================
val bc1211DataDir = layout.buildDirectory.dir("generated/bc1211-data")
if (sc.current.parsed < "1.21.2") {
    // string -> {item|tag}, array -> array of those, object -> unchanged. A Fabric custom ingredient
    // ("fabric:type") is reduced to its base item/tag: 1.21.1 cannot parse it, so a gate recipe there accepts
    // any base plug_gate rather than failing to load.
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

    // 1.21.1 has no client-item-definition system (assets/<ns>/items/<id>.json); it resolves models straight
    // from assets/<ns>/models/item/<id>.json. A plain "model" def becomes {"parent": <target>}; a
    // custom_model_data range_dispatch becomes legacy integer "overrides" merged onto the base model.
    @Suppress("UNCHECKED_CAST")
    fun build1211ItemModel(def: Map<*, *>, existing: File): MutableMap<String, Any?>? {
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
                val def = JsonSlurper().parse(file) as? Map<*, *> ?: return@forEach
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

    // Drop the modern copies the converter replaces, or processResources keeps them and the converted models
    // lose their integer overrides (this is what once left paintbrush without any).
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

// ===========================================================================
// PRE-26.3 DATA BACKPORT (build-time converter)
//
// Advancements/loot are authored once in the 26.3 registry format (condition discriminator "type", single
// "condition"/"player", recipe_crafted "recipes"); this rewrites them into the pre-26.3 shapes for the
// older nodes only. fabric:load_conditions subtrees keep Fabric's own schema and are skipped.
// ===========================================================================
val bcPre263DataDir = layout.buildDirectory.dir("generated/bc-pre263-data")
if (sc.current.parsed < "26.3-pre-1") {
    @Suppress("UNCHECKED_CAST")
    fun backportCondition(node: Any?) {
        val map = node as? MutableMap<String, Any?> ?: return
        val type = map.remove("type")
        if (type is String) {
            map["condition"] = type
        }
        (map["terms"] as? List<*>)?.forEach { backportCondition(it) }
        backportCondition(map["term"])
    }

    @Suppress("UNCHECKED_CAST")
    fun backportLoot(node: Any?) {
        when (node) {
            is MutableMap<*, *> -> {
                val map = node as MutableMap<String, Any?>
                val single = map["condition"]
                if (single is Map<*, *>) {
                    map.remove("condition")
                    backportCondition(single)
                    map["conditions"] = listOf(single)
                }
                map.forEach { (key, value) ->
                    if (key != "fabric:load_conditions") {
                        backportLoot(value)
                    }
                }
            }
            is List<*> -> node.forEach { backportLoot(it) }
            else -> {}
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun backportAdvancement(json: Any?) {
        val map = json as? MutableMap<String, Any?> ?: return
        val criteria = map["criteria"] as? MutableMap<String, Any?> ?: return
        for (c in criteria.values) {
            val crit = c as? MutableMap<String, Any?> ?: continue
            val conditions = crit["conditions"] as? MutableMap<String, Any?> ?: continue
            val recipes = conditions.remove("recipes")
            if (recipes is List<*> && recipes.size == 1) {
                conditions["recipe_id"] = recipes[0]
            }
            val player = conditions["player"]
            if (player is Map<*, *>) {
                backportCondition(player)
                conditions["player"] = listOf(player)
            }
        }
        backportLoot(map["criteria"])
    }

    tasks.register("convertDataPre263") {
        group = "buildcraft"
        description = "Rewrite advancement + loot table JSON into the pre-26.3 registry format."
        val srcRoots = listOf(rootProject.file("src/main/resources"), rootProject.file("src/main/generated"))
        val outDir = bcPre263DataDir.get().asFile
        srcRoots.forEachIndexed { i, root -> inputs.dir(root).withPropertyName("dataSource$i") }
        outputs.dir(outDir).withPropertyName("convertedPre263Data")
        doLast {
            outDir.deleteRecursively()
            var advancements = 0
            var lootTables = 0
            srcRoots.forEach { root ->
                fileTree(root) { include("data/*/advancement/**/*.json", "data/*/loot_table/**/*.json") }.forEach { file ->
                    val rel = file.relativeTo(root).invariantSeparatorsPath
                    val json = JsonSlurper().parse(file)
                    if (rel.contains("/advancement/")) {
                        backportAdvancement(json)
                        advancements++
                    } else {
                        backportLoot(json)
                        lootTables++
                    }
                    val target = outDir.resolve(rel)
                    target.parentFile.mkdirs()
                    target.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(json)) + "\n")
                }
            }
            logger.lifecycle("Converted $advancements advancement + $lootTables loot table JSON(s) to the pre-26.3 format -> ${outDir.path}")
        }
    }

    sourceSets.named("main") {
        resources {
            exclude("data/*/advancement/**", "data/*/loot_table/**")
        }
    }
}

tasks.processResources {
    // Added last so the converted 1.21.1 dir wins duplicates against the modern copies.
    if (sc.current.parsed < "1.21.2") {
        dependsOn("convertDataFor1211")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(bc1211DataDir)
    }
    if (sc.current.parsed < "26.3-pre-1") {
        dependsOn("convertDataPre263")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(bcPre263DataDir)
    }
    // Generators are deliberately NOT in the build graph: a clean build only packages the committed output and
    // never mutates shared source. Refresh it explicitly with `:26.3:generateAssets`.
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
    // Drop entrypoints whose integration was excluded from compilation. Each must stay on ONE line in
    // fabric.mod.json — the filter blanks whole lines, so a reformatted array would leave invalid JSON.
    val dropEntrypoints = buildList {
        if (jeiVer == null) add("jei_mod_plugin")
        if (reiVer == null) add("rei_client")
    }
    inputs.property("dropEntrypoints", dropEntrypoints.toString())
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
        if (dropEntrypoints.isNotEmpty()) {
            filter { line: String ->
                if (dropEntrypoints.any { line.contains("\"$it\":") }) "" else line
            }
        }
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
    // Lets 26.x (release 25) compile even when Gradle runs on JDK 21; Foojay auto-downloads a missing JDK.
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaRelease))
    }
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    if (sc.current.parsed >= "26.1") {
        jvmArgs("--sun-misc-unsafe-memory-access=allow")
    }
}

/** MUST stay typed as Delete: an untyped block binds delete() to Project.delete(), which runs at CONFIGURE
 * time of every gradle invocation and silently wipes run/ (dev-server worlds, eula, properties). */
tasks.named<Delete>("clean") {
    delete(layout.projectDirectory.dir("run"))
    delete(layout.projectDirectory.dir("run_server"))
}

// ===========================================================================
// Asset generation (generator node only) — mutates the shared src/main tree; output is committed.
// ===========================================================================

if (isGeneratorNode) {

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

    // The generator writes into src/main/resources, which these tasks read; without an explicit order Gradle
    // rejects the undeclared producer→consumer relationship whenever both end up in the same graph.
    listOf("stonecutterPrepare", "stonecutterGenerate", "processResources").forEach { tn ->
        tasks.matching { it.name == tn }.configureEach { mustRunAfter("generateFluidBucketAssets") }
    }

    tasks.register("generateAssets") {
        group = "buildcraft"
        description = "Run all asset generators (fluid bucket baking + data generation). 26.3 only."
        dependsOn("generateFluidBucketAssets", "runDatagen")
    }
}

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
