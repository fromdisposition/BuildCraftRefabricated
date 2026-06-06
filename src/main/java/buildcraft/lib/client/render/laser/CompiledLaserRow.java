package buildcraft.lib.client.render.laser;

import buildcraft.api.core.render.ISprite;

public class CompiledLaserRow {
   public final LaserData_BC8.LaserRow[] rows;
   private final ISprite[] sprites;
   public final double width;
   public final double height;
   private int currentRowIndex;

   public CompiledLaserRow(LaserData_BC8.LaserRow row) {
      this(new LaserData_BC8.LaserRow[]{row});
   }

   public CompiledLaserRow(LaserData_BC8.LaserRow[] rows) {
      if (rows.length < 1) {
         throw new IllegalArgumentException("Not enough rows!");
      }

      this.rows = rows;
      this.width = rows[0].width;
      this.height = rows[0].height;
      this.sprites = new ISprite[rows.length];

      for (int i = 0; i < rows.length; i++) {
         this.sprites[i] = rows[i].sprite;
      }
   }

   private double texU(double between) {
      ISprite sprite = this.sprites[this.currentRowIndex];
      LaserData_BC8.LaserRow row = this.rows[this.currentRowIndex];
      if (between == 0.0) {
         return sprite.getInterpU(row.uMin);
      }

      if (between == 1.0) {
         return sprite.getInterpU(row.uMax);
      }

      double interp = row.uMin * (1.0 - between) + row.uMax * between;
      return sprite.getInterpU(interp);
   }

   private double texV(double between) {
      ISprite sprite = this.sprites[this.currentRowIndex];
      LaserData_BC8.LaserRow row = this.rows[this.currentRowIndex];
      if (between == 0.0) {
         return sprite.getInterpV(row.vMin);
      }

      if (between == 1.0) {
         return sprite.getInterpV(row.vMax);
      }

      double interp = row.vMin * (1.0 - between) + row.vMax * between;
      return sprite.getInterpV(interp);
   }

   public void bakeStartCap(LaserContext context) {
      this.currentRowIndex = 0;
      double h = this.height / 2.0;
      context.setFaceNormal(-1.0, 0.0, 0.0);
      context.addPoint(0.0, h, h, this.texU(1.0), this.texV(1.0));
      context.addPoint(0.0, h, -h, this.texU(1.0), this.texV(0.0));
      context.addPoint(0.0, -h, -h, this.texU(0.0), this.texV(0.0));
      context.addPoint(0.0, -h, h, this.texU(0.0), this.texV(1.0));
   }

   public void bakeEndCap(LaserContext context) {
      this.currentRowIndex = 0;
      double h = this.height / 2.0;
      context.setFaceNormal(1.0, 0.0, 0.0);
      context.addPoint(context.length, -h, h, this.texU(0.0), this.texV(1.0));
      context.addPoint(context.length, -h, -h, this.texU(0.0), this.texV(0.0));
      context.addPoint(context.length, h, -h, this.texU(1.0), this.texV(0.0));
      context.addPoint(context.length, h, h, this.texU(1.0), this.texV(1.0));
   }

   public void bakeStart(LaserContext context, double length) {
      this.currentRowIndex = 0;
      double h = this.height / 2.0;
      double l = length;
      double i = 1.0 - length / this.width;
      context.setFaceNormal(0.0, 1.0, 0.0);
      context.addPoint(0.0, h, -h, this.texU(i), this.texV(0.0));
      context.addPoint(0.0, h, h, this.texU(i), this.texV(1.0));
      context.addPoint(l, h, h, this.texU(1.0), this.texV(1.0));
      context.addPoint(l, h, -h, this.texU(1.0), this.texV(0.0));
      context.setFaceNormal(0.0, -1.0, 0.0);
      context.addPoint(l, -h, -h, this.texU(1.0), this.texV(0.0));
      context.addPoint(l, -h, h, this.texU(1.0), this.texV(1.0));
      context.addPoint(0.0, -h, h, this.texU(i), this.texV(1.0));
      context.addPoint(0.0, -h, -h, this.texU(i), this.texV(0.0));
      context.setFaceNormal(0.0, 0.0, -1.0);
      context.addPoint(0.0, -h, -h, this.texU(i), this.texV(0.0));
      context.addPoint(0.0, h, -h, this.texU(i), this.texV(1.0));
      context.addPoint(l, h, -h, this.texU(1.0), this.texV(1.0));
      context.addPoint(l, -h, -h, this.texU(1.0), this.texV(0.0));
      context.setFaceNormal(0.0, 0.0, 1.0);
      context.addPoint(l, -h, h, this.texU(1.0), this.texV(0.0));
      context.addPoint(l, h, h, this.texU(1.0), this.texV(1.0));
      context.addPoint(0.0, h, h, this.texU(i), this.texV(1.0));
      context.addPoint(0.0, -h, h, this.texU(i), this.texV(0.0));
   }

