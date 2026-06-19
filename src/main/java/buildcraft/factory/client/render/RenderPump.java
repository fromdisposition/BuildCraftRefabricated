/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TilePump;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import buildcraft.lib.client.render.BCLibRenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//? if >= 26.1 {
import net.minecraft.client.resources.model.sprite.SpriteId;
//?} else {
/*import net.minecraft.client.resources.model.sprite.Material;
*///?}
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderPump implements BlockEntityRenderer<TilePump, PumpRenderState> {
   private static final Identifier BLOCKS_ATLAS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
   private static final Identifier SHAFT_TEXTURE = Identifier.fromNamespaceAndPath("buildcraftfactory", "block/pump/tube");
   private static final int[] COLOUR_POWER = new int[16];
   private static final RenderPartCube[] LED_POWER = new RenderPartCube[4];
   private static final RenderPartCube[] LED_STATUS = new RenderPartCube[4];
   private final TextureAtlasSprite shaftSprite;

   public RenderPump(Context context) {
      //? if >= 26.1 {
      this.shaftSprite = context.sprites().get(new SpriteId(BLOCKS_ATLAS_TEXTURE, SHAFT_TEXTURE));
      //?} else {
      /*this.shaftSprite = context.materials().get(new Material(BLOCKS_ATLAS_TEXTURE, SHAFT_TEXTURE));
      *///?}
   }

   public PumpRenderState createRenderState() {
      return new PumpRenderState();
   }

   @Override
   public void extractRenderState(TilePump tile, PumpRenderState state, float partialTick, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
      BlockEntityRenderer.super.extractRenderState(tile, state, partialTick, cameraPos, crumblingOverlay);
      this.extract(tile, state, partialTick);
   }

   private void extract(TilePump tile, PumpRenderState state, float partialTick) {
      float percentFilled = tile.getPercentFilledForRender();
      state.powerColour = COLOUR_POWER[(int)(percentFilled * (COLOUR_POWER.length - 1))];
      state.statusColour = tile.isComplete() ? -14741477 : -8921737;
      state.shaftLength = tile.getLength(partialTick);
   }

   public void submit(PumpRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      poseStack.pushPose();

      for (int i = 0; i < 4; i++) {
         Direction skipFace = Direction.from2DDataValue(i).getOpposite();
         LedRenderUtil.submit(poseStack, collector, LED_POWER[i], skipFace, renderState.powerColour);
         LedRenderUtil.submit(poseStack, collector, LED_STATUS[i], skipFace, renderState.statusColour);
      }

      if (renderState.shaftLength > 0.0) {
         collector.submitCustomGeometry(poseStack, BCLibRenderTypes.entityTranslucent(BLOCKS_ATLAS_TEXTURE), (pose, buffer) -> MinerShaftBer.renderShaft(
            pose, buffer, renderState.blockPos, this.shaftSprite, (float)renderState.shaftLength
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
