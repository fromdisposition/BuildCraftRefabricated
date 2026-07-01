/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.plug;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
//? if >= 26.1 {
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
//?} else {
/*import net.minecraft.client.renderer.block.model.BlockModelPart;
*///?}
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;

public enum PlugBakerFacade implements IPluggableStaticBaker<KeyPlugFacade> {
   INSTANCE;

   public static final int FACADE_TINT_BASE = 2;
   public static final int FACADE_TINT_MAX_DATA = 4;
   public static final int FACADE_TINT_LIST_SIZE = 2 + 4 * Direction.values().length;
   private static final int ROT_NONE = 0;
   private static final int ROT_CW90 = 1;
   private static final int ROT_CW180 = 2;
   private static final int ROT_CCW90 = 3;
   private static final int[] ROTATIONS = new int[]{0, 1, 2, 3};
   private static final RandomSource RANDOM = RandomSource.create();

   private static List<BakedQuad> getQuadsFromModel(BlockStateModel model, Direction side) {
      //? if >= 26.1 {
      List<BlockStateModelPart> parts = new ArrayList<>();
      //?} else {
      /*List<BlockModelPart> parts = new ArrayList<>();
      *///?}
      model.collectParts(RANDOM, parts);
      List<BakedQuad> result = new ArrayList<>();

      //? if >= 26.1 {
      for (BlockStateModelPart part : parts) {
      //?} else {
      /*for (BlockModelPart part : parts) {
      *///?}
         if (part instanceof SimpleModelWrapper smw) {
            QuadCollection qc = smw.quads();
            result.addAll(qc.getQuads(side));
         }
      }

      return result;
   }

