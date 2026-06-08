/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.lib.client.model.IModelCache;
import buildcraft.lib.client.model.ModelCacheJoiner;
import buildcraft.transport.client.render.PipeBehaviourRendererStripes;
import buildcraft.transport.client.render.PipeWireRenderer;
import buildcraft.transport.tile.TilePipeHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PipeModelCacheAll {
   static final IModelCache<PipeModelCacheAll.PipeAllCutoutKey> cacheCutout;
   static final IModelCache<PipeModelCacheAll.PipeAllTranslucentKey> cacheTranslucent;

   public static void clearModels() {
      cacheCutout.clear();
      cacheTranslucent.clear();
   }

   public static void clearAll() {
      clearModels();
      PipeMutableQuadCache.clearCaches();
      PipeBaseModelGenStandard.clearSpriteCaches();
      PipeWireRenderer.clearCaches();
      PipeBehaviourRendererStripes.clearCaches();
   }

   static {
      List<ModelCacheJoiner.ModelKeyWrapper<PipeModelCacheAll.PipeAllCutoutKey, ?>> cutout = new ArrayList<>();
      cutout.add(new ModelCacheJoiner.ModelKeyWrapper<>(PipeModelCacheAll.PipeAllCutoutKey::getBaseCutout, PipeModelCacheBase.cacheCutout));
      cutout.add(new ModelCacheJoiner.ModelKeyWrapper<>(PipeModelCacheAll.PipeAllCutoutKey::getPluggable, PipeModelCachePluggable.cacheCutoutAll));
      cacheCutout = new ModelCacheJoiner<>(cutout);
      List<ModelCacheJoiner.ModelKeyWrapper<PipeModelCacheAll.PipeAllTranslucentKey, ?>> translucent = new ArrayList<>();
      translucent.add(new ModelCacheJoiner.ModelKeyWrapper<>(PipeModelCacheAll.PipeAllTranslucentKey::getBaseTranslucent, PipeModelCacheBase.cacheTranslucent));
      translucent.add(
         new ModelCacheJoiner.ModelKeyWrapper<>(PipeModelCacheAll.PipeAllTranslucentKey::getPluggable, PipeModelCachePluggable.cacheTranslucentAll)
      );
      cacheTranslucent = new ModelCacheJoiner<>(translucent);
   }

   public static class PipeAllCutoutKey {
      private final PipeModelCacheBase.PipeBaseCutoutKey cutout;
      private final PipeModelCachePluggable.PluggableKey pluggable;
      private final int hash;

      public PipeAllCutoutKey(TilePipeHolder tile) {
         this.cutout = new PipeModelCacheBase.PipeBaseCutoutKey(tile.getPipe().getModel());
         this.pluggable = new PipeModelCachePluggable.PluggableKey(true, tile);
         this.hash = Objects.hash(this.cutout, this.pluggable);
      }

      public PipeModelCacheBase.PipeBaseCutoutKey getBaseCutout() {
         return this.cutout;
      }

      public PipeModelCachePluggable.PluggableKey getPluggable() {
         return this.pluggable;
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

         PipeModelCacheAll.PipeAllCutoutKey other = (PipeModelCacheAll.PipeAllCutoutKey)obj;
         return !this.cutout.equals(other.cutout) ? false : this.pluggable.equals(other.pluggable);
      }
   }

   public static class PipeAllTranslucentKey {
      private final PipeModelCacheBase.PipeBaseTranslucentKey translucent;
      private final PipeModelCachePluggable.PluggableKey pluggable;
      private final int hash;

      public PipeAllTranslucentKey(TilePipeHolder tile) {
         this.translucent = new PipeModelCacheBase.PipeBaseTranslucentKey(tile.getPipe().getModel());
         this.pluggable = new PipeModelCachePluggable.PluggableKey(false, tile);
         this.hash = Objects.hash(this.translucent, this.pluggable);
      }

      public PipeModelCacheBase.PipeBaseTranslucentKey getBaseTranslucent() {
         return this.translucent;
      }

      public PipeModelCachePluggable.PluggableKey getPluggable() {
         return this.pluggable;
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

         PipeModelCacheAll.PipeAllTranslucentKey other = (PipeModelCacheAll.PipeAllTranslucentKey)obj;
         return !this.translucent.equals(other.translucent) ? false : this.pluggable.equals(other.pluggable);
      }
   }
}
