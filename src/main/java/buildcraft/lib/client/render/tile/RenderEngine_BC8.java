/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public class RenderEngine_BC8 extends BcBlockEntityRenderer<TileEngineBase_BC8, EngineRenderState> {
   private final BiFunction<TileEngineBase_BC8, Float, MutableQuad[]> quadProvider;

   public RenderEngine_BC8(BiFunction<TileEngineBase_BC8, Float, MutableQuad[]> quadProvider) {
      this.quadProvider = quadProvider;
   }

   public EngineRenderState createRenderState() {
      return new EngineRenderState();
   }

   @Override
   protected void extract(TileEngineBase_BC8 engine, EngineRenderState state, float partialTick) {
      state.quads = this.quadProvider.apply(engine, partialTick);
   }

   public void submit(EngineRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      ProfilerFiller _profiler = Profiler.get();
      _profiler.push("buildcraft:engine_submit");

      try {
         MutableQuad[] quads = state.quads;
         if (quads != null && quads.length != 0) {
            poseStack.pushPose();
            int light = state.light;
            BcBerRenderUtil.submit(poseStack, collector,
               //? if >= 26.1.3 {
               /*Sheets.cutoutBlockItemSheet(),*/
               //?} else {
               Sheets.cutoutBlockSheet(),
               //?}
               (pose, buffer) -> {
               for (MutableQuad quad : quads) {
                  quad.setCalculatedDiffuse();
                  quad.lighti(light);
                  quad.render(pose, buffer);
               }
            });
            poseStack.popPose();
         }
      } finally {
         _profiler.pop();
      }
   }
}
