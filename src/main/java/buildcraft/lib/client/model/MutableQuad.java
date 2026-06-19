/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import buildcraft.lib.client.model.quad.BakedColors;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.model.geom.builders.UVPair;
//? if >= 26.1 {
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
//?}
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
//? if >= 26.1 {
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
//?}
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public class MutableQuad {
   public static final MutableQuad[] EMPTY_ARRAY = new MutableQuad[0];
   public final MutableVertex vertex_0 = new MutableVertex();
   public final MutableVertex vertex_1 = new MutableVertex();
   public final MutableVertex vertex_2 = new MutableVertex();
   public final MutableVertex vertex_3 = new MutableVertex();
   private int tintIndex = -1;
   private Direction face = null;
   private boolean shade = false;
   private TextureAtlasSprite sprite = null;
   private int lightEmission = 0;
   private boolean hasAmbientOcclusion = true;
   private static final ThreadLocal<Vector3f> TL_POS_SCRATCH = ThreadLocal.withInitial(Vector3f::new);
   private static final ThreadLocal<Vector3f> TL_NORM_SCRATCH = ThreadLocal.withInitial(Vector3f::new);

   public MutableQuad() {
   }

   public MutableQuad(int tintIndex, Direction face) {
      this(tintIndex, face, false);
   }

   public MutableQuad(int tintIndex, Direction face, boolean shade) {
      this.tintIndex = tintIndex;
      this.face = face;
      this.shade = shade;
   }

   public MutableQuad(MutableQuad from) {
      this.copyFrom(from);
   }

   public MutableQuad copyFrom(MutableQuad from) {
      this.tintIndex = from.tintIndex;
      this.face = from.face;
      this.shade = from.shade;
      this.sprite = from.sprite;
      this.lightEmission = from.lightEmission;
      this.hasAmbientOcclusion = from.hasAmbientOcclusion;
      this.vertex_0.copyFrom(from.vertex_0);
      this.vertex_1.copyFrom(from.vertex_1);
      this.vertex_2.copyFrom(from.vertex_2);
      this.vertex_3.copyFrom(from.vertex_3);
      return this;
   }

   public MutableQuad setTint(int tint) {
      this.tintIndex = tint;
      return this;
   }

   public int getTint() {
      return this.tintIndex;
   }

   public MutableQuad setFace(Direction face) {
      this.face = face;
      return this;
   }

   public Direction getFace() {
      return this.face;
   }

   public void setShade(boolean shade) {
      this.shade = shade;
   }

   public boolean isShade() {
      return this.shade;
   }

   public void setSprite(TextureAtlasSprite sprite) {
      this.sprite = sprite;
   }

   public TextureAtlasSprite getSprite() {
      return this.sprite;
   }

   public void setLightEmission(int lightEmission) {
      this.lightEmission = lightEmission;
   }

   public int getLightEmission() {
      return this.lightEmission;
   }

   public void setAmbientOcclusion(boolean hasAmbientOcclusion) {
      this.hasAmbientOcclusion = hasAmbientOcclusion;
   }

   private static int packArgb(MutableVertex v) {
      return (v.colour_a & 0xFF) << 24 | (v.colour_r & 0xFF) << 16 | (v.colour_g & 0xFF) << 8 | v.colour_b & 0xFF;
   }

   private BakedColors buildBakedColors() {
      return BakedColors.of(packArgb(this.vertex_0), packArgb(this.vertex_1), packArgb(this.vertex_2), packArgb(this.vertex_3));
   }

   public BakedQuad toBakedBlock() {
      //? if >= 26.1 {
      MaterialInfo matInfo = new MaterialInfo(
         this.sprite, ChunkSectionLayer.CUTOUT, Sheets.cutoutBlockItemSheet(), this.tintIndex, this.shade, this.lightEmission
      );
      return new BakedQuad(
         this.vertex_0.positionvf(),
         this.vertex_1.positionvf(),
         this.vertex_2.positionvf(),
         this.vertex_3.positionvf(),
         UVPair.pack(this.vertex_0.tex_u, this.vertex_0.tex_v),
         UVPair.pack(this.vertex_1.tex_u, this.vertex_1.tex_v),
         UVPair.pack(this.vertex_2.tex_u, this.vertex_2.tex_v),
         UVPair.pack(this.vertex_3.tex_u, this.vertex_3.tex_v),
         this.face,
         matInfo
      );
      //?} else if >= 1.21.11 {
      /*// 1.21.11 BakedQuad stores sprite/tint/shade/light directly; the render layer is decided by the
      // model/renderer, not the quad, so cutout and translucent build the same quad here.
      return new BakedQuad(
         this.vertex_0.positionvf(),
         this.vertex_1.positionvf(),
         this.vertex_2.positionvf(),
         this.vertex_3.positionvf(),
         UVPair.pack(this.vertex_0.tex_u, this.vertex_0.tex_v),
         UVPair.pack(this.vertex_1.tex_u, this.vertex_1.tex_v),
         UVPair.pack(this.vertex_2.tex_u, this.vertex_2.tex_v),
         UVPair.pack(this.vertex_3.tex_u, this.vertex_3.tex_v),
         this.tintIndex,
         this.face,
         this.sprite,
         this.shade,
         this.lightEmission
      );
      *///?} else {
      /*// 1.21.10 BakedQuad still uses the legacy int[] vertex array (8 ints/vertex).
      int[] data = new int[32];
      this.vertex_0.toBakedBlock(data, 0);
      this.vertex_1.toBakedBlock(data, 8);
      this.vertex_2.toBakedBlock(data, 16);
      this.vertex_3.toBakedBlock(data, 24);
      return new BakedQuad(data, this.tintIndex, this.face, this.sprite, this.shade, this.lightEmission);
      *///?}
   }

   public BakedQuad toBakedTranslucent() {
      //? if >= 26.1 {
      MaterialInfo matInfo = new MaterialInfo(
         this.sprite, ChunkSectionLayer.TRANSLUCENT, Sheets.translucentBlockItemSheet(), this.tintIndex, this.shade, this.lightEmission
      );
      return new BakedQuad(
         this.vertex_0.positionvf(),
         this.vertex_1.positionvf(),
         this.vertex_2.positionvf(),
         this.vertex_3.positionvf(),
         UVPair.pack(this.vertex_0.tex_u, this.vertex_0.tex_v),
         UVPair.pack(this.vertex_1.tex_u, this.vertex_1.tex_v),
         UVPair.pack(this.vertex_2.tex_u, this.vertex_2.tex_v),
         UVPair.pack(this.vertex_3.tex_u, this.vertex_3.tex_v),
         this.face,
         matInfo
      );
      //?} else if >= 1.21.11 {
      /*return new BakedQuad(
         this.vertex_0.positionvf(),
         this.vertex_1.positionvf(),
         this.vertex_2.positionvf(),
         this.vertex_3.positionvf(),
         UVPair.pack(this.vertex_0.tex_u, this.vertex_0.tex_v),
         UVPair.pack(this.vertex_1.tex_u, this.vertex_1.tex_v),
         UVPair.pack(this.vertex_2.tex_u, this.vertex_2.tex_v),
         UVPair.pack(this.vertex_3.tex_u, this.vertex_3.tex_v),
         this.tintIndex,
         this.face,
         this.sprite,
         this.shade,
         this.lightEmission
      );
      *///?} else {
      /*int[] data = new int[32];
      this.vertex_0.toBakedBlock(data, 0);
      this.vertex_1.toBakedBlock(data, 8);
      this.vertex_2.toBakedBlock(data, 16);
      this.vertex_3.toBakedBlock(data, 24);
      return new BakedQuad(data, this.tintIndex, this.face, this.sprite, this.shade, this.lightEmission);
      *///?}
   }

   public BakedQuad toBakedItem() {
      return this.toBakedBlock();
   }

   public void render(Pose pose, VertexConsumer buffer) {
      renderVertex(pose, buffer, this.vertex_0);
      renderVertex(pose, buffer, this.vertex_1);
      renderVertex(pose, buffer, this.vertex_2);
      renderVertex(pose, buffer, this.vertex_3);
   }

   private static void renderVertex(Pose pose, VertexConsumer buffer, MutableVertex v) {
      Vector3f pos = TL_POS_SCRATCH.get();
      pos.set(v.position_x, v.position_y, v.position_z);
      pose.pose().transformPosition(pos);
      Vector3f norm = TL_NORM_SCRATCH.get();
      norm.set(v.normal_x, v.normal_y, v.normal_z);
      pose.normal().transform(norm);
      buffer.addVertex(pos.x, pos.y, pos.z)
         .setColor(v.colour_r, v.colour_g, v.colour_b, v.colour_a)
         .setUv(v.tex_u, v.tex_v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(v.light_block << 4, v.light_sky << 4)
         .setNormal(norm.x, norm.y, norm.z);
   }

   public MutableQuad fromBakedBlock(BakedQuad quad) {
      this.face = quad.direction();
      //? if >= 26.1 {
      MaterialInfo mat = quad.materialInfo();
      this.tintIndex = mat.tintIndex();
      this.sprite = mat.sprite();
      this.shade = mat.shade();
      this.lightEmission = mat.lightEmission();
      //?} else {
      /*this.tintIndex = quad.tintIndex();
      this.sprite = quad.sprite();
      this.shade = quad.shade();
      this.lightEmission = quad.lightEmission();
      *///?}
      //? if >= 1.21.11 {
      readVertexFromBaked(this.vertex_0, quad.position0(), quad.packedUV0());
      readVertexFromBaked(this.vertex_1, quad.position1(), quad.packedUV1());
      readVertexFromBaked(this.vertex_2, quad.position2(), quad.packedUV2());
      readVertexFromBaked(this.vertex_3, quad.position3(), quad.packedUV3());
      //?} else {
      /*// 1.21.10 BakedQuad exposes the legacy int[] vertex array (8 ints/vertex).
      int[] data = quad.vertices();
      readVertexFromBakedArray(this.vertex_0, data, 0);
      readVertexFromBakedArray(this.vertex_1, data, 8);
      readVertexFromBakedArray(this.vertex_2, data, 16);
      readVertexFromBakedArray(this.vertex_3, data, 24);
      *///?}
      return this;
   }

   private static void readVertexFromBaked(MutableVertex v, Vector3fc pos, long packedUV) {
      v.positionf(pos.x(), pos.y(), pos.z());
      v.texf(Float.intBitsToFloat((int)(packedUV >> 32)), Float.intBitsToFloat((int)packedUV));
   }

   private static void readVertexFromBakedArray(MutableVertex v, int[] data, int o) {
      v.positionf(Float.intBitsToFloat(data[o]), Float.intBitsToFloat(data[o + 1]), Float.intBitsToFloat(data[o + 2]));
      v.texf(Float.intBitsToFloat(data[o + 4]), Float.intBitsToFloat(data[o + 5]));
   }

   public MutableQuad fromBakedItem(BakedQuad quad) {
      return this.fromBakedBlock(quad);
   }

   public void render(VertexConsumer bb) {
      this.vertex_0.render(bb);
      this.vertex_1.render(bb);
      this.vertex_2.render(bb);
      this.vertex_3.render(bb);
   }

   public Vector3f getCalculatedNormal() {
      Vector3f a = new Vector3f(this.vertex_1.positionvf());
      a.sub(this.vertex_0.positionvf());
      Vector3f b = new Vector3f(this.vertex_2.positionvf());
      b.sub(this.vertex_0.positionvf());
      Vector3f c = new Vector3f();
      c.set(a).cross(b);
      return c;
   }

   public void setCalculatedNormal() {
      Vector3f n = this.getCalculatedNormal();
      if (n.lengthSquared() > 1.0E-6F) {
         n.normalize();
      }

      this.normalvf(n);
   }

   public static float diffuseLight(Vector3f normal) {
      return diffuseLight(normal.x, normal.y, normal.z);
   }

   public static float diffuseLight(float x, float y, float z) {
      boolean up = y >= 0.0F;
      float xx = x * x;
      float yy = y * y;
      float zz = z * z;
      float t = xx + yy + zz;
      float light = (xx * 0.6F + zz * 0.8F) / t;
      float yyt = yy / t;
      if (!up) {
         yyt *= 0.5F;
      }

      return light + yyt;
   }

   public float getCalculatedDiffuse() {
      return diffuseLight(this.getCalculatedNormal());
   }

   public void setDiffuse(Vector3f normal) {
      float diffuse = diffuseLight(normal);
      this.colourf(diffuse, diffuse, diffuse, 1.0F);
   }

   public void setCalculatedDiffuse() {
      float ax = this.vertex_1.position_x - this.vertex_0.position_x;
      float ay = this.vertex_1.position_y - this.vertex_0.position_y;
      float az = this.vertex_1.position_z - this.vertex_0.position_z;
      float bx = this.vertex_2.position_x - this.vertex_0.position_x;
      float by = this.vertex_2.position_y - this.vertex_0.position_y;
      float bz = this.vertex_2.position_z - this.vertex_0.position_z;
      float nx = ay * bz - az * by;
      float ny = az * bx - ax * bz;
      float nz = ax * by - ay * bx;
      float diffuse = diffuseLight(nx, ny, nz);
      this.colourf(diffuse, diffuse, diffuse, 1.0F);
   }

   public MutableQuad copyAndInvertNormal() {
      MutableQuad copy = new MutableQuad(this);
      copy.vertex_0.copyFrom(this.vertex_3).invertNormal();
      copy.vertex_1.copyFrom(this.vertex_2).invertNormal();
      copy.vertex_2.copyFrom(this.vertex_1).invertNormal();
      copy.vertex_3.copyFrom(this.vertex_0).invertNormal();
      return copy;
   }

   public MutableQuad rotateTextureUp(int times) {
      switch (times & 3) {
         case 0:
            return this;
         case 1: {
            Vector2f t = this.vertex_0.tex();
            this.vertex_0.texv(this.vertex_1.tex());
            this.vertex_1.texv(this.vertex_2.tex());
            this.vertex_2.texv(this.vertex_3.tex());
            this.vertex_3.texv(t);
            return this;
         }
         case 2:
            Vector2f t0 = this.vertex_0.tex();
            Vector2f t1 = this.vertex_1.tex();
            this.vertex_0.texv(this.vertex_2.tex());
            this.vertex_1.texv(this.vertex_3.tex());
            this.vertex_2.texv(t0);
            this.vertex_3.texv(t1);
            return this;
         case 3: {
            Vector2f t = this.vertex_3.tex();
            this.vertex_3.texv(this.vertex_2.tex());
            this.vertex_2.texv(this.vertex_1.tex());
            this.vertex_1.texv(this.vertex_0.tex());
            this.vertex_0.texv(t);
            return this;
         }
         default:
            throw new IllegalStateException("'times & 3' was not 0, 1, 2 or 3!");
      }
   }

   public MutableQuad normalf(float x, float y, float z) {
      this.vertex_0.normalf(x, y, z);
      this.vertex_1.normalf(x, y, z);
      this.vertex_2.normalf(x, y, z);
      this.vertex_3.normalf(x, y, z);
      return this;
   }

   public MutableQuad normald(double x, double y, double z) {
      return this.normalf((float)x, (float)y, (float)z);
   }

   public MutableQuad normalvf(Vector3f vec) {
      return this.normalf(vec.x, vec.y, vec.z);
   }

   public MutableQuad normalvd(Vec3 vec) {
      return this.normald(vec.x, vec.y, vec.z);
   }

   public Vector3f normalvf() {
      return new Vector3f(this.vertex_0.normal_x, this.vertex_0.normal_y, this.vertex_0.normal_z);
   }

   public Vec3 normalvd() {
      return new Vec3(this.vertex_0.normal_x, this.vertex_0.normal_y, this.vertex_0.normal_z);
   }

   public MutableQuad colouri(int r, int g, int b, int a) {
      this.vertex_0.colouri(r, g, b, a);
      this.vertex_1.colouri(r, g, b, a);
      this.vertex_2.colouri(r, g, b, a);
      this.vertex_3.colouri(r, g, b, a);
      return this;
   }

   public MutableQuad colouri(int rgba) {
      this.vertex_0.colouri(rgba);
      this.vertex_1.colouri(rgba);
      this.vertex_2.colouri(rgba);
      this.vertex_3.colouri(rgba);
      return this;
   }

   public MutableQuad colourf(float r, float g, float b, float a) {
      this.vertex_0.colourf(r, g, b, a);
      this.vertex_1.colourf(r, g, b, a);
      this.vertex_2.colourf(r, g, b, a);
      this.vertex_3.colourf(r, g, b, a);
      return this;
   }

   public MutableQuad colourvf(Vector4f vec) {
      return this.colourf(vec.x, vec.y, vec.z, vec.w);
   }

   public MutableQuad multColourd(double r, double g, double b, double a) {
      this.vertex_0.multColourd(r, g, b, a);
      this.vertex_1.multColourd(r, g, b, a);
      this.vertex_2.multColourd(r, g, b, a);
      this.vertex_3.multColourd(r, g, b, a);
      return this;
   }

   public MutableQuad multColourd(double by) {
      int m = (int)(by * 255.0);
      return this.multColouri(m);
   }

   public MutableQuad multColouri(int by) {
      this.vertex_0.multColouri(by);
      this.vertex_1.multColouri(by);
      this.vertex_2.multColouri(by);
      this.vertex_3.multColouri(by);
      return this;
   }

   public MutableQuad multColouri(int r, int g, int b, int a) {
      r &= 255;
      g &= 255;
      b &= 255;
      a &= 255;
      this.vertex_0.multColouri(r, g, b, a);
      this.vertex_1.multColouri(r, g, b, a);
      this.vertex_2.multColouri(r, g, b, a);
      this.vertex_3.multColouri(r, g, b, a);
      return this;
   }

   public MutableQuad multShade() {
      if (this.isShade()) {
         this.setShade(false);
         this.vertex_0.multShade();
         this.vertex_1.multShade();
         this.vertex_2.multShade();
         this.vertex_3.multShade();
      }

      return this;
   }

   public MutableQuad texFromSprite(TextureAtlasSprite sprite) {
      this.vertex_0.texFromSprite(sprite);
      this.vertex_1.texFromSprite(sprite);
      this.vertex_2.texFromSprite(sprite);
      this.vertex_3.texFromSprite(sprite);
      return this;
   }

   public MutableQuad lighti(int block, int sky) {
      this.vertex_0.lighti(block, sky);
      this.vertex_1.lighti(block, sky);
      this.vertex_2.lighti(block, sky);
      this.vertex_3.lighti(block, sky);
      return this;
   }

   public MutableQuad lighti(int combined) {
      this.vertex_0.lighti(combined);
      this.vertex_1.lighti(combined);
      this.vertex_2.lighti(combined);
      this.vertex_3.lighti(combined);
      return this;
   }

   public MutableQuad lightf(float block, float sky) {
      return this.lighti((int)(block * 15.0F), (int)(sky * 15.0F));
   }

   public MutableQuad lightvf(Vector2f vec) {
      return this.lightf(vec.x, vec.y);
   }

   public MutableQuad maxLighti(int block, int sky) {
      this.vertex_0.maxLighti(block, sky);
      this.vertex_1.maxLighti(block, sky);
      this.vertex_2.maxLighti(block, sky);
      this.vertex_3.maxLighti(block, sky);
      return this;
   }

   public MutableQuad transform(Matrix4f transformation) {
      this.vertex_0.transform(transformation);
      this.vertex_1.transform(transformation);
      this.vertex_2.transform(transformation);
      this.vertex_3.transform(transformation);
      return this;
   }

   public MutableQuad translatei(int x, int y, int z) {
      return this.translatef(x, y, z);
   }

   public MutableQuad translatef(float x, float y, float z) {
      this.vertex_0.translatef(x, y, z);
      this.vertex_1.translatef(x, y, z);
      this.vertex_2.translatef(x, y, z);
      this.vertex_3.translatef(x, y, z);
      return this;
   }

   public MutableQuad translated(double x, double y, double z) {
      return this.translatef((float)x, (float)y, (float)z);
   }

   public MutableQuad translatevi(Vec3i vec) {
      return this.translatei(vec.getX(), vec.getY(), vec.getZ());
   }

   public MutableQuad translatevf(Vector3f vec) {
      return this.translatef(vec.x, vec.y, vec.z);
   }

   public MutableQuad translatevd(Vec3 vec) {
      return this.translated(vec.x, vec.y, vec.z);
   }

   public MutableQuad scalef(float scale) {
      this.vertex_0.scalef(scale);
      this.vertex_1.scalef(scale);
      this.vertex_2.scalef(scale);
      this.vertex_3.scalef(scale);
      return this;
   }

   public MutableQuad scaled(double scale) {
      return this.scalef((float)scale);
   }

   public MutableQuad scalef(float x, float y, float z) {
      this.vertex_0.scalef(x, y, z);
      this.vertex_1.scalef(x, y, z);
      this.vertex_2.scalef(x, y, z);
      this.vertex_3.scalef(x, y, z);
      return this;
   }

   public MutableQuad scaled(double x, double y, double z) {
      return this.scalef((float)x, (float)y, (float)z);
   }

   public void rotateX(float angle) {
      this.vertex_0.rotateX(angle);
      this.vertex_1.rotateX(angle);
      this.vertex_2.rotateX(angle);
      this.vertex_3.rotateX(angle);
   }

   public void rotateY(float angle) {
      this.vertex_0.rotateY(angle);
      this.vertex_1.rotateY(angle);
      this.vertex_2.rotateY(angle);
      this.vertex_3.rotateY(angle);
   }

   public void rotateZ(float angle) {
      this.vertex_0.rotateZ(angle);
      this.vertex_1.rotateZ(angle);
      this.vertex_2.rotateZ(angle);
      this.vertex_3.rotateZ(angle);
   }

   public void rotateDirectlyX(float cos, float sin) {
      this.vertex_0.rotateDirectlyX(cos, sin);
      this.vertex_1.rotateDirectlyX(cos, sin);
      this.vertex_2.rotateDirectlyX(cos, sin);
      this.vertex_3.rotateDirectlyX(cos, sin);
   }

   public void rotateDirectlyY(float cos, float sin) {
      this.vertex_0.rotateDirectlyY(cos, sin);
      this.vertex_1.rotateDirectlyY(cos, sin);
      this.vertex_2.rotateDirectlyY(cos, sin);
      this.vertex_3.rotateDirectlyY(cos, sin);
   }

   public void rotateDirectlyZ(float cos, float sin) {
      this.vertex_0.rotateDirectlyZ(cos, sin);
      this.vertex_1.rotateDirectlyZ(cos, sin);
      this.vertex_2.rotateDirectlyZ(cos, sin);
      this.vertex_3.rotateDirectlyZ(cos, sin);
   }

   public MutableQuad rotate(Direction from, Direction to, float ox, float oy, float oz) {
      if (from == to) {
         return this;
      }

      this.translatef(-ox, -oy, -oz);
      switch (from.getAxis()) {
         case X:
            int mult = from.getStepX();
            switch (to.getAxis()) {
               case X:
                  this.rotateY_180();
                  break;
               case Y:
                  this.rotateZ_90(mult * to.getStepY());
                  break;
               case Z:
                  this.rotateY_90(mult * to.getStepZ());
            }
            break;
         case Y:
            int multY = from.getStepY();
            switch (to.getAxis()) {
               case X:
                  this.rotateZ_90(-multY * to.getStepX());
                  break;
               case Y:
                  this.rotateZ_180();
                  break;
               case Z:
                  this.rotateX_90(multY * to.getStepZ());
            }
            break;
         case Z:
            int multZ = -from.getStepZ();
            switch (to.getAxis()) {
               case X:
                  this.rotateY_90(multZ * to.getStepX());
                  break;
               case Y:
                  this.rotateX_90(multZ * to.getStepY());
                  break;
               case Z:
                  this.rotateY_180();
            }
      }

      this.translatef(ox, oy, oz);
      return this;
   }

   public MutableQuad rotateX_90(float scale) {
      this.vertex_0.rotateX_90(scale);
      this.vertex_1.rotateX_90(scale);
      this.vertex_2.rotateX_90(scale);
      this.vertex_3.rotateX_90(scale);
      return this;
   }

   public MutableQuad rotateY_90(float scale) {
      this.vertex_0.rotateY_90(scale);
      this.vertex_1.rotateY_90(scale);
      this.vertex_2.rotateY_90(scale);
      this.vertex_3.rotateY_90(scale);
      return this;
   }

   public MutableQuad rotateZ_90(float scale) {
      this.vertex_0.rotateZ_90(scale);
      this.vertex_1.rotateZ_90(scale);
      this.vertex_2.rotateZ_90(scale);
      this.vertex_3.rotateZ_90(scale);
      return this;
   }

   public MutableQuad rotateX_180() {
      this.vertex_0.rotateX_180();
      this.vertex_1.rotateX_180();
      this.vertex_2.rotateX_180();
      this.vertex_3.rotateX_180();
      return this;
   }

   public MutableQuad rotateY_180() {
      this.vertex_0.rotateY_180();
      this.vertex_1.rotateY_180();
      this.vertex_2.rotateY_180();
      this.vertex_3.rotateY_180();
      return this;
   }

   public MutableQuad rotateZ_180() {
      this.vertex_0.rotateZ_180();
      this.vertex_1.rotateZ_180();
      this.vertex_2.rotateZ_180();
      this.vertex_3.rotateZ_180();
      return this;
   }

   @Override
   public String toString() {
      return "MutableQuad [vertices=" + this.vToS() + ", tintIndex=" + this.tintIndex + ", face=" + this.face + "]";
   }

   private String vToS() {
      return "[ " + this.vertex_0 + ", " + this.vertex_1 + ", " + this.vertex_2 + ", " + this.vertex_3 + " ]";
   }
}
