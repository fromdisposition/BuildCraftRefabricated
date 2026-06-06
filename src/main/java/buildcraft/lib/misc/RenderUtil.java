package buildcraft.lib.misc;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.AABB;

public class RenderUtil {
   public static RenderUtil.AutoTessellator getThreadLocalUnusedTessellator() {
      return new RenderUtil.AutoTessellator();
   }

   public static void drawAABB(AABB box, VertexConsumer bb) {
   }

   public static int swapARGBforABGR(int argb) {
      int a = argb >>> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb >> 0 & 0xFF;
      return a << 24 | b << 16 | g << 8 | r;
   }

   public static class AutoTessellator implements AutoCloseable {
      public Object tessellator;

      @Override
      public void close() {
      }
   }
}
