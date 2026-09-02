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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import org.apache.commons.io.IOUtils;

public class ScriptableRegistry<E> extends SimpleReloadableRegistry<E> implements IScriptableRegistry<E> {
   private final String entryPath;
   private final Map<String, IScriptableRegistry.IEntryDeserializer<? extends E>> deserializers = new HashMap<>();

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
   public Map<String, IScriptableRegistry.IEntryDeserializer<? extends E>> getCustomDeserializers() {
      return this.deserializers;
   }

   void loadEntries(Gson gson) {
      List<FileSystem> openFileSystems = new ArrayList<>();
      Set<File> visited = new HashSet<>();
      List<Path> roots = new ArrayList<>();

      for (String modId : FabricModResources.getModIds()) {
         Path modRoot = FabricModResources.getModRootPath(modId);
         if (modRoot != null) {
            File source = modRoot.toFile();
            if (source.exists()) {
               this.visitFile(openFileSystems, visited, roots, source);
               if (source.isDirectory()) {
                  String sourcePath = source.getAbsolutePath();
                  if (sourcePath.endsWith("classes" + File.separator + "java" + File.separator + "main")) {
                     File resourcesDir = new File(source.getParentFile().getParentFile().getParentFile(), "resources" + File.separator + "main");
                     if (resourcesDir.isDirectory()) {
                        this.visitFile(openFileSystems, visited, roots, resourcesDir);
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

      this.visitFile(openFileSystems, visited, roots, baseFile);

      for (Path root : roots) {
         this.loadEntries(gson, root);
      }

      for (FileSystem system : openFileSystems) {
         IOUtils.closeQuietly(system);
      }
   }

   private void visitFile(List<FileSystem> openFileSystems, Set<File> visited, List<Path> roots, File source) {
      if (visited.add(source)) {
         Path root = this.getRoot(openFileSystems, source);
         if (root != null) {
            roots.add(root);
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

   private void loadEntries(Gson gson, Path root) {
      try (DirectoryStream<Path> domains = Files.newDirectoryStream(root)) {
         for (Path domainDir : domains) {
            Path entryDir = domainDir.resolve("compat/" + this.entryPath);
            if (!Files.isDirectory(entryDir)) {
               continue;
            }

            String domain = domainDir.getFileName().toString().replace("/", "");
            List<Path> files;
            try (Stream<Path> walk = Files.walk(entryDir)) {
               files = walk.filter(f -> f.getFileName().toString().endsWith(".json")).sorted().toList();
            }

            for (Path file : files) {
               String rel = entryDir.relativize(file).toString().replace('\\', '/');
               Identifier name = Identifier.fromNamespaceAndPath(domain, rel.substring(0, rel.length() - ".json".length()));
               this.loadEntry(name, gson, file);
            }
         }
      } catch (IOException io) {
         BCLog.logger.warn("[lib.script] Unable to load " + this.entryPath + " entries from " + root, io);
      }
   }

   private void loadEntry(Identifier name, Gson gson, Path file) {
      try (BufferedReader reader = Files.newBufferedReader(file)) {
         JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
         String type = json.has("type") ? json.get("type").getAsString() : "";
         IScriptableRegistry.IEntryDeserializer<? extends E> deserializer = this.deserializers.get(type);
         if (deserializer == null) {
            BCLog.logger.warn("[lib.script] Unable to add '" + name + "' as the type '" + type + "' is not defined!");
            return;
         }

         IScriptableRegistry.OptionallyDisabled<? extends E> optional = deserializer.deserialize(name, json, gson::fromJson);
         if (optional.isPresent()) {
            this.getReloadableEntryMap().put(name, optional.get());
         }
      } catch (IOException | JsonParseException | IllegalStateException e) {
         BCLog.logger.warn("[lib.script] Unable to load " + name + " from " + file, e);
      }
   }
}