   private int getVertexIndex(List<Vec3> positions, Axis axis, boolean minOrMax1, boolean minOrMax2) {
      Axis axis1;
      Axis axis2;
      switch (axis) {
         case X:
            axis1 = Axis.Y;
            axis2 = Axis.Z;
            break;
         case Y:
            axis1 = Axis.X;
            axis2 = Axis.Z;
            break;
         case Z:
            axis1 = Axis.X;
            axis2 = Axis.Y;
            break;
         default:
            throw new IllegalArgumentException();
      }

      double min1 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis1)).min().orElse(0.0);
      double min2 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis2)).min().orElse(0.0);
      double max1 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis1)).max().orElse(0.0);
      double max2 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis2)).max().orElse(0.0);
      double center1 = (min1 + max1) / 2.0;
      double center2 = (min2 + max2) / 2.0;
      return positions.indexOf(
         positions.stream()
            .filter(
               pos -> (minOrMax1 ? VecUtil.getValue(pos, axis1) < center1 : VecUtil.getValue(pos, axis1) > center1)
                  && (minOrMax2 ? VecUtil.getValue(pos, axis2) < center2 : VecUtil.getValue(pos, axis2) > center2)
            )
            .findFirst()
            .orElse(positions.get(0))
      );
   }

   private List<MutableQuad> getTransformedQuads(BlockState state, BlockStateModel model, Direction side, Vec3 pos0, Vec3 pos1, Vec3 pos2, Vec3 pos3) {
      return getQuadsFromModel(model, side)
         .stream()
         .map(
            quad -> {
               MutableQuad mutableQuad = new MutableQuad().fromBakedItem(quad);
               boolean positive = side.getAxisDirection() == AxisDirection.POSITIVE;
               Function<Vec3, Vec3> transformPosition = pos -> {
                  switch (side.getAxis()) {
                     case X:
                        return new Vec3(positive ? 1.0 - pos.z : pos.z, pos.y, pos.x);
                     case Y:
                        return new Vec3(pos.x, positive ? 1.0 - pos.z : pos.z, pos.y);
                     case Z:
                        return new Vec3(pos.y, pos.x, positive ? 1.0 - pos.z : pos.z);
                     default:
                        throw new IllegalArgumentException();
                  }
               };
               List<Vec3> poses = Arrays.asList(
                  transformPosition.apply(pos0), transformPosition.apply(pos1), transformPosition.apply(pos2), transformPosition.apply(pos3)
               );
               List<MutableVertex> vertexes = Arrays.asList(mutableQuad.vertex_0, mutableQuad.vertex_1, mutableQuad.vertex_2, mutableQuad.vertex_3);
               List<Vec3> vertexesPoses = vertexes.stream()
                  .map(vertex -> new Vec3(vertex.position_x, vertex.position_y, vertex.position_z))
                  .collect(Collectors.toList());
               double minU = vertexes.stream().mapToDouble(vertex -> vertex.tex_u).min().orElse(0.0);
               double minV = vertexes.stream().mapToDouble(vertex -> vertex.tex_v).min().orElse(0.0);
               double maxU = vertexes.stream().mapToDouble(vertex -> vertex.tex_u).max().orElse(0.0);
               double maxV = vertexes.stream().mapToDouble(vertex -> vertex.tex_v).max().orElse(0.0);
               Stream.of(Pair.of(false, false), Pair.of(false, true), Pair.of(true, true), Pair.of(true, false))
                  .forEach(
                     minOrMaxPair -> {
                        Vec3 newPos = poses.get(this.getVertexIndex(poses, side.getAxis(), (Boolean)minOrMaxPair.getLeft(), (Boolean)minOrMaxPair.getRight()));
                        MutableVertex vertex = vertexes.get(
                           this.getVertexIndex(vertexesPoses, side.getAxis(), (Boolean)minOrMaxPair.getLeft(), (Boolean)minOrMaxPair.getRight())
                        );
                        vertex.positiond(newPos.x, newPos.y, newPos.z);
                        switch (side.getAxis()) {
                           case X:
                              vertex.texf(
                                 (float)(minU + (maxU - minU) * (positive ? 1.0 - newPos.z : newPos.z)), (float)(minV + (maxV - minV) * (1.0 - newPos.y))
                              );
                              break;
                           case Y:
                              vertex.texf((float)(minU + (maxU - minU) * newPos.x), (float)(minV + (maxV - minV) * (positive ? newPos.z : 1.0 - newPos.z)));
                              break;
                           case Z:
                              vertex.texf(
                                 (float)(minU + (maxU - minU) * (positive ? newPos.x : 1.0 - newPos.x)), (float)(minV + (maxV - minV) * (1.0 - newPos.y))
                              );
                        }
                     }
                  );
               return mutableQuad;
            }
         )
         .collect(Collectors.toList());
   }

   private Vec3 rotate(Vec3 vec, int rotation) {
      switch (rotation) {
         case 0:
            return new Vec3(vec.x, vec.y, vec.z);
         case 1:
            return new Vec3(1.0 - vec.y, 1.0 - vec.x, vec.z);
         case 2:
            return new Vec3(1.0 - vec.x, 1.0 - vec.y, vec.z);
         case 3:
            return new Vec3(vec.y, vec.x, vec.z);
         default:
            throw new IllegalArgumentException();
      }
   }

   private void addRotatedQuads(
      List<MutableQuad> quads, BlockState state, BlockStateModel model, Direction side, int rotation, Vec3 pos0, Vec3 pos1, Vec3 pos2, Vec3 pos3
   ) {
      quads.addAll(
         this.getTransformedQuads(
            state, model, side, this.rotate(pos0, rotation), this.rotate(pos1, rotation), this.rotate(pos2, rotation), this.rotate(pos3, rotation)
         )
      );
   }

   public List<MutableQuad> bakeForKey(KeyPlugFacade key) {
      //? if >= 26.1 {
      BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(key.state);
      //?} else {
      /*BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(key.state);
      *///?}
      List<MutableQuad> quads = new ArrayList<>();
      int pS = 2;
      int nS = 16 - pS;
      if (!key.isHollow) {
         quads.addAll(
            this.getTransformedQuads(
               key.state, model, key.side, new Vec3(0.0, 1.0, 0.0), new Vec3(1.0, 1.0, 0.0), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 0.0)
            )
         );
         quads.addAll(
            this.getTransformedQuads(
               key.state,
               model,
               key.side.getOpposite(),
               new Vec3(pS / 16.0, nS / 16.0, nS / 16.0),
               new Vec3(nS / 16.0, nS / 16.0, nS / 16.0),
               new Vec3(nS / 16.0, pS / 16.0, nS / 16.0),
               new Vec3(pS / 16.0, pS / 16.0, nS / 16.0)
            )
         );
      }

      for (int rotation : ROTATIONS) {
         if (key.isHollow) {
            this.addRotatedQuads(
               quads,
               key.state,
               model,
               key.side,
               rotation,
               new Vec3(0.0, rotation % 2 == 0 ? 0.25 : 0.0, 0.0),
               new Vec3(0.25, rotation % 2 == 0 ? 0.25 : 0.0, 0.0),
               new Vec3(0.25, rotation % 2 == 0 ? 1.0 : 0.75, 0.0),
               new Vec3(0.0, rotation % 2 == 0 ? 1.0 : 0.75, 0.0)
            );
         }

         this.addRotatedQuads(
            quads,
            key.state,
            model,
            key.side.getOpposite(),
            rotation,
            new Vec3(0.0, 1.0, 1.0),
            new Vec3(pS / 16.0, nS / 16.0, nS / 16.0),
            new Vec3(pS / 16.0, pS / 16.0, nS / 16.0),
            new Vec3(0.0, 0.0, 1.0)
         );
         if (key.isHollow) {
            this.addRotatedQuads(
               quads,
               key.state,
               model,
               key.side.getOpposite(),
               rotation,
               new Vec3(pS / 16.0, rotation % 2 == 0 ? nS / 16.0 : 0.75, nS / 16.0),
               new Vec3(0.25, rotation % 2 == 0 ? nS / 16.0 : 0.75, nS / 16.0),
               new Vec3(0.25, rotation % 2 == 0 ? 0.25 : pS / 16.0, nS / 16.0),
               new Vec3(pS / 16.0, rotation % 2 == 0 ? 0.25 : pS / 16.0, nS / 16.0)
            );
         }
      }

      if (key.isHollow) {
         for (Direction facing : Direction.values()) {
            if (facing.getAxis() != key.side.getAxis()) {
               boolean positive = key.side.getAxisDirection() == AxisDirection.POSITIVE;
               if (key.side.getAxis() == Axis.Z && facing.getAxis() == Axis.X
                  || key.side.getAxis() == Axis.X && facing.getAxis() == Axis.Y
                  || key.side.getAxis() == Axis.Y && facing.getAxis() == Axis.Z) {
                  quads.addAll(
                     this.getTransformedQuads(
                        key.state,
                        model,
                        facing,
                        new Vec3(positive ? 1.0 : pS / 16.0, 0.25, 0.7501875),
                        new Vec3(positive ? 1.0 : pS / 16.0, 0.75, 0.7501875),
                        new Vec3(positive ? nS / 16.0 : 0.0, 0.75, 0.7501875),
                        new Vec3(positive ? nS / 16.0 : 0.0, 0.25, 0.7501875)
                     )
                  );
               } else {
                  quads.addAll(
                     this.getTransformedQuads(
                        key.state,
                        model,
                        facing,
                        new Vec3(0.25, positive ? 1.0 : pS / 16.0, 0.7501875),
                        new Vec3(0.25, positive ? nS / 16.0 : 0.0, 0.7501875),
                        new Vec3(0.75, positive ? nS / 16.0 : 0.0, 0.7501875),
                        new Vec3(0.75, positive ? 1.0 : pS / 16.0, 0.7501875)
                     )
                  );
               }
            }
         }
      }

      for (MutableQuad quad : quads) {
         int tint = quad.getTint();
         if (tint >= 0) {
            if (tint < 4) {
               quad.setTint(2 + tint * Direction.values().length + key.side.ordinal());
            } else {
               quad.setTint(-1);
            }
         }
      }

      return quads;
   }

   public List<BakedQuad> bake(KeyPlugFacade key) {
      List<MutableQuad> mutableQuads = this.bakeForKey(key);
      List<BakedQuad> baked = new ArrayList<>();

      for (MutableQuad quad : mutableQuads) {
         baked.add(quad.toBakedItem());
      }

      if (!key.isHollow) {
         baked.addAll(createPlugQuads(key.side));
      }

      return baked;
   }

   private static List<BakedQuad> createPlugQuads(Direction side) {
      float x0 = 0.125F;
      float x1 = 0.250625F;
      float y0 = 0.25F;
      float y1 = 0.75F;
      float z0 = 0.25F;
      float z1 = 0.75F;
      TextureAtlasSprite sprite = SpriteUtil.getSprite(Identifier.fromNamespaceAndPath("buildcrafttransport", "pipes/plug"));
      if (sprite == null) {
         sprite = SpriteUtil.missingSprite();
      }

      Vector3f center = new Vector3f((x0 + x1) / 2.0F, (y0 + y1) / 2.0F, (z0 + z1) / 2.0F);
      Vector3f radius = new Vector3f((x1 - x0) / 2.0F, (y1 - y0) / 2.0F, (z1 - z0) / 2.0F);
      AABB box = new AABB(x0, y0, z0, x1, y1, z1);
      List<BakedQuad> result = new ArrayList<>();

      for (Direction face : Direction.values()) {
         ModelUtil.UvFaceData uvs = new ModelUtil.UvFaceData();
         ModelUtil.mapBoxToUvs(box, face, uvs);
         MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
         q.setSprite(sprite);
         q.vertex_0.texFromSprite(sprite);
         q.vertex_1.texFromSprite(sprite);
         q.vertex_2.texFromSprite(sprite);
         q.vertex_3.texFromSprite(sprite);
         q.rotate(Direction.WEST, side, 0.5F, 0.5F, 0.5F);
         q.multShade();
         result.add(q.toBakedBlock());
      }

      return result;
   }
}
