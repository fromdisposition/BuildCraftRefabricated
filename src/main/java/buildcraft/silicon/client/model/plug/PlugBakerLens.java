/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.plug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;

import buildcraft.silicon.BCSilicon;
import buildcraft.silicon.client.model.key.KeyPlugLens;

public enum PlugBakerLens implements IPluggableStaticBaker<KeyPlugLens> {
    INSTANCE;

    private static final Map<KeyPlugLens, List<BakedQuad>> cached = new HashMap<>();

    public static void onModelBake() {
        cached.clear();
    }

    private static MutableQuad makeFace(Direction face,
            float x0, float y0, float z0, float x1, float y1, float z1,
            float[] uvArr, TextureAtlasSprite sprite) {
        Vector3f center = new Vector3f((x0 + x1) / 2f, (y0 + y1) / 2f, (z0 + z1) / 2f);
        Vector3f radius = new Vector3f((x1 - x0) / 2f, (y1 - y0) / 2f, (z1 - z0) / 2f);
        ModelUtil.UvFaceData uvs = ModelUtil.UvFaceData.from16(uvArr[0], uvArr[1], uvArr[2], uvArr[3]);
        MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
        q.setSprite(sprite);
        q.texFromSprite(sprite);
        if (uvArr.length >= 5) {
            int rotation = (int) uvArr[4];
            if (rotation != 0) {
                q.rotateTextureUp(rotation);
            }
        }
        return q;
    }

    private static void addBox(List<MutableQuad> quads, TextureAtlasSprite sprite,
            float x0, float y0, float z0, float x1, float y1, float z1,
            float[] uvDown, float[] uvUp, float[] uvNorth, float[] uvSouth,
            float[] uvWest, float[] uvEast) {

        float bx0 = x0 / 16f, by0 = y0 / 16f, bz0 = z0 / 16f;
        float bx1 = x1 / 16f, by1 = y1 / 16f, bz1 = z1 / 16f;

        quads.add(makeFace(Direction.DOWN,  bx0, by0, bz0, bx1, by1, bz1, uvDown,  sprite));
        quads.add(makeFace(Direction.UP,    bx0, by0, bz0, bx1, by1, bz1, uvUp,    sprite));
        quads.add(makeFace(Direction.NORTH, bx0, by0, bz0, bx1, by1, bz1, uvNorth, sprite));
        quads.add(makeFace(Direction.SOUTH, bx0, by0, bz0, bx1, by1, bz1, uvSouth, sprite));
        quads.add(makeFace(Direction.WEST,  bx0, by0, bz0, bx1, by1, bz1, uvWest,  sprite));
        quads.add(makeFace(Direction.EAST,  bx0, by0, bz0, bx1, by1, bz1, uvEast,  sprite));
    }

    private static void bakeCutoutQuads(List<MutableQuad> rawQuads, boolean isFilter) {
        TextureAtlasSprite sprite = SpriteUtil.getSprite(Identifier.fromNamespaceAndPath(BCSilicon.MODID,
            isFilter ? "block/plugs/filter" : "block/plugs/lens"));
        if (sprite == null) sprite = SpriteUtil.missingSprite();

        addBox(rawQuads, sprite, 0, 3, 3, 2, 4.01f, 13,
            new float[]{2, 3, 4, 13},
            new float[]{2, 3, 4, 13},
            new float[]{2, 12, 4, 13},
            new float[]{2, 3, 4, 4},
            new float[]{2, 3, 3, 13, 1},
            new float[]{3, 3, 4, 13, 3}
        );

        addBox(rawQuads, sprite, 0, 11.99f, 3, 2, 13, 13,
            new float[]{12, 3, 14, 13},
            new float[]{12, 3, 14, 13},
            new float[]{12, 12, 14, 13},
            new float[]{12, 3, 14, 4},
            new float[]{12, 3, 13, 13, 1},
            new float[]{13, 3, 14, 13, 3}
        );

        addBox(rawQuads, sprite, 0, 4.01f, 3, 2, 11.99f, 4.01f,
            new float[]{12, 2, 13, 4, 1},
            new float[]{3, 2, 4, 4, 1},
            new float[]{3, 2, 13, 4, 1},
            new float[]{3, 2, 13, 4, 3},
            new float[]{3, 2, 13, 3, 3},
            new float[]{3, 3, 13, 4, 1}
        );

        addBox(rawQuads, sprite, 0, 4.01f, 11.99f, 2, 11.99f, 13,
            new float[]{3, 12, 4, 14, 1},
            new float[]{12, 12, 13, 14, 1},
            new float[]{3, 12, 13, 14, 1},
            new float[]{3, 12, 13, 14, 3},
            new float[]{3, 12, 13, 13, 3},
            new float[]{3, 13, 13, 14, 1}
        );

        if (isFilter) {

            addBox(rawQuads, sprite, 0, 4.01f, 6, 2, 11.99f, 7.01f,
                new float[]{12, 12, 13, 14, 1},
                new float[]{3, 12, 4, 14, 1},
                new float[]{3, 12, 13, 14, 1},
                new float[]{3, 12, 13, 14, 3},
                new float[]{3, 12, 13, 13, 3},
                new float[]{3, 13, 13, 14, 1}
            );

            addBox(rawQuads, sprite, 0, 4.01f, 9, 2, 11.99f, 10.01f,
                new float[]{12, 2, 13, 4, 1},
                new float[]{3, 2, 4, 4, 1},
                new float[]{3, 2, 13, 4, 1},
                new float[]{3, 2, 13, 4, 3},
                new float[]{3, 2, 13, 3, 3},
                new float[]{3, 3, 13, 4, 1}
            );
        }
    }

