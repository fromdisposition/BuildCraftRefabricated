/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;

@SuppressWarnings("unchecked")
public class ModelCacheJoiner<K> implements IModelCache<K> {
   private final IModelCache<K> mainCache;
   private final ImmutableList<ModelCacheJoiner.ModelKeyWrapper<K, ?>> modelKeyWrappers;

   public ModelCacheJoiner(List<ModelCacheJoiner.ModelKeyWrapper<K, ?>> wrappers) {
      this.modelKeyWrappers = ImmutableList.copyOf(wrappers);
      this.mainCache = new ModelCache<>(this::load);
   }

   private List<BakedQuad> load(K key) {
      List<BakedQuad> quads = new ArrayList<>();
      UnmodifiableIterator var3 = this.modelKeyWrappers.iterator();

      while (var3.hasNext()) {
         ModelCacheJoiner.ModelKeyWrapper<K, ?> wrapper = (ModelCacheJoiner.ModelKeyWrapper<K, ?>)var3.next();
         quads.addAll(wrapper.getQuads(key));
      }

      return quads;
   }

   @Override
   public List<BakedQuad> bake(K key) {
      return ModelCache.cacheJoined ? this.mainCache.bake(key) : this.load(key);
   }

   @Override
   public void clear() {
      this.mainCache.clear();
      UnmodifiableIterator var1 = this.modelKeyWrappers.iterator();

      while (var1.hasNext()) {
         ModelCacheJoiner.ModelKeyWrapper<K, ?> wrapper = (ModelCacheJoiner.ModelKeyWrapper<K, ?>)var1.next();
         wrapper.cache.clear();
      }
   }

   public interface IModelKeyMapper<F, T> {
      T getInternKey(F var1);
   }

   public static class ModelKeyWrapper<K, T> {
      private final ModelCacheJoiner.IModelKeyMapper<K, T> mapper;
      private final IModelCache<T> cache;

      public ModelKeyWrapper(ModelCacheJoiner.IModelKeyMapper<K, T> mapper, ModelCache.IModelGenerator<T> generator) {
         this.mapper = mapper;
         this.cache = new ModelCache<>(generator);
      }

      public ModelKeyWrapper(ModelCacheJoiner.IModelKeyMapper<K, T> mapper, IModelCache<T> cache) {
         this.mapper = mapper;
         this.cache = cache;
      }

      public List<BakedQuad> getQuads(K key) {
         return this.cache.bake(this.mapper.getInternKey(key));
      }
   }
}
