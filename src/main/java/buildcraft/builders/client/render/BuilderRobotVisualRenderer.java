/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.laser.BcLaserRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import buildcraft.lib.client.render.laser.LaserBatch;
import net.minecraft.client.Minecraft;
//? if >= 26.2 {
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
*///?}
import buildcraft.lib.client.render.BCLibRenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class BuilderRobotVisualRenderer {
   private BuilderRobotVisualRenderer() {
   }

   public static void renderRobotAndBreakTasks(
      Minecraft mc, PoseStack poseStack, Vec3 cameraPos, Vec3 robotPos, SnapshotBuilder<?> active
   ) {
      if (!active.clientBreakTasks.isEmpty()) {
         renderRobotCube(mc, poseStack, cameraPos, robotPos);
      }

      for (SnapshotBuilder.BreakTask breakTask : active.clientBreakTasks) {
         double progress = Math.max(0.0, Math.min(1.0, breakTask.power * 1.0 / breakTask.getTarget()));
         int powerIdx = (int)Math.round(progress * (BuildCraftLaserManager.POWERS.length - 1));
         BcLaserRenderer.renderLaserStatic(
            poseStack,
            new LaserData_BC8(
               BuildCraftLaserManager.POWERS[powerIdx],
               robotPos.subtract(new Vec3(0.0, 0.27, 0.0)),
               Vec3.atCenterOf(breakTask.pos),
               0.0625
            ),
            cameraPos
         );
      }
   }

   private static void renderRobotCube(Minecraft mc, PoseStack poseStack, Vec3 cameraPos, Vec3 robotPos) {
      //? if >= 26.2 {
      poseStack.pushPose();
      poseStack.translate(robotPos.x - cameraPos.x, robotPos.y - cameraPos.y, robotPos.z - cameraPos.z);
      int worldLight = BcLaserRenderer.computeLightmap(robotPos.x, robotPos.y, robotPos.z, 0);
      LaserBatch.submitGeometry(poseStack, BCLibRenderTypes.entityTranslucent(BCBuildersSprites.ROBOT.getAtlasLocation()), (pose, vc) -> {
         int i = 0;
         for (Direction face : Direction.values()) {
            ModelUtil.createFace(
                  face,
                  new Vector3f(0.0F, 0.0F, 0.0F),
                  new Vector3f(0.25F, 0.25F, 0.25F),
                  new ModelUtil.UvFaceData(
                     BCBuildersSprites.ROBOT.getInterpU(i * 8 / 64.0),
                     BCBuildersSprites.ROBOT.getInterpV(0.0),
                     BCBuildersSprites.ROBOT.getInterpU((i + 1) * 8 / 64.0),
                     BCBuildersSprites.ROBOT.getInterpV(0.125)
                  )
               )
               .lighti(worldLight)
               .render(pose, vc);
            i++;
         }
      });
      poseStack.popPose();
      //?} else {
      /*BufferSource bufferSource = mc.renderBuffers().bufferSource();
      VertexConsumer buffer = bufferSource.getBuffer(BCLibRenderTypes.entityTranslucent(BCBuildersSprites.ROBOT.getAtlasLocation()));
      poseStack.pushPose();
      poseStack.translate(robotPos.x - cameraPos.x, robotPos.y - cameraPos.y, robotPos.z - cameraPos.z);
      int worldLight = BcLaserRenderer.computeLightmap(robotPos.x, robotPos.y, robotPos.z, 0);
      Pose pose = poseStack.last();
      int i = 0;

      for (Direction face : Direction.values()) {
         ModelUtil.createFace(
               face,
               new Vector3f(0.0F, 0.0F, 0.0F),
               new Vector3f(0.25F, 0.25F, 0.25F),
               new ModelUtil.UvFaceData(
                  BCBuildersSprites.ROBOT.getInterpU(i * 8 / 64.0),
                  BCBuildersSprites.ROBOT.getInterpV(0.0),
                  BCBuildersSprites.ROBOT.getInterpU((i + 1) * 8 / 64.0),
                  BCBuildersSprites.ROBOT.getInterpV(0.125)
               )
            )
            .lighti(worldLight)
            .render(pose, buffer);
         i++;
      }

      poseStack.popPose();
      bufferSource.endBatch();
      *///?}
   }
}
