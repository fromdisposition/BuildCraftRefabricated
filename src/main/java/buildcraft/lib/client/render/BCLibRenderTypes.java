/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

//? if >= 26.1 {
import com.mojang.blaze3d.pipeline.DepthStencilState;
//?}
//? if >= 1.21.10 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import net.minecraft.client.renderer.RenderPipelines;
//?}
//? if < 1.21.10 {
/*import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
*///?}
import java.util.function.Function;
import net.minecraft.client.renderer.Sheets;
//? if >= 1.21.11 {
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
//?} else {
/*import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.rendertype.RenderType;
*///?}
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class BCLibRenderTypes {
   //? if >= 1.21.10 {
   public static final RenderPipeline LED_PIPELINE = RenderPipeline.builder(new Snippet[]{RenderPipelines.DEBUG_FILLED_SNIPPET})
      .withLocation(Identifier.fromNamespaceAndPath("buildcraftlib", "pipeline/led"))
      //? if >= 26.1 {
      .withDepthStencilState(DepthStencilState.DEFAULT)
      //?}
      .build();
   //?}

   //? if >= 1.21.11 {
   private static final RenderType LED = RenderType.create(
      "buildcraft:led", RenderSetup.builder(LED_PIPELINE).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup()
   );
   private static final RenderType DEBUG_SOLID = RenderType.create("buildcraft:debug_solid", RenderSetup.builder(LED_PIPELINE).createRenderSetup());
   private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(
      texture -> RenderType.create(
         "buildcraft:entity_translucent_cull",
         //? if >= 26.1 {
         RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
         //?} else {
         /*RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
         *///?}
            .withTexture("Sampler0", texture)
            .useLightmap()
            .useOverlay()
            .affectsCrumbling()
            .sortOnUpload()
            .setOutline(OutlineProperty.NONE)
            .createRenderSetup()
      )
   );
   //?} else if >= 1.21.10 {
   /*// 1.21.10 uses the legacy RenderType.create(name, bufferSize[, crumbling, sort], pipeline, CompositeState) API;
   // RenderSetup / RenderTypes / the rendertype.* package do not exist yet there.
   private static final RenderType LED = RenderType.create(
      "buildcraft:led", 1536, LED_PIPELINE,
      RenderType.CompositeState.builder().setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING).createCompositeState(false)
   );
   private static final RenderType DEBUG_SOLID = RenderType.create(
      "buildcraft:debug_solid", 1536, LED_PIPELINE, RenderType.CompositeState.builder().createCompositeState(false)
   );
   private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(
      texture -> RenderType.create(
         "buildcraft:entity_translucent_cull", 1536, true, true, RenderPipelines.ENTITY_TRANSLUCENT,
         RenderType.CompositeState.builder()
            .setTextureState(new RenderStateShard.TextureStateShard(texture, false))
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setOverlayState(RenderStateShard.OVERLAY)
            .createCompositeState(false)
      )
   );
   *///?} else {
   /*// 1.21.1 predates RenderPipeline: build render types the classic way (VertexFormat + ShaderStateShard).
   // LED/DEBUG_SOLID mirror vanilla debug_filled_box (POSITION_COLOR, position-color shader, translucent);
   // ENTITY_TRANSLUCENT_CULL maps straight to the vanilla helper.
   // BuildCraft's RenderPartCube emits 4 vertices per face in quad winding, so these use QUADS mode.
   // (Vanilla debug_filled_box is TRIANGLE_STRIP with a special vertex order; copying that mode here turned
   // the LED cubes into a single connected triangle strip — the long green/red smear between LEDs.)
   private static final RenderType LED = RenderType.create(
      "buildcraft:led", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, true,
      RenderType.CompositeState.builder()
         .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
         // POLYGON_OFFSET_LAYERING (glPolygonOffset) biases depth toward the camera independent of distance,
         // the standard fix for a quad coplanar with the machine face. VIEW_OFFSET_Z_LAYERING was too weak
         // and z-fought; this removes the need for the old positional epsilon nudge in LedRenderUtil.
         .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
         .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
         .createCompositeState(false)
   );
   private static final RenderType DEBUG_SOLID = RenderType.create(
      "buildcraft:debug_solid", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, true,
      RenderType.CompositeState.builder()
         .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
         .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
         .createCompositeState(false)
   );
   private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(RenderType::entityTranslucentCull);
   *///?}

   public static RenderType led() {
      return LED;
   }

   public static RenderType debugFilled() {
      //? if >= 1.21.11 {
      return RenderTypes.debugFilledBox();
      //?} else {
      /*return RenderType.debugFilledBox();
      *///?}
   }

   public static RenderType debugSolid() {
      return DEBUG_SOLID;
   }

   public static RenderType entityTranslucentCull(Identifier texture) {
      return ENTITY_TRANSLUCENT_CULL.apply(texture);
   }

   public static RenderType entityCutout(Identifier texture) {
      //? if >= 1.21.11 {
      return RenderTypes.entityCutout(texture);
      //?} else {
      /*return RenderType.entityCutout(texture);
      *///?}
   }

   public static RenderType entityTranslucent(Identifier texture) {
      //? if >= 1.21.11 {
      return RenderTypes.entityTranslucent(texture);
      //?} else {
      /*return RenderType.entityTranslucent(texture);
      *///?}
   }

   public static RenderType entitySolid(Identifier texture) {
      //? if >= 1.21.11 {
      return RenderTypes.entitySolid(texture);
      //?} else {
      /*return RenderType.entitySolid(texture);
      *///?}
   }

   public static RenderType lines() {
      //? if >= 1.21.11 {
      return RenderTypes.lines();
      //?} else {
      /*return RenderType.lines();
      *///?}
   }

   public static RenderType entityTranslucentEmissive(Identifier texture) {
      //? if >= 1.21.11 {
      return RenderTypes.entityTranslucentEmissive(texture);
      //?} else {
      /*return RenderType.entityTranslucentEmissive(texture);
      *///?}
   }

   public static RenderType blockScreenEffect(Identifier texture) {
      //? if >= 1.21.11 {
      return RenderTypes.blockScreenEffect(texture);
      //?} else if >= 1.21.10 {
      /*return RenderType.blockScreenEffect(texture);
      *///?} else {
      /*// 1.21.1 has no blockScreenEffect render type; a textured translucent type stands in (used only by
      // the in-fluid screen overlay, itself gated on 1.21.1).
      return RenderType.entityTranslucent(texture);
      *///?}
   }

   public static RenderType entityCutoutCull(Identifier texture) {
      //? if >= 26.1 {
      return RenderTypes.entityCutoutCull(texture);
      //?} else if >= 1.21.11 {
      /*// 1.21.x entityCutout is already back-face culled; entityCutoutNoCull is the non-culled variant.
      return RenderTypes.entityCutout(texture);
      *///?} else {
      /*return RenderType.entityCutout(texture);
      *///?}
   }

   public static RenderType translucentItemSheet() {
      //? if >= 1.21.11 {
      return Sheets.translucentBlockItemSheet();
      //?} else {
      /*// 1.21.10 has only translucentItemSheet() and it is already bound to the BLOCK atlas
      // (the item/block translucent sheet split happened in 1.21.11).
      return Sheets.translucentItemSheet();
      *///?}
   }

   public static RenderType cutoutBlockSheet() {
      //? if >= 26.2 {
      return Sheets.cutoutBlockItemSheet();
      //?} else {
      /*return Sheets.cutoutBlockSheet();
      *///?}
   }

   public static RenderType translucentBlockSheet() {
      //? if >= 26.2 {
      return Sheets.translucentBlockItemSheet();
      //?} else if >= 26.1 {
      /*return Sheets.translucentBlockSheet();
      *///?} else if >= 1.21.11 {
      /*// 1.21.11 translucentBlockItemSheet() is the BLOCK-atlas translucent type.
      return Sheets.translucentBlockItemSheet();
      *///?} else {
      /*// 1.21.10 only has translucentItemSheet(), already bound to the BLOCK atlas.
      return Sheets.translucentItemSheet();
      *///?}
   }

   private BCLibRenderTypes() {
   }
}
