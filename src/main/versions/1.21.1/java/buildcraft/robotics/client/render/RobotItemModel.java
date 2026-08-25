/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.item.ItemRobot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Draws the robot item as the robot itself, wearing the texture of the board fused into it. */
public class RobotItemModel implements BakedModel {

   public static void register() {
      BuiltinItemRendererRegistry.INSTANCE.register(BCRoboticsItems.ROBOT, RobotItemModel::draw);
   }

   private static void draw(
      ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay
   ) {
      Identifier texture = (Identifier) ItemRobot.getRobotNBT(stack).getRobotTexture();
      org.joml.Vector3f rotation = RobotDisplay.rotation(context);
      float scale = RobotDisplay.scale(context);
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation.y));
      poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotation.x));
      poseStack.scale(scale, scale, scale);
      VertexConsumer body = buffers.getBuffer(BCLibRenderTypes.entityCutout(RenderRobot.tex(texture)));
      Pose pose = poseStack.last();
      for (MutableQuad face : RenderRobot.FACES) {
         face.colourf(1.0F, 1.0F, 1.0F, 1.0F).lighti(light).render(pose, body);
      }

      poseStack.popPose();
   }

   @Override
   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
      return Collections.emptyList();
   }

   @Override
   public boolean useAmbientOcclusion() {
      return false;
   }

   @Override
   public boolean isGui3d() {
      return true;
   }

   @Override
   public boolean usesBlockLight() {
      return false;
   }

   @Override
   public boolean isCustomRenderer() {
      return true;
   }

   @Override
   public TextureAtlasSprite getParticleIcon() {
      return Minecraft.getInstance().getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS)
         .apply(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
   }

   @Override
   public ItemTransforms getTransforms() {
      return ItemTransforms.NO_TRANSFORMS;
   }

   @Override
   public ItemOverrides getOverrides() {
      return ItemOverrides.EMPTY;
   }
}
