/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiConsumer;
//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeStorage;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource;
*///?}
import net.minecraft.client.renderer.rendertype.RenderType;

@FunctionalInterface
public interface GeometrySink {
   void submit(PoseStack poseStack, RenderType renderType, BiConsumer<Pose, VertexConsumer> geometry);

   //? if >= 26.2 {
   static GeometrySink of(SubmitNodeStorage storage) {
      return (poseStack, renderType, geometry) -> {
         Pose pose = new Pose();
         pose.set(poseStack.last());
         storage.submitCustomGeometry(poseStack, renderType, (p, vc) -> geometry.accept(pose, vc));
      };
   }
   //?} else {
   /*static GeometrySink of(MultiBufferSource bufferSource) {
      return (poseStack, renderType, geometry) -> geometry.accept(poseStack.last(), bufferSource.getBuffer(renderType));
   }
   *///?}
}
