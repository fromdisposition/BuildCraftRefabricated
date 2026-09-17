/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;

public class JsonQuad {
   public boolean shade = false;
   public int tint = -1;
   public String texture;
   public final JsonVertex[] vertices = new JsonVertex[4];
   public Direction face;

   public JsonQuad(JsonObject obj, float[] from, float[] to, Direction face) {
      this.face = face;
      this.tint = GsonHelper.getAsInt(obj, "tintindex", -1);
      this.texture = GsonHelper.getAsString(obj, "texture");
      int rotation = GsonHelper.getAsInt(obj, "rotation", 0);
      float[] uv = JsonUtil.getSubAsFloatArray(obj, "uv");
      if (uv.length != 4) {
         throw new JsonSyntaxException("Expected exactly 4 floats, but got " + Arrays.toString(uv));
      }

      ModelUtil.UvFaceData uvs = new ModelUtil.UvFaceData();
      uvs.minU = uv[0] / 16.0F;
      uvs.minV = uv[1] / 16.0F;
      uvs.maxU = uv[2] / 16.0F;
      uvs.maxV = uv[3] / 16.0F;
      Vector3f radius = new Vector3f(to[0] - from[0], to[1] - from[1], to[2] - from[2]);
      radius.mul(0.5F);
      Vector3f center = new Vector3f(from[0], from[1], from[2]);
      center.add(radius);
      MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
      quad.rotateTextureUp(rotation);
      this.vertices[0] = new JsonVertex(quad.vertex_0);
      this.vertices[1] = new JsonVertex(quad.vertex_1);
      this.vertices[2] = new JsonVertex(quad.vertex_2);
      this.vertices[3] = new JsonVertex(quad.vertex_3);
   }

   public MutableQuad toQuad(TextureAtlasSprite sprite) {
      MutableQuad quad = new MutableQuad(this.tint, this.face, this.shade);
      this.vertices[0].loadInto(quad.vertex_0);
      this.vertices[1].loadInto(quad.vertex_1);
      this.vertices[2].loadInto(quad.vertex_2);
      this.vertices[3].loadInto(quad.vertex_3);
      if (sprite != null) {
         quad.texFromSprite(sprite);
         quad.setSprite(sprite);
      }

      return quad;
   }
}
