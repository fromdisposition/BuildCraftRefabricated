/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import java.util.EnumMap;
import java.util.Map;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.transport.EnumWirePart;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.VecUtil;

import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;
import buildcraft.transport.wire.WireManager;

public class PipeWireRenderer {

    private static final Map<EnumWirePart, MutableQuad[]> partQuads = new EnumMap<>(EnumWirePart.class);
    private static final Map<EnumWireBetween, MutableQuad[]> betweenQuads = new EnumMap<>(EnumWireBetween.class);
    private static final Map<DyeColor, SpriteHolder> wireSprites = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            wireSprites.put(color, SpriteHolderRegistry.getHolder("buildcrafttransport:wires/" + color.getName()));
        }

        for (EnumWirePart part : EnumWirePart.VALUES) {
            partQuads.put(part, buildPartQuads(part));
        }
        for (EnumWireBetween part : EnumWireBetween.VALUES) {
            betweenQuads.put(part, buildBetweenQuads(part));
        }
    }

    private static int func(AxisDirection dir) {
        return dir == AxisDirection.POSITIVE ? 1 : 0;
    }

    private static MutableQuad[] buildPartQuads(EnumWirePart part) {
        MutableQuad[] quads = new MutableQuad[6];

        Vector3f center = new Vector3f(
            0.5f + (part.x.getStep() * 4.51f / 16f),
            0.5f + (part.y.getStep() * 4.51f / 16f),
            0.5f + (part.z.getStep() * 4.51f / 16f)
        );
        Vector3f radius = new Vector3f(1 / 32f, 1 / 32f, 1 / 32f);
        UvFaceData uvs = new UvFaceData();
        int off = func(part.x) * 4 + func(part.y) * 2 + func(part.z);
        uvs.minU = off / 16f;
        uvs.maxU = (off + 1) / 16f;
        uvs.minV = 0;
        uvs.maxV = 1 / 16f;
        for (Direction face : Direction.values()) {
            quads[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
        }
        return quads;
    }

    private static MutableQuad[] buildBetweenQuads(EnumWireBetween between) {

        MutableQuad[] quads = new MutableQuad[4];
        int i = 0;

        Vec3 center;
        Vec3 radius;

        boolean ax = between.mainAxis == Axis.X;
        boolean ay = between.mainAxis == Axis.Y;
        boolean az = between.mainAxis == Axis.Z;

        if (between.to == null) {
            double cL = 0.5f - 4.51f / 16f;
            double cU = 0.5f + 4.51f / 16f;
            center = new Vec3(
                ax ? 0.5f : (between.xy ? cU : cL),
                ay ? 0.5f : ((ax ? between.xy : between.yz) ? cU : cL),
                az ? 0.5f : (between.yz ? cU : cL)
            );
            double rC = 4.01f / 16f;
            double rN = 1f / 16f / 2;
            radius = new Vec3(
                ax ? rC : rN,
                ay ? rC : rN,
                az ? rC : rN
            );
        } else {
            double cL = (8 - 4.51) / 16;
            double cU = (8 + 4.51) / 16;
            radius = new Vec3(
                ax ? 2.99 / 32 : 1 / 32.0,
                ay ? 2.99 / 32 : 1 / 32.0,
                az ? 2.99 / 32 : 1 / 32.0
            );
            center = new Vec3(
                ax ? (0.5 + 6.505 / 16 * between.to.getStepX()) : (between.xy ? cU : cL),
                ay ? (0.5 + 6.505 / 16 * between.to.getStepY()) : ((ax ? between.xy : between.yz) ? cU : cL),
                az ? (0.5 + 6.505 / 16 * between.to.getStepZ()) : (between.yz ? cU : cL)
            );
        }

        UvFaceData uvBase = new UvFaceData();
        uvBase.minU = (float) VecUtil.getValue(center.subtract(radius), between.mainAxis);
        uvBase.maxU = (float) VecUtil.getValue(center.add(radius), between.mainAxis);
        uvBase.minV = 0;
        uvBase.maxV = 1 / 16f;

        Vector3f centerFloat = new Vector3f((float) center.x, (float) center.y, (float) center.z);
        Vector3f radiusFloat = new Vector3f((float) radius.x, (float) radius.y, (float) radius.z);

        for (Direction face : Direction.values()) {
            if (face.getAxis() == between.mainAxis) {
                continue;
            }
            UvFaceData uvs = new UvFaceData(uvBase);

            Axis aAxis = between.mainAxis;
            Axis fAxis = face.getAxis();
            boolean fPositive = face.getAxisDirection() == AxisDirection.POSITIVE;

            int rotations = 0;
            boolean swapU = false;
            boolean swapV = false;

            if (aAxis == Axis.X) {
                swapV = fPositive;
            } else if (aAxis == Axis.Y) {
                rotations = 1;
                swapU = (fAxis == Axis.X) != fPositive;
                swapV = fAxis == Axis.Z;
            } else {
                if (fAxis == Axis.Y) {
                    rotations = 1;
                }
                swapU = face == Direction.DOWN;
                swapV = face != Direction.EAST;
            }

            if (swapU) {
                float t = uvs.minU;
                uvs.minU = uvs.maxU;
                uvs.maxU = t;
            }
            if (swapV) {
                float t = uvs.minV;
                uvs.minV = uvs.maxV;
                uvs.maxV = t;
            }

            MutableQuad quad = ModelUtil.createFace(face, centerFloat, radiusFloat, uvs);
            if (rotations > 0) quad.rotateTextureUp(rotations);
            quads[i++] = quad;
        }
        return quads;
    }

    public static void renderWires(TilePipeHolder pipe, PoseStack.Pose pose, int packedLight) {
        WireManager wm = pipe.getWireManager();
        if (wm == null || (wm.parts.isEmpty() && wm.betweens.isEmpty())) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource =
            Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer bb = bufferSource.getBuffer(Sheets.cutoutBlockSheet());

        for (Map.Entry<EnumWirePart, DyeColor> entry : wm.parts.entrySet()) {
            EnumWirePart part = entry.getKey();
            DyeColor color = entry.getValue();
            boolean isOn = wm.isPowered(part);
            renderQuads(partQuads.get(part), color, isOn, bb, pose, packedLight);
        }

        for (Map.Entry<EnumWireBetween, DyeColor> entry : wm.betweens.entrySet()) {
            EnumWireBetween between = entry.getKey();
            DyeColor color = entry.getValue();
            boolean isOn = wm.isPowered(between.parts[0]);
            renderQuads(betweenQuads.get(between), color, isOn, bb, pose, packedLight);
        }

        bufferSource.endBatch(Sheets.cutoutBlockSheet());
    }

    private static void renderQuads(MutableQuad[] quads, DyeColor colour, boolean isOn,
                                     VertexConsumer bb, PoseStack.Pose pose, int packedLight) {
        SpriteHolder holder = wireSprites.get(colour);
        if (holder == null) return;
        TextureAtlasSprite sprite = holder.getSprite();
        if (sprite == null) return;

        float vOffset = isOn ? (15 / 16f) : 0;
        for (MutableQuad q : quads) {
            if (q == null) continue;
            MutableQuad q2 = new MutableQuad(q);

            q2.vertex_0.tex_v += vOffset;
            q2.vertex_1.tex_v += vOffset;
            q2.vertex_2.tex_v += vOffset;
            q2.vertex_3.tex_v += vOffset;
            q2.texFromSprite(sprite);

            if (q2.getFace() != Direction.UP && !isOn) {
                float shade = 1 - q2.getCalculatedDiffuse();
                shade = shade * 15 / 15;
                shade = 1 - shade;
                q2.colourf(shade, shade, shade, 1);
            } else {
                q2.colourf(1, 1, 1, 1);
            }

            if (isOn) {
                q2.lighti(15, 15);
            } else {
                int blockLight = packedLight & 0xFFFF;
                int skyLight = (packedLight >> 16) & 0xFFFF;
                q2.lighti(blockLight >> 4, skyLight >> 4);
            }

            q2.render(pose, bb);
        }
    }

    public static void init() {

    }
}
