/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
//? if >= 26.1.3 {
//?} else {
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
//?}
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

public class LaserBoxRenderer {
   private static final double RENDER_SCALE = 0.06230529595015576;
   private static final Map<LaserBoxRenderer.BoxKey, List<LaserData_BC8>> BOX_CACHE = new ConcurrentHashMap<>();

   public static void renderLaserBoxStatic(PoseStack poseStack, Box box, LaserData_BC8.LaserType type, boolean center, Vec3 cameraPos) {
      renderLaserBoxStatic(poseStack, box, type, center, false, cameraPos);
   }

   public static void renderLaserBoxStatic(PoseStack poseStack, Box box, LaserData_BC8.LaserType type, boolean center, boolean enableDiffuse, Vec3 cameraPos) {
      if (box != null && box.min() != null && box.max() != null) {
         if (BOX_CACHE.size() > 256) {
            BOX_CACHE.clear();
         }

         List<LaserData_BC8> datas = BOX_CACHE.computeIfAbsent(
            new LaserBoxRenderer.BoxKey(box.min(), box.max(), type, center, enableDiffuse), k -> makeLaserBox(box, type, center, enableDiffuse)
         );
         //? if >= 26.1.3 {
         /*LaserBatch.submitGeometry(poseStack, BCLibRenderTypes.entitySolid(BcTextureAtlases.BLOCKS_TEXTURE), (pose, vc) -> {
            for (LaserData_BC8 data : datas) {
               BcLaserRenderer.renderLaserPose(pose, vc, data, cameraPos);
            }
         });*/
         //?} else {
         BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
         VertexConsumer consumer = bufferSource.getBuffer(BCLibRenderTypes.entitySolid(BcTextureAtlases.BLOCKS_TEXTURE));

         for (LaserData_BC8 data : datas) {
            BcLaserRenderer.renderLaser(poseStack, consumer, data, cameraPos);
         }

         if (!LaserBatch.isActive()) {
            bufferSource.endBatch();
         }
         //?}
      }
   }

   private static List<LaserData_BC8> makeLaserBox(Box box, LaserData_BC8.LaserType type, boolean center, boolean enableDiffuse) {
      boolean renderX = !center || box.size().getX() > 1;
      boolean renderY = !center || box.size().getY() > 1;
      boolean renderZ = !center || box.size().getZ() > 1;
      Vec3 min = Vec3.atLowerCornerOf(box.min()).add(center ? VecUtil.VEC_HALF : Vec3.ZERO);
      Vec3 max = Vec3.atLowerCornerOf(box.max()).add(center ? VecUtil.VEC_HALF : VecUtil.VEC_ONE);
      List<LaserData_BC8> datas = new ArrayList<>();
      Vec3[][][] vecs = new Vec3[2][2][2];
      vecs[0][0][0] = new Vec3(min.x, min.y, min.z);
      vecs[1][0][0] = new Vec3(max.x, min.y, min.z);
      vecs[0][1][0] = new Vec3(min.x, max.y, min.z);
      vecs[1][1][0] = new Vec3(max.x, max.y, min.z);
      vecs[0][0][1] = new Vec3(min.x, min.y, max.z);
      vecs[1][0][1] = new Vec3(max.x, min.y, max.z);
      vecs[0][1][1] = new Vec3(min.x, max.y, max.z);
      vecs[1][1][1] = new Vec3(max.x, max.y, max.z);
      if (renderX) {
         datas.add(makeLaser(type, enableDiffuse, vecs[0][0][0], vecs[1][0][0], Axis.X));
         if (renderY) {
            datas.add(makeLaser(type, enableDiffuse, vecs[0][1][0], vecs[1][1][0], Axis.X));
            if (renderZ) {
               datas.add(makeLaser(type, enableDiffuse, vecs[0][1][1], vecs[1][1][1], Axis.X));
            }
         }

         if (renderZ) {
            datas.add(makeLaser(type, enableDiffuse, vecs[0][0][1], vecs[1][0][1], Axis.X));
         }
      }

      if (renderY) {
         datas.add(makeLaser(type, enableDiffuse, vecs[0][0][0], vecs[0][1][0], Axis.Y));
         if (renderX) {
            datas.add(makeLaser(type, enableDiffuse, vecs[1][0][0], vecs[1][1][0], Axis.Y));
            if (renderZ) {
               datas.add(makeLaser(type, enableDiffuse, vecs[1][0][1], vecs[1][1][1], Axis.Y));
            }
         }

         if (renderZ) {
            datas.add(makeLaser(type, enableDiffuse, vecs[0][0][1], vecs[0][1][1], Axis.Y));
         }
      }

      if (renderZ) {
         datas.add(makeLaser(type, enableDiffuse, vecs[0][0][0], vecs[0][0][1], Axis.Z));
         if (renderX) {
            datas.add(makeLaser(type, enableDiffuse, vecs[1][0][0], vecs[1][0][1], Axis.Z));
            if (renderY) {
               datas.add(makeLaser(type, enableDiffuse, vecs[1][1][0], vecs[1][1][1], Axis.Z));
            }
         }

         if (renderY) {
            datas.add(makeLaser(type, enableDiffuse, vecs[0][1][0], vecs[0][1][1], Axis.Z));
         }
      }

      return datas;
   }

   private static LaserData_BC8 makeLaser(LaserData_BC8.LaserType type, boolean enableDiffuse, Vec3 min, Vec3 max, Axis axis) {
      Direction faceForMin = VecUtil.getFacing(axis, true);
      Direction faceForMax = VecUtil.getFacing(axis, false);
      Vec3 dirMin = Vec3.atLowerCornerOf(faceForMin.getUnitVec3i());
      Vec3 dirMax = Vec3.atLowerCornerOf(faceForMax.getUnitVec3i());
      Vec3 one = min.add(dirMin.scale(0.0625));
      Vec3 two = max.add(dirMax.scale(0.0625));
      return new LaserData_BC8(type, one, two, 0.06230529595015576, enableDiffuse, false, 15);
   }

   private record BoxKey(BlockPos min, BlockPos max, LaserData_BC8.LaserType type, boolean center, boolean diffuse) {
   }
}
