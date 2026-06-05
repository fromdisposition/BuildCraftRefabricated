/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.model.MutableQuad;

import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.silicon.client.model.plug.PlugBakerFacade;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadePhasedState;

public class FacadeItemModel implements ItemModel {

    private static final LoadingCache<KeyPlugFacade, List<BakedQuad>> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build(CacheLoader.from(key -> PlugBakerFacade.INSTANCE.bake(key)));

    private static final LoadingCache<KeyPlugFacade, List<BakedQuad>> guiCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build(CacheLoader.from(key -> {
            List<BakedQuad> quads = new ArrayList<>();
            float offsetZ = (16 - buildcraft.silicon.plug.PluggableFacade.SIZE) / 2f / 16f;
            for (MutableQuad quad : PlugBakerFacade.INSTANCE.bakeForKey(key)) {
                quad.setShade(false);
                quad.vertex_0.translatef(0, 0, offsetZ).normalf(0, 1, 0).colouri(255, 255, 255, 255);
                quad.vertex_1.translatef(0, 0, offsetZ).normalf(0, 1, 0).colouri(255, 255, 255, 255);
                quad.vertex_2.translatef(0, 0, offsetZ).normalf(0, 1, 0).colouri(255, 255, 255, 255);
                quad.vertex_3.translatef(0, 0, offsetZ).normalf(0, 1, 0).colouri(255, 255, 255, 255);
                quads.add(quad.toBakedItem());
            }
            return quads;
        }));

    public FacadeItemModel() {

    }

    public static void onModelBake() {
        cache.invalidateAll();
        guiCache.invalidateAll();
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver,
                       ItemDisplayContext displayContext, @Nullable ClientLevel level,
                       @Nullable ItemOwner owner, int seed) {
        FacadeInstance inst = ItemPluggableFacade.getStates(stack);
        FacadePhasedState state = inst.getCurrentStateForStack();

        List<BakedQuad> quads;
        KeyPlugFacade key;
        if (displayContext == ItemDisplayContext.GUI) {
            key = new KeyPlugFacade("cutout", Direction.NORTH, state.stateInfo.state, inst.isHollow());
            quads = guiCache.getUnchecked(key);
        } else {
            key = new KeyPlugFacade("cutout", Direction.EAST, state.stateInfo.state, inst.isHollow());
            quads = cache.getUnchecked(key);
        }

        if (quads.isEmpty()) {
            return;
        }

        renderState.appendModelIdentityElement(this);
        renderState.appendModelIdentityElement(key);

        var layer = renderState.newLayer();
        layer.prepareQuadList().addAll(quads);

    }
}
