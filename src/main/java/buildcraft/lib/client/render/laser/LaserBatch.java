/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

//? if >= 26.1.3 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;*/
//?} else {
import net.minecraft.client.Minecraft;
//?}

public final class LaserBatch {
   private static boolean active;
   //? if >= 26.1.3 {
   /*private static SubmitNodeStorage nodeStorage;*/
   //?}

   private LaserBatch() {
   }

   public static void begin() {
      active = true;
   }

   public static boolean isActive() {
      return active;
   }

   public static void end() {
      if (active) {
         active = false;
         //? if >= 26.1.3 {
         //?} else {
         Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
         //?}
      }
   }

   //? if >= 26.1.3 {
   /*public static void setNodeStorage(SubmitNodeStorage storage) {
      nodeStorage = storage;
   }

   public static void submitGeometry(PoseStack poseStack, RenderType type, SubmitNodeCollector.CustomGeometryRenderer renderer) {
      nodeStorage.submitCustomGeometry(poseStack, type, renderer);
   }*/
   //?}
}
