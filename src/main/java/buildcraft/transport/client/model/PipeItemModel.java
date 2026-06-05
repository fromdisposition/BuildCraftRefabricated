/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.client.renderer.item.CuboidItemModelWrapper;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.client.resources.model.geometry.QuadCollection;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;

import buildcraft.transport.BCTransportItems;

@SuppressWarnings("deprecation")
public class PipeItemModel implements ItemModel {

    private static final Field QUADS_FIELD;
    private static final Field PROPERTIES_FIELD;
    private static final Field TINTS_FIELD;
    private static final Field EXTENTS_FIELD;
    static {
        try {
            QUADS_FIELD = CuboidItemModelWrapper.class.getDeclaredField("quads");
            QUADS_FIELD.setAccessible(true);
            PROPERTIES_FIELD = CuboidItemModelWrapper.class.getDeclaredField("properties");
            PROPERTIES_FIELD.setAccessible(true);
            TINTS_FIELD = CuboidItemModelWrapper.class.getDeclaredField("tints");
            TINTS_FIELD.setAccessible(true);
            EXTENTS_FIELD = CuboidItemModelWrapper.class.getDeclaredField("extents");
            EXTENTS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to access CuboidItemModelWrapper fields", e);
        }
    }

    private final ItemModel vanillaDelegate;
    private final PipeDefinition definition;

    private final @Nullable QuadCollection vanillaQuads;
    private final @Nullable ModelRenderProperties renderProperties;
    @SuppressWarnings("unchecked")
    private final java.util.function.Supplier<org.joml.Vector3fc[]> extents;

    @SuppressWarnings("unchecked")
    public PipeItemModel(ItemModel vanillaDelegate, PipeDefinition definition) {
        this.vanillaDelegate = vanillaDelegate;
        this.definition = definition;

        if (vanillaDelegate instanceof CuboidItemModelWrapper wrapper) {
            try {
                this.vanillaQuads = (QuadCollection) QUADS_FIELD.get(wrapper);
                this.renderProperties = (ModelRenderProperties) PROPERTIES_FIELD.get(wrapper);
                this.extents = (java.util.function.Supplier<org.joml.Vector3fc[]>) EXTENTS_FIELD.get(wrapper);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to read CuboidItemModelWrapper fields", e);
            }
        } else {
            this.vanillaQuads = null;
            this.renderProperties = null;
            this.extents = null;
        }

    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver,
                       ItemDisplayContext displayContext, @Nullable ClientLevel level,
                       @Nullable ItemOwner owner, int seed) {

        DyeColor colour = stack.get(BCTransportItems.PIPE_COLOUR.get());

        if (vanillaQuads == null || renderProperties == null) {
            vanillaDelegate.update(renderState, stack, modelResolver, displayContext, level, owner, seed);
            return;
        }

        if (colour == null) {
            vanillaDelegate.update(renderState, stack, modelResolver, displayContext, level, owner, seed);
            return;
        }

        renderState.appendModelIdentityElement(this);
        renderState.appendModelIdentityElement(colour);

        if (definition.flowType == PipeApi.flowFluids) {

            TextureAtlasSprite[] dyedSprites = PipeBaseModelGenStandard.ensureDyedSprites(definition, colour);
            int itemTexIndex = definition.itemTextureTop;
            TextureAtlasSprite dyedSprite = itemTexIndex < dyedSprites.length
                    ? dyedSprites[itemTexIndex] : dyedSprites[0];
            var layer = renderState.newLayer();
            layer.prepareQuadList().addAll(generatePipeQuads(dyedSprite));
            if (extents != null) layer.setExtents(extents);
            renderProperties.applyToLayer(layer, displayContext);
            return;
        }

        var baseLayer = renderState.newLayer();
        baseLayer.prepareQuadList().addAll(vanillaQuads.getAll());
        if (extents != null) baseLayer.setExtents(extents);
        renderProperties.applyToLayer(baseLayer, displayContext);

        TextureAtlasSprite[] maskArray = PipeBaseModelGenStandard.ensureMaskSprites(definition);
        TextureAtlasSprite maskSprite = maskArray != null && maskArray.length > 0
                ? maskArray[0] : null;
        if (maskSprite != null && maskSprite != SpriteUtil.missingSprite()) {
            List<BakedQuad> overlayQuads = generateOverlayQuads(maskSprite);
            if (!overlayQuads.isEmpty()) {
                var overlayLayer = renderState.newLayer();
                overlayLayer.prepareQuadList().addAll(overlayQuads);
                if (extents != null) overlayLayer.setExtents(extents);
                renderProperties.applyToLayer(overlayLayer, displayContext);

                int tintColour = (76 << 24) | buildcraft.lib.misc.ColourUtil.getLightHex(colour);
                overlayLayer.tintLayers().add(tintColour);
            }
        }

    }

