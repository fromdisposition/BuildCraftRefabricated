/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.render.laser;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.world.phys.Vec3;

public class LaserContext {
    public final Matrix4f matrix = new Matrix4f();
    private final Vector3f point = new Vector3f();
    private final Vector4f normal = new Vector4f();
    private final ILaserRenderer renderer;
    public final double length;
    private final boolean useNormalColour, drawBothSides;
    private final int minBlockLight;

    public LaserContext(ILaserRenderer renderer, LaserData_BC8 data, boolean useNormalColour, boolean drawBothSides) {
        this.renderer = renderer;
        this.useNormalColour = useNormalColour;
        this.drawBothSides = drawBothSides;
        this.minBlockLight = data.minBlockLight;
        Vec3 delta = data.start.subtract(data.end);
        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;

        final double angleY, angleZ;

        double realLength = delta.length();
        length = realLength / data.scale;
        angleZ = Math.PI - Math.atan2(dz, dx);
        double rl_squared = realLength * realLength;
        double dy_dy = dy * dy;
        if (dx == 0 && dz == 0) {
            final double angle = Math.PI / 2;
            if (dy < 0) {
                angleY = angle;
            } else {
                angleY = -angle;
            }
        } else {
            dx = Math.sqrt(rl_squared - dy_dy);
            angleY = -Math.atan2(dy, dx);
        }

        matrix.identity();

        matrix.translate((float) data.start.x, (float) data.start.y, (float) data.start.z);

        matrix.scale((float) data.scale);

        matrix.rotateY((float) angleZ);

        matrix.rotateZ((float) angleY);
    }

    public void setFaceNormal(double nx, double ny, double nz) {
        if (useNormalColour) {
            normal.set((float) nx, (float) ny, (float) nz, 0);
            matrix.transform(normal);

            float len = (float) Math.sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z);
            if (len > 0) {
                n[0] = normal.x / len;
                n[1] = normal.y / len;
                n[2] = normal.z / len;
            } else {
                n[0] = 0;
                n[1] = 1;
                n[2] = 0;
            }
            diffuse = diffuseLight(n[0], n[1], n[2]);
        }
    }

    private static float diffuseLight(float x, float y, float z) {
        boolean up = y >= 0;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float t = xx + yy + zz;
        if (t == 0) return 1.0f;
        float light = (xx * 0.6f + zz * 0.8f) / t;
        float yyt = yy / t;
        if (!up) yyt *= 0.5f;
        light += yyt;
        return light;
    }

    private int index = 0;
    private final double[] x = { 0, 0, 0, 0 };
    private final double[] y = { 0, 0, 0, 0 };
    private final double[] z = { 0, 0, 0, 0 };
    private final double[] u = { 0, 0, 0, 0 };
    private final double[] v = { 0, 0, 0, 0 };
    private final int[] l = { 0, 0, 0, 0 };
    private final float[] n = { 0, 1, 0 };
    private float diffuse;

    public void addPoint(double xIn, double yIn, double zIn, double uIn, double vIn) {
        point.set((float) xIn, (float) yIn, (float) zIn);
        matrix.transformPosition(point);
        int lmap = LaserRenderer_BC8.computeLightmap(point.x, point.y, point.z, minBlockLight);
        x[index] = point.x;
        y[index] = point.y;
        z[index] = point.z;
        u[index] = uIn;
        v[index] = vIn;
        l[index] = lmap;
        index++;
        if (index == 4) {
            index = 0;
            vertex(0);
            vertex(1);
            vertex(2);
            vertex(3);
            if (drawBothSides) {
                n[0] = -n[0];
                n[1] = -n[1];
                n[2] = -n[2];
                diffuse = diffuseLight(n[0], n[1], n[2]);
                vertex(3);
                vertex(2);
                vertex(1);
                vertex(0);
            }
            n[0] = 0;
            n[1] = 1;
            n[2] = 0;
        }
    }

    private void vertex(int i) {
        if (useNormalColour) {
            renderer.vertex(x[i], y[i], z[i], u[i], v[i], l[i], n[0], n[1], n[2], diffuse);
        } else {
            renderer.vertex(x[i], y[i], z[i], u[i], v[i], l[i], 0, 1, 0, 1);
        }
    }
}
