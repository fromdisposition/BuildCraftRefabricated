/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import buildcraft.api.core.BCLog;
import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.entry.PageEntryExternal;
import buildcraft.lib.client.guide.entry.PageEntryFluidStack;
import buildcraft.lib.client.guide.entry.PageEntryItemStack;
import buildcraft.lib.client.guide.entry.PageEntryStatement;
import buildcraft.lib.client.guide.entry.PageValueType;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

/**
 * Registry of the guide-book pages that exist. Which pages there are — plus their kind, contents-tab tags and
 * sort order — is declared purely as data in {@code assets/buildcraft/compat/buildcraft/guide_entries.json} and
 * loaded straight through GSON (no bespoke script parser). Page BODIES remain per-language markdown loaded by
 * {@link GuideManager}, and page TITLES resolve through lang keys, so the whole book stays translatable: a
 * resource pack only needs to add {@code guide/<lang>/**.md} and the matching {@code lang/<lang>.json} keys.
 */
public final class GuidePageRegistry {
   public static final GuidePageRegistry INSTANCE = new GuidePageRegistry();

   private static final Identifier ENTRY_INDEX = Identifier.fromNamespaceAndPath("buildcraft", "compat/buildcraft/guide_entries.json");
   private static final Gson GSON = new Gson();

   /** Entry type ({@code "item_stack"}, {@code "external"}, {@code "statement"}, …) -> its deserializer. */
   public final Map<String, PageValueType<?>> types = new HashMap<>();
   /** Registered entries, keyed by their {@link Identifier} (e.g. {@code buildcraft:block/quarry}). */
   private final Map<Object, PageEntry<?>> entries = new HashMap<>();

   private GuidePageRegistry() {
      this.types.put("item_stack", PageEntryItemStack.INSTANCE);
      this.types.put("fluid_stack", PageEntryFluidStack.INSTANCE);
      this.types.put("external", PageEntryExternal.INSTANCE);
      this.types.put("statement", PageEntryStatement.INSTANCE);
   }

   public Map<Object, PageEntry<?>> getReloadableEntryMap() {
      return this.entries;
   }

   public Iterable<PageEntry<?>> getAllEntries() {
      return this.entries.values();
   }

   /** Re-reads the entry index from the active resource packs. */
   public void reload(ResourceManager resources) {
      this.entries.clear();

      Optional<Resource> resource = resources.getResource(ENTRY_INDEX);
      if (resource.isEmpty()) {
         BCLog.logger.warn("[lib.guide] Guide entry index " + ENTRY_INDEX + " is missing — the guide book will be empty.");
         return;
      }

      try (InputStream stream = resource.get().open(); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
         JsonObject root = GSON.fromJson(reader, JsonObject.class);
         String book = GsonHelper.getAsString(root, "book");
         JsonObject index = GsonHelper.getAsJsonObject(root, "entries");
         for (Map.Entry<String, JsonElement> node : index.entrySet()) {
            this.loadEntry(node.getKey(), node.getValue(), book);
         }
         BCLog.logger.info("[lib.guide] Registered " + this.entries.size() + " guide entries.");
      } catch (Exception ex) {
         BCLog.logger.error("[lib.guide] Failed to read guide entry index " + ENTRY_INDEX + ".", ex);
      }
   }

   private void loadEntry(String path, JsonElement element, String book) {
      Identifier id = Identifier.fromNamespaceAndPath("buildcraft", path);
      try {
         JsonObject json = GsonHelper.convertToJsonObject(element, path);
         if (!json.has("book")) {
            json.addProperty("book", book);
         }
         String typeName = GsonHelper.getAsString(json, "type");
         PageValueType<?> type = this.types.get(typeName);
         if (type == null) {
            BCLog.logger.warn("[lib.guide] Skipping guide entry " + id + ": unknown type '" + typeName + "'.");
            return;
         }
         // One bad entry never takes down the rest of the book.
         IScriptableRegistry.OptionallyDisabled<? extends PageEntry<?>> result = type.deserialize(id, json, GSON::fromJson);
         if (result.isPresent()) {
            this.entries.put(id, result.get());
         }
      } catch (RuntimeException ex) {
         BCLog.logger.warn("[lib.guide] Skipping malformed guide entry " + id + ".", ex);
      }
   }
}