    private static List<BakedQuad> generatePipeQuads(TextureAtlasSprite sprite) {
        List<BakedQuad> quads = new ArrayList<>();

        UvFaceData bottomSideUvs = UvFaceData.from16(4, 12, 12, 16);
        UvFaceData centerUvs     = UvFaceData.from16(4, 4, 12, 12);
        UvFaceData topSideUvs    = UvFaceData.from16(4, 0, 12, 4);
        UvFaceData capFaceUvs    = UvFaceData.from16(4, 4, 12, 12);

        addPipeFaces(quads, sprite, new Vector3f(0.5f, 0.125f, 0.5f),
                new Vector3f(0.25f, 0.125f, 0.25f),
                new Direction[]{ Direction.DOWN }, capFaceUvs);
        addPipeFaces(quads, sprite, new Vector3f(0.5f, 0.125f, 0.5f),
                new Vector3f(0.25f, 0.125f, 0.25f),
                new Direction[]{ Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST }, bottomSideUvs);

        addPipeFaces(quads, sprite, new Vector3f(0.5f, 0.5f, 0.5f),
                new Vector3f(0.25f, 0.25f, 0.25f),
                new Direction[]{ Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST }, centerUvs);

        addPipeFaces(quads, sprite, new Vector3f(0.5f, 0.875f, 0.5f),
                new Vector3f(0.25f, 0.125f, 0.25f),
                new Direction[]{ Direction.UP }, capFaceUvs);
        addPipeFaces(quads, sprite, new Vector3f(0.5f, 0.875f, 0.5f),
                new Vector3f(0.25f, 0.125f, 0.25f),
                new Direction[]{ Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST }, topSideUvs);

        return quads;
    }

    private static void addPipeFaces(List<BakedQuad> quads, TextureAtlasSprite sprite,
                                      Vector3f center, Vector3f radius,
                                      Direction[] faces, UvFaceData uvs) {
        for (Direction face : faces) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
            quad.setSprite(sprite);
            quad.texFromSprite(sprite);
            quads.add(quad.toBakedBlock());
        }
    }

    private static final double OVERLAY_OFFSET = 0.01;

    private static List<BakedQuad> generateOverlayQuads(TextureAtlasSprite maskSprite) {
        List<BakedQuad> quads = new ArrayList<>();

        UvFaceData bottomSideUvs = UvFaceData.from16(4, 12, 12, 16);
        UvFaceData centerUvs     = UvFaceData.from16(4, 4, 12, 12);
        UvFaceData topSideUvs    = UvFaceData.from16(4, 0, 12, 4);
        UvFaceData capFaceUvs    = UvFaceData.from16(4, 4, 12, 12);

        addOverlayFaces(quads, maskSprite, new Vector3f(0.5f, 0.125f, 0.5f),
                new Vector3f(0.25f, 0.125f, 0.25f),
                new Direction[]{ Direction.DOWN }, capFaceUvs);
        addOverlayFaces(quads, maskSprite, new Vector3f(0.5f, 0.125f, 0.5f),
                new Vector3f(0.25f, 0.125f, 0.25f),
                new Direction[]{ Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST }, bottomSideUvs);

        addOverlayFaces(quads, maskSprite, new Vector3f(0.5f, 0.5f, 0.5f),
                new Vector3f(0.25f, 0.25f, 0.25f),
                new Direction[]{ Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST }, centerUvs);

        addOverlayFaces(quads, maskSprite, new Vector3f(0.5f, 0.875f, 0.5f),
                new Vector3f(0.25f, 0.125f, 0.25f),
                new Direction[]{ Direction.UP }, capFaceUvs);
        addOverlayFaces(quads, maskSprite, new Vector3f(0.5f, 0.875f, 0.5f),
                new Vector3f(0.25f, 0.125f, 0.25f),
                new Direction[]{ Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST }, topSideUvs);

        return quads;
    }

    private static void addOverlayFaces(List<BakedQuad> quads, TextureAtlasSprite sprite,
                                         Vector3f center, Vector3f radius,
                                         Direction[] faces, UvFaceData uvs) {
        for (Direction face : faces) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);

            quad.setSprite(sprite);
            quad.texFromSprite(sprite);
            quad.setTint(0);
            quads.add(quad.toBakedTranslucent());
        }
    }
}
