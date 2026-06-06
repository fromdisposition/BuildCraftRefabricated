package buildcraft.lib.client.render;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class BCLibRenderTypes {
   public static final RenderPipeline LED_PIPELINE = RenderPipeline.builder(new Snippet[]{RenderPipelines.DEBUG_FILLED_SNIPPET})
      .withLocation(Identifier.fromNamespaceAndPath("buildcraftlib", "pipeline/led"))
      .withDepthStencilState(DepthStencilState.DEFAULT)
      .build();
   private static final RenderType LED = RenderType.create(
      "buildcraft:led", RenderSetup.builder(LED_PIPELINE).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup()
   );
   private static final RenderType DEBUG_SOLID = RenderType.create("buildcraft:debug_solid", RenderSetup.builder(LED_PIPELINE).createRenderSetup());
   private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(
      texture -> RenderType.create(
         "buildcraft:entity_translucent_cull",
         RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
            .withTexture("Sampler0", texture)
            .useLightmap()
            .useOverlay()
            .affectsCrumbling()
            .sortOnUpload()
            .setOutline(OutlineProperty.NONE)
            .createRenderSetup()
      )
   );

   public static RenderType led() {
      return LED;
   }

   public static RenderType debugFilled() {
      return RenderTypes.debugFilledBox();
   }

   public static RenderType debugSolid() {
      return DEBUG_SOLID;
   }

   public static RenderType entityTranslucentCull(Identifier texture) {
      return ENTITY_TRANSLUCENT_CULL.apply(texture);
   }

   public static RenderType entityCutout(Identifier texture) {
      return RenderTypes.entityCutout(texture);
   }

   public static RenderType entityTranslucent(Identifier texture) {
      return RenderTypes.entityTranslucent(texture);
   }

   public static RenderType entitySolid(Identifier texture) {
      return RenderTypes.entitySolid(texture);
   }

   public static RenderType lines() {
      return RenderTypes.lines();
   }

   public static RenderType entityCutoutCull(Identifier texture) {
      return RenderTypes.entityCutoutCull(texture);
   }

   public static RenderType translucentItemSheet() {
      return Sheets.translucentBlockItemSheet();
   }

   public static RenderType cutoutBlockSheet() {
      return Sheets.cutoutBlockSheet();
   }

   public static RenderType translucentBlockSheet() {
      return Sheets.translucentBlockSheet();
   }

   private BCLibRenderTypes() {
   }
}
