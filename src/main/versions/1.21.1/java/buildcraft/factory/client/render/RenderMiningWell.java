/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileMiningWell;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 1.21.1 (versions/1.21.1) mining-well renderer: immediate-mode port (status/power LEDs + mining shaft).
 */
public class RenderMiningWell implements BlockEntityRenderer<TileMiningWell> {
   private static final Identifier BLOCKS_ATLAS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
   private static final Identifier SHAFT_TEXTURE = Identifier.fromNamespaceAndPath("buildcraftfactory", "block/mining_well/tube");
   private static final int[] COLOUR_POWER = new int[16];
   private static final RenderPartCube LED_POWER = new RenderPartCube();
   private static final RenderPartCube LED_STATUS = new RenderPartCube();
   private final TextureAtlasSprite shaftSprite;

   public RenderMiningWell(BlockEntityRendererProvider.Context context) {
      this.shaftSprite = SpriteUtil.getSprite(SHAFT_TEXTURE);
   }

   @Override
   public void render(TileMiningWell tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      BlockState blockState = tile.getBlockState();
      Direction facing = blockState.is(BCFactoryBlocks.MINING_WELL) ? (Direction) blockState.getValue(BuildCraftProperties.BLOCK_FACING) : Direction.NORTH;
      float percentFilled = tile.getPercentFilledForRender();
      int powerColour = COLOUR_POWER[(int) (percentFilled * (COLOUR_POWER.length - 1))];
      int statusColour = tile.isComplete() ? -14741477 : -8921737;

      poseStack.pushPose();
      LedRenderUtil.setFacePosition(LED_POWER, facing, 0.0125, 0.15625, 0.34375);
      LedRenderUtil.setFacePosition(LED_STATUS, facing, 0.0125, 0.28125, 0.34375);
      Direction skipFace = facing.getOpposite();
      VertexConsumer led = buffers.getBuffer(BCLibRenderTypes.led());
      Pose pose = poseStack.last();
      LedRenderUtil.render(LED_POWER, pose, led, skipFace, powerColour);
      LedRenderUtil.render(LED_STATUS, pose, led, skipFace, statusColour);

      double shaftLength = tile.getLength(partialTick);
      if (shaftLength > 0.0) {
         VertexConsumer shaft = buffers.getBuffer(BCLibRenderTypes.entityTranslucent(BLOCKS_ATLAS_TEXTURE));
         MinerShaftBer.renderShaft(poseStack.last(), shaft, tile.getBlockPos(), this.shaftSprite, (float) shaftLength);
      }

      poseStack.popPose();
   }

   @Override
   public boolean shouldRenderOffScreen(TileMiningWell blockEntity) {
      return MinerShaftBer.shouldRenderOffScreen();
   }

   @Override
   public int getViewDistance() {
      return MinerShaftBer.getViewDistance();
   }

   @Override
   public boolean shouldRender(TileMiningWell blockEntity, Vec3 cameraPosition) {
      return MinerShaftBer.shouldRender(blockEntity, cameraPosition);
   }

   static {
      for (int i = 0; i < COLOUR_POWER.length; i++) {
         int c = i * 64 / COLOUR_POWER.length & 0xFF;
         int r = (i * 176 / COLOUR_POWER.length & 0xFF) + 79;
         COLOUR_POWER[i] = 0xFF000000 | c << 16 | c << 8 | r;
      }
   }
}
