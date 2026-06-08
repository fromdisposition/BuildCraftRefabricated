/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import buildcraft.api.core.BCLog;
import buildcraft.lib.expression.info.ContextInfo;
import buildcraft.lib.expression.info.VariableInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

public class AdvModelCache {
   private static final int MODEL_INDEX_INCORRECT = -1;
   private static final int MODEL_INDEX_NO_CACHE = -2;
   public final ModelHolderVariable model;
   public final ContextInfo modelCtxInfo;
   final List<VariableInfo<?>> variables = new ArrayList<>();
   @Nullable
   private AdvModelCache.CacheBase cache = null;

   public AdvModelCache(ModelHolderVariable model, ContextInfo modelCtxInfo) {
      this.model = model;
      this.modelCtxInfo = modelCtxInfo;
   }

   public void clear() {
      AdvModelCache.CacheBase base = this.cache;
      if (base != null) {
         base.clear();
      }
   }

   public void reset() {
      this.clear();
      this.variables.clear();
      this.cache = null;
   }

   public MutableQuad[] getCutoutQuads() {
      return this.getCurrentValue().cutout;
   }

   public MutableQuad[] getTranslucentQuads() {
      return this.getCurrentValue().translucent;
   }

   AdvModelCache.CacheValue computeFullModel() {
      return new AdvModelCache.CacheValue(this.model.getCutoutQuads(), this.model.getTranslucentQuads());
   }

   AdvModelCache.CacheValue getCurrentValue() {
      AdvModelCache.CacheBase c = this.cache;
      if (c == null) {
         c = this.cache = this.createNewCache();
      }

      return c.getCurrentValue();
   }

   AdvModelCache.CacheBase createNewCache() {
      this.variables.clear();
      this.variables.addAll(this.modelCtxInfo.variables.values());
      int[] multipliers = new int[this.variables.size()];
      List<VariableInfo<?>> missKeys = new ArrayList<>();
      int m = 1;

      for (int i = 0; i < this.variables.size(); i++) {
         multipliers[i] = m;
         VariableInfo<?> info = this.variables.get(i);
         m *= info.getPossibleValues().size();
         if (!info.setIsComplete) {
            missKeys.add(info);
         }
      }

      AdvModelCache.CacheIndexed indexedCache = new AdvModelCache.CacheIndexed(multipliers, m);
      if (!missKeys.isEmpty()) {
         BCLog.logger.warn("[lib.model.adv_cache] Creating an indexed cache despite knowing that there will be cache misses!");

         for (VariableInfo<?> info : missKeys) {
            BCLog.logger.warn("[lib.model.adv_cache]  - " + info.node + " (" + info.cacheType + ", " + info.getPossibleValues() + ")");
         }
      }

      return indexedCache;
   }

   abstract class CacheBase {
      abstract AdvModelCache.CacheValue getCurrentValue();

      abstract void clear();
   }

   class CacheIndexed extends AdvModelCache.CacheBase {
      final int[] multipliers;
      final AdvModelCache.CacheValue[] values;

      private CacheIndexed(int[] multipliers, int possible) {
         this.multipliers = multipliers;
         this.values = new AdvModelCache.CacheValue[possible];
      }

      @Override
      AdvModelCache.CacheValue getCurrentValue() {
         int index = this.computeIndex();
         if (index >= 0 && index < this.values.length) {
            AdvModelCache.CacheValue val = this.values[index];
            if (val == null) {
               val = AdvModelCache.this.computeFullModel();
               this.values[index] = val;
            }

            return val;
         } else {
            if (index == -1) {
               BCLog.logger
                  .warn(
                     "[lib.model.adv_cache] Cache miss for indexed cache - this should be impossible! (index = "
                        + index
                        + ", length = "
                        + this.values.length
                        + ")"
                  );

               for (VariableInfo<?> var : AdvModelCache.this.variables) {
                  BCLog.logger.warn("            - " + var);
               }
            }

            return AdvModelCache.this.computeFullModel();
         }
      }

      private int computeIndex() {
         int index = 0;

         for (int i = 0; i < AdvModelCache.this.variables.size(); i++) {
            VariableInfo<?> info = AdvModelCache.this.variables.get(i);
            if (!info.shouldCacheCurrentValue()) {
               return -2;
            }

            int ord = info.getCurrentOrdinal();
            if (ord < 0) {
               return -1;
            }

            index += ord * this.multipliers[i];
         }

         return index;
      }

      @Override
      void clear() {
         Arrays.fill(this.values, null);
      }
   }

   static class CacheValue {
      final MutableQuad[] cutout;
      final MutableQuad[] translucent;

      CacheValue(MutableQuad[] cutout, MutableQuad[] translucent) {
         this.cutout = cutout;
         this.translucent = translucent;
      }
   }
}
