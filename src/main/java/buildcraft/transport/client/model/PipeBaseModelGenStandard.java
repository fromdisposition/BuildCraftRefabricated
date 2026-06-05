/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import org.joml.Vector3f;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.transport.pipe.EnumPipeColourType;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFaceTex;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.SpriteUtil;

import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTranslucentKey;

public enum PipeBaseModelGenStandard implements IPipeBaseModelGen {
    INSTANCE;

    private static final Map<Long, TextureAtlasSprite[]> SPRITES = new java.util.HashMap<>();
    private static final Map<Long, TextureAtlasSprite[]> MASK_SPRITES = new java.util.HashMap<>();

    private static final Map<Long, TextureAtlasSprite[]> DYED_SPRITES = new java.util.HashMap<>();

    private static boolean isCb() {
        return buildcraft.lib.client.ColorBlindUtil.isActive();
    }

    static long defKey(PipeDefinition def, boolean cb) {
        return ((long) System.identityHashCode(def) << 32) | (cb ? 1L : 0L);
    }

    static long dyedKey(PipeDefinition def, DyeColor colour, boolean cb) {
        return ((long) System.identityHashCode(def) << 32) | ((long) colour.ordinal() << 1) | (cb ? 1L : 0L);
    }

    private static String maybeCbName(String texName, boolean cb) {
        if (!cb) return texName;
        String candidate = texName + "_cb";
        TextureAtlasSprite probe = SpriteUtil.getSprite(candidate);
        return (probe == null || probe == SpriteUtil.missingSprite()) ? texName : candidate;
    }

    @Override
    public TextureAtlasSprite[] getItemSprites(PipeDefinition def) {
        return SPRITES.get(defKey(def, isCb()));
    }

    private static TextureAtlasSprite[] ensureSprites(PipeDefinition def) {
        boolean cb = isCb();
        long key = defKey(def, cb);
        TextureAtlasSprite[] cached = SPRITES.get(key);
        if (cached != null) return cached;
        TextureAtlasSprite missing = SpriteUtil.missingSprite();
        TextureAtlasSprite[] array = new TextureAtlasSprite[def.textures.length];
        boolean allResolved = true;
        for (int i = 0; i < array.length; i++) {
            String name = maybeCbName(def.textures[i], cb);
            array[i] = SpriteUtil.getSprite(name);
            if (array[i] == null || array[i] == missing) {
                array[i] = missing;
                allResolved = false;
            }
        }

        if (allResolved) {
            SPRITES.put(key, array);
        }
        return array;
    }

    private static final Map<String, String> MASK_MAP = new java.util.HashMap<>();
    static {

        for (String s : new String[]{"clay_item","cobblestone_item","diamond_item",
                "diamond_wood_item_clear","emzuli_item_clear","gold_item",
                "iron_item_clear","obsidian_item","quartz_item",
                "sandstone_item","stone_item","stripes_item","wood_item_clear"})
            MASK_MAP.put(s, "mask_shared_item");

        for (String s : new String[]{"cobblestone_power","cobblestone_rf",
                "diamond_wood_power_clear","diamond_wood_rf_clear",
                "gold_power","gold_rf",
                "quartz_power","quartz_rf",
                "sandstone_power","sandstone_rf",
                "stone_power","stone_rf",
                "wood_power_clear","wood_rf_clear"})
            MASK_MAP.put(s, "mask_shared_power");

        for (String prefix : new String[]{"diamond_power_m","diamond_rf_m",
                "iron_power_m","iron_rf_m"})
            for (String suffix : new String[]{"0","4","8","16","32","64","128"})
                MASK_MAP.put(prefix + suffix, "mask_shared_power_limiter");

        for (String s : new String[]{"diamond_wood_power_filled","diamond_wood_rf_filled",
                "wood_power_filled","wood_rf_filled"})
            MASK_MAP.put(s, "mask_shared_power_filled");

        for (String s : new String[]{"clay_fluid","cobblestone_fluid",
                "diamond_fluid","diamond_fluid_down","diamond_fluid_east","diamond_fluid_north",
                "diamond_fluid_south","diamond_fluid_up","diamond_fluid_west",
                "diamond_fluid_west_cb","diamond_wood_fluid_clear","diamond_wood_fluid_filled",
                "gold_fluid","iron_fluid_clear","iron_fluid_filled","quartz_fluid",
                "sandstone_fluid","stone_fluid","void_fluid","wood_fluid_clear","wood_fluid_filled"})
            MASK_MAP.put(s, "mask_shared_fluid");

        for (String prefix : new String[]{"daizuli_item_","lapis_item_"})
            for (String colour : new String[]{"black","blue","brown","cyan","gray","green",
                    "light_blue","light_gray","lime","magenta","orange","pink","purple","red",
                    "white","yellow"})
                MASK_MAP.put(prefix + colour, "mask_shared_daizuli");

        for (String s : new String[]{"daizuli_item_filled","diamond_item_down","diamond_item_east",
                "diamond_item_north","diamond_item_south","diamond_item_up","diamond_item_west",
                "diamond_item_west_cb","diamond_wood_item_filled","emzuli_item_filled",
                "iron_item_filled","wood_item_filled"})
            MASK_MAP.put(s, "mask_shared_diamond_filled");

        MASK_MAP.put("diamond_fluid_itemstack", "mask_shared_fluid");
        MASK_MAP.put("diamond_item_itemstack", "mask_diamond_item_itemstack");
        MASK_MAP.put("void_item", "mask_void_item");
    }

