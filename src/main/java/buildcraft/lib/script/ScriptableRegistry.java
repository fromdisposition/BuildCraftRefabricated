package buildcraft.lib.script;

import net.minecraft.resources.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.IOUtils;

import buildcraft.lib.fabric.loader.ModList;
import buildcraft.lib.fabric.loader.GamePaths;
import buildcraft.lib.fabric.loader.IModInfo;

import buildcraft.api.core.BCLog;
import buildcraft.api.registry.IReloadableRegistryManager;
import buildcraft.api.registry.IScriptableRegistry;

import buildcraft.lib.misc.JsonUtil;
import buildcraft.lib.misc.TimeUtil;
import buildcraft.lib.script.SimpleScript.ScriptAction;
import buildcraft.lib.script.SimpleScript.ScriptActionAdd;
import buildcraft.lib.script.SimpleScript.ScriptActionRemove;
import buildcraft.lib.script.SimpleScript.ScriptActionReplace;

public class ScriptableRegistry<E> extends SimpleReloadableRegistry<E> implements IScriptableRegistry<E> {

    private final String entryPath;
    private final Map<String, Class<? extends E>> types = new HashMap<>();
    private final Map<String, IEntryDeserializer<? extends E>> deserializers = new HashMap<>();
    private final Set<String> sourceDomains = new HashSet<>();

    public ScriptableRegistry(IReloadableRegistryManager manager, String entryPath) {
        super(manager);
        this.entryPath = entryPath;
    }

    public ScriptableRegistry(PackType type, String entryPath) {
        this(type == PackType.DATA_PACK ? ReloadableRegistryManager.DATA_PACKS
            : ReloadableRegistryManager.RESOURCE_PACKS, entryPath);
        manager.registerRegistry(this);
    }

    @Override
    public String getEntryType() {
        return entryPath;
    }

    @Override
    public Map<String, Class<? extends E>> getScriptableTypes() {
        return types;
    }

    @Override
    public Map<String, IEntryDeserializer<? extends E>> getCustomDeserializers() {
        return deserializers;
    }

    @Override
    public Set<String> getSourceDomains() {
        return Collections.unmodifiableSet(sourceDomains);
    }

    void loadScripts(Gson gson) {
        try (AutoCloseable logFile = SimpleScript.createLogFile(entryPath)) {
            long start = System.currentTimeMillis();
            SimpleScript.logForAll("Started at: " + TimeUtil.formatNow());

            loadScripts0(gson);

            long end = System.currentTimeMillis();
            SimpleScript.logForAll("Finished at: " + TimeUtil.formatNow() + ", took " + (end - start) + "ms");
        } catch (Exception e) {
            BCLog.logger.warn("[lib.script] Failed to reload scripts", e);
        }
    }

    private void loadScripts0(Gson gson) {

        SimpleScript.logForAll("#############");
        SimpleScript.logForAll("#");
        SimpleScript.logForAll("# Loading");
        SimpleScript.logForAll("#");
        SimpleScript.logForAll("#############");

        sourceDomains.clear();

        List<ScriptAction> actions = new ArrayList<>();

        List<FileSystem> openFileSystems = new ArrayList<>();
        Map<File, Path> loadedFiles = new HashMap<>();
        List<Path> jarRoots = new ArrayList<>();
        for (IModInfo modInfo : ModList.get().getMods()) {

            var modFile = ModList.get().getModFileById(modInfo.getModId());
            if (modFile == null) continue;
            File source = modFile.getFilePath().toFile();
            if (!source.exists()) {
                continue;
            }
            visitFile(openFileSystems, loadedFiles, jarRoots, source);

            if (source.isDirectory()) {
                String sourcePath = source.getAbsolutePath();
                if (sourcePath.endsWith("classes" + File.separator + "java" + File.separator + "main")) {
                    File resourcesDir = new File(source.getParentFile().getParentFile().getParentFile(),
                        "resources" + File.separator + "main");
                    if (resourcesDir.isDirectory()) {
                        visitFile(openFileSystems, loadedFiles, jarRoots, resourcesDir);
                    }
                }
            }
        }

        File baseFile = new File(GamePaths.CONFIGDIR.toFile(), "buildcraft/scripts");
        if (!baseFile.isDirectory()) {
            baseFile.mkdirs();
        }
        visitFile(openFileSystems, loadedFiles, null, baseFile);

        for (Entry<File, Path> entry : loadedFiles.entrySet()) {
            File file = entry.getKey();
            loadScripts(openFileSystems, actions, file, entry.getValue(), jarRoots, file == baseFile);
        }

        SimpleScript.logForAll("#############");
        SimpleScript.logForAll("#");
        SimpleScript.logForAll("# Executing");
        SimpleScript.logForAll("#");
        SimpleScript.logForAll("#############");
        SimpleScript.logForAll("");

        executeScripts(gson, actions);

        for (FileSystem system : openFileSystems) {
            IOUtils.closeQuietly(system);
        }
    }

