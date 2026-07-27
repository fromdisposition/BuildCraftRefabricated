/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.VecUtil;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;
import buildcraft.transport.wire.WireManager;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class PipeWireRenderer {
   private static final Map<EnumWirePart, MutableQuad[]> partQuads = new EnumMap<>(EnumWirePart.class);
   private static final Map<EnumWireBetween, MutableQuad[]> betweenQuads = new EnumMap<>(EnumWireBetween.class);
   private static final Map<DyeColor, SpriteHolderRegistry.SpriteHolder> wireSprites = new EnumMap<>(DyeColor.class);
   private static final Map<Long, MutableQuad[]> bakedWireQuads = new ConcurrentHashMap<>();
   private static final ThreadLocal<MutableQuad> RENDER_SCRATCH = ThreadLocal.withInitial(MutableQuad::new);
   private static final int BETWEEN_KEY_OFFSET = 1000;

   public static void clearCaches() {
      bakedWireQuads.clear();
   }

   private static int func(AxisDirection dir) {
      return dir == AxisDirection.POSITIVE ? 1 : 0;
   }

   private static MutableQuad[] buildPartQuads(EnumWirePart part) {
      MutableQuad[] quads = new MutableQuad[6];
      Vector3f center = new Vector3f(0.5F + part.x.getStep() * 4.51F / 16.0F, 0.5F + part.y.getStep() * 4.51F / 16.0F, 0.5F + part.z.getStep() * 4.51F / 16.0F);
      Vector3f radius = new Vector3f(0.03125F, 0.03125F, 0.03125F);
      ModelUtil.UvFaceData uvs = new ModelUtil.UvFaceData();
      int off = func(part.x) * 4 + func(part.y) * 2 + func(part.z);
      uvs.minU = off / 16.0F;
      uvs.maxU = (off + 1) / 16.0F;
      uvs.minV = 0.0F;
      uvs.maxV = 0.0625F;

      for (Direction face : Direction.values()) {
         quads[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
      }

      return quads;
   }

   private static MutableQuad[] buildBetweenQuads(EnumWireBetween between) {
      MutableQuad[] quads = new MutableQuad[4];
      int i = 0;
      boolean ax = between.mainAxis == Axis.X;
      boolean ay = between.mainAxis == Axis.Y;
      boolean az = between.mainAxis == Axis.Z;
      Vec3 center;
      Vec3 radius;
      if (between.to == null) {
         double cL = 0.21812499F;
         double cU = 0.781875F;
         center = new Vec3(ax ? 0.5 : (between.xy ? cU : cL), ay ? 0.5 : ((ax ? !between.xy : !between.yz) ? cL : cU), az ? 0.5 : (between.yz ? cU : cL));
         double rC = 0.250625F;
         double rN = 0.03125;
         radius = new Vec3(ax ? rC : rN, ay ? rC : rN, az ? rC : rN);
      } else {
         double cL = 0.218125;
         double cU = 0.781875;
         radius = new Vec3(ax ? 0.0934375 : 0.03125, ay ? 0.0934375 : 0.03125, az ? 0.0934375 : 0.03125);
         center = new Vec3(
            ax ? 0.5 + 0.4065625 * between.to.getStepX() : (between.xy ? cU : cL),
            ay ? 0.5 + 0.4065625 * between.to.getStepY() : ((ax ? !between.xy : !between.yz) ? cL : cU),
            az ? 0.5 + 0.4065625 * between.to.getStepZ() : (between.yz ? cU : cL)
         );
      }

      ModelUtil.UvFaceData uvBase = new ModelUtil.UvFaceData();
      uvBase.minU = (float)VecUtil.getValue(center.subtract(radius), between.mainAxis);
      uvBase.maxU = (float)VecUtil.getValue(center.add(radius), between.mainAxis);
      uvBase.minV = 0.0F;
      uvBase.maxV = 0.0625F;
      Vector3f centerFloat = new Vector3f((float)center.x, (float)center.y, (float)center.z);
      Vector3f radiusFloat = new Vector3f((float)radius.x, (float)radius.y, (float)radius.z);

      for (Direction face : Direction.values()) {
         if (face.getAxis() != between.mainAxis) {
            ModelUtil.UvFaceData uvs = new ModelUtil.UvFaceData(uvBase);
            Axis aAxis = between.mainAxis;
            Axis fAxis = face.getAxis();
            boolean fPositive = face.getAxisDirection() == AxisDirection.POSITIVE;
            int rotations = 0;
            boolean swapU = false;
            boolean swapV = false;
            if (aAxis == Axis.X) {
               swapV = fPositive;
            } else if (aAxis == Axis.Y) {
               rotations = 1;
               swapU = fAxis == Axis.X != fPositive;
               swapV = fAxis == Axis.Z;
            } else {
               if (fAxis == Axis.Y) {
                  rotations = 1;
               }

               swapU = face == Direction.DOWN;
               swapV = face != Direction.EAST;
            }

            if (swapU) {
               float t = uvs.minU;
               uvs.minU = uvs.maxU;
               uvs.maxU = t;
            }

            if (swapV) {
               float t = uvs.minV;
               uvs.minV = uvs.maxV;
               uvs.maxV = t;
            }

            MutableQuad quad = ModelUtil.createFace(face, centerFloat, radiusFloat, uvs);
            if (rotations > 0) {
               quad.rotateTextureUp(rotations);
            }

            quads[i++] = quad;
         }
      }

      return quads;
   }

   public static void renderWires(TilePipeHolder pipe, Pose pose, int packedLight, VertexConsumer bb) {
      WireManager wm = pipe.getWireManager();
      if (wm != null && (!wm.parts.isEmpty() || !wm.betweens.isEmpty())) {
         for (Entry<EnumWirePart, DyeColor> entry : wm.parts.entrySet()) {
            EnumWirePart part = entry.getKey();
            DyeColor color = entry.getValue();
            boolean isOn = wm.isPowered(part);
            renderQuads(part, partQuads.get(part), color, isOn, bb, pose, packedLight);
         }

         for (Entry<EnumWireBetween, DyeColor> entry : wm.betweens.entrySet()) {
            EnumWireBetween between = entry.getKey();
            DyeColor color = entry.getValue();
            boolean isOn = wm.isPowered(between.parts[0]) || wm.isPowered(between.parts[1]);
            renderQuads(between, betweenQuads.get(between), color, isOn, bb, pose, packedLight);
         }
      }
   }

   private static void renderQuads(Object geometry, MutableQuad[] templates, DyeColor colour, boolean isOn, VertexConsumer bb, Pose pose, int packedLight) {
      MutableQuad[] baked = resolveBakedQuads(geometry, templates, colour, isOn);
      if (isOn) {
         for (MutableQuad quad : baked) {
            if (quad != null) {
               quad.render(pose, bb);
            }
         }
      } else {
         int blockLight = (packedLight & 65535) >> 4;
         int skyLight = (packedLight >> 16 & 65535) >> 4;
         MutableQuad scratch = RENDER_SCRATCH.get();

         for (MutableQuad template : baked) {
            if (template != null) {
               scratch.copyFrom(template);
               scratch.lighti(blockLight, skyLight);
               scratch.render(pose, bb);
            }
         }
      }
   }

   private static long bakeKey(Object geometry, DyeColor colour, boolean powered) {
      int geoKey;
      if (geometry instanceof EnumWirePart part) {
         geoKey = part.ordinal();
      } else if (geometry instanceof EnumWireBetween between) {
         geoKey = 1000 + between.ordinal();
      } else {
         geoKey = geometry.hashCode();
      }

      return (long)geoKey << 32 | (long)colour.ordinal() << 1 | (powered ? 1L : 0L);
   }

   private static MutableQuad[] resolveBakedQuads(Object geometry, MutableQuad[] templates, DyeColor colour, boolean powered) {
      long key = bakeKey(geometry, colour, powered);
      MutableQuad[] cached = bakedWireQuads.get(key);
      if (cached != null) {
         return cached;
      }

      SpriteHolderRegistry.SpriteHolder holder = wireSprites.get(colour);
      if (holder == null) {
         return templates;
      }

      TextureAtlasSprite sprite = holder.getSprite();
      if (sprite == null) {
         return templates;
      }

      MutableQuad[] baked = bakeWireQuads(templates, sprite, powered);
      bakedWireQuads.put(key, baked);
      return baked;
   }

   private static MutableQuad[] bakeWireQuads(MutableQuad[] templates, TextureAtlasSprite sprite, boolean powered) {
      float vOffset = powered ? 0.9375F : 0.0F;
      MutableQuad[] baked = new MutableQuad[templates.length];

      for (int i = 0; i < templates.length; i++) {
         MutableQuad source = templates[i];
         if (source != null) {
            MutableQuad quad = new MutableQuad(source);
            quad.vertex_0.tex_v += vOffset;
            quad.vertex_1.tex_v += vOffset;
            quad.vertex_2.tex_v += vOffset;
            quad.vertex_3.tex_v += vOffset;
            quad.texFromSprite(sprite);
            if (powered) {
               quad.colourf(1.0F, 1.0F, 1.0F, 1.0F);
               quad.lighti(15, 15);
            } else if (quad.getFace() != Direction.UP) {
               float shade = 1.0F - quad.getCalculatedDiffuse();
               shade = 1.0F - shade;
               quad.colourf(shade, shade, shade, 1.0F);
            } else {
               quad.colourf(1.0F, 1.0F, 1.0F, 1.0F);
            }

            baked[i] = quad;
         }
      }

      return baked;
   }

   static {
      for (DyeColor color : DyeColor.values()) {
         wireSprites.put(color, SpriteHolderRegistry.getHolder("buildcrafttransport:wires/" + color.getName()));
      }

      for (EnumWirePart part : EnumWirePart.VALUES) {
         partQuads.put(part, buildPartQuads(part));
      }

      for (EnumWireBetween part : EnumWireBetween.VALUES) {
         betweenQuads.put(part, buildBetweenQuads(part));
      }
   }
}