    static TextureAtlasSprite[] ensureMaskSprites(PipeDefinition def) {
        boolean cb = isCb();
        long key = defKey(def, cb);
        TextureAtlasSprite[] cached = MASK_SPRITES.get(key);
        if (cached != null) return cached;
        TextureAtlasSprite missing = SpriteUtil.missingSprite();
        TextureAtlasSprite[] array = new TextureAtlasSprite[def.textures.length];
        boolean allResolved = true;
        for (int i = 0; i < array.length; i++) {

            String texName = maybeCbName(def.textures[i], cb);
            int colonIdx = texName.indexOf(':');
            String namespace = colonIdx >= 0 ? texName.substring(0, colonIdx) : "minecraft";
            String path = colonIdx >= 0 ? texName.substring(colonIdx + 1) : texName;
            int lastSlash = path.lastIndexOf('/');
            String baseName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;

            String maskBaseName = MASK_MAP.getOrDefault(baseName, "mask_" + baseName);
            String maskPath = lastSlash >= 0
                ? path.substring(0, lastSlash + 1) + maskBaseName
                : maskBaseName;
            String maskLoc = namespace + ":" + maskPath;

            array[i] = SpriteUtil.getSprite(net.minecraft.resources.Identifier.parse(maskLoc));
            if (array[i] == null || array[i] == missing) {
                array[i] = missing;
                allResolved = false;
            }
        }
        if (allResolved) {
            MASK_SPRITES.put(key, array);
        }
        return array;
    }

    static TextureAtlasSprite[] ensureDyedSprites(PipeDefinition def, DyeColor colour) {
        boolean cb = isCb();
        long cacheKey = dyedKey(def, colour, cb);
        TextureAtlasSprite[] cached = DYED_SPRITES.get(cacheKey);
        if (cached != null) return cached;
        TextureAtlasSprite missing = SpriteUtil.missingSprite();
        TextureAtlasSprite[] array = new TextureAtlasSprite[def.textures.length];
        String dyeSuffix = "_dyed_" + colour.getName();
        for (int i = 0; i < array.length; i++) {
            String dyedTex = maybeCbName(def.textures[i], cb) + dyeSuffix;
            array[i] = SpriteUtil.getSprite(Identifier.parse(dyedTex));
            if (array[i] == null || array[i] == missing) {
                throw new IllegalStateException(
                    "Dyed sprite missing from blocks atlas: " + dyedTex
                        + " — DyeReplaceSpriteSource didn't emit it. Check that "
                        + "assets/minecraft/atlases/blocks.json has a buildcraftlib:dye_replace "
                        + "entry whose source matches " + def.textures[i]
                        + " and that the source/mask PNGs exist and have matching dimensions.");
            }
        }
        DYED_SPRITES.put(cacheKey, array);
        return array;
    }

