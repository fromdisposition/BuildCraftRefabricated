/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

//? if >= 1.21.10 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
//?}

/**
 * Bridges block-entity geometry into the 1.21.5 submit-pipeline (SubmitNodeCollector.submitCustomGeometry).
 * That pipeline does not exist on 1.21.1 (BERs render immediately via MultiBufferSource), where BC's BER
 * overrides live in versions/1.21.1 and never call this helper — so it degrades to an empty class there.
 */
public final class BcBerRenderUtil {
   private BcBerRenderUtil() {
   }

   //? if >= 1.21.10 {
   public static void submit(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, BiConsumer<Pose, VertexConsumer> draw) {
      collector.submitCustomGeometry(poseStack, renderType, draw::accept);
   }

   public static void submitWithPoseStack(
      PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, BiConsumer<PoseStack, VertexConsumer> draw
   ) {
      collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> draw.accept(copyPoseStack(pose), buffer));
   }

   public static PoseStack copyPoseStack(Pose pose) {
      PoseStack stack = new PoseStack();
      stack.pushPose();
      stack.last().set(pose);
      return stack;
   }
   //?}
}
