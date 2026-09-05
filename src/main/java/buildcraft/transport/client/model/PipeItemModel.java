/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.client.model.ModelCache;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.transport.BCTransportItems;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
//? if >= 26.1 {
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
//?} else {
/*import net.minecraft.client.renderer.item.BlockModelWrapper;
*///?}
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class PipeItemModel implements ItemModel {
   private static final Field PROPERTIES_FIELD;
   private static final Field EXTENTS_FIELD;
   // Keyed by sprite instance, so a resource reload is a natural cache miss and stale geometry cannot survive it.
   private static final ModelCache<TextureAtlasSprite> OVERLAY_CACHE = new ModelCache<>(PipeItemModel::generateOverlayQuads);
   private final ItemModel vanillaDelegate;
   private final PipeDefinition definition;
   private final @Nullable ModelRenderProperties renderProperties;
   //? if >= 1.21.11 {
   private final @Nullable Supplier<Vector3fc[]> extents;
   //?} else {
   /*private final @Nullable Supplier<Vector3f[]> extents;
   *///?}

   @SuppressWarnings("unchecked")
   public PipeItemModel(ItemModel vanillaDelegate, PipeDefinition definition) {
      this.vanillaDelegate = vanillaDelegate;
      this.definition = definition;
      //? if >= 26.1 {
      if (vanillaDelegate instanceof CuboidItemModelWrapper wrapper) {
      //?} else {
      /*if (vanillaDelegate instanceof BlockModelWrapper wrapper) {
      *///?}
         try {
            this.renderProperties = (ModelRenderProperties)PROPERTIES_FIELD.get(wrapper);
            //? if >= 1.21.11 {
            this.extents = (Supplier<Vector3fc[]>)EXTENTS_FIELD.get(wrapper);
            //?} else {
            /*this.extents = (Supplier<Vector3f[]>)EXTENTS_FIELD.get(wrapper);
            *///?}
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to read CuboidItemModelWrapper bake data", e);
         }
      } else {
         this.renderProperties = null;
         this.extents = null;
      }
   }

   @Override
   public void update(
      ItemStackRenderState renderState,
      ItemStack stack,
      ItemModelResolver modelResolver,
      ItemDisplayContext displayContext,
      @Nullable ClientLevel level,
      @Nullable ItemOwner owner,
      int seed
   ) {
      this.vanillaDelegate.update(renderState, stack, modelResolver, displayContext, level, owner, seed);
      DyeColor colour = stack.get(BCTransportItems.PIPE_COLOUR);
      if (colour == null || this.renderProperties == null) {
         return;
      }

      renderState.appendModelIdentityElement(colour);
      TextureAtlasSprite[] maskArray = PipeBaseModelGenStandard.ensureMaskSprites(this.definition);
      TextureAtlasSprite maskSprite = maskArray != null && maskArray.length > 0 ? maskArray[0] : null;
      if (maskSprite == null || maskSprite == SpriteUtil.missingSprite()) {
         return;
      }

      List<BakedQuad> overlayQuads = OVERLAY_CACHE.bake(maskSprite);
      if (overlayQuads.isEmpty()) {
         return;
      }

      LayerRenderState overlayLayer = renderState.newLayer();
      overlayLayer.setQuads(net.minecraft.client.resources.model.geometry.ItemQuads.split(overlayQuads));
      if (this.extents != null) {
         overlayLayer.setExtents(this.extents);
      }

      this.renderProperties.applyToLayer(overlayLayer, displayContext);
      int alpha = this.definition.flowType == PipeApi.flowFluids ? 0xFF000000 : 0x4C000000;
      int tintColour = alpha | ColourUtil.getLightHex(colour);
      //? if >= 26.1 {
      overlayLayer.tintLayers().add(tintColour);
      //?} else {
      /*overlayLayer.prepareTintLayers(1)[0] = tintColour;
      *///?}
   }

   private static List<BakedQuad> generateOverlayQuads(TextureAtlasSprite maskSprite) {
      List<BakedQuad> quads = new ArrayList<>();
      ModelUtil.UvFaceData bottomSideUvs = ModelUtil.UvFaceData.from16(4, 12, 12, 16);
      ModelUtil.UvFaceData centerUvs = ModelUtil.UvFaceData.from16(4, 4, 12, 12);
      ModelUtil.UvFaceData topSideUvs = ModelUtil.UvFaceData.from16(4, 0, 12, 4);
      ModelUtil.UvFaceData capFaceUvs = ModelUtil.UvFaceData.from16(4, 4, 12, 12);
      addOverlayFaces(quads, maskSprite, new Vector3f(0.5F, 0.125F, 0.5F), new Vector3f(0.25F, 0.125F, 0.25F), new Direction[]{Direction.DOWN}, capFaceUvs);
      addOverlayFaces(
         quads,
         maskSprite,
         new Vector3f(0.5F, 0.125F, 0.5F),
         new Vector3f(0.25F, 0.125F, 0.25F),
         new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST},
         bottomSideUvs
      );
      addOverlayFaces(
         quads,
         maskSprite,
         new Vector3f(0.5F, 0.5F, 0.5F),
         new Vector3f(0.25F, 0.25F, 0.25F),
         new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST},
         centerUvs
      );
      addOverlayFaces(quads, maskSprite, new Vector3f(0.5F, 0.875F, 0.5F), new Vector3f(0.25F, 0.125F, 0.25F), new Direction[]{Direction.UP}, capFaceUvs);
      addOverlayFaces(
         quads,
         maskSprite,
         new Vector3f(0.5F, 0.875F, 0.5F),
         new Vector3f(0.25F, 0.125F, 0.25F),
         new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST},
         topSideUvs
      );
      return quads;
   }

   private static void addOverlayFaces(
      List<BakedQuad> quads, TextureAtlasSprite sprite, Vector3f center, Vector3f radius, Direction[] faces, ModelUtil.UvFaceData uvs
   ) {
      for (Direction face : faces) {
         MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
         quad.setSprite(sprite);
         quad.texFromSprite(sprite);
         quad.setTint(0);
         quads.add(quad.toBakedTranslucent());
      }
   }

   static {
      try {
         //? if >= 26.1 {
         Class<?> wrapperClass = CuboidItemModelWrapper.class;
         //?} else {
         /*Class<?> wrapperClass = BlockModelWrapper.class;
         *///?}
         // Lookup by type, not name: production runtimes are intermediary-mapped and lack the Mojang field name.
         PROPERTIES_FIELD = findFieldByType(wrapperClass, ModelRenderProperties.class);
         EXTENTS_FIELD = findFieldByType(wrapperClass, Supplier.class);
         PROPERTIES_FIELD.setAccessible(true);
         EXTENTS_FIELD.setAccessible(true);
      } catch (NoSuchFieldException e) {
         throw new RuntimeException("Failed to access pipe item model bake data", e);
      }
   }

   private static Field findFieldByType(Class<?> owner, Class<?> type) throws NoSuchFieldException {
      for (Field field : owner.getDeclaredFields()) {
         if (field.getType() == type) {
            return field;
         }
      }
      throw new NoSuchFieldException("No field of type " + type.getName() + " in " + owner.getName());
   }
}
