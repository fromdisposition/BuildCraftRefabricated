/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;

import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.plug.PluggableGate;

@SuppressWarnings("deprecation")
public enum PlugGateRenderer implements IPlugDynamicRenderer<PluggableGate> {
    INSTANCE;

    private static List<MutableQuad> onBox;
    private static List<MutableQuad> offBox;

    private static final Map<GateVariant, List<MutableQuad>> staticByVariant = new ConcurrentHashMap<>();

    private static void initDynamicCache() {
        if (onBox != null) return;
        onBox = new ArrayList<>();
        offBox = new ArrayList<>();
        TextureAtlasSprite onSprite = getMcSprite("buildcraftsilicon:block/gates/gate_on");
        TextureAtlasSprite offSprite = getMcSprite("minecraft:block/black_concrete");
        addDynamicBox(onBox, onSprite);
        addDynamicBox(offBox, offSprite);
    }

    private static List<MutableQuad> staticQuadsFor(GateVariant variant) {
        List<MutableQuad> cached = staticByVariant.get(variant);
        if (cached != null) return cached;
        List<MutableQuad> list = buildStaticQuads(variant);
        staticByVariant.put(variant, list);
        return list;
    }

    private static List<MutableQuad> buildStaticQuads(GateVariant variant) {
        List<MutableQuad> list = new ArrayList<>();

        TextureAtlasSprite matSprite = getMcSprite(materialSpritePath(variant.material));
        addBox(list, 2f / 16f, 5f / 16f, 5f / 16f, 4.01f / 16f, 11f / 16f, 11f / 16f, matSprite, true);

        if (variant.material != EnumGateMaterial.CLAY_BRICK) {
            TextureAtlasSprite logicSprite = getMcSprite(
                "buildcraftsilicon:block/gates/gate_" + variant.logic.tag);
            addBox(list, 1.8f / 16f, 7f / 16f, 7f / 16f, 4.2f / 16f, 9f / 16f, 9f / 16f, logicSprite, true);
        }

        if (variant.modifier != EnumGateModifier.NO_MODIFIER) {
            TextureAtlasSprite modSprite = getMcSprite(modifierSpritePath(variant.modifier));
            addBox(list, 1.8f / 16f, 5.5f / 16f, 5.5f / 16f, 4.2f / 16f, 6.5f / 16f, 6.5f / 16f, modSprite, true);
            addBox(list, 1.8f / 16f, 9.5f / 16f, 5.5f / 16f, 4.2f / 16f, 10.5f / 16f, 6.5f / 16f, modSprite, true);
            addBox(list, 1.8f / 16f, 5.5f / 16f, 9.5f / 16f, 4.2f / 16f, 6.5f / 16f, 10.5f / 16f, modSprite, true);
            addBox(list, 1.8f / 16f, 9.5f / 16f, 9.5f / 16f, 4.2f / 16f, 10.5f / 16f, 10.5f / 16f, modSprite, true);
        }
        return list;
    }

    private static String materialSpritePath(EnumGateMaterial material) {
        return switch (material) {
            case CLAY_BRICK -> "minecraft:block/bricks";
            case IRON -> "minecraft:block/iron_block";
            case NETHER_BRICK -> "minecraft:block/nether_bricks";
            case GOLD -> "minecraft:block/gold_block";
        };
    }

    private static String modifierSpritePath(EnumGateModifier mod) {
        return switch (mod) {
            case LAPIS -> "minecraft:block/lapis_block";
            case QUARTZ -> "minecraft:block/quartz_block_top";
            case DIAMOND -> "minecraft:block/diamond_block";
            default -> "minecraft:block/stone";
        };
    }

    private static TextureAtlasSprite getMcSprite(String path) {
        net.minecraft.client.renderer.texture.TextureAtlas atlas =
            (net.minecraft.client.renderer.texture.TextureAtlas) Minecraft.getInstance()
                .getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(Identifier.parse(path));
        return sprite != null ? sprite : SpriteUtil.missingSprite();
    }

    private static void addBox(List<MutableQuad> list, float minX, float minY, float minZ,
                               float maxX, float maxY, float maxZ, TextureAtlasSprite sprite, boolean shade) {
        Vector3f center = new Vector3f((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f);
        Vector3f radius = new Vector3f((maxX - minX) / 2f, (maxY - minY) / 2f, (maxZ - minZ) / 2f);
        for (Direction face : Direction.values()) {
            ModelUtil.UvFaceData uv = makeGateUVs(face);
            MutableQuad q = ModelUtil.createFace(face, center, radius, uv);
            q.texFromSprite(sprite);
            q.setTint(-1);
            q.setShade(shade);
            list.add(q);
        }
    }

    private static void addDynamicBox(List<MutableQuad> list, TextureAtlasSprite sprite) {
        addBox(list, 1.9f / 16f, 6f / 16f, 6f / 16f, 4.1f / 16f, 10f / 16f, 10f / 16f, sprite, false);
    }

    private static ModelUtil.UvFaceData makeGateUVs(Direction face) {
        ModelUtil.UvFaceData uv = new ModelUtil.UvFaceData();
        if (face == Direction.WEST || face == Direction.EAST) {
            uv.minU = 5f / 16f;
            uv.maxU = 11f / 16f;
            uv.minV = 5f / 16f;
            uv.maxV = 11f / 16f;
        } else {
            uv.minU = 2f / 16f;
            uv.maxU = 4f / 16f;
            uv.minV = 5f / 16f;
            uv.maxV = 11f / 16f;
        }
        return uv;
    }

    public static void onModelBake() {
        onBox = null;
        offBox = null;
        staticByVariant.clear();
    }

    @Override
    public void render(PluggableGate plug, double x, double y, double z, float partialTicks,
                       VertexConsumer bb, PoseStack ps) {
        initDynamicCache();

        int naturalBlockLight = 0;
        int naturalSkyLight = 0;
        boolean on = plug.logic.isOn;
        if (plug.holder != null && plug.holder.getPipeWorld() != null) {
            Level world = plug.holder.getPipeWorld();
            BlockPos sample = plug.holder.getPipePos().relative(plug.side);
            naturalBlockLight = world.getBrightness(LightLayer.BLOCK, sample);
            naturalSkyLight = world.getBrightness(LightLayer.SKY, sample);
        }

        ps.pushPose();
        ps.translate(x, y, z);

        ps.translate(0.5f, 0.5f, 0.5f);
        switch (plug.side) {
            case EAST -> ps.mulPose(Axis.YP.rotationDegrees(180));
            case NORTH -> ps.mulPose(Axis.YP.rotationDegrees(-90));
            case SOUTH -> ps.mulPose(Axis.YP.rotationDegrees(90));
            case DOWN -> ps.mulPose(Axis.ZP.rotationDegrees(90));
            case UP -> ps.mulPose(Axis.ZP.rotationDegrees(-90));
            case WEST -> {}
        }
        ps.translate(-0.5f, -0.5f, -0.5f);

        for (MutableQuad q : staticQuadsFor(plug.logic.variant)) {
            MutableQuad mq = new MutableQuad(q);
            mq.lighti(naturalBlockLight, naturalSkyLight);
            mq.render(ps.last(), bb);
        }

        for (MutableQuad q : (on ? onBox : offBox)) {
            MutableQuad mq = new MutableQuad(q);
            if (on) {
                mq.lighti(15, 15);
            } else {
                mq.lighti(naturalBlockLight, naturalSkyLight);
            }
            mq.render(ps.last(), bb);
        }

        ps.popPose();
    }
}
