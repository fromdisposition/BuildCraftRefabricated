/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public interface IPipeFlowRenderer<F extends PipeFlow> {
   default void render(F flow, double x, double y, double z, float partialTicks, VertexConsumer bufferBuilder, PoseStack poseStack) {
      this.render(flow, x, y, z, partialTicks, bufferBuilder, poseStack.last());
   }

   default void render(F flow, double x, double y, double z, float partialTicks, VertexConsumer bufferBuilder, Pose pose) {
      this.render(flow, x, y, z, partialTicks, bufferBuilder);
   }

   @Deprecated
   default void render(F flow, double x, double y, double z, float partialTicks, VertexConsumer bufferBuilder) {
   }
}
