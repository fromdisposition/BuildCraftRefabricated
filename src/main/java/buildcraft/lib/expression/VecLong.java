/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

public class VecLong {
   public static final VecLong ZERO = new VecLong(0L, 0L, 0L, 0L);
   public final long a;
   public final long b;
   public final long c;
   public final long d;

   public VecLong(long a) {
      this(a, 0L, 0L, 0L);
   }

   public VecLong(long a, long b) {
      this(a, b, 0L, 0L);
   }

   public VecLong(long a, long b, long c) {
      this(a, b, c, 0L);
   }

   public VecLong(long a, long b, long c, long d) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
   }

   public VecLong add(long a_, long b_, long c_, long d_) {
      return new VecLong(this.a + a_, this.b + b_, this.c + c_, this.d + d_);
   }

   public VecLong sub(long a_, long b_, long c_, long d_) {
      return new VecLong(this.a - a_, this.b - b_, this.c - c_, this.d - d_);
   }

   public VecLong scale(long a_, long b_, long c_, long d_) {
      return new VecLong(this.a * a_, this.b * b_, this.c * c_, this.d * d_);
   }

   public VecLong div(long a_, long b_, long c_, long d_) {
      return new VecLong(this.a / a_, this.b / b_, this.c / c_, this.d / d_);
   }

   public VecLong add(VecLong w) {
      return new VecLong(this.a + w.a, this.b + w.b, this.c + w.c, this.d + w.d);
   }

   public VecLong sub(VecLong neg) {
      return new VecLong(this.a - neg.a, this.b - neg.b, this.c - neg.c, this.d - neg.d);
   }

   public VecLong scale(VecLong s) {
      return new VecLong(this.a * s.a, this.b * s.b, this.c * s.c, this.d * s.d);
   }

   public VecLong div(VecLong s) {
      return new VecLong(this.a / s.a, this.b / s.b, this.c / s.c, this.d / s.d);
   }

   public long dotProduct2(VecLong w) {
      return this.a * w.a + this.b * w.b;
   }

   public long dotProduct3(VecLong w) {
      return this.a * w.a + this.b * w.b + this.c * w.c;
   }

   public long dotProduct4(VecLong w) {
      return this.a * w.a + this.b * w.b + this.c * w.c + this.d * w.d;
   }

   public double length() {
      return Math.sqrt(this.a * this.a + this.b * this.b + this.c * this.c + this.d * this.d);
   }

   public VecLong crossProduct(VecLong w) {
      long x = this.b * w.c - this.c * w.b;
      long y = this.c * w.b - this.a * w.c;
      long z = this.a * w.b - this.b * w.a;
      return new VecLong(x, y, z, 1L);
   }

   public double distance(VecLong to) {
      long da = this.a - to.a;
      long db = this.b - to.b;
      long dc = this.c - to.c;
      long dd = this.d - to.d;
      return Math.sqrt(da * da + db * db + dc * dc + dd * dd);
   }

   public VecDouble castToDouble() {
      return new VecDouble(this.a, this.b, this.c, this.d);
   }

   @Override
   public String toString() {
      return "{ " + this.a + ", " + this.b + ", " + this.c + ", " + this.d + " }";
   }
}
