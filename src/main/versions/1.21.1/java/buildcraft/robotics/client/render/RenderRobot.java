/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.robotics.entity.EntityRobot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

/**
 * 1.21.1 (versions/1.21.1) robot entity renderer: immediate-mode port of the shared render-state/submit BER.
 * Draws the robot body cube, the energy emissive overlays, and the held item (classic ItemRenderer), then
 * delegates to super for the name tag / shadow.
 */
public class RenderRobot extends EntityRenderer<EntityRobot> {
   private static final Identifier OVERLAY_SIDE = tex(Identifier.fromNamespaceAndPath("buildcraftrobotics", "entities/overlay_side"));
   private static final Identifier OVERLAY_BOTTOM = tex(Identifier.fromNamespaceAndPath("buildcraftrobotics", "entities/overlay_bottom"));

   /**
    * The robot/overlay texture ids are stored logically as {@code entities/<name>} (as classic BuildCraft used
    * them, and how they travel over the entity's synched data). A render type needs the resolved file location,
    * so expand them to {@code textures/<name>.png} exactly like a vanilla entity texture location.
    */
   private static Identifier tex(Identifier logical) {
      return Identifier.fromNamespaceAndPath(logical.getNamespace(), "textures/" + logical.getPath() + ".png");
   }
   private static final float RADIUS = 0.25F;
   private static final Vector3f CENTER = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Vector3f EXTENT = new Vector3f(RADIUS, RADIUS, RADIUS);
   // The cube geometry + UVs never change, so build the six faces once and re-emit them each frame (only
   // colour/light vary). Avoids allocating 6-18 MutableQuad + UvFaceData per robot per frame. Safe to share:
   // entity rendering is single-threaded and MutableQuad.render() transforms into scratch + the buffer, never
   // mutating the quad's stored geometry.
   private static final MutableQuad[] FACES = buildFaces();

   private static MutableQuad[] buildFaces() {
      Direction[] dirs = Direction.values();
      MutableQuad[] faces = new MutableQuad[dirs.length];
      for (int i = 0; i < dirs.length; i++) {
         faces[i] = ModelUtil.createFace(dirs[i], CENTER, EXTENT, uvFor(dirs[i]));
      }
      return faces;
   }

   public RenderRobot(EntityRendererProvider.Context context) {
      super(context);
   }

   @Override
   public Identifier getTextureLocation(EntityRobot entity) {
      return tex(EntityRobot.DEFAULT_TEXTURE);
   }

   @Override
   public void render(EntityRobot entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      Identifier texture = entity.getTexture();
      float energy = entity.getEnergyFraction();
      float aimYaw = entity.getRenderAimYaw();

      poseStack.pushPose();
      poseStack.translate(0.0, 0.25, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(-aimYaw));

      VertexConsumer body = buffers.getBuffer(BCLibRenderTypes.entityCutout(tex(texture)));
      Pose bodyPose = poseStack.last();
      for (MutableQuad face : FACES) {
         face.colourf(1.0F, 1.0F, 1.0F, 1.0F).lighti(light).render(bodyPose, body);
      }

      if (energy > 0.01F) {
         float alpha = Math.max(0.1F, Math.min(1.0F, energy));
         VertexConsumer sideOverlay = buffers.getBuffer(BCLibRenderTypes.entityTranslucentEmissive(OVERLAY_SIDE));
         Pose sidePose = poseStack.last();
         for (MutableQuad face : FACES) {
            face.colourf(1.0F, 1.0F, 1.0F, alpha).lighti(light).render(sidePose, sideOverlay);
         }
         VertexConsumer bottomOverlay = buffers.getBuffer(BCLibRenderTypes.entityTranslucentEmissive(OVERLAY_BOTTOM));
         Pose bottomPose = poseStack.last();
         for (MutableQuad face : FACES) {
            face.colourf(1.0F, 1.0F, 1.0F, 1.0F).lighti(light).render(bottomPose, bottomOverlay);
         }
      }

      poseStack.popPose();

      ItemStack held = entity.getRenderItem();
      if (held != null && !held.isEmpty()) {
         poseStack.pushPose();
         poseStack.translate(0.0, 0.25, 0.0);
         poseStack.mulPose(Axis.YP.rotationDegrees(-aimYaw));
         poseStack.translate(-0.4, 0.0, 0.0);
         poseStack.mulPose(Axis.YP.rotationDegrees(135.0F));
         poseStack.scale(0.8F, 0.8F, 0.8F);
         Minecraft.getInstance()
            .getItemRenderer()
            .renderStatic(held, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, buffers, entity.level(), entity.getId());
         poseStack.popPose();
      }

      super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
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
