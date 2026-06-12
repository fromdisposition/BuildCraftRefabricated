/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderMiningWell implements BlockEntityRenderer<TileMiningWell, MiningWellRenderState> {
   private static final Identifier BLOCKS_ATLAS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
   private static final Identifier SHAFT_TEXTURE = Identifier.fromNamespaceAndPath("buildcraftfactory", "block/mining_well/tube");
   private static final int[] COLOUR_POWER = new int[16];
   private static final RenderPartCube LED_POWER = new RenderPartCube();
   private static final RenderPartCube LED_STATUS = new RenderPartCube();
   private final TextureAtlasSprite shaftSprite;

   public RenderMiningWell(Context context) {
      this.shaftSprite = context.sprites().get(new SpriteId(BLOCKS_ATLAS_TEXTURE, SHAFT_TEXTURE));
   }

   public MiningWellRenderState createRenderState() {
      return new MiningWellRenderState();
   }

   @Override
   public void extractRenderState(TileMiningWell tile, MiningWellRenderState state, float partialTick, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
      BlockEntityRenderer.super.extractRenderState(tile, state, partialTick, cameraPos, crumblingOverlay);
      this.extract(tile, state, partialTick);
   }

   private void extract(TileMiningWell tile, MiningWellRenderState state, float partialTick) {
      BlockState blockState = tile.getBlockState();
      state.facing = blockState.is(BCFactoryBlocks.MINING_WELL) ? (Direction)blockState.getValue(BuildCraftProperties.BLOCK_FACING) : Direction.NORTH;
      float percentFilled = tile.getPercentFilledForRender();
      state.powerColour = COLOUR_POWER[(int)(percentFilled * (COLOUR_POWER.length - 1))];
      state.statusColour = tile.isComplete() ? -14741477 : -8921737;
      state.shaftLength = tile.getLength(partialTick);
      state.shaftSegmentLights = MinerShaftBer.computeSegmentLights(tile.getLevel(), tile.getBlockPos(), state.shaftLength);
   }

   public void submit(MiningWellRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      poseStack.pushPose();
      LedRenderUtil.setFacePosition(LED_POWER, renderState.facing, 0.0125, 0.15625, 0.34375);
      LedRenderUtil.setFacePosition(LED_STATUS, renderState.facing, 0.0125, 0.28125, 0.34375);
      Direction skipFace = renderState.facing.getOpposite();
      LedRenderUtil.submit(poseStack, collector, LED_POWER, skipFace, renderState.powerColour);
      LedRenderUtil.submit(poseStack, collector, LED_STATUS, skipFace, renderState.statusColour);

      if (renderState.shaftLength > 0.0) {
         collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(BLOCKS_ATLAS_TEXTURE), (pose, buffer) -> MinerShaftBer.renderShaft(
            pose, buffer, this.shaftSprite, (float)renderState.shaftLength, renderState.shaftSegmentLights
         ));
      }

      poseStack.popPose();
   }

   @Override
   public boolean shouldRenderOffScreen() {
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
