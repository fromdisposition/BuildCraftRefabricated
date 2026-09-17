/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.texture.BcTextureAtlases;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

public final class EngineModel {
   private static final Identifier CHAMBER = Identifier.fromNamespaceAndPath("buildcraftlib", "block/engine/chamber_base");
   private static final Identifier TRUNK_BLUE = Identifier.fromNamespaceAndPath("buildcraftlib", "block/engine/trunk_blue");
   private static final Identifier TRUNK_GREEN = Identifier.fromNamespaceAndPath("buildcraftlib", "block/engine/trunk_green");
   private static final Identifier TRUNK_YELLOW = Identifier.fromNamespaceAndPath("buildcraftlib", "block/engine/trunk_yellow");
   private static final Identifier TRUNK_RED = Identifier.fromNamespaceAndPath("buildcraftlib", "block/engine/trunk_red");
   private static final Identifier TRUNK_BLACK = Identifier.fromNamespaceAndPath("buildcraftlib", "block/engine/trunk_black");
   private final Identifier back;
   private final Identifier side;
   @Nullable
   private final Identifier front;

   private EngineModel(Identifier back, Identifier side, @Nullable Identifier front) {
      this.back = back;
      this.side = side;
      this.front = front;
   }

   public static EngineModel engine(String back, String side) {
      return new EngineModel(Identifier.parse(back), Identifier.parse(side), null);
   }

   public static EngineModel dynamo(String back, String side, String front) {
      return new EngineModel(Identifier.parse(back), Identifier.parse(side), Identifier.parse(front));
   }

   public MutableQuad[] bake(EnumPowerStage stage, double progress, Direction facing) {
      double size = progress > 0.5 ? (1 - progress) * (16 - 0.01) : progress * (16 - 0.01);
      TextureAtlasSprite back = BcTextureAtlases.getBlockSprite(this.back);
      TextureAtlasSprite side = BcTextureAtlases.getBlockSprite(this.side);
      TextureAtlasSprite trunk = BcTextureAtlases.getBlockSprite(trunkTexture(stage));
      TextureAtlasSprite chamber = BcTextureAtlases.getBlockSprite(CHAMBER);
      List<MutableQuad> quads = new ArrayList<>(22);
      cuboid(quads, 0, 0, 0, 16, 4, 16, 0, back, 0, 0, 16, 16, side, 0, 0, 16, 4);
      if (this.front == null) {
         cuboid(quads, 0, 4 + size, 0, 16, 8 + size, 16, 0, back, 0, 0, 16, 16, side, 0, 0, 16, 4);
      } else {
         TextureAtlasSprite front = BcTextureAtlases.getBlockSprite(this.front);
         cuboid(quads, 2, 4 + size, 2, 14, 8 + size, 14, 0, front, 0, 0, 12, 12, front, 0, 12, 12, 16);
      }
      cuboid(quads, 4, 4, 4, 12, 16, 12, stageLight(stage), trunk, 0, 0, 8, 8, trunk, 8, 0, 16, 12);
      for (Direction face : Direction.values()) {
         if (face.getAxis() != Direction.Axis.Y) {
            quads.add(face(face, 3, 4, 3, 13, 4 + size, 13, 0, chamber, 3, size, 13, 0));
         }
      }
      if (facing != Direction.UP) {
         for (MutableQuad quad : quads) {
            quad.rotate(Direction.UP, facing, 0.5F, 0.5F, 0.5F);
         }
      }
      return quads.toArray(MutableQuad.EMPTY_ARRAY);
   }

   private static void cuboid(
      List<MutableQuad> to,
      double x0,
      double y0,
      double z0,
      double x1,
      double y1,
      double z1,
      int light,
      TextureAtlasSprite capSprite,
      double cu0,
      double cv0,
      double cu1,
      double cv1,
      TextureAtlasSprite sideSprite,
      double su0,
      double sv0,
      double su1,
      double sv1
   ) {
      for (Direction face : Direction.values()) {
         if (face.getAxis() == Direction.Axis.Y) {
            to.add(face(face, x0, y0, z0, x1, y1, z1, light, capSprite, cu0, cv0, cu1, cv1));
         } else {
            to.add(face(face, x0, y0, z0, x1, y1, z1, light, sideSprite, su0, sv0, su1, sv1));
         }
      }
   }

   private static MutableQuad face(
      Direction face,
      double x0,
      double y0,
      double z0,
      double x1,
      double y1,
      double z1,
      int light,
      TextureAtlasSprite sprite,
      double u0,
      double v0,
      double u1,
      double v1
   ) {
      float fx = (float) x0 / 16.0F;
      float fy = (float) y0 / 16.0F;
      float fz = (float) z0 / 16.0F;
      float tx = (float) x1 / 16.0F;
      float ty = (float) y1 / 16.0F;
      float tz = (float) z1 / 16.0F;
      Vector3f radius = new Vector3f(tx - fx, ty - fy, tz - fz);
      radius.mul(0.5F);
      Vector3f center = new Vector3f(fx, fy, fz);
      center.add(radius);
      ModelUtil.UvFaceData uvs = new ModelUtil.UvFaceData((float) (u0 / 16.0), (float) (v0 / 16.0), (float) (u1 / 16.0), (float) (v1 / 16.0));
      MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
      quad.lighti(light, 0);
      quad.colouri(-1);
      quad.texFromSprite(sprite);
      quad.setSprite(sprite);
      quad.setShade(true);
      return quad;
   }

   private static int stageLight(EnumPowerStage stage) {
      return switch (stage) {
         case OVERHEAT, RED -> 10;
         case YELLOW -> 7;
         case GREEN -> 4;
         default -> 0;
      };
   }

   private static Identifier trunkTexture(EnumPowerStage stage) {
      return switch (stage) {
         case BLUE -> TRUNK_BLUE;
         case GREEN -> TRUNK_GREEN;
         case YELLOW -> TRUNK_YELLOW;
         case RED -> TRUNK_RED;
         default -> TRUNK_BLACK;
      };
   }
}
