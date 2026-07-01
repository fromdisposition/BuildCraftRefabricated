/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import buildcraft.api.core.render.ISprite;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MutableVertex {
   public float position_x;
   public float position_y;
   public float position_z;
   public float normal_x;
   public float normal_y;
   public float normal_z;
   public short colour_r;
   public short colour_g;
   public short colour_b;
   public short colour_a;
   public float tex_u;
   public float tex_v;
   public byte light_block;
   public byte light_sky;

   public MutableVertex() {
      this.normal_x = 0.0F;
      this.normal_y = 1.0F;
      this.normal_z = 0.0F;
      this.colour_r = 255;
      this.colour_g = 255;
      this.colour_b = 255;
      this.colour_a = 255;
   }

   public MutableVertex(MutableVertex from) {
      this.copyFrom(from);
   }

   @Override
   public String toString() {
      return "{ pos = [ "
         + this.position_x
         + ", "
         + this.position_y
         + ", "
         + this.position_z
         + " ], norm = [ "
         + this.normal_x
         + ", "
         + this.normal_y
         + ", "
         + this.normal_z
         + " ], colour = [ "
         + this.colour_r
         + ", "
         + this.colour_g
         + ", "
         + this.colour_b
         + ", "
         + this.colour_a
         + " ], tex = [ "
         + this.tex_u
         + ", "
         + this.tex_v
         + " ], light_block = "
         + this.light_block
         + ", light_sky = "
         + this.light_sky
         + " }";
   }

   public MutableVertex copyFrom(MutableVertex from) {
      this.position_x = from.position_x;
      this.position_y = from.position_y;
      this.position_z = from.position_z;
      this.normal_x = from.normal_x;
      this.normal_y = from.normal_y;
      this.normal_z = from.normal_z;
      this.colour_r = from.colour_r;
      this.colour_g = from.colour_g;
      this.colour_b = from.colour_b;
      this.colour_a = from.colour_a;
      this.tex_u = from.tex_u;
      this.tex_v = from.tex_v;
      this.light_block = from.light_block;
      this.light_sky = from.light_sky;
      return this;
   }

   public void toBakedBlock(int[] data, int offset) {
      data[offset + 0] = Float.floatToRawIntBits(this.position_x);
      data[offset + 1] = Float.floatToRawIntBits(this.position_y);
      data[offset + 2] = Float.floatToRawIntBits(this.position_z);
      data[offset + 3] = this.colourRGBA();
      data[offset + 4] = Float.floatToRawIntBits(this.tex_u);
      data[offset + 5] = Float.floatToRawIntBits(this.tex_v);
      data[offset + 6] = this.lightc();
      data[offset + 7] = this.normalToPackedInt();
   }

   public void toBakedItem(int[] data, int offset) {
      this.toBakedBlock(data, offset);
   }

   public void fromBakedBlock(int[] data, int offset) {
      this.position_x = Float.intBitsToFloat(data[offset + 0]);
      this.position_y = Float.intBitsToFloat(data[offset + 1]);
      this.position_z = Float.intBitsToFloat(data[offset + 2]);
      this.colouri(data[offset + 3]);
      this.tex_u = Float.intBitsToFloat(data[offset + 4]);
      this.tex_v = Float.intBitsToFloat(data[offset + 5]);
      this.lighti(data[offset + 6]);
      this.normali(data[offset + 7]);
   }

   public void fromBakedItem(int[] data, int offset) {
      this.fromBakedBlock(data, offset);
   }

   public void render(VertexConsumer bb) {
      this.renderAsBlock(bb);
   }

   public void renderAsBlock(VertexConsumer bb) {
      bb.addVertex(this.position_x, this.position_y, this.position_z)
         .setColor(this.colour_r, this.colour_g, this.colour_b, this.colour_a)
         .setUv(this.tex_u, this.tex_v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(this.light_block << 4, this.light_sky << 4)
         .setNormal(this.normal_x, this.normal_y, this.normal_z);
   }

   public void renderPosition(VertexConsumer bb) {
      bb.addVertex(this.position_x, this.position_y, this.position_z);
   }

   public void renderNormal(VertexConsumer bb) {
      bb.setNormal(this.normal_x, this.normal_y, this.normal_z);
   }

   public void renderColour(VertexConsumer bb) {
      bb.setColor(this.colour_r, this.colour_g, this.colour_b, this.colour_a);
   }

   public void renderTex(VertexConsumer bb) {
      bb.setUv(this.tex_u, this.tex_v);
   }

   public void renderTex(VertexConsumer bb, ISprite sprite) {
      bb.setUv((float)sprite.getInterpU(this.tex_u), (float)sprite.getInterpV(this.tex_v));
   }

   public void renderLightMap(VertexConsumer bb) {
      bb.setUv2(this.light_block << 4, this.light_sky << 4);
   }

   public MutableVertex positionv(Vector3f vec) {
      return this.positionf(vec.x, vec.y, vec.z);
   }

   public MutableVertex positiond(double x, double y, double z) {
      return this.positionf((float)x, (float)y, (float)z);
   }

   public MutableVertex positionf(float x, float y, float z) {
      this.position_x = x;
      this.position_y = y;
      this.position_z = z;
      return this;
   }

   public Vector3f positionvf() {
      return new Vector3f(this.position_x, this.position_y, this.position_z);
   }

   public MutableVertex normalv(Vector3f vec) {
      return this.normalf(vec.x, vec.y, vec.z);
   }

   public MutableVertex normalf(float x, float y, float z) {
      this.normal_x = x;
      this.normal_y = y;
      this.normal_z = z;
      return this;
   }

   public MutableVertex normali(int combined) {
      this.normal_x = (byte)(combined & 0xFF) / 127.0F;
      this.normal_y = (byte)(combined >> 8 & 0xFF) / 127.0F;
      this.normal_z = (byte)(combined >> 16 & 0xFF) / 127.0F;
      return this;
   }

   public MutableVertex invertNormal() {
      return this.normalf(-this.normal_x, -this.normal_y, -this.normal_z);
   }

   public Vector3f normal() {
      return new Vector3f(this.normal_x, this.normal_y, this.normal_z);
   }

   public int normalToPackedInt() {
      return normalAsByte(this.normal_x, 0) | normalAsByte(this.normal_y, 8) | normalAsByte(this.normal_z, 16);
   }

   private static int normalAsByte(float norm, int offset) {
      int as = (byte)(Mth.clamp(norm, -1.0F, 1.0F) * 127.0F);
      return (as & 0xFF) << offset;
   }

   public MutableVertex colourv(Vector4f vec) {
      return this.colourf(vec.x, vec.y, vec.z, vec.w);
   }

   public MutableVertex colourf(float r, float g, float b, float a) {
      return this.colouri((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F), (int)(a * 255.0F));
   }

   public MutableVertex colouri(int rgba) {
      return this.colouri(rgba, rgba >> 8, rgba >> 16, rgba >>> 24);
   }

   public MutableVertex colouri(int r, int g, int b, int a) {
      this.colour_r = (short)(r & 0xFF);
      this.colour_g = (short)(g & 0xFF);
      this.colour_b = (short)(b & 0xFF);
      this.colour_a = (short)(a & 0xFF);
      return this;
   }

   public Vector4f colourv() {
      return new Vector4f(this.colour_r / 255.0F, this.colour_g / 255.0F, this.colour_b / 255.0F, this.colour_a / 255.0F);
   }

   public int colourRGBA() {
      int rgba = 0;
      rgba |= (this.colour_r & 255) << 0;
      rgba |= (this.colour_g & 255) << 8;
      rgba |= (this.colour_b & 255) << 16;
      return rgba | (this.colour_a & 0xFF) << 24;
   }

   public int colourABGR() {
      int rgba = 0;
      rgba |= (this.colour_r & 255) << 24;
      rgba |= (this.colour_g & 255) << 16;
      rgba |= (this.colour_b & 255) << 8;
      return rgba | (this.colour_a & 0xFF) << 0;
   }

   public MutableVertex multColourd(double d) {
      int m = (int)(d * 255.0);
      return this.multColouri(m);
   }

   public MutableVertex multColourd(double r, double g, double b, double a) {
      return this.multColouri((int)(r * 255.0), (int)(g * 255.0), (int)(b * 255.0), (int)(a * 255.0));
   }

   public MutableVertex multColouri(int by) {
      return this.multColouri(by, by, by, 255);
   }

   public MutableVertex multColouri(int r, int g, int b, int a) {
      this.colour_r = (short)(this.colour_r * r / 255);
      this.colour_g = (short)(this.colour_g * g / 255);
      this.colour_b = (short)(this.colour_b * b / 255);
      this.colour_a = (short)(this.colour_a * a / 255);
      return this;
   }

   public MutableVertex multShade() {
      return this.multColourd(MutableQuad.diffuseLight(this.normal_x, this.normal_y, this.normal_z));
   }

   public MutableVertex texFromSprite(TextureAtlasSprite sprite) {
      this.tex_u = sprite.getU(this.tex_u);
      this.tex_v = sprite.getV(this.tex_v);
      return this;
   }

   public MutableVertex texv(Vector2f vec) {
      return this.texf(vec.x, vec.y);
   }

   public MutableVertex texf(float u, float v) {
      this.tex_u = u;
      this.tex_v = v;
      return this;
   }

   public Vector2f tex() {
      return new Vector2f(this.tex_u, this.tex_v);
   }

   public MutableVertex lightv(Vector2f vec) {
      return this.lightf(vec.x, vec.y);
   }

   public MutableVertex lightf(float block, float sky) {
      return this.lighti((int)(block * 15.0F), (int)(sky * 15.0F));
   }

   public MutableVertex lighti(int combined) {
      return this.lighti(combined >> 4 & 15, combined >> 20 & 15);
   }

   public MutableVertex lighti(int block, int sky) {
      this.light_block = (byte)block;
      this.light_sky = (byte)sky;
      return this;
   }

   public MutableVertex maxLighti(int block, int sky) {
      return this.lighti(Math.max(block, this.light_block), Math.max(sky, this.light_sky));
   }

   public Vector2f lightvf() {
      return new Vector2f(this.light_block * 15.0F, this.light_sky * 15.0F);
   }

   public int lightc() {
      return this.light_block << 4 | this.light_sky << 20;
   }

   public int[] lighti() {
      return new int[]{this.light_block, this.light_sky};
   }

   public MutableVertex transform(Matrix4f matrix) {
      Vector3f point = this.positionvf();
      matrix.transformPosition(point);
      this.positionv(point);
      Vector3f normal = this.normal();
      matrix.transformDirection(normal);
      this.normalv(normal);
      return this;
   }

   public MutableVertex translatei(int x, int y, int z) {
      this.position_x += x;
      this.position_y += y;
      this.position_z += z;
      return this;
   }

   public MutableVertex translatef(float x, float y, float z) {
      this.position_x += x;
      this.position_y += y;
      this.position_z += z;
      return this;
   }

   public MutableVertex translated(double x, double y, double z) {
      this.position_x += (float)x;
      this.position_y += (float)y;
      this.position_z += (float)z;
      return this;
   }

   public MutableVertex translatevi(Vec3i vec) {
      return this.translatei(vec.getX(), vec.getY(), vec.getZ());
   }

   public MutableVertex translatevd(Vec3 vec) {
      return this.translated(vec.x, vec.y, vec.z);
   }

   public MutableVertex scalef(float scale) {
      this.position_x *= scale;
      this.position_y *= scale;
      this.position_z *= scale;
      return this;
   }

   public MutableVertex scaled(double scale) {
      return this.scalef((float)scale);
   }

   public MutableVertex scalef(float x, float y, float z) {
      this.position_x *= x;
      this.position_y *= y;
      this.position_z *= z;
      return this;
   }

   public MutableVertex scaled(double x, double y, double z) {
      return this.scalef((float)x, (float)y, (float)z);
   }

   public void rotateX(float angle) {
      float cos = Mth.cos(angle);
      float sin = Mth.sin(angle);
      this.rotateDirectlyX(cos, sin);
   }

   public void rotateY(float angle) {
      float cos = Mth.cos(angle);
      float sin = Mth.sin(angle);
      this.rotateDirectlyY(cos, sin);
   }

   public void rotateZ(float angle) {
      float cos = Mth.cos(angle);
      float sin = Mth.sin(angle);
      this.rotateDirectlyZ(cos, sin);
   }

   public void rotateDirectlyX(float cos, float sin) {
      float y = this.position_y;
      float z = this.position_z;
      this.position_y = y * cos - z * sin;
      this.position_z = y * sin + z * cos;
   }

   public void rotateDirectlyY(float cos, float sin) {
      float x = this.position_x;
      float z = this.position_z;
      this.position_x = x * cos - z * sin;
      this.position_z = x * sin + z * cos;
   }

   public void rotateDirectlyZ(float cos, float sin) {
      float x = this.position_x;
      float y = this.position_y;
      this.position_x = x * cos + y * sin;
      this.position_y = x * -sin + y * cos;
   }

   public MutableVertex rotateX_90(float scale) {
      float ym = scale;
      float zm = -ym;
      float t = this.position_y * ym;
      this.position_y = this.position_z * zm;
      this.position_z = t;
      t = this.normal_y * ym;
      this.normal_y = this.normal_z * zm;
      this.normal_z = t;
      return this;
   }

   public MutableVertex rotateY_90(float scale) {
      float xm = scale;
      float zm = -xm;
      float t = this.position_x * xm;
      this.position_x = this.position_z * zm;
      this.position_z = t;
      t = this.normal_x * xm;
      this.normal_x = this.normal_z * zm;
      this.normal_z = t;
      return this;
   }

   public MutableVertex rotateZ_90(float scale) {
      float xm = scale;
      float ym = -xm;
      float t = this.position_x * xm;
      this.position_x = this.position_y * ym;
      this.position_y = t;
      t = this.normal_x * xm;
      this.normal_x = this.normal_y * ym;
      this.normal_y = t;
      return this;
   }

   public MutableVertex rotateX_180() {
      this.position_y = -this.position_y;
      this.position_z = -this.position_z;
      this.normal_y = -this.normal_y;
      this.normal_z = -this.normal_z;
      return this;
   }

   public MutableVertex rotateY_180() {
      this.position_x = -this.position_x;
      this.position_z = -this.position_z;
      this.normal_x = -this.normal_x;
      this.normal_z = -this.normal_z;
      return this;
   }

   public MutableVertex rotateZ_180() {
      this.position_x = -this.position_x;
      this.position_y = -this.position_y;
      this.normal_x = -this.normal_x;
      this.normal_y = -this.normal_y;
      return this;
   }
}
