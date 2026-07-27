/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.script;

import buildcraft.api.core.BCLog;
import buildcraft.api.registry.IReloadableRegistry;
import buildcraft.api.registry.IReloadableRegistryManager;
import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.fabric.loader.FabricModResources;
import buildcraft.lib.fabric.loader.GamePaths;
import buildcraft.lib.misc.JsonUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import org.apache.commons.io.IOUtils;

public class ScriptableRegistry<E> extends SimpleReloadableRegistry<E> implements IScriptableRegistry<E> {
   private final String entryPath;
   private final Map<String, Class<? extends E>> types = new HashMap<>();
   private final Map<String, IScriptableRegistry.IEntryDeserializer<? extends E>> deserializers = new HashMap<>();
   private final Set<String> sourceDomains = new HashSet<>();

   public ScriptableRegistry(IReloadableRegistryManager manager, String entryPath) {
      super(manager);
      this.entryPath = entryPath;
   }

   public ScriptableRegistry(IReloadableRegistry.PackType type, String entryPath) {
      this(type == IReloadableRegistry.PackType.DATA_PACK ? ReloadableRegistryManager.DATA_PACKS : ReloadableRegistryManager.RESOURCE_PACKS, entryPath);
      this.manager.registerRegistry(this);
   }

   @Override
   public String getEntryType() {
      return this.entryPath;
   }

   @Override
   public Map<String, Class<? extends E>> getScriptableTypes() {
      return this.types;
   }

   @Override
   public Map<String, IScriptableRegistry.IEntryDeserializer<? extends E>> getCustomDeserializers() {
      return this.deserializers;
   }

   @Override
   public Set<String> getSourceDomains() {
      return Collections.unmodifiableSet(this.sourceDomains);
   }

   private static String formatNow() {
      return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
   }