   public void bakeEnd(LaserContext context, double length) {
      this.currentRowIndex = 0;
      double h = this.height / 2.0;
      double ls = context.length - length;
      double lb = context.length;
      double i = length / this.width;
      context.setFaceNormal(0.0, 1.0, 0.0);
      context.addPoint(ls, h, -h, this.texU(0.0), this.texV(0.0));
      context.addPoint(ls, h, h, this.texU(0.0), this.texV(1.0));
      context.addPoint(lb, h, h, this.texU(i), this.texV(1.0));
      context.addPoint(lb, h, -h, this.texU(i), this.texV(0.0));
      context.setFaceNormal(0.0, -1.0, 0.0);
      context.addPoint(lb, -h, -h, this.texU(i), this.texV(0.0));
      context.addPoint(lb, -h, h, this.texU(i), this.texV(1.0));
      context.addPoint(ls, -h, h, this.texU(0.0), this.texV(1.0));
      context.addPoint(ls, -h, -h, this.texU(0.0), this.texV(0.0));
      context.setFaceNormal(0.0, 0.0, -1.0);
      context.addPoint(ls, -h, -h, this.texU(0.0), this.texV(0.0));
      context.addPoint(ls, h, -h, this.texU(0.0), this.texV(1.0));
      context.addPoint(lb, h, -h, this.texU(i), this.texV(1.0));
      context.addPoint(lb, -h, -h, this.texU(i), this.texV(0.0));
      context.setFaceNormal(0.0, 0.0, 1.0);
      context.addPoint(lb, -h, h, this.texU(i), this.texV(0.0));
      context.addPoint(lb, h, h, this.texU(i), this.texV(1.0));
      context.addPoint(ls, h, h, this.texU(0.0), this.texV(1.0));
      context.addPoint(ls, -h, h, this.texU(0.0), this.texV(0.0));
   }

   public void bakeFor(LaserContext context, LaserData_BC8.LaserSide side, double startX, int count) {
      double xMin = startX;
      double xMax = startX + this.width;
      double h = this.height / 2.0;

      for (int i = 0; i < count; i++) {
         this.currentRowIndex = i % this.rows.length;
         double ls = xMin;
         double lb = xMax;
         if (side == LaserData_BC8.LaserSide.TOP) {
            context.setFaceNormal(0.0, 1.0, 0.0);
            context.addPoint(ls, h, -h, this.texU(0.0), this.texV(0.0));
            context.addPoint(ls, h, h, this.texU(0.0), this.texV(1.0));
            context.addPoint(lb, h, h, this.texU(1.0), this.texV(1.0));
            context.addPoint(lb, h, -h, this.texU(1.0), this.texV(0.0));
         } else if (side == LaserData_BC8.LaserSide.BOTTOM) {
            context.setFaceNormal(0.0, -1.0, 0.0);
            context.addPoint(lb, -h, -h, this.texU(1.0), this.texV(0.0));
            context.addPoint(lb, -h, h, this.texU(1.0), this.texV(1.0));
            context.addPoint(ls, -h, h, this.texU(0.0), this.texV(1.0));
            context.addPoint(ls, -h, -h, this.texU(0.0), this.texV(0.0));
         } else if (side == LaserData_BC8.LaserSide.LEFT) {
            context.setFaceNormal(0.0, 0.0, -1.0);
            context.addPoint(ls, -h, -h, this.texU(0.0), this.texV(0.0));
            context.addPoint(ls, h, -h, this.texU(0.0), this.texV(1.0));
            context.addPoint(lb, h, -h, this.texU(1.0), this.texV(1.0));
            context.addPoint(lb, -h, -h, this.texU(1.0), this.texV(0.0));
         } else if (side == LaserData_BC8.LaserSide.RIGHT) {
            context.setFaceNormal(0.0, 0.0, 1.0);
            context.addPoint(lb, -h, h, this.texU(1.0), this.texV(0.0));
            context.addPoint(lb, h, h, this.texU(1.0), this.texV(1.0));
            context.addPoint(ls, h, h, this.texU(0.0), this.texV(1.0));
            context.addPoint(ls, -h, h, this.texU(0.0), this.texV(0.0));
         }

         xMin += this.width;
         xMax += this.width;
      }
   }
}