    private static void bakeTranslucentQuads(List<MutableQuad> rawQuads, @Nullable DyeColor colour, boolean isFilter) {

        if (isFilter && colour == null) {
            return;
        }

        TextureAtlasSprite sprite;
        if (colour != null) {
            sprite = SpriteUtil.getSprite(Identifier.fromNamespaceAndPath(BCSilicon.MODID, "block/plugs/overlay_lens"));
        } else {

            sprite = SpriteUtil.getSprite(Identifier.fromNamespaceAndPath("minecraft", "block/water_flow"));
        }
        if (sprite == null) sprite = SpriteUtil.missingSprite();

        float bx0 = 0.5f / 16f, by0 = 4f / 16f, bz0 = 4f / 16f;
        float bx1 = 1.5f / 16f, by1 = 12f / 16f, bz1 = 12f / 16f;

        rawQuads.add(makeFace(Direction.EAST, bx0, by0, bz0, bx1, by1, bz1, new float[]{6, 6, 10, 10}, sprite));
        rawQuads.add(makeFace(Direction.WEST, bx0, by0, bz0, bx1, by1, bz1, new float[]{6, 6, 10, 10}, sprite));

        if (colour != null) {
            int argb = colour.getTextureDiffuseColor();
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            for (MutableQuad q : rawQuads) {
                q.colouri(r, g, b, 255);
            }
        } else {

            for (MutableQuad q : rawQuads) {
                q.colouri(63, 118, 228, 255);
            }
        }
    }

    public static List<MutableQuad> bakeForItem(@Nullable DyeColor colour, boolean isFilter, boolean cutout) {
        List<MutableQuad> rawQuads = new ArrayList<>();
        if (cutout) {
            bakeCutoutQuads(rawQuads, isFilter);
        } else {
            bakeTranslucentQuads(rawQuads, colour, isFilter);
        }
        return rawQuads;
    }

    @Override
    public List<BakedQuad> bake(KeyPlugLens key) {
        if (!cached.containsKey(key)) {
            List<MutableQuad> rawQuads = new ArrayList<>();
            String layerName = key.layer != null ? key.layer.toString().toLowerCase() : "";

            List<BakedQuad> baked = new ArrayList<>();

            if (layerName.contains("cutout")) {
                bakeCutoutQuads(rawQuads, key.isFilter);
                for (MutableQuad q : rawQuads) {
                    q.rotate(Direction.WEST, key.side, 0.5f, 0.5f, 0.5f);
                    q.multShade();
                    baked.add(q.toBakedBlock());
                }
            } else if (layerName.contains("translucent")) {
                bakeTranslucentQuads(rawQuads, key.colour, key.isFilter);
                for (MutableQuad q : rawQuads) {
                    q.rotate(Direction.WEST, key.side, 0.5f, 0.5f, 0.5f);
                    baked.add(q.toBakedTranslucent());
                }
            }

            cached.put(key, baked);
        }
        return cached.get(key);
    }
}
