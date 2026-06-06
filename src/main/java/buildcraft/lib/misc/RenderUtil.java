package buildcraft.lib.misc;

public final class RenderUtil {
   private RenderUtil() {
   }

   public static int swapARGBforABGR(int argb) {
      int a = argb >>> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb >> 0 & 0xFF;
      return a << 24 | b << 16 | g << 8 | r;
   }
}
