package buildcraft.lib.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class ModelUtil {
   public static MutableQuad createFace(Direction face, Vector3f a, Vector3f b, Vector3f c, Vector3f d, ModelUtil.UvFaceData uvs) {
      MutableQuad quad = new MutableQuad(-1, face);
      if (uvs == null) {
         uvs = ModelUtil.UvFaceData.DEFAULT;
      }

      if (face != null && !shouldInvertForRender(face)) {
         quad.vertex_3.positionv(a).texf(uvs.minU, uvs.minV);
         quad.vertex_2.positionv(b).texf(uvs.minU, uvs.maxV);
         quad.vertex_1.positionv(c).texf(uvs.maxU, uvs.maxV);
         quad.vertex_0.positionv(d).texf(uvs.maxU, uvs.minV);
      } else {
         quad.vertex_0.positionv(a).texf(uvs.minU, uvs.minV);
         quad.vertex_1.positionv(b).texf(uvs.minU, uvs.maxV);
         quad.vertex_2.positionv(c).texf(uvs.maxU, uvs.maxV);
         quad.vertex_3.positionv(d).texf(uvs.maxU, uvs.minV);
      }

      return quad;
   }

   public static MutableQuad createFace(Direction face, Vector3f[] points, ModelUtil.UvFaceData uvs) {
      return createFace(face, points[0], points[1], points[2], points[3], uvs);
   }

   public static MutableQuad createFace(Direction face, Vector3f center, Vector3f radius, ModelUtil.UvFaceData uvs) {
      Vector3f[] points = getPointsForFace(face, center, radius);
      return createFace(face, points, uvs).normalf(face.getStepX(), face.getStepY(), face.getStepZ());
   }

   public static MutableQuad createInverseFace(Direction face, Vector3f center, Vector3f radius, ModelUtil.UvFaceData uvs) {
      return createFace(face, center, radius, uvs).copyAndInvertNormal();
   }

   public static MutableQuad[] createDoubleFace(Direction face, Vector3f center, Vector3f radius, ModelUtil.UvFaceData uvs) {
      MutableQuad norm = createFace(face, center, radius, uvs);
      return new MutableQuad[]{norm, norm.copyAndInvertNormal()};
   }

   public static void mapBoxToUvs(AABB box, Direction side, ModelUtil.UvFaceData uvs) {
      switch (side) {
         case WEST:
            uvs.minU = (float)box.minZ;
            uvs.maxU = (float)box.maxZ;
            uvs.minV = 1.0F - (float)box.maxY;
            uvs.maxV = 1.0F - (float)box.minY;
            return;
         case EAST:
            uvs.minU = 1.0F - (float)box.minZ;
            uvs.maxU = 1.0F - (float)box.maxZ;
            uvs.minV = 1.0F - (float)box.maxY;
            uvs.maxV = 1.0F - (float)box.minY;
            return;
         case DOWN:
            uvs.minU = (float)box.minX;
            uvs.maxU = (float)box.maxX;
            uvs.minV = 1.0F - (float)box.maxZ;
            uvs.maxV = 1.0F - (float)box.minZ;
            return;
         case UP:
            uvs.minU = (float)box.minX;
            uvs.maxU = (float)box.maxX;
            uvs.minV = (float)box.maxZ;
            uvs.maxV = (float)box.minZ;
            return;
         case NORTH:
            uvs.minU = 1.0F - (float)box.minX;
            uvs.maxU = 1.0F - (float)box.maxX;
            uvs.minV = 1.0F - (float)box.maxY;
            uvs.maxV = 1.0F - (float)box.minY;
            return;
         case SOUTH:
            uvs.minU = (float)box.minX;
            uvs.maxU = (float)box.maxX;
            uvs.minV = 1.0F - (float)box.maxY;
            uvs.maxV = 1.0F - (float)box.minY;
            return;
         default:
            throw new IllegalStateException("Unknown Direction " + side);
      }
   }

   public static Vector3f[] getPointsForFace(Direction face, Vector3f center, Vector3f radius) {
      Vector3f centerOfFace = new Vector3f(center);
      Vector3f faceAdd = new Vector3f(face.getStepX() * radius.x, face.getStepY() * radius.y, face.getStepZ() * radius.z);
      centerOfFace.add(faceAdd);
      Vector3f faceRadius = new Vector3f(radius);
      if (face.getAxisDirection() == AxisDirection.POSITIVE) {
         faceRadius.sub(faceAdd);
      } else {
         faceRadius.add(faceAdd);
      }

      return getPoints(centerOfFace, faceRadius);
   }

   public static Vector3f[] getPoints(Vector3f centerFace, Vector3f faceRadius) {
      Vector3f[] array = new Vector3f[]{new Vector3f(centerFace), new Vector3f(centerFace), new Vector3f(centerFace), new Vector3f(centerFace)};
      array[0].add(addOrNegate(faceRadius, false, false));
      array[1].add(addOrNegate(faceRadius, false, true));
      array[2].add(addOrNegate(faceRadius, true, true));
      array[3].add(addOrNegate(faceRadius, true, false));
      return array;
   }

   public static Vector3f addOrNegate(Vector3f coord, boolean u, boolean v) {
      boolean zisv = coord.x != 0.0F && coord.y == 0.0F;
      float x = coord.x * (u ? 1 : -1);
      float y = coord.y * (v ? -1 : 1);
      float z = coord.z * (zisv ? (v ? -1 : 1) : (u ? 1 : -1));
      return new Vector3f(x, y, z);
   }

   public static boolean shouldInvertForRender(Direction face) {
      boolean flip = face.getAxisDirection() == AxisDirection.NEGATIVE;
      if (face.getAxis() == Axis.Z) {
         flip = !flip;
      }

      return flip;
   }

   public static Direction faceForRender(Direction face) {
      return shouldInvertForRender(face) ? face.getOpposite() : face;
   }

   public static class TexturedFace {
      public TextureAtlasSprite sprite;
      public ModelUtil.UvFaceData faceData = new ModelUtil.UvFaceData();
   }

   public static class UvFaceData {
      private static final ModelUtil.UvFaceData DEFAULT = new ModelUtil.UvFaceData(0.0F, 0.0F, 1.0F, 1.0F);
      public float minU;
      public float maxU;
      public float minV;
      public float maxV;

      public UvFaceData() {
      }

      public UvFaceData(ModelUtil.UvFaceData from) {
         this.minU = from.minU;
         this.maxU = from.maxU;
         this.minV = from.minV;
         this.maxV = from.maxV;
      }

      public static ModelUtil.UvFaceData from16(double minU, double minV, double maxU, double maxV) {
         return new ModelUtil.UvFaceData(minU / 16.0, minV / 16.0, maxU / 16.0, maxV / 16.0);
      }

      public static ModelUtil.UvFaceData from16(int minU, int minV, int maxU, int maxV) {
         return new ModelUtil.UvFaceData(minU / 16.0F, minV / 16.0F, maxU / 16.0F, maxV / 16.0F);
      }

      public UvFaceData(float uMin, float vMin, float uMax, float vMax) {
         this.minU = uMin;
         this.maxU = uMax;
         this.minV = vMin;
         this.maxV = vMax;
      }

      public UvFaceData(double minU, double minV, double maxU, double maxV) {
         this((float)minU, (float)minV, (float)maxU, (float)maxV);
      }

      public ModelUtil.UvFaceData andSub(ModelUtil.UvFaceData sub) {
         float size_u = this.maxU - this.minU;
         float size_v = this.maxV - this.minV;
         float min_u = this.minU + sub.minU * size_u;
         float min_v = this.minV + sub.minV * size_v;
         float max_u = this.minU + sub.maxU * size_u;
         float max_v = this.minV + sub.maxV * size_v;
         return new ModelUtil.UvFaceData(min_u, min_v, max_u, max_v);
      }

      public ModelUtil.UvFaceData inParent(ModelUtil.UvFaceData parent) {
         return parent.andSub(this);
      }

      @Override
      public String toString() {
         return "[ " + this.minU * 16.0F + ", " + this.minV * 16.0F + ", " + this.maxU * 16.0F + ", " + this.maxV * 16.0F + " ]";
      }
   }
}
