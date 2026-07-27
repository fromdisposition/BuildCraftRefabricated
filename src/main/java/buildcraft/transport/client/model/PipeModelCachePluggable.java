/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.client.model.IModelCache;
import buildcraft.lib.client.model.ModelCache;
import buildcraft.lib.client.model.ModelCacheMultipleSame;
import buildcraft.transport.client.PipeRegistryClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

public class PipeModelCachePluggable {
   public static final ModelCache<PluggableModelKey> cacheCutoutSingle = new ModelCache<>(PipeModelCachePluggable::generate);
   public static final ModelCache<PluggableModelKey> cacheTranslucentSingle = new ModelCache<>(PipeModelCachePluggable::generate);
   public static final IModelCache<PipeModelCachePluggable.PluggableKey> cacheCutoutAll = new ModelCacheMultipleSame<>(
      PipeModelCachePluggable.PluggableKey::getKeys, PipeModelCachePluggable.cacheCutoutSingle
   );

   @SuppressWarnings("unchecked")
   private static <K extends PluggableModelKey> List<BakedQuad> generate(K key) {
      if (key == null) {
         return ImmutableList.of();
      }

      IPluggableStaticBaker<K> baker = PipeRegistryClient.getPlugBaker(key);
      return (List<BakedQuad>)(baker == null ? ImmutableList.of() : baker.bake(key));
   }

   public static class PluggableKey {
      private final ImmutableSet<PluggableModelKey> pluggables;
      private final int hash;

      public PluggableKey(boolean isCutout, IPipeHolder holder) {
         Builder<PluggableModelKey> builder = ImmutableSet.builder();

         for (Direction side : Direction.values()) {
            PipePluggable pluggable = holder.getPluggable(side);
            if (pluggable != null) {
               PluggableModelKey key = pluggable.getModelRenderKey(isCutout ? "cutout" : "translucent");
               // World-tinted pluggables (facades of biome-coloured blocks) render individually so each quad's
               // tint resolves against the world; keeping them in the merged untinted batch drew them grey.
               if (key != null && !(isCutout && key.hasWorldTint())) {
                  builder.add(key);
               }
            }
         }

         this.pluggables = builder.build();
         this.hash = this.pluggables.hashCode();
      }

      public ImmutableSet<PluggableModelKey> getKeys() {
         return this.pluggables;
      }

      @Override
      public int hashCode() {
         return this.hash;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (obj == null) {
            return false;
         }

         if (this.getClass() != obj.getClass()) {
            return false;
         }

         PipeModelCachePluggable.PluggableKey other = (PipeModelCachePluggable.PluggableKey)obj;
         return this.pluggables.equals(other.pluggables);
      }
   }
}
