/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.silicon.client.model.plug.PlugBakerFacade;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadePhasedState;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
//? if >= 26.1 {
import net.minecraft.client.resources.model.cuboid.ItemTransform;
//?} else {
/*import net.minecraft.client.renderer.block.model.ItemTransform;
*///?}
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class FacadeItemModel implements ItemModel {
   private static final LoadingCache<KeyPlugFacade, List<BakedQuad>> cache = CacheBuilder.newBuilder()
      //? if >= 26.2 {
      .expireAfterAccess(java.time.Duration.ofMinutes(1))
      //?} else {
      /*.expireAfterAccess(1L, TimeUnit.MINUTES)
      *///?}
      .build(CacheLoader.from(key -> PlugBakerFacade.INSTANCE.bake(key)));
   private static final LoadingCache<KeyPlugFacade, List<BakedQuad>> guiCache = CacheBuilder.newBuilder()
      //? if >= 26.2 {
      .expireAfterAccess(java.time.Duration.ofMinutes(1))
      //?} else {
      /*.expireAfterAccess(1L, TimeUnit.MINUTES)
      *///?}
      .build(CacheLoader.from(key -> {
         List<BakedQuad> quads = new ArrayList<>();
         float offsetZ = 0.4375F;

         for (MutableQuad quad : PlugBakerFacade.INSTANCE.bakeForKey(key)) {
            quad.setShade(false);
            quad.vertex_0.translatef(0.0F, 0.0F, offsetZ).normalf(0.0F, 1.0F, 0.0F).colouri(255, 255, 255, 255);
            quad.vertex_1.translatef(0.0F, 0.0F, offsetZ).normalf(0.0F, 1.0F, 0.0F).colouri(255, 255, 255, 255);
            quad.vertex_2.translatef(0.0F, 0.0F, offsetZ).normalf(0.0F, 1.0F, 0.0F).colouri(255, 255, 255, 255);
            quad.vertex_3.translatef(0.0F, 0.0F, offsetZ).normalf(0.0F, 1.0F, 0.0F).colouri(255, 255, 255, 255);
            quads.add(quad.toBakedItem());
         }

         return quads;
      }));

   // The canonical minecraft:block/block.json display transforms. Vanilla injects them into every block item via
   // the JSON parent chain, but this ItemModel builds its quads in code, bypassing JSON entirely -- with no
   // transform set the renderer used identity (scale 1.0), so a dropped/held facade rendered as big as a placed
   // block. Translations are already /16, exactly as the vanilla deserializer stores them; the left third-person
   // mirror is applied by ItemTransform itself via the leftHand flag.
   private static final ItemTransform TRANSFORM_GROUND =
      new ItemTransform(new Vector3f(), new Vector3f(0.0F, 0.1875F, 0.0F), new Vector3f(0.25F));
   private static final ItemTransform TRANSFORM_FIXED =
      new ItemTransform(new Vector3f(), new Vector3f(), new Vector3f(0.5F));
   private static final ItemTransform TRANSFORM_THIRD_PERSON =
      new ItemTransform(new Vector3f(75.0F, 45.0F, 0.0F), new Vector3f(0.0F, 0.15625F, 0.0F), new Vector3f(0.375F));
   private static final ItemTransform TRANSFORM_FIRST_PERSON_RIGHT =
      new ItemTransform(new Vector3f(0.0F, 45.0F, 0.0F), new Vector3f(), new Vector3f(0.4F));
   private static final ItemTransform TRANSFORM_FIRST_PERSON_LEFT =
      new ItemTransform(new Vector3f(0.0F, 225.0F, 0.0F), new Vector3f(), new Vector3f(0.4F));

   private static ItemTransform transformFor(ItemDisplayContext displayContext) {
      return switch (displayContext) {
         case GROUND -> TRANSFORM_GROUND;
         case FIXED -> TRANSFORM_FIXED;
         case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> TRANSFORM_THIRD_PERSON;
         case FIRST_PERSON_RIGHT_HAND -> TRANSFORM_FIRST_PERSON_RIGHT;
         case FIRST_PERSON_LEFT_HAND -> TRANSFORM_FIRST_PERSON_LEFT;
         default -> ItemTransform.NO_TRANSFORM;
      };
   }

   public static void onModelBake() {
      cache.invalidateAll();
      guiCache.invalidateAll();
   }

   /**
    * Fill the layer's tint colours so biome-tinted source blocks (grass, leaves) keep their BASE colour in the
    * item render -- without them every remapped tint index resolved to nothing and the icon drew grey. Uses the
    * world-less colour (BlockTintSource.color / getColor with no level), i.e. the same default green a grass
    * block item shows in the inventory.
    */
   private static void fillTintLayers(LayerRenderState layer, net.minecraft.world.level.block.state.BlockState state) {
      //? if >= 26.1 {
      it.unimi.dsi.fastutil.ints.IntList tints = layer.tintLayers();
      for (int i = 0; i < PlugBakerFacade.FACADE_TINT_LIST_SIZE; i++) {
         tints.add(defaultTint(state, i));
      }
      //?} else {
      /*int[] tints = layer.prepareTintLayers(PlugBakerFacade.FACADE_TINT_LIST_SIZE);
      for (int i = 0; i < tints.length; i++) {
         tints[i] = defaultTint(state, i);
      }
      *///?}
   }

   private static int defaultTint(net.minecraft.world.level.block.state.BlockState state, int remappedIndex) {
      if (remappedIndex < PlugBakerFacade.FACADE_TINT_BASE) {
         return -1;
      }

      int original = (remappedIndex - PlugBakerFacade.FACADE_TINT_BASE) / Direction.values().length;
      //? if >= 26.1 {
      net.minecraft.client.color.block.BlockTintSource source =
         net.minecraft.client.Minecraft.getInstance().getBlockColors().getTintSource(state, original);
      return source == null ? -1 : 0xFF000000 | source.color(state);
      //?} else {
      /*return 0xFF000000 | net.minecraft.client.Minecraft.getInstance().getBlockColors().getColor(state, null, null, original);
      *///?}
   }

   public void update(
      ItemStackRenderState renderState,
      ItemStack stack,
      ItemModelResolver modelResolver,
      ItemDisplayContext displayContext,
      @Nullable ClientLevel level,
      @Nullable ItemOwner owner,
      int seed
   ) {
      FacadeInstance inst = ItemPluggableFacade.getStates(stack);
      FacadePhasedState state = inst.getCurrentStateForStack();
      List<BakedQuad> quads;
      KeyPlugFacade key;
      if (displayContext == ItemDisplayContext.GUI) {
         key = new KeyPlugFacade("item", Direction.NORTH, state.stateInfo.state, inst.isHollow());
         quads = (List<BakedQuad>)guiCache.getUnchecked(key);
      } else {
         key = new KeyPlugFacade("item", Direction.EAST, state.stateInfo.state, inst.isHollow());
         quads = (List<BakedQuad>)cache.getUnchecked(key);
      }

      if (!quads.isEmpty()) {
         renderState.appendModelIdentityElement(this);
         renderState.appendModelIdentityElement(key);
         LayerRenderState layer = renderState.newLayer();
         layer.prepareQuadList().addAll(quads);
         fillTintLayers(layer, state.stateInfo.state);
         // The GUI icon is the purpose-built flat tile from guiCache (identity transform fills the slot face-on);
         // every world context gets the standard block-item display transform, or the item renders full block size.
         if (displayContext != ItemDisplayContext.GUI) {
            //? if >= 26.1 {
            layer.setItemTransform(transformFor(displayContext));
            //?} else {
            /*layer.setTransform(transformFor(displayContext));
            *///?}
         }
         //? if < 26.1 {
         /*// Pre-26.1 quads carry no per-quad item sheet, so pick the layer's sheet from the source block's own
         // chunk layer -- a translucent block (stained glass, ice) must keep its alpha in hand/on the ground.
         layer.setRenderType(
            net.minecraft.client.renderer.ItemBlockRenderTypes.getChunkRenderType(state.stateInfo.state)
                  == net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT
               ? buildcraft.lib.client.render.BCLibRenderTypes.translucentBlockSheet()
               : buildcraft.lib.client.render.BCLibRenderTypes.cutoutBlockSheet()
         );
         *///?}
      }
   }
}
