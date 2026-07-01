/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.robotics.entity.EntityRobot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import buildcraft.lib.client.render.BCLibRenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class RenderRobot extends EntityRenderer<EntityRobot, RobotRenderState> {
   private static final Identifier OVERLAY_SIDE = Identifier.fromNamespaceAndPath("buildcraftrobotics", "entities/overlay_side");
   private static final Identifier OVERLAY_BOTTOM = Identifier.fromNamespaceAndPath("buildcraftrobotics", "entities/overlay_bottom");

   
   private static final float RADIUS = 0.25F;
   private static final Vector3f CENTER = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Vector3f EXTENT = new Vector3f(RADIUS, RADIUS, RADIUS);

   private final ItemModelResolver itemModelResolver;

   public RenderRobot(EntityRendererProvider.Context context) {
      super(context);
      this.itemModelResolver = context.getItemModelResolver();
   }

   @Override
   public RobotRenderState createRenderState() {
      return new RobotRenderState();
   }

   @Override
   public void extractRenderState(EntityRobot entity, RobotRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.texture = entity.getTexture();
      state.energy = entity.getEnergyFraction();
      state.aimYaw = entity.getRenderAimYaw();
      ItemStack held = entity.getRenderItem();
      this.itemModelResolver.updateForTopItem(
         state.heldItemState, held == null ? ItemStack.EMPTY : held, ItemDisplayContext.GROUND, entity.level(), null, entity.getId()
      );
   }

   @Override
   public void submit(RobotRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      int light = state.lightCoords;

      poseStack.pushPose();
      poseStack.translate(0.0, 0.25, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(-state.aimYaw));

      RenderType body = BCLibRenderTypes.entityCutout(state.texture);
      collector.submitCustomGeometry(poseStack, body, (pose, buffer) -> {
         for (Direction face : Direction.values()) {
            ModelUtil.createFace(face, CENTER, EXTENT, uvFor(face)).lighti(light).render(pose, buffer);
         }
      });

      if (state.energy > 0.01F) {
         float alpha = Math.max(0.1F, Math.min(1.0F, state.energy));
         RenderType sideOverlay = BCLibRenderTypes.entityTranslucentEmissive(OVERLAY_SIDE);
         collector.submitCustomGeometry(poseStack, sideOverlay, (pose, buffer) -> {
            for (Direction face : Direction.values()) {
               ModelUtil.createFace(face, CENTER, EXTENT, uvFor(face)).colourf(1.0F, 1.0F, 1.0F, alpha).lighti(light).render(pose, buffer);
            }
         });

         RenderType bottomOverlay = BCLibRenderTypes.entityTranslucentEmissive(OVERLAY_BOTTOM);
         collector.submitCustomGeometry(poseStack, bottomOverlay, (pose, buffer) -> {
            for (Direction face : Direction.values()) {
               ModelUtil.createFace(face, CENTER, EXTENT, uvFor(face)).colourf(1.0F, 1.0F, 1.0F, 1.0F).lighti(light).render(pose, buffer);
            }
         });
      }

      poseStack.popPose();

      if (!state.heldItemState.isEmpty()) {
         poseStack.pushPose();
         poseStack.translate(0.0, 0.25, 0.0);
         poseStack.mulPose(Axis.YP.rotationDegrees(-state.aimYaw));
         poseStack.translate(-0.4, 0.0, 0.0);
         poseStack.mulPose(Axis.YP.rotationDegrees(135.0F));
         poseStack.scale(0.8F, 0.8F, 0.8F);
         state.heldItemState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
         poseStack.popPose();
      }
   }

   private static UvFaceData uvFor(Direction face) {
      switch (face) {
         case DOWN:
            return uv(16, 0, 24, 8);
         case UP:
            return uv(8, 0, 16, 8);
         case NORTH:
            return uv(8, 8, 16, 16);
         case SOUTH:
            return uv(24, 8, 32, 16);
         case WEST:
            return uv(0, 8, 8, 16);
         case EAST:
         default:
            return uv(16, 8, 24, 16);
      }
   }

   private static UvFaceData uv(int u0, int v0, int u1, int v1) {
      return new UvFaceData(u0 / 64.0F, v0 / 32.0F, u1 / 64.0F, v1 / 32.0F);
   }
}
