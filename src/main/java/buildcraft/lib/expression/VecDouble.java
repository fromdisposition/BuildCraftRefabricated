package buildcraft.lib.expression;

public class VecDouble {
   public static final VecDouble ZERO = new VecDouble(0.0, 0.0, 0.0, 0.0);
   public final double a;
   public final double b;
   public final double c;
   public final double d;

   public VecDouble(double a) {
      this(a, 0.0, 0.0, 0.0);
   }

   public VecDouble(double a, double b) {
      this(a, b, 0.0, 0.0);
   }

   public VecDouble(double a, double b, double c) {
      this(a, b, c, 0.0);
   }

   public VecDouble(double a, double b, double c, double d) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
   }

   public VecDouble add(double a_, double b_, double c_, double d_) {
      return new VecDouble(this.a + a_, this.b + b_, this.c + c_, this.d + d_);
   }

   public VecDouble sub(double a_, double b_, double c_, double d_) {
      return new VecDouble(this.a - a_, this.b - b_, this.c - c_, this.d - d_);
   }

   public VecDouble scale(double a_, double b_, double c_, double d_) {
      return new VecDouble(this.a * a_, this.b * b_, this.c * c_, this.d * d_);
   }

   public VecDouble div(double a_, double b_, double c_, double d_) {
      return new VecDouble(this.a / a_, this.b / b_, this.c / c_, this.d / d_);
   }

   public VecDouble add(VecDouble w) {
      return new VecDouble(this.a + w.a, this.b + w.b, this.c + w.c, this.d + w.d);
   }

   public VecDouble sub(VecDouble w) {
      return new VecDouble(this.a - w.a, this.b - w.b, this.c - w.c, this.d - w.d);
   }

   public VecDouble scale(VecDouble w) {
      return new VecDouble(this.a * w.a, this.b * w.b, this.c * w.c, this.d * w.d);
   }

   public VecDouble div(VecDouble w) {
      return new VecDouble(this.a / w.a, this.b / w.b, this.c / w.c, this.d / w.d);
   }

   public VecDouble normalize() {
      double sqrt = Math.sqrt(this.a * this.a + this.b * this.b + this.c * this.c + this.d * this.d);
      return sqrt < 1.0E-4 ? ZERO : new VecDouble(this.a / sqrt, this.b / sqrt, this.c / sqrt, this.d / sqrt);
   }

   public double length() {
      return Math.sqrt(this.a * this.a + this.b * this.b + this.c * this.c + this.d * this.d);
   }

   public double distance(VecDouble to) {
      double da = this.a - to.a;
      double db = this.b - to.b;
      double dc = this.c - to.c;
      double dd = this.d - to.d;
      return Math.sqrt(da * da + db * db + dc * dc + dd * dd);
   }

   public double dotProduct2(VecDouble w) {
      return this.a * w.a + this.b * w.b;
   }

   public double dotProduct3(VecDouble w) {
      return this.a * w.a + this.b * w.b + this.c * w.c;
   }

   public double dotProduct4(VecDouble w) {
      return this.a * w.a + this.b * w.b + this.c * w.c + this.d * w.d;
   }

   public VecDouble crossProduct(VecDouble w) {
      double x = this.b * w.c - this.c * w.b;
      double y = this.c * w.b - this.a * w.c;
      double z = this.a * w.b - this.b * w.a;
      return new VecDouble(x, y, z, 1.0);
   }

   public VecLong roundToLong() {
      return new VecLong(Math.round(this.a), Math.round(this.b), Math.round(this.c), Math.round(this.d));
   }

   public VecLong floorToLong() {
      return new VecLong((long)Math.floor(this.a), (long)Math.floor(this.b), (long)Math.floor(this.c), (long)Math.floor(this.d));
   }

   public VecLong ceilToLong() {
      return new VecLong((long)Math.ceil(this.a), (long)Math.ceil(this.b), (long)Math.ceil(this.c), (long)Math.ceil(this.d));
   }
}
