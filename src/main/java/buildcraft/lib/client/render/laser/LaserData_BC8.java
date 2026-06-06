package buildcraft.lib.client.render.laser;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import java.util.Objects;
import net.minecraft.world.phys.Vec3;

public class LaserData_BC8 {
   public final LaserData_BC8.LaserType laserType;
   public final Vec3 start;
   public final Vec3 end;
   public final double scale;
   public final boolean enableDiffuse;
   public final boolean doubleFace;
   public final int minBlockLight;
   private final int hash;

   public LaserData_BC8(LaserData_BC8.LaserType laserType, Vec3 start, Vec3 end, double scale) {
      this(laserType, start, end, scale, true, false, 0);
   }

   public LaserData_BC8(LaserData_BC8.LaserType laserType, Vec3 start, Vec3 end, double scale, boolean enableDiffuse, boolean doubleFace, int minBlockLight) {
      this.laserType = laserType;
      this.start = start;
      this.end = end;
      this.scale = scale;
      this.enableDiffuse = enableDiffuse;
      this.doubleFace = doubleFace;
      this.minBlockLight = minBlockLight;
      this.hash = Objects.hash(laserType, start, end, Double.doubleToLongBits(scale), enableDiffuse, doubleFace, minBlockLight);
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (obj.getClass() != this.getClass()) {
         return false;
      } else {
         LaserData_BC8 other = (LaserData_BC8)obj;
         if (this.laserType != other.laserType) {
            return false;
         } else if (!this.start.equals(other.start)) {
            return false;
         } else if (!this.end.equals(other.end)) {
            return false;
         } else if (Double.compare(this.scale, other.scale) != 0) {
            return false;
         } else if (this.enableDiffuse != other.enableDiffuse) {
            return false;
         } else {
            return this.doubleFace != other.doubleFace ? false : this.minBlockLight == other.minBlockLight;
         }
      }
   }

   public static class LaserRow {
      public final ISprite sprite;
      public final double uMin;
      public final double vMin;
      public final double uMax;
      public final double vMax;
      public final int width;
      public final int height;
      public final LaserData_BC8.LaserSide[] validSides;

      public LaserRow(ISprite sprite, int uMin, int vMin, int uMax, int vMax, int textureSize, LaserData_BC8.LaserSide... sides) {
         this.sprite = sprite;
         this.uMin = (double)uMin / textureSize;
         this.vMin = (double)vMin / textureSize;
         this.uMax = (double)uMax / textureSize;
         this.vMax = (double)vMax / textureSize;
         this.width = uMax - uMin;
         this.height = vMax - vMin;
         if (sides != null && sides.length != 0) {
            this.validSides = sides;
         } else {
            this.validSides = LaserData_BC8.LaserSide.VALUES;
         }
      }

      public LaserRow(ISprite sprite, int uMin, int vMin, int uMax, int vMax, LaserData_BC8.LaserSide... sides) {
         this(sprite, uMin, vMin, uMax, vMax, 16, sides);
      }

      public LaserRow(LaserData_BC8.LaserRow from, ISprite sprite) {
         this.sprite = sprite;
         this.uMin = from.uMin;
         this.vMin = from.vMin;
         this.uMax = from.uMax;
         this.vMax = from.vMax;
         this.width = from.width;
         this.height = from.height;
         this.validSides = from.validSides;
      }
   }

   public enum LaserSide {
      TOP,
      BOTTOM,
      LEFT,
      RIGHT;

      public static final LaserData_BC8.LaserSide[] VALUES = values();
   }

   public static class LaserType {
      public final LaserData_BC8.LaserRow capStart;
      public final LaserData_BC8.LaserRow capEnd;
      public final LaserData_BC8.LaserRow start;
      public final LaserData_BC8.LaserRow end;
      public final LaserData_BC8.LaserRow[] variations;

      public LaserType(
         LaserData_BC8.LaserRow capStart,
         LaserData_BC8.LaserRow start,
         LaserData_BC8.LaserRow[] middle,
         LaserData_BC8.LaserRow end,
         LaserData_BC8.LaserRow capEnd
      ) {
         this.capStart = capStart;
         this.start = start;
         this.variations = middle;
         this.end = end;
         this.capEnd = capEnd;
      }

      public LaserType(LaserData_BC8.LaserType from, SpriteHolderRegistry.SpriteHolder replacementSprite) {
         this.capStart = new LaserData_BC8.LaserRow(from.capStart, replacementSprite);
         this.capEnd = new LaserData_BC8.LaserRow(from.capEnd, replacementSprite);
         this.start = new LaserData_BC8.LaserRow(from.start, replacementSprite);
         this.end = new LaserData_BC8.LaserRow(from.end, replacementSprite);
         this.variations = new LaserData_BC8.LaserRow[from.variations.length];

         for (int i = 0; i < this.variations.length; i++) {
            this.variations[i] = new LaserData_BC8.LaserRow(from.variations[i], replacementSprite);
         }
      }
   }
}
