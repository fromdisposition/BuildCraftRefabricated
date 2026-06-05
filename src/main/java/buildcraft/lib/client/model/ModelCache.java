/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.client.resources.model.geometry.BakedQuad;

public class ModelCache<K> implements IModelCache<K> {
    public static boolean cacheJoined = true;

    private final LoadingCache<K, List<BakedQuad>> modelCache;

    public ModelCache(IModelGenerator<K> generator) {
        modelCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build(CacheLoader.from(generator::generate));
    }

    @Override
    public List<BakedQuad> bake(K key) {
        return modelCache.getUnchecked(key);
    }

    @Override
    public void clear() {
        modelCache.invalidateAll();
    }

    public interface IModelGenerator<T> {
        List<BakedQuad> generate(T key);
    }
}
