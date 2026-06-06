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
