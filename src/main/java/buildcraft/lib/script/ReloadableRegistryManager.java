/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.script;

import buildcraft.api.registry.BuildCraftRegistryManager;
import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.api.registry.IReloadableRegistry;
import buildcraft.api.registry.IReloadableRegistryManager;
import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.misc.JsonUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum ReloadableRegistryManager implements IReloadableRegistryManager {
   DATA_PACKS(IReloadableRegistry.PackType.DATA_PACK),
   RESOURCE_PACKS(IReloadableRegistry.PackType.RESOURCE_PACK);

   private static boolean isLoadingAll;
   private final IReloadableRegistry.PackType sourceType;
   private final BiMap<String, IReloadableRegistry<?>> registries = HashBiMap.create();
   private boolean isReloading;
   private int reloadCount = 0;

   ReloadableRegistryManager(IReloadableRegistry.PackType sourceType) {
      this.sourceType = sourceType;
   }

   public static void loadAll() {
      try {
         isLoadingAll = true;
         DATA_PACKS.reloadAll();
         if (BuildCraftRegistryManager.managerResourcePacks != null) {
            RESOURCE_PACKS.reloadAll();
         }
      } finally {
         isLoadingAll = false;
      }
   }

   @Override
   public IReloadableRegistry.PackType getType() {
      return this.sourceType;
   }

   @Override
   public boolean isLoadingAll() {
      return isLoadingAll;
   }

   public void reloadAll() {
      this.reload(new HashSet<>(this.registries.values()));
   }

   @Override
   public void reload(IReloadableRegistry<?> registry) {
      this.reload(Collections.singleton(registry));
   }

   @Override
   public void reload(IReloadableRegistry<?>... all) {
      this.reload(new HashSet<>(Arrays.asList(all)));
   }

   @Override
   public void reload(Set<IReloadableRegistry<?>> set) {
      if (this.isInReload()) {
         throw new IllegalStateException("Cannot reload while we are reloading!");
      }

      try {
         this.isReloading = true;
         EventBuildCraftReload.fireBeforeClear(new EventBuildCraftReload.BeforeClear(this, set));
         set.forEach(registryx -> registryx.getReloadableEntryMap().clear());
         EventBuildCraftReload.firePreLoad(new EventBuildCraftReload.PreLoad(this, set));
         GsonBuilder builder = new GsonBuilder();
         JsonUtil.registerTypeAdaptors(builder);
         EventBuildCraftReload.firePopulateGson(new EventBuildCraftReload.PopulateGson(this, set, builder));
         Gson gson = builder.create();

         for (IReloadableRegistry<?> registry : set) {
            if (registry instanceof ScriptableRegistry) {
               ((ScriptableRegistry)registry).loadScripts(gson);
            }
         }

         EventBuildCraftReload.firePostLoad(new EventBuildCraftReload.PostLoad(this, set));
      } finally {
         this.reloadCount++;
         this.isReloading = false;
      }

      EventBuildCraftReload.fireFinishLoad(new EventBuildCraftReload.FinishLoad(this, set));
   }

   @Override
   public boolean isInReload() {
      return this.isReloading;
   }

   @Override
   public int getReloadCount() {
      return this.reloadCount;
   }

   @Override
   public Map<String, IReloadableRegistry<?>> getAllRegistries() {
      return this.registries;
   }

   @Override
   public <R> IReloadableRegistry<R> createRegistry(String name) {
      SimpleReloadableRegistry<R> registry = new SimpleReloadableRegistry<>(this);
      this.getAllRegistries().put(name, registry);
      return registry;
   }

   @Override
   public <R> IScriptableRegistry<R> createScriptableRegistry(String entryPath) {
      ScriptableRegistry<R> registry = new ScriptableRegistry<>(this, entryPath);
      this.registerRegistry(registry);
      return registry;
   }

   @Override
   public void registerRegistry(String entryType, IScriptableRegistry<?> registry) {
      if (entryType.indexOf(58) != -1) {
         throw new IllegalArgumentException("The entry type must be a valid resource path! (so it must not contain a colon)");
      }

      this.registries.put(entryType, registry);
   }
}
