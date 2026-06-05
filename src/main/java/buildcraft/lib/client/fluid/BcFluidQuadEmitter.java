package buildcraft.lib.client.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import buildcraft.fabric.BCEnergyFluidsFabric;

/** Subdivided fluid quads with 8.0 per-vertex recolor on white heat sprites. */
public final class BcFluidQuadEmitter {
    public static final int SUBDIVISIONS = 16;

    private BcFluidQuadEmitter() {}

    public static void emitHorizontalFace(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            TextureAtlasSprite sprite,
            BCEnergyFluidsFabric.FluidEntry entry,
            float x0, float x1, float z0, float z1, float y,
            float nx, float ny, float nz,
            float r, float g, float b, float a,
            int light, int overlay) {
        if (entry != null) {
            emitSubdividedHorizontal(pose, buffer, sprite, entry, x0, x1, z0, z1, y, nx, ny, nz, light, overlay);
            return;
        }
        emitFlatHorizontal(pose, buffer, sprite, x0, x1, z0, z1, y, nx, ny, nz, r, g, b, a, light, overlay);
    }

    public static void emitVerticalFace(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            TextureAtlasSprite sprite,
            BCEnergyFluidsFabric.FluidEntry entry,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float u0, float v0, float u1, float v1,
            float nx, float ny, float nz,
            float r, float g, float b, float a,
            int light, int overlay) {
        if (entry != null) {
            emitSubdividedQuad(pose, buffer, sprite, entry,
                    x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                    sprite.getU(u0), sprite.getV(v0), sprite.getU(u1), sprite.getV(v0),
                    sprite.getU(u1), sprite.getV(v1), sprite.getU(u0), sprite.getV(v1),
                    nx, ny, nz, light, overlay);
            return;
        }
        putVertex(pose, buffer, x0, y0, z0, sprite.getU(u0), sprite.getV(v0), nx, ny, nz, r, g, b, a, light, overlay);
        putVertex(pose, buffer, x1, y1, z1, sprite.getU(u1), sprite.getV(v0), nx, ny, nz, r, g, b, a, light, overlay);
        putVertex(pose, buffer, x2, y2, z2, sprite.getU(u1), sprite.getV(v1), nx, ny, nz, r, g, b, a, light, overlay);
        putVertex(pose, buffer, x3, y3, z3, sprite.getU(u0), sprite.getV(v1), nx, ny, nz, r, g, b, a, light, overlay);
    }