    private void visitFile(List<FileSystem> openFileSystems, Map<File, Path> loadedFiles, List<Path> roots,
        File source) {
        if (loadedFiles.containsKey(source)) {
            return;
        }
        Path root = getRoot(openFileSystems, source);
        if (root != null) {
            loadedFiles.put(source, root);
            if (roots != null) {
                roots.add(root);
            }
        }
    }

    @Nullable
    private Path getRoot(List<FileSystem> openFileSystems, File file) {
        final PackType sourceType = manager.getType();
        Path scriptDirRoot = file.toPath();
        if (file.isDirectory()) {
            Path root = scriptDirRoot.resolve(sourceType.prefix);
            return Files.exists(root) ? root : null;
        }
        try {
            FileSystem fileSystem = FileSystems.newFileSystem(scriptDirRoot,  (ClassLoader) null);
            Path root = fileSystem.getPath("/" + sourceType.prefix);
            if (!Files.exists(root)) {
                return null;
            }
            openFileSystems.add(fileSystem);
            return root;
        } catch (IOException e) {
            BCLog.logger.error("Unable to load " + file + " as a separate file system!", e);
            return null;
        }
    }

    private void loadScripts(List<FileSystem> openFileSystems, List<ScriptAction> actions, File file, Path root,
        List<Path> jarRoots, boolean genInfo) {
        try {
            boolean loggedInsn = false;
            String postPath = "compat/" + this.entryPath;
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(root)) {
                Iterator<Path> iter = dirStream.iterator();
                while (iter.hasNext()) {
                    Path subFolder = iter.next();

                    String scriptDomain = subFolder.getFileName().toString().replace("/", "");
                    Path scriptDir = subFolder.resolve(postPath);
                    Path scriptFile = subFolder.resolve(postPath + ".txt");

                    if (!Files.exists(scriptFile)) {

                        continue;
                    }
                    if (!loggedInsn) {
                        loggedInsn = true;
                        SimpleScript.logForAll("");
                        SimpleScript.logForAll("# Found pack: " + file);
                        SimpleScript.logForAll("");
                    }
                    List<String> contents = Files.readAllLines(scriptFile);
                    if (contents.isEmpty()) {
                        SimpleScript.logForAll(root.relativize(scriptFile) + " was empty!");
                        continue;
                    }
                    if (!"~{buildcraft/json/insn}".equals(contents.set(0, "// Valid file declaration was here"))) {
                        SimpleScript.logForAll(
                            root.relativize(scriptFile) + " didn't start with '~{buildcraft/json/insn}', ignoring.");
                        continue;
                    }
                    SimpleScript script =
                        new SimpleScript(this, root, scriptDomain, scriptDir, scriptFile, jarRoots, contents);
                    actions.addAll(script.actions);
                    if (!script.actions.isEmpty()) {
                        sourceDomains.add(scriptDomain);
                    }
                }
            }
        } catch (IOException io) {
            BCLog.logger.warn("Unable to load from ...", io);
        }
    }

    private void executeScripts(Gson gson, List<ScriptAction> actions) {
        Multimap<Identifier, ScriptAction> added = HashMultimap.create();
        Multimap<Identifier, ScriptAction> removed = HashMultimap.create();

        for (ScriptAction action : actions) {
            if (action instanceof ScriptActionRemove) {
                removed.put(((ScriptActionRemove) action).name, action);
            } else if (action instanceof ScriptActionAdd) {
                ScriptActionAdd add = (ScriptActionAdd) action;
                added.put(add.name, add);
            } else if (action instanceof ScriptActionReplace) {
                ScriptActionReplace replace = (ScriptActionReplace) action;
                removed.put(replace.toReplace, replace);
                added.put(replace.name, replace);
            } else {
                throw new IllegalStateException("Unknown action " + action.getClass());
            }
        }

        for (Identifier name : added.keySet()) {
            Collection<ScriptAction> adders = added.get(name);
            if (adders.size() > 1) {
                SimpleScript.logForAll("Multiple scripts attempting to add " + name
                    + "! This is likely caused by either a single script containing duplicate 'add' entries "
                    + "with the same id, or multiple datapacks with the same namespace!");
                continue;
            }
            ScriptAction adder = adders.iterator().next();
            Collection<ScriptAction> removers = removed.get(name);
            removers.remove(adder);
            if (!removers.isEmpty()) {
                SimpleScript.logForAll("Skipping " + name + " as it is marked as removed.");
                continue;
            }
            JsonObject json = null;
            while (!(adder instanceof ScriptActionAdd)) {
                if (adder instanceof ScriptActionReplace) {
                    ScriptActionReplace replace = (ScriptActionReplace) adder;
                    if (replace.inheritTags) {

                        Identifier location = replace.toReplace;
                        adders = added.get(location);
                        if (adders.size() > 1) {

                            adder = null;
                            break;
                        }
                        adder = adders.iterator().next();
                        if (json == null) {
                            json = replace.json;
                        }
                        json = JsonUtil.inheritTags(adder.getJson(), json);
                    } else {

                        adder = replace.convertToAdder();
                        json = null;
                    }
                } else {
                    throw new IllegalStateException("Unknown action " + adder.getClass());
                }
            }
            if (adder == null) {

                continue;
            } else if (adder instanceof ScriptActionAdd) {
                ScriptActionAdd action = (ScriptActionAdd) adder;
                if (action.json == null) {
                    SimpleScript.logForAll("Skipping " + name + " as it couldn't find a JSON to load from.");
                    continue;
                } else if (json != null) {
                    json = JsonUtil.inheritTags(json, action.json);
                } else {
                    json = action.json;
                }
                try {
                    loadReloadable(name, gson, json);
                } catch (JsonSyntaxException jse) {
                    SimpleScript.logForAll("Unable to load " + name + " from " + json + " because " + jse.getMessage());
                }
            } else {
                throw new IllegalStateException("Unknown action " + adder.getClass());
            }
        }
    }

    private void loadReloadable(Identifier name, Gson gson, JsonObject json) throws JsonSyntaxException {
        String type = "";
        if (json.has("type")) {
            type = json.get("type").getAsString();
        }

        IEntryDeserializer<? extends E> deserializer = getCustomDeserializers().get(type);
        if (deserializer != null) {
            OptionallyDisabled<? extends E> optional = deserializer.deserialize(name, json, gson::fromJson);
            if (optional.isPresent()) {
                E instance = optional.get();
                SimpleScript.logForAll("Adding " + name + " as " + instance);
                getReloadableEntryMap().put(name, instance);
                return;
            }
            SimpleScript.logForAll("Skipping " + name + " because " + optional.getDisabledReason());
            return;
        }
        Class<? extends E> recipeClass = getScriptableTypes().get(type);
        if (recipeClass != null) {
            E recipe = gson.fromJson(json, recipeClass);
            SimpleScript.logForAll("Adding " + name + " as " + recipe);
            return;
        }
        SimpleScript.logForAll("Unable to add '" + name + "' as the type '" + type + "' is not defined!");
    }
}
