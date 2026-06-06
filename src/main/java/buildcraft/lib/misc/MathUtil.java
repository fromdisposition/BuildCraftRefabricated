package buildcraft.lib.misc;

public class MathUtil {
   private static final short HCF_SIZE = 64;
   private static final short[][] HCF_TABLE = new short[64][64];

   private static short findHcfDirect(short a, short b) {
      while (b > 0) {
         short t = b;
         b = (short)(a % b);
         a = t;
      }

      return a;
   }

   public static double interp(double interp, double from, double to) {
      return from * (1.0 - interp) + to * interp;
   }

   public static int clamp(int toClamp, int min, int max) {
      return Math.max(Math.min(toClamp, max), min);
   }

   public static int clamp(double toClamp, int min, int max) {
      return clamp((int)toClamp, min, max);
   }

   public static double clamp(double toClamp, double min, double max) {
      return Math.max(Math.min(toClamp, max), min);
   }

   public static long clamp(long toClamp, long min, long max) {
      return Math.max(Math.min(toClamp, max), min);
   }

   public static int findHighestCommonFactor(int a, int b) {
      if (b > a) {
         int t = b;
         b = a;
         a = t;
      }

      if (a < 64) {
         return HCF_TABLE[a][b];
      }

      while (b > 0) {
         int t = b;
         b = a % b;
         a = t;
      }

      return a;
   }

   public static int findLowestCommonMultiple(int a, int b) {
      return a / findHighestCommonFactor(a, b) * b;
   }

   static {
      short a = 0;

      while (a < 64) {
         HCF_TABLE[a][0] = a++;
      }

      for (short ax = 0; ax < 64; ax++) {
         for (short b = 1; b <= ax; b++) {
            HCF_TABLE[ax][b] = findHcfDirect(ax, b);
         }
      }
   }
}
