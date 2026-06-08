/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LaserContext {
   public final Matrix4f matrix = new Matrix4f();
   private final Vector3f point = new Vector3f();
   private final Vector4f normal = new Vector4f();
   private final ILaserRenderer renderer;
   public final double length;
   private final boolean useNormalColour;
   private final boolean drawBothSides;
   private final int minBlockLight;
   private int index = 0;
   private final double[] x = new double[]{0.0, 0.0, 0.0, 0.0};
   private final double[] y = new double[]{0.0, 0.0, 0.0, 0.0};
   private final double[] z = new double[]{0.0, 0.0, 0.0, 0.0};
   private final double[] u = new double[]{0.0, 0.0, 0.0, 0.0};
   private final double[] v = new double[]{0.0, 0.0, 0.0, 0.0};
   private final int[] l = new int[]{0, 0, 0, 0};
   private final float[] n = new float[]{0.0F, 1.0F, 0.0F};
   private float diffuse;

   public LaserContext(ILaserRenderer renderer, LaserData_BC8 data, boolean useNormalColour, boolean drawBothSides) {
      this.renderer = renderer;
      this.useNormalColour = useNormalColour;
      this.drawBothSides = drawBothSides;
      this.minBlockLight = data.minBlockLight;
      Vec3 delta = data.start.subtract(data.end);
      double dx = delta.x;
      double dy = delta.y;
      double dz = delta.z;
      double realLength = delta.length();
      this.length = realLength / data.scale;
      double angleZ = Math.PI - Math.atan2(dz, dx);
      double rl_squared = realLength * realLength;
      double dy_dy = dy * dy;
      double angleY;
      if (dx == 0.0 && dz == 0.0) {
         double angle = Math.PI / 2;
         if (dy < 0.0) {
            angleY = Math.PI / 2;
         } else {
            angleY = -Math.PI / 2;
         }
      } else {
         dx = Math.sqrt(rl_squared - dy_dy);
         angleY = -Math.atan2(dy, dx);
      }

      this.matrix.identity();
      this.matrix.translate((float)data.start.x, (float)data.start.y, (float)data.start.z);
      this.matrix.scale((float)data.scale);
      this.matrix.rotateY((float)angleZ);
      this.matrix.rotateZ((float)angleY);
   }

   public void setFaceNormal(double nx, double ny, double nz) {
      if (this.useNormalColour) {
         this.normal.set((float)nx, (float)ny, (float)nz, 0.0F);
         this.matrix.transform(this.normal);
         float len = (float)Math.sqrt(this.normal.x * this.normal.x + this.normal.y * this.normal.y + this.normal.z * this.normal.z);
         if (len > 0.0F) {
            this.n[0] = this.normal.x / len;
            this.n[1] = this.normal.y / len;
            this.n[2] = this.normal.z / len;
         } else {
            this.n[0] = 0.0F;
            this.n[1] = 1.0F;
            this.n[2] = 0.0F;
         }

         this.diffuse = diffuseLight(this.n[0], this.n[1], this.n[2]);
      }
   }

   private static float diffuseLight(float x, float y, float z) {
      boolean up = y >= 0.0F;
      float xx = x * x;
      float yy = y * y;
      float zz = z * z;
      float t = xx + yy + zz;
      if (t == 0.0F) {
         return 1.0F;
      }

      float light = (xx * 0.6F + zz * 0.8F) / t;
      float yyt = yy / t;
      if (!up) {
         yyt *= 0.5F;
      }

      return light + yyt;
   }

   public void addPoint(double xIn, double yIn, double zIn, double uIn, double vIn) {
      this.point.set((float)xIn, (float)yIn, (float)zIn);
      this.matrix.transformPosition(this.point);
      int lmap = BcLaserRenderer.computeLightmap(this.point.x, this.point.y, this.point.z, this.minBlockLight);
      this.x[this.index] = this.point.x;
      this.y[this.index] = this.point.y;
      this.z[this.index] = this.point.z;
      this.u[this.index] = uIn;
      this.v[this.index] = vIn;
      this.l[this.index] = lmap;
      this.index++;
      if (this.index == 4) {
         this.index = 0;
         this.vertex(0);
         this.vertex(1);
         this.vertex(2);
         this.vertex(3);
         if (this.drawBothSides) {
            this.n[0] = -this.n[0];
            this.n[1] = -this.n[1];
            this.n[2] = -this.n[2];
            this.diffuse = diffuseLight(this.n[0], this.n[1], this.n[2]);
            this.vertex(3);
            this.vertex(2);
            this.vertex(1);
            this.vertex(0);
         }

         this.n[0] = 0.0F;
         this.n[1] = 1.0F;
         this.n[2] = 0.0F;
      }
   }

   private void vertex(int i) {
      if (this.useNormalColour) {
         this.renderer.vertex(this.x[i], this.y[i], this.z[i], this.u[i], this.v[i], this.l[i], this.n[0], this.n[1], this.n[2], this.diffuse);
      } else {
         this.renderer.vertex(this.x[i], this.y[i], this.z[i], this.u[i], this.v[i], this.l[i], 0.0F, 1.0F, 0.0F, 1.0F);
      }
   }
}
