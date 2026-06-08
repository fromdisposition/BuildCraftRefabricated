/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import net.minecraft.client.Minecraft;

public final class LaserBatch {
   private static boolean active;

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
         Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
      }
   }
}
