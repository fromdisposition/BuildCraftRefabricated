/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pluggable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;

public interface IPlugDynamicRenderer<P extends PipePluggable> {
   void render(P var1, double var2, double var4, double var6, float var8, VertexConsumer var9, PoseStack var10);

   /**
    * Rotates the pose around the block centre so geometry authored for the WEST face lands on {@code side} —
    * the shared prelude of every dynamic plug renderer (gate, pulsar). WEST itself is the identity.
    */
   static void rotateToSide(PoseStack ps, Direction side) {
      ps.translate(0.5F, 0.5F, 0.5F);
      switch (side) {
         case EAST:
            ps.mulPose(Axis.YP.rotationDegrees(180.0F));
            break;
         case NORTH:
            ps.mulPose(Axis.YP.rotationDegrees(-90.0F));
            break;
         case SOUTH:
            ps.mulPose(Axis.YP.rotationDegrees(90.0F));
            break;
         case DOWN:
            ps.mulPose(Axis.ZP.rotationDegrees(90.0F));
            break;
         case UP:
            ps.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            break;
         case WEST:
         default:
            break;
      }

      ps.translate(-0.5F, -0.5F, -0.5F);
   }
}