    private static void emitSubdividedHorizontal(
            PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
            BCEnergyFluidsFabric.FluidEntry entry,
            float x0, float x1, float z0, float z1, float y,
            float nx, float ny, float nz, int light, int overlay) {
        int n = SUBDIVISIONS;
        float du = (sprite.getU1() - sprite.getU0()) / n;
        float dv = (sprite.getV1() - sprite.getV0()) / n;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                float lx0 = x0 + (x1 - x0) * i / n;
                float lx1 = x0 + (x1 - x0) * (i + 1) / n;
                float lz0 = z0 + (z1 - z0) * j / n;
                float lz1 = z0 + (z1 - z0) * (j + 1) / n;
                float su0 = sprite.getU0() + du * i;
                float su1 = sprite.getU0() + du * (i + 1);
                float sv0 = sprite.getV0() + dv * j;
                float sv1 = sprite.getV0() + dv * (j + 1);
                emitColoredVertex(pose, buffer, sprite, entry, lx0, y, lz0, su0, sv0, nx, ny, nz, light, overlay);
                emitColoredVertex(pose, buffer, sprite, entry, lx1, y, lz0, su1, sv0, nx, ny, nz, light, overlay);
                emitColoredVertex(pose, buffer, sprite, entry, lx1, y, lz1, su1, sv1, nx, ny, nz, light, overlay);
                emitColoredVertex(pose, buffer, sprite, entry, lx0, y, lz1, su0, sv1, nx, ny, nz, light, overlay);
            }
        }
    }

    private static void emitSubdividedQuad(
            PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
            BCEnergyFluidsFabric.FluidEntry entry,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float au1, float av1, float au2, float av2, float au3, float av3, float au4, float av4,
            float nx, float ny, float nz, int light, int overlay) {
        int n = SUBDIVISIONS;
        float[][] xs = new float[n + 1][n + 1];
        float[][] ys = new float[n + 1][n + 1];
        float[][] zs = new float[n + 1][n + 1];
        float[][] us = new float[n + 1][n + 1];
        float[][] vs = new float[n + 1][n + 1];
        for (int i = 0; i <= n; i++) {
            float u = i / (float) n;
            for (int j = 0; j <= n; j++) {
                float v = j / (float) n;
                xs[i][j] = bilinear(x0, x1, x2, x3, u, v);
                ys[i][j] = bilinear(y0, y1, y2, y3, u, v);
                zs[i][j] = bilinear(z0, z1, z2, z3, u, v);
                us[i][j] = bilinear(au1, au2, au3, au4, u, v);
                vs[i][j] = bilinear(av1, av2, av3, av4, u, v);
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                emitColoredVertex(pose, buffer, sprite, entry, xs[i][j], ys[i][j], zs[i][j], us[i][j], vs[i][j], nx, ny, nz, light, overlay);
                emitColoredVertex(pose, buffer, sprite, entry, xs[i + 1][j], ys[i + 1][j], zs[i + 1][j], us[i + 1][j], vs[i + 1][j], nx, ny, nz, light, overlay);
                emitColoredVertex(pose, buffer, sprite, entry, xs[i + 1][j + 1], ys[i + 1][j + 1], zs[i + 1][j + 1], us[i + 1][j + 1], vs[i + 1][j + 1], nx, ny, nz, light, overlay);
                emitColoredVertex(pose, buffer, sprite, entry, xs[i][j + 1], ys[i][j + 1], zs[i][j + 1], us[i][j + 1], vs[i][j + 1], nx, ny, nz, light, overlay);
            }
        }
    }

    private static float bilinear(float c0, float c1, float c2, float c3, float u, float v) {
        float top = lerp(c0, c1, u);
        float bot = lerp(c3, c2, u);
        return lerp(top, bot, v);
    }

    private static void emitFlatHorizontal(
            PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
            float x0, float x1, float z0, float z1, float y,
            float nx, float ny, float nz,
            float r, float g, float b, float a, int light, int overlay) {
        putVertex(pose, buffer, x0, y, z0, sprite.getU0(), sprite.getV0(), nx, ny, nz, r, g, b, a, light, overlay);
        putVertex(pose, buffer, x1, y, z0, sprite.getU1(), sprite.getV0(), nx, ny, nz, r, g, b, a, light, overlay);
        putVertex(pose, buffer, x1, y, z1, sprite.getU1(), sprite.getV1(), nx, ny, nz, r, g, b, a, light, overlay);
        putVertex(pose, buffer, x0, y, z1, sprite.getU0(), sprite.getV1(), nx, ny, nz, r, g, b, a, light, overlay);
    }

    private static void emitColoredVertex(
            PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
            BCEnergyFluidsFabric.FluidEntry entry,
            float x, float y, float z, float u, float v,
            float nx, float ny, float nz, int light, int overlay) {
        float nu = BcFluidTintUtil.normalizedU(u, sprite.getU0(), sprite.getU1());
        float nv = BcFluidTintUtil.normalizedV(v, sprite.getV0(), sprite.getV1());
        int packed = BcFluidTintUtil.vertexColorFromTemplate(entry.texLight(), entry.texDark(), entry.heat(), nu, nv);
        float a = ((packed >> 24) & 0xFF) / 255f;
        float r = ((packed >> 16) & 0xFF) / 255f;
        float g = ((packed >> 8) & 0xFF) / 255f;
        float b = (packed & 0xFF) / 255f;
        if (a <= 0f) {
            a = 1f;
        }
        putVertex(pose, buffer, x, y, z, u, v, nx, ny, nz, r, g, b, a, light, overlay);
    }

    private static void putVertex(
            PoseStack.Pose pose, VertexConsumer buffer,
            float x, float y, float z, float u, float v,
            float nx, float ny, float nz,
            float r, float g, float b, float a,
            int light, int overlay) {
        buffer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    public static void emitQuadWithAtlasUv(
            PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
            BCEnergyFluidsFabric.FluidEntry entry,
            float x1, float y1, float z1, float au1, float av1,
            float x2, float y2, float z2, float au2, float av2,
            float x3, float y3, float z3, float au3, float av3,
            float x4, float y4, float z4, float au4, float av4,
            float nx, float ny, float nz,
            float r, float g, float b, float a, int light, int overlay) {
        if (entry != null) {
            emitSubdividedQuad(pose, buffer, sprite, entry,
                    x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
                    au1, av1, au2, av2, au3, av3, au4, av4,
                    nx, ny, nz, light, overlay);
            return;
        }
        putVertex(pose, buffer, x1, y1, z1, au1, av1, nx, ny, nz, r, g, b, a, light, overlay);
        putVertex(pose, buffer, x2, y2, z2, au2, av2, nx, ny, nz, r, g, b, a, light, overlay);
        putVertex(pose, buffer, x3, y3, z3, au3, av3, nx, ny, nz, r, g, b, a, light, overlay);
        putVertex(pose, buffer, x4, y4, z4, au4, av4, nx, ny, nz, r, g, b, a, light, overlay);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static void emitTankQuad(
            PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4,
            float nx, float ny, float nz,
            float r, float g, float b, float a, int light, int overlay) {
        emitQuadWithAtlasUv(pose, buffer, sprite, null,
                x1, y1, z1, posU(sprite, nx, x1, z1), posV(sprite, y1),
                x2, y2, z2, posU(sprite, nx, x2, z2), posV(sprite, y2),
                x3, y3, z3, posU(sprite, nx, x3, z3), posV(sprite, y3),
                x4, y4, z4, posU(sprite, nx, x4, z4), posV(sprite, y4),
                nx, ny, nz, r, g, b, a, light, overlay);
    }

    public static void emitTankHorizontal(
            PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
            float x1, float x2, float z1, float z2, float y,
            float nx, float ny, float nz,
            float r, float g, float b, float a, int light, int overlay) {
        emitQuadWithAtlasUv(pose, buffer, sprite, null,
                x1, y, z1, sprite.getU(x1), sprite.getV(z1),
                x2, y, z1, sprite.getU(x2), sprite.getV(z1),
                x2, y, z2, sprite.getU(x2), sprite.getV(z2),
                x1, y, z2, sprite.getU(x1), sprite.getV(z2),
                nx, ny, nz, r, g, b, a, light, overlay);
    }

    public static float posU(TextureAtlasSprite sprite, float nx, float x, float z) {
        return sprite.getU(nx != 0 ? z : x);
    }

    public static float posV(TextureAtlasSprite sprite, float y) {
        return sprite.getV(1.0f - y);
    }
}
