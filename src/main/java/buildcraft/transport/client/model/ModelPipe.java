/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import buildcraft.lib.client.model.MutableQuad;

import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.tile.TilePipeHolder;

public class ModelPipe {

    public static List<BakedQuad> getCutoutQuads(TilePipeHolder tile) {
        if (tile == null || tile.getPipe() == null) {
            return ImmutableList.of();
        }
        return PipeModelCacheAll.getCutoutModel(tile);
    }

    public static List<BakedQuad> getTranslucentQuads(TilePipeHolder tile) {
        if (tile == null || tile.getPipe() == null) {
            return ImmutableList.of();
        }
        return PipeModelCacheAll.getTranslucentModel(tile);
    }

    public static void renderDirect(TilePipeHolder tile, PoseStack.Pose pose, VertexConsumer buffer, int light) {
        if (tile == null || tile.getPipe() == null) return;
        PipeModelKey key = tile.getPipe().getModel();
        if (key == null) return;
        PipeBaseCutoutKey cutoutKey = new PipeBaseCutoutKey(key);
        List<MutableQuad> quads = PipeModelCacheBase.generator.generateCutoutMutable(cutoutKey);
        for (MutableQuad q : quads) {
            q.setCalculatedDiffuse();
            q.lighti(light);
            q.render(pose, buffer);
        }
    }

    public static void renderCutoutPluggables(TilePipeHolder tile, PoseStack.Pose pose, VertexConsumer buffer, int light) {
        if (tile == null || tile.getPipe() == null) return;
        PipeModelCachePluggable.PluggableKey key = new PipeModelCachePluggable.PluggableKey(true, tile);
        List<BakedQuad> quads = PipeModelCachePluggable.cacheCutoutAll.bake(key);
        for (BakedQuad baked : quads) {
            MutableQuad q = new MutableQuad().fromBakedBlock(baked);
            q.setCalculatedDiffuse();
            q.lighti(light);
            q.render(pose, buffer);
        }
    }

    public static void renderMaskOverlay(TilePipeHolder tile, PoseStack.Pose pose,
            VertexConsumer buffer, int light, int alpha) {
        if (tile == null || tile.getPipe() == null) return;
        renderMaskOverlay(tile.getPipe().getModel(), pose, buffer, light, alpha);
    }

    public static void renderDirect(PipeModelKey modelKey, PoseStack.Pose pose, VertexConsumer buffer, int light) {
        if (modelKey == null) return;
        PipeBaseCutoutKey key = new PipeBaseCutoutKey(modelKey);
        List<MutableQuad> quads = PipeModelCacheBase.generator.generateCutoutMutable(key);
        for (MutableQuad q : quads) {
            q.lighti(light);
            q.render(pose, buffer);
        }
    }

    public static void renderMaskOverlay(PipeModelKey modelKey, PoseStack.Pose pose,
            VertexConsumer buffer, int light, int alpha) {
        if (modelKey == null) return;
        PipeBaseCutoutKey key = new PipeBaseCutoutKey(modelKey);
        List<MutableQuad> quads = PipeBaseModelGenStandard.INSTANCE.generateMaskMutable(key, alpha);
        for (MutableQuad q : quads) {
            q.lighti(light);
            q.render(pose, buffer);
        }
    }
}
