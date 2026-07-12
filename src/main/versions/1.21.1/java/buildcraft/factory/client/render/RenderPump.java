/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TilePump;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import buildcraft.lib.misc.SpriteUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * 1.21.1 (versions/1.21.1) pump renderer: immediate-mode port. Status/power LEDs drawn via the version-neutral
 * LedRenderUtil.render, and the mining shaft via the version-neutral MinerShaftBer.renderShaft.
 */
public class RenderPump implements BlockEntityRenderer<TilePump> {
   private static final Identifier BLOCKS_ATLAS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
   private static final Identifier SHAFT_TEXTURE = Identifier.fromNamespaceAndPath("buildcraftfactory", "block/pump/tube");
   private static final int[] COLOUR_POWER = new int[16];
   private static final RenderPartCube[] LED_POWER = new RenderPartCube[4];
   private static final RenderPartCube[] LED_STATUS = new RenderPartCube[4];
   private final TextureAtlasSprite shaftSprite;

   public RenderPump(BlockEntityRendererProvider.Context context) {
      this.shaftSprite = SpriteUtil.getSprite(SHAFT_TEXTURE);
   }

   @Override
   public void render(TilePump tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      poseStack.pushPose();
      float percentFilled = tile.getPercentFilledForRender();
      int powerColour = COLOUR_POWER[(int) (percentFilled * (COLOUR_POWER.length - 1))];
      int statusColour = tile.isComplete() ? -14741477 : -8921737;

      VertexConsumer led = buffers.getBuffer(BCLibRenderTypes.led());
      Pose pose = poseStack.last();
      for (int i = 0; i < 4; i++) {
         Direction skipFace = Direction.from2DDataValue(i).getOpposite();
         LedRenderUtil.render(LED_POWER[i], pose, led, skipFace, powerColour);
         LedRenderUtil.render(LED_STATUS[i], pose, led, skipFace, statusColour);
      }

      double shaftLength = tile.getLength(partialTick);
      if (shaftLength > 0.0) {
         VertexConsumer shaft = buffers.getBuffer(BCLibRenderTypes.entityTranslucent(BLOCKS_ATLAS_TEXTURE));
         MinerShaftBer.renderShaft(poseStack.last(), shaft, tile.getBlockPos(), this.shaftSprite, (float) shaftLength);
      }

      poseStack.popPose();
   }

   @Override
   public boolean shouldRenderOffScreen(TilePump blockEntity) {
      return MinerShaftBer.shouldRenderOffScreen();
   }

   @Override
   public int getViewDistance() {
      return MinerShaftBer.getViewDistance();
   }

   @Override
   public boolean shouldRender(TilePump blockEntity, Vec3 cameraPosition) {
      return MinerShaftBer.shouldRender(blockEntity, cameraPosition);
   }

   static {
      for (int i = 0; i < COLOUR_POWER.length; i++) {
         int c = i * 64 / COLOUR_POWER.length;
         int r = i * 224 / COLOUR_POWER.length + 31;
         COLOUR_POWER[i] = 0xFF000000 | c << 16 | c << 8 | r;
      }

      for (int i = 0; i < 4; i++) {
         Direction face = Direction.from2DDataValue(i);
         LED_POWER[i] = new RenderPartCube();
         LED_STATUS[i] = new RenderPartCube();
         LedRenderUtil.setFacePosition(LED_POWER[i], face, 0.025, 0.09375, 0.84375);
         LedRenderUtil.setFacePosition(LED_STATUS[i], face, 0.025, 0.21875, 0.84375);
      }
   }
}
