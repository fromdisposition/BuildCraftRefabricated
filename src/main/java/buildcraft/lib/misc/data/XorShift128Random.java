package buildcraft.lib.misc.data;

import java.util.Random;

public class XorShift128Random {
   private static final Random seed = new Random();
   private static final double DOUBLE_UNIT = 1.110223E-16F;
   private final long[] s = new long[2];

   public XorShift128Random() {
      this.s[0] = seed.nextLong();
      this.s[1] = seed.nextLong();
   }

   public long nextLong() {
      long s1 = this.s[0];
      long s0 = this.s[1];
      this.s[0] = s0;
      s1 ^= s1 << 23;
      this.s[1] = (s1 ^ s0 ^ s1 >> 17 ^ s0 >> 26) + s0;
      return this.s[1];
   }

   public int nextInt() {
      return (int)this.nextLong();
   }

   public boolean nextBoolean() {
      return (this.nextLong() & 1L) != 0L;
   }

   public int nextInt(int size) {
      int nl = (int)this.nextLong();
      return nl < 0 ? (nl + Integer.MIN_VALUE) % size : nl % size;
   }

   public double nextDouble() {
      return (this.nextLong() & 9007199254740991L) * 1.110223E-16F;
   }
}