    public static void onColorBlindToggle() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc != null && mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    public static void clearSpriteCaches() {
        SPRITES.clear();
        MASK_SPRITES.clear();
        DYED_SPRITES.clear();
    }

    private static final MutableQuad[][][] QUADS;
    private static final MutableQuad[][][] QUADS_COLOURED;

    static {
        QUADS = new MutableQuad[2][][];
        QUADS_COLOURED = new MutableQuad[2][][];
        final double colourOffset = 0.01;
        Vec3[] faceOffset = new Vec3[6];
        for (Direction face : Direction.values()) {
            Vec3 dir = Vec3.atLowerCornerOf(face.getOpposite().getUnitVec3i());
            faceOffset[face.ordinal()] = dir.scale(colourOffset);
        }

        QUADS[0] = new MutableQuad[6][2];
        QUADS_COLOURED[0] = new MutableQuad[6][2];
        Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radius = new Vector3f(0.25f, 0.25f, 0.25f);
        UvFaceData uvs = new UvFaceData();
        uvs.minU = uvs.minV = 4 / 16f;
        uvs.maxU = uvs.maxV = 12 / 16f;
        for (Direction face : Direction.values()) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
            quad.setDiffuse(quad.normalvf());
            QUADS[0][face.ordinal()][0] = quad;
            dupDarker(QUADS[0][face.ordinal()]);

            MutableQuad[] colQuads = ModelUtil.createDoubleFace(face, center, radius, uvs);
            for (MutableQuad q : colQuads) {
                q.translatevd(faceOffset[face.ordinal()]);
            }
            QUADS_COLOURED[0][face.ordinal()] = colQuads;
        }

        int[][] uvsRot = {
            { 2, 0, 3, 3 },
            { 0, 2, 1, 1 },
            { 2, 0, 0, 2 },
            { 0, 2, 2, 0 },
            { 3, 3, 0, 2 },
            { 1, 1, 2, 0 }
        };

        UvFaceData[] types = {
            UvFaceData.from16(4, 0, 12, 4),
            UvFaceData.from16(4, 12, 12, 16),
            UvFaceData.from16(0, 4, 4, 12),
            UvFaceData.from16(12, 4, 16, 12)
        };

