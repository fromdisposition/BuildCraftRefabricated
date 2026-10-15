/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeCollector;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
*///?}

public class TooltipBlueprintPipRenderer extends PictureInPictureRenderer<TooltipBlueprintPipRenderState> {
   private final BlueprintPipRenderer renderer;

   //? if >= 26.2 {
   public TooltipBlueprintPipRenderer() {
      super();
      this.renderer = new BlueprintPipRenderer();
   }
   //?} else {
   /*public TooltipBlueprintPipRenderer(BufferSource bufferSource) {
      super(bufferSource);
      this.renderer = new BlueprintPipRenderer(bufferSource);
   }
   *///?}

   @Override
   public Class<TooltipBlueprintPipRenderState> getRenderStateClass() {
      return TooltipBlueprintPipRenderState.class;
   }

   @Override
   protected String getTextureLabel() {
      return "buildcraft_blueprint_tooltip_preview";
   }

   @Override
   protected float getTranslateY(int height, int guiScale) {
      return height / 2.0F;
   }

   //? if >= 26.2 {
   @Override
   protected void renderToTexture(TooltipBlueprintPipRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      this.renderer.renderToTexture(
         new BlueprintPipRenderState(
            renderState.snapshot(), renderState.x0(), renderState.y0(), renderState.x1(), renderState.y1(), renderState.scale(), renderState.scissorArea()
         ),
         poseStack,
         submitNodeCollector
      );
   }
   //?} else {
   /*@Override
   protected void renderToTexture(TooltipBlueprintPipRenderState renderState, PoseStack poseStack) {
      this.renderer
         .renderToTexture(
            new BlueprintPipRenderState(
               renderState.snapshot(), renderState.x0(), renderState.y0(), renderState.x1(), renderState.y1(), renderState.scale(), renderState.scissorArea()
            ),
            poseStack
         );
   }
   *///?}

   @Override
   public void close() {
      super.close();
      this.renderer.close();
   }
}
