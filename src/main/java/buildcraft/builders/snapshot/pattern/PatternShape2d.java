/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterAxis;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterHollow;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterRotation;
import buildcraft.lib.misc.PositionUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PatternShape2d extends Pattern implements IFillerPatternShape {
   public PatternShape2d(String tag) {
      super(tag);
   }

   @Override
   public int minParameters() {
      return 3;
   }

   @Override
   public int maxParameters() {
      return 3;
   }

   @Override
   public IStatementParameter createParameter(int index) {
      switch (index) {
         case 0:
            return PatternParameterAxis.Y;
         case 1:
            return PatternParameterHollow.HOLLOW;
         case 2:
            return PatternParameterRotation.NONE;
         default:
            return null;
      }
   }

   @Override
   public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
      PatternParameterAxis axis = getParam(0, params, PatternParameterAxis.Y);
      PatternParameterRotation dir = getParam(2, params, PatternParameterRotation.NONE);
      PositionUtil.PathIterator2d iterator = getIterator(filledTemplate, axis);
      int maxA = axis == PatternParameterAxis.X ? filledTemplate.getMax().getY() : filledTemplate.getMax().getX();
      int maxB = axis == PatternParameterAxis.Z ? filledTemplate.getMax().getY() : filledTemplate.getMax().getZ();
      int normMaxA = maxA;
      int normMaxB = maxB;
      if (dir.rotationCount % 2 == 1) {
         int maxT = maxA;
         maxA = maxB;
         maxB = maxT;
         int max_b = maxB;
         PositionUtil.PathIterator2d old = iterator;
         iterator = (ax, b) -> old.iterate(max_b - b, ax);
      }

      if (dir.rotationCount > 1) {
         PositionUtil.PathIterator2d old = iterator;
         int max_a = maxA;
         int max_b = maxB;
         iterator = (ax, b) -> old.iterate(max_a - ax, max_b - b);
      }

      PatternShape2d.LineList list = new PatternShape2d.LineList(iterator);
      this.genShape(maxA, maxB, list);
      PatternParameterHollow filled = getParam(1, params, PatternParameterHollow.HOLLOW);
      if (filled.filled) {
         int fillA = list.fillInA;
         int fillB = list.fillInB;
         if (fillA != -1 && fillB != -1) {
            maxA = normMaxA;
            maxB = normMaxB;
            if (dir.rotationCount % 2 == 1) {
               int fillT = fillA;
               fillA = maxB - fillB;
               fillB = fillT;
            }

            if (dir.rotationCount > 1) {
               fillA = maxA - fillA;
               fillB = maxB - fillB;
            }

            iterator = getIterator(filledTemplate, axis);
            PatternShape2d.PositionGetter getter = getFillGetter(filledTemplate, axis);
            if (filled.outerFilled) {
               iterator = (ax, b) -> {};
            }

            Set<PatternShape2d.Point> visited = new HashSet<>();
            List<PatternShape2d.Point> open = new ArrayList<>();
            open.add(new PatternShape2d.Point(fillA, fillB));

            while (!open.isEmpty()) {
               List<PatternShape2d.Point> next = new ArrayList<>();

               for (PatternShape2d.Point p : open) {
                  if (p.a >= 0 && p.a <= maxA && p.b >= 0 && p.b <= maxB && visited.add(p) && !getter.isFilled(p.a, p.b)) {
                     iterator.iterate(p.a, p.b);
                     next.add(new PatternShape2d.Point(p.a + 1, p.b));
                     next.add(new PatternShape2d.Point(p.a - 1, p.b));
                     next.add(new PatternShape2d.Point(p.a, p.b + 1));
                     next.add(new PatternShape2d.Point(p.a, p.b - 1));
                  }
               }

               open = next;
            }

            if (filled.outerFilled) {
               iterator = getIterator(filledTemplate, axis);

               for (int a = 0; a <= maxA; a++) {
                  for (int b = 0; b <= maxB; b++) {
                     if (!visited.contains(new PatternShape2d.Point(a, b))) {
                        iterator.iterate(a, b);
                     }
                  }
               }
            }
         }
      }

      return true;
   }

   private static PositionUtil.PathIterator2d getIterator(IFilledTemplate filledTemplate, PatternParameterAxis axis) {
      switch (axis) {
         case X:
            return (y, z) -> filledTemplate.setLineX(0, filledTemplate.getMax().getX(), y, z, true);
         case Y:
            return (x, z) -> filledTemplate.setLineY(x, 0, filledTemplate.getMax().getY(), z, true);
         case Z:
            return (x, y) -> filledTemplate.setLineZ(x, y, 0, filledTemplate.getMax().getZ(), true);
         default:
            throw new IllegalArgumentException("Unknown axis " + axis);
      }
   }

   private static PatternShape2d.PositionGetter getFillGetter(IFilledTemplate filledTemplate, PatternParameterAxis axis) {
      switch (axis) {
         case X:
            return (a, b) -> filledTemplate.get(0, a, b);
         case Y:
            return (a, b) -> filledTemplate.get(a, 0, b);
         case Z:
            return (a, b) -> filledTemplate.get(a, b, 0);
         default:
            throw new IllegalArgumentException("Unknown axis " + axis);
      }
   }

   protected abstract void genShape(int var1, int var2, PatternShape2d.LineList var3);

   public enum ArcType {
      ARC(false, false),
      SEMI_CIRCLE(true, false),
      FULL_CIRCLE(true, true);

      final boolean second;
      final boolean all;

      ArcType(boolean second, boolean all) {
         this.second = second;
         this.all = all;
      }

      public boolean shouldDrawSecondQuadrant() {
         return this.second;
      }

      public boolean shouldDrawAllQuadrants() {
         return this.all;
      }
   }

   public static class LineList {
      private PositionUtil.PathIterator2d iterator;
      private int lastA;
      private int lastB;
      private int fillInA = -1;
      private int fillInB = -1;

      public LineList(PositionUtil.PathIterator2d iterator) {
         this.iterator = iterator;
      }

      public void setFillPoint(int a, int b) {
         this.fillInA = a;
         this.fillInB = b;
      }

      public void moveTo(int a, int b) {
         this.lastA = a;
         this.lastB = b;
      }

      public void lineTo(int a, int b) {
         PositionUtil.forAllOnPath2d(this.lastA, this.lastB, a, b, this.iterator);
         this.moveTo(a, b);
      }

      public void lineFrom(int a, int b) {
         int a2 = this.lastA;
         int b2 = this.lastB;
         this.moveTo(a, b);
         this.lineTo(a2, b2);
         this.moveTo(a, b);
      }

      public void arc(int ca, int cb, double ra, double rb) {
         this.arc(ca, cb, ra, rb, 0, 0, PatternShape2d.ArcType.ARC);
      }

      public void arc(int ca, int cb, double ra, double rb, int da, int db, PatternShape2d.ArcType type) {
         if (ra <= 0.0) {
            throw new IllegalArgumentException("'ra' was less than or equal to 0! (Was " + ra + ")");
         }

         if (rb <= 0.0) {
            throw new IllegalArgumentException("'rb' was less than or equal to 0! (Was " + rb + ")");
         }

         double ra2 = ra * ra;
         double rb2 = rb * rb;
         double sigma = 2.0 * rb2 + ra2 * (1.0 - 2.0 * rb);
         int a = 0;

         for (int b = (int)rb; rb2 * a <= ra2 * b; a++) {
            this.iterator.iterate(ca - a, cb - b);
            if (type.shouldDrawSecondQuadrant()) {
               this.iterator.iterate(ca + a + da, cb - b);
               if (type.shouldDrawAllQuadrants()) {
                  this.iterator.iterate(ca - a, cb + b + db);
                  this.iterator.iterate(ca + a + da, cb + b + db);
               }
            }

            if (sigma >= 0.0) {
               sigma += 4.0 * ra2 * (1 - b);
               b--;
            }

            sigma += rb2 * (4 * a + 6);
         }

         sigma = 2.0 * ra2 + rb2 * (1.0 - 2.0 * ra);
         a = (int)ra;

         for (int b = 0; ra2 * b <= rb2 * a; b++) {
            this.iterator.iterate(ca - a, cb - b);
            if (type.shouldDrawSecondQuadrant()) {
               this.iterator.iterate(ca + a + da, cb - b);
               if (type.shouldDrawAllQuadrants()) {
                  this.iterator.iterate(ca - a, cb + b + db);
                  this.iterator.iterate(ca + a + da, cb + b + db);
               }
            }

            if (sigma >= 0.0) {
               sigma += 4.0 * rb2 * (1 - a);
               a--;
            }

            sigma += ra2 * (4 * b + 6);
         }
      }
   }

   static class Point {
      final int a;
      final int b;

      Point(int a, int b) {
         this.a = a;
         this.b = b;
      }

      @Override
      public int hashCode() {
         int prime = 31;
         int result = 1;
         result = 31 * result + this.a;
         return 31 * result + this.b;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (obj == null) {
            return false;
         }

         if (this.getClass() != obj.getClass()) {
            return false;
         }

         PatternShape2d.Point other = (PatternShape2d.Point)obj;
         return this.a != other.a ? false : this.b == other.b;
      }
   }

   @FunctionalInterface
   interface PositionGetter {
      boolean isFilled(int var1, int var2);
   }
}