        QUADS[1] = new MutableQuad[6][8];
        QUADS_COLOURED[1] = new MutableQuad[6][8];
        for (Direction side : Direction.values()) {
            Vector3f sCenter = new Vector3f(
                side.getStepX() * 0.375f,
                side.getStepY() * 0.375f,
                side.getStepZ() * 0.375f
            );
            Vector3f sRadius = new Vector3f(
                side.getAxis() == Axis.X ? 0.125f : 0.25f,
                side.getAxis() == Axis.Y ? 0.125f : 0.25f,
                side.getAxis() == Axis.Z ? 0.125f : 0.25f
            );
            sCenter.add(new Vector3f(0.5f, 0.5f, 0.5f));

            int i = 0;
            for (Direction face : Direction.values()) {
                if (face.getAxis() == side.getAxis()) continue;
                MutableQuad quad = ModelUtil.createFace(face, sCenter, sRadius, types[i]);
                quad.rotateTextureUp(uvsRot[side.ordinal()][i]);

                MutableQuad col = new MutableQuad(quad);

                quad.setDiffuse(quad.normalvf());
                QUADS[1][side.ordinal()][i] = quad;

                col.translatevd(faceOffset[face.ordinal()]);
                QUADS_COLOURED[1][side.ordinal()][i++] = col;
            }
            dupDarker(QUADS[1][side.ordinal()]);
            dupInverted(QUADS_COLOURED[1][side.ordinal()]);
        }
    }

    private static void dupDarker(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        float mult = OPTION_INSIDE_COLOUR_MULT.getAsFloat();
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                MutableQuad to = from.copyAndInvertNormal();
                to.setCalculatedDiffuse();
                to.multColourd(mult);
                quads[n] = to;
            }
        }
    }

    private static void dupInverted(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                quads[n] = from.copyAndInvertNormal();
            }
        }
    }

    @Override
    public List<MutableQuad> generateCutoutMutable(PipeBaseCutoutKey key) {
        List<MutableQuad> quads = new ArrayList<>();

        TextureAtlasSprite[] spriteArray;
        if (key.definition != null && key.colour != null
                && key.definition.flowType == PipeApi.flowFluids) {
            spriteArray = ensureDyedSprites(key.definition, key.colour);
        } else {
            spriteArray = key.definition != null ? ensureSprites(key.definition) : null;
        }
        TextureAtlasSprite borderSprite = getBorderSprite(key);
        int colour = borderSprite == null ? -1 : getPipeModelColour(key.colour);
        int border_r = (colour >> 0) & 0xFF;
        int border_g = (colour >> 8) & 0xFF;
        int border_b = (colour >> 16) & 0xFF;
        for (Direction face : Direction.values()) {
            float size = key.connections[face.ordinal()];
            PipeFaceTex tex = size > 0 ? key.sideSprites[face.ordinal()] : key.centerSprite;
            int quadsIndex = size > 0 ? 1 : 0;
            MutableQuad[] quadArray = QUADS[quadsIndex][face.ordinal()];

            int startIndex = quads.size();

            for (int i = 0; i < tex.getCount(); i++) {
                addQuads(quadArray, quads, getSprite(spriteArray, tex, i));

                int c = tex.getColour(i);
                int r = (c >> 0) & 0xFF;
                int g = (c >> 8) & 0xFF;
                int b = (c >> 16) & 0xFF;

                for (int q = startIndex; q < quads.size(); q++) {
                    quads.get(q).multColouri(r, g, b, 0xFF);
                }

                startIndex = quads.size();
            }

            if (borderSprite != null) {
                addQuads(quadArray, quads, borderSprite);

                for (int ii = startIndex; ii < quads.size(); ii++) {
                    quads.get(ii).multColouri(border_r, border_g, border_b, 0xFF);
                }
            }
        }

        return quads;
    }

    public List<MutableQuad> generateMaskMutable(PipeBaseCutoutKey key, int alpha) {
        if (key.colour == null || key.colourType != EnumPipeColourType.TRANSLUCENT) {
            return Collections.emptyList();
        }

        List<MutableQuad> quads = new ArrayList<>();
        TextureAtlasSprite[] maskArray = key.definition != null ? ensureMaskSprites(key.definition) : null;
        int dyeColour = getPipeModelColour(key.colour);
        int dye_r = (dyeColour >> 0) & 0xFF;
        int dye_g = (dyeColour >> 8) & 0xFF;
        int dye_b = (dyeColour >> 16) & 0xFF;

        for (Direction face : Direction.values()) {
            float size = key.connections[face.ordinal()];
            PipeFaceTex tex = size > 0 ? key.sideSprites[face.ordinal()] : key.centerSprite;
            int quadsIndex = size > 0 ? 1 : 0;
            MutableQuad[] quadArray = QUADS[quadsIndex][face.ordinal()];

            int startIndex = quads.size();
            for (int i = 0; i < tex.getCount(); i++) {
                TextureAtlasSprite maskSprite = getSprite(maskArray, tex, i);
                if (maskSprite != SpriteUtil.missingSprite()) {
                    addQuads(quadArray, quads, maskSprite);
                    for (int q = startIndex; q < quads.size(); q++) {
                        quads.get(q).multColouri(dye_r, dye_g, dye_b, alpha);
                    }
                }
                startIndex = quads.size();
            }
        }
        return quads;
    }

    @Override
    public List<BakedQuad> generateCutout(PipeBaseCutoutKey key) {
        List<MutableQuad> quads = generateCutoutMutable(key);
        List<BakedQuad> bakedQuads = new ArrayList<>();
        for (MutableQuad q : quads) {
            bakedQuads.add(q.toBakedBlock());
        }
        return bakedQuads;
    }

    @Nullable
    private static TextureAtlasSprite getBorderSprite(PipeBaseCutoutKey key) {
        if (key.colour == null) {
            return null;
        }
        if (key.colourType == EnumPipeColourType.BORDER_INNER) {
            return BCTransportSprites.PIPE_COLOUR_BORDER_INNER.getSprite();
        }
        if (key.colourType == EnumPipeColourType.BORDER_OUTER) {
            return BCTransportSprites.PIPE_COLOUR_BORDER_OUTER.getSprite();
        }
        return null;
    }

    private static TextureAtlasSprite getSprite(TextureAtlasSprite[] array, PipeFaceTex tex, int spriteIndex) {
        int index = tex.getTexture(spriteIndex);
        return getSprite(array, index);
    }

    private static TextureAtlasSprite getSprite(TextureAtlasSprite[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return SpriteUtil.missingSprite();
        }
        return array[index];
    }

    @Override
    public List<BakedQuad> generateTranslucent(PipeBaseTranslucentKey key) {
        if (!key.shouldRender()) return ImmutableList.of();
        List<MutableQuad> mutableQuads;
        if (key.cutoutKey != null && key.cutoutKey.definition != null
                && key.cutoutKey.definition.flowType == PipeApi.flowFluids) {

            if (key.cutoutKey.colour != null) {
                return ImmutableList.of();
            }
            mutableQuads = generateMaskMutable(key.cutoutKey, 255);
        } else {
            mutableQuads = generateTranslucentMutable(key);
        }
        List<BakedQuad> bakedQuads = new ArrayList<>();
        for (MutableQuad q : mutableQuads) {
            bakedQuads.add(q.toBakedTranslucent());
        }
        return bakedQuads;
    }

    public List<MutableQuad> generateTranslucentMutable(PipeBaseTranslucentKey key) {
        if (!key.shouldRender()) return ImmutableList.of();
        List<MutableQuad> quads = new ArrayList<>();

        TextureAtlasSprite[] maskArray = key.cutoutKey != null && key.cutoutKey.definition != null
                ? ensureMaskSprites(key.cutoutKey.definition) : null;

        for (Direction face : Direction.values()) {
            float size = key.connections[face.ordinal()];
            PipeFaceTex tex = key.cutoutKey != null
                    ? (size > 0 ? key.cutoutKey.sideSprites[face.ordinal()] : key.cutoutKey.centerSprite)
                    : null;
            int quadsIndex = size > 0 ? 1 : 0;

            if (tex != null) {
                for (int i = 0; i < tex.getCount(); i++) {
                    TextureAtlasSprite maskSprite = getSprite(maskArray, tex, i);
                    int startIndex = quads.size();
                    addQuads(QUADS[quadsIndex][face.ordinal()], quads, maskSprite);

                    for (int q = startIndex; q < quads.size(); q++) {
                        quads.get(q).multColouri(0xFF, 0xFF, 0xFF, 76);
                    }
                }
            } else {

                TextureAtlasSprite sprite = BCTransportSprites.PIPE_COLOUR.getSprite();
                if (sprite == null) sprite = SpriteUtil.missingSprite();
                addQuads(QUADS[quadsIndex][face.ordinal()], quads, sprite);
            }
        }

        for (MutableQuad q : quads) {
            q.setTint(1);
        }
        return quads;
    }

    private static int getPipeModelColour(DyeColor c) {
        if (c == null) return 0xFF_FF_FF_FF;
        return 0xFF_00_00_00 | ColourUtil.swapArgbToAbgr(ColourUtil.getLightHex(c));
    }

    public static int getDyeTintColour(DyeColor c) {
        return getPipeModelColour(c);
    }

    private static void addQuads(MutableQuad[] from, List<MutableQuad> to, TextureAtlasSprite sprite) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            MutableQuad copy = new MutableQuad(f);
            copy.setSprite(sprite);
            copy.texFromSprite(sprite);
            to.add(copy);
        }
    }
}
