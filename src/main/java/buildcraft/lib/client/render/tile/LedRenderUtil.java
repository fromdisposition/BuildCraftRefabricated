/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import buildcraft.lib.client.render.BCLibRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.SubmitNodeCollector;
//?}
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class LedRenderUtil {
   public static final int COLOUR_OFF = -14741477;
   public static final int COLOUR_GREEN_ON = -8921737;
   public static final int COLOUR_RED_ON = -14540067;
   private static final int[] COLOUR_ENERGY = new int[16];

   static {
      for (int i = 0; i < COLOUR_ENERGY.length; i++) {
         int c = i * 64 / COLOUR_ENERGY.length;
         int r = i * 176 / COLOUR_ENERGY.length + 79;
         COLOUR_ENERGY[i] = 0xFF000000 | c << 16 | c << 8 | r;
      }
   }

   public static int energyColour(float fraction) {
      return fraction > 0.01F ? COLOUR_ENERGY[Mth.clamp((int)(fraction * (COLOUR_ENERGY.length - 1)), 0, COLOUR_ENERGY.length - 1)] : COLOUR_OFF;
   }

   public static int stateColour(boolean working, boolean blocked) {
      return working ? COLOUR_GREEN_ON : blocked ? COLOUR_RED_ON : COLOUR_OFF;
   }

   //? if >= 1.21.10 {
   public static void submit(PoseStack poseStack, SubmitNodeCollector collector, RenderPartCube led, Direction skipFace, int colour) {
      BcBerRenderUtil.submit(poseStack, collector, BCLibRenderTypes.led(), (pose, consumer) -> render(led, pose, consumer, skipFace, colour));
   }
   //?}

   public static void render(RenderPartCube led, Pose pose, VertexConsumer consumer, Direction skipFace, int colour) {
      led.center.colouri(colour);
      led.render(pose, consumer, skipFace);
   }

   public static void setFacePosition(RenderPartCube led, Direction face, double insetBlocks, double sideOffset, double y) {
      double ledX;
      double ledZ;
      int dX;
      int dZ;
      if (face.getAxis() == Direction.Axis.X) {
         dX = 0;
         dZ = face.getAxisDirection().getStep();
         ledZ = 0.5;
         ledX = face == Direction.EAST ? 1.0 - insetBlocks : insetBlocks;
      } else {
         dX = -face.getAxisDirection().getStep();
         dZ = 0;
         ledX = 0.5;
         ledZ = face == Direction.SOUTH ? 1.0 - insetBlocks : insetBlocks;
      }

      // Sits exactly on the machine face; the LED render type's layering bias prevents z-fighting, no nudge needed.
      led.center.positiond(ledX + dX * sideOffset, y, ledZ + dZ * sideOffset);
   }

   private LedRenderUtil() {
   }
}