   void loadScripts(Gson gson) {
      try (AutoCloseable logFile = SimpleScript.createLogFile(this.entryPath)) {
         long start = System.currentTimeMillis();
         SimpleScript.logForAll("Started at: " + formatNow());
         this.loadScripts0(gson);
         long end = System.currentTimeMillis();
         SimpleScript.logForAll("Finished at: " + formatNow() + ", took " + (end - start) + "ms");
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
      this.sourceDomains.clear();
      List<SimpleScript.ScriptAction> actions = new ArrayList<>();
      List<FileSystem> openFileSystems = new ArrayList<>();
      Map<File, Path> loadedFiles = new HashMap<>();
      List<Path> jarRoots = new ArrayList<>();

      for (String modId : FabricModResources.getModIds()) {
         Path modRoot = FabricModResources.getModRootPath(modId);
         if (modRoot != null) {
            File source = modRoot.toFile();
            if (source.exists()) {
               this.visitFile(openFileSystems, loadedFiles, jarRoots, source);
               if (source.isDirectory()) {
                  String sourcePath = source.getAbsolutePath();
                  if (sourcePath.endsWith("classes" + File.separator + "java" + File.separator + "main")) {
                     File resourcesDir = new File(source.getParentFile().getParentFile().getParentFile(), "resources" + File.separator + "main");
                     if (resourcesDir.isDirectory()) {
                        this.visitFile(openFileSystems, loadedFiles, jarRoots, resourcesDir);
                     }
                  }
               }
            }
         }
      }

      File baseFile = GamePaths.BUILDCRAFT_CONFIG_DIR.resolve("scripts").toFile();
      if (!baseFile.isDirectory()) {
         baseFile.mkdirs();
      }

      this.visitFile(openFileSystems, loadedFiles, null, baseFile);

      for (Entry<File, Path> entry : loadedFiles.entrySet()) {
         File file = entry.getKey();
         this.loadScripts(openFileSystems, actions, file, entry.getValue(), jarRoots, file == baseFile);
      }

      SimpleScript.logForAll("#############");
      SimpleScript.logForAll("#");
      SimpleScript.logForAll("# Executing");
      SimpleScript.logForAll("#");
      SimpleScript.logForAll("#############");
      SimpleScript.logForAll("");
      this.executeScripts(gson, actions);

      for (FileSystem system : openFileSystems) {
         IOUtils.closeQuietly(system);
      }
   }

   private void visitFile(List<FileSystem> openFileSystems, Map<File, Path> loadedFiles, List<Path> roots, File source) {
      if (!loadedFiles.containsKey(source)) {
         Path root = this.getRoot(openFileSystems, source);
         if (root != null) {
            loadedFiles.put(source, root);
            if (roots != null) {
               roots.add(root);
            }
         }
      }
   }

   @Nullable
   private Path getRoot(List<FileSystem> openFileSystems, File file) {
      IReloadableRegistry.PackType sourceType = this.manager.getType();
      Path scriptDirRoot = file.toPath();
      if (file.isDirectory()) {
         Path root = scriptDirRoot.resolve(sourceType.prefix);
         return Files.exists(root) ? root : null;
      }

      try {
         FileSystem fileSystem = FileSystems.newFileSystem(scriptDirRoot, (ClassLoader)null);
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

   private void loadScripts(
      List<FileSystem> openFileSystems, List<SimpleScript.ScriptAction> actions, File file, Path root, List<Path> jarRoots, boolean genInfo
   ) {
      try {
         boolean loggedInsn = false;
         String postPath = "compat/" + this.entryPath;

         try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(root)) {
            for (Path subFolder : dirStream) {
               String scriptDomain = subFolder.getFileName().toString().replace("/", "");
               Path scriptDir = subFolder.resolve(postPath);
               Path scriptFile = subFolder.resolve(postPath + ".txt");
               if (Files.exists(scriptFile)) {
                  if (!loggedInsn) {
                     loggedInsn = true;
                     SimpleScript.logForAll("");
                     SimpleScript.logForAll("# Found pack: " + file);
                     SimpleScript.logForAll("");
                  }

                  List<String> contents = Files.readAllLines(scriptFile);
                  if (contents.isEmpty()) {
                     SimpleScript.logForAll(root.relativize(scriptFile) + " was empty!");
                  } else if (!"~{buildcraft/json/insn}".equals(contents.set(0, "// Valid file declaration was here"))) {
                     SimpleScript.logForAll(root.relativize(scriptFile) + " didn't start with '~{buildcraft/json/insn}', ignoring.");
                  } else {
                     SimpleScript script = new SimpleScript(this, root, scriptDomain, scriptDir, scriptFile, jarRoots, contents);
                     actions.addAll(script.actions);
                     if (!script.actions.isEmpty()) {
                        this.sourceDomains.add(scriptDomain);
                     }
                  }
               }
            }
         }
      } catch (IOException io) {
         BCLog.logger.warn("Unable to load from ...", io);
      }
   }

   private void executeScripts(Gson gson, List<SimpleScript.ScriptAction> actions) {
      Multimap<Identifier, SimpleScript.ScriptAction> added = HashMultimap.create();
      Multimap<Identifier, SimpleScript.ScriptAction> removed = HashMultimap.create();

      for (SimpleScript.ScriptAction action : actions) {
         if (action instanceof SimpleScript.ScriptActionRemove) {
            removed.put(((SimpleScript.ScriptActionRemove)action).name, action);
         } else if (action instanceof SimpleScript.ScriptActionAdd add) {
            added.put(add.name, add);
         } else {
            if (!(action instanceof SimpleScript.ScriptActionReplace replace)) {
               throw new IllegalStateException("Unknown action " + action.getClass());
            }

            removed.put(replace.toReplace, replace);
            added.put(replace.name, replace);
         }
      }

      for (Identifier name : added.keySet()) {
         Collection<SimpleScript.ScriptAction> adders = added.get(name);
         if (adders.size() > 1) {
            SimpleScript.logForAll(
               "Multiple scripts attempting to add "
                  + name
                  + "! This is likely caused by either a single script containing duplicate 'add' entries with the same id, or multiple datapacks with the same namespace!"
            );
         } else {
            SimpleScript.ScriptAction adder = adders.iterator().next();
            Collection<SimpleScript.ScriptAction> removers = removed.get(name);
            removers.remove(adder);
            if (!removers.isEmpty()) {
               SimpleScript.logForAll("Skipping " + name + " as it is marked as removed.");
            } else {
               JsonObject json = null;

               while (true) {
                  if (!(adder instanceof SimpleScript.ScriptActionAdd)) {
                     if (!(adder instanceof SimpleScript.ScriptActionReplace replace)) {
                        throw new IllegalStateException("Unknown action " + adder.getClass());
                     }

                     if (!replace.inheritTags) {
                        adder = replace.convertToAdder();
                        json = null;
                        continue;
                     }

                     Identifier location = replace.toReplace;
                     adders = added.get(location);
                     if (adders.size() <= 1) {
                        adder = adders.iterator().next();
                        if (json == null) {
                           json = replace.json;
                        }

                        json = JsonUtil.inheritTags(adder.getJson(), json);
                        continue;
                     }

                     adder = null;
                  }

                  if (adder != null) {
                     if (!(adder instanceof SimpleScript.ScriptActionAdd action)) {
                        throw new IllegalStateException("Unknown action " + adder.getClass());
                     }

                     if (action.json == null) {
                        SimpleScript.logForAll("Skipping " + name + " as it couldn't find a JSON to load from.");
                     } else {
                        if (json != null) {
                           json = JsonUtil.inheritTags(json, action.json);
                        } else {
                           json = action.json;
                        }

                        try {
                           this.loadReloadable(name, gson, json);
                        } catch (JsonSyntaxException jse) {
                           SimpleScript.logForAll("Unable to load " + name + " from " + json + " because " + jse.getMessage());
                        }
                     }
                  }
                  break;
               }
            }
         }
      }
   }

   private void loadReloadable(Identifier name, Gson gson, JsonObject json) throws JsonSyntaxException {
      String type = "";
      if (json.has("type")) {
         type = json.get("type").getAsString();
      }

      IScriptableRegistry.IEntryDeserializer<? extends E> deserializer = this.getCustomDeserializers().get(type);
      if (deserializer != null) {
         IScriptableRegistry.OptionallyDisabled<? extends E> optional = deserializer.deserialize(name, json, gson::fromJson);
         if (optional.isPresent()) {
            E instance = (E)optional.get();
            SimpleScript.logForAll("Adding " + name + " as " + instance);
            this.getReloadableEntryMap().put(name, instance);
         } else {
            SimpleScript.logForAll("Skipping " + name + " because " + optional.getDisabledReason());
         }
      } else {
         Class<? extends E> recipeClass = this.getScriptableTypes().get(type);
         if (recipeClass != null) {
            E recipe = (E)gson.fromJson(json, recipeClass);
            SimpleScript.logForAll("Adding " + name + " as " + recipe);
         } else {
            SimpleScript.logForAll("Unable to add '" + name + "' as the type '" + type + "' is not defined!");
         }
      }
   }
}
