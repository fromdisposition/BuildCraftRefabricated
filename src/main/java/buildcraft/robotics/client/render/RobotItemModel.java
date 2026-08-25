/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.robotics.item.ItemRobot;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

/** Draws the robot item as the robot itself, wearing the texture of the board fused into it. */
public class RobotItemModel implements ItemModel {
   private static final SpecialModelRenderer<Identifier> RENDERER = new RobotItemModel.Renderer();
   private static final float HALF = 0.25F;

   private static Identifier textureOf(ItemStack stack) {
      return (Identifier) ItemRobot.getRobotNBT(stack).getRobotTexture();
   }

   public void update(
      ItemStackRenderState renderState,
      ItemStack stack,
      ItemModelResolver modelResolver,
      ItemDisplayContext displayContext,
      @Nullable ClientLevel level,
      @Nullable ItemOwner owner,
      int seed
   ) {
      Identifier texture = textureOf(stack);
      renderState.appendModelIdentityElement(this);
      renderState.appendModelIdentityElement(texture);
      ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
      layer.setupSpecialModel(RENDERER, texture);
      Vector3f rotation = RobotDisplay.rotation(displayContext);
      float scale = RobotDisplay.scale(displayContext);
      //? if >= 26.1 {
      layer.setItemTransform(new net.minecraft.client.resources.model.cuboid.ItemTransform(
         rotation, new Vector3f(), new Vector3f(scale, scale, scale)));
      //?} else {
      /*layer.setTransform(new net.minecraft.client.renderer.block.model.ItemTransform(
         rotation, new Vector3f(), new Vector3f(scale, scale, scale)));
      *///?}
   }

   private static final class Renderer implements SpecialModelRenderer<Identifier> {
      private static void draw(Identifier texture, PoseStack poseStack, SubmitNodeCollector collector, int light) {
         poseStack.pushPose();
         poseStack.translate(0.5, 0.5, 0.5);
         collector.submitCustomGeometry(poseStack, BCLibRenderTypes.entityCutout(RenderRobot.tex(texture)), (pose, buffer) -> {
            for (MutableQuad face : RenderRobot.FACES) {
               face.colourf(1.0F, 1.0F, 1.0F, 1.0F).lighti(light).render(pose, buffer);
            }
         });
         poseStack.popPose();
      }

      //? if >= 26.1 {
      @Override
      public void submit(
         Identifier texture, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline
      ) {
         draw(texture, poseStack, collector, light);
      }
      //?} else {
      /*@Override
      public void submit(
         Identifier texture,
         ItemDisplayContext context,
         PoseStack poseStack,
         SubmitNodeCollector collector,
         int light,
         int overlay,
         boolean foil,
         int outline
      ) {
         draw(texture, poseStack, collector, light);
      }
      *///?}

      //? if >= 1.21.11 {
      @Override
      public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> extents) {
         extents.accept(new Vector3f(0.5F - HALF, 0.5F - HALF, 0.5F - HALF));
         extents.accept(new Vector3f(0.5F + HALF, 0.5F + HALF, 0.5F + HALF));
      }
      //?} else {
      /*@Override
      public void getExtents(java.util.Set<Vector3f> extents) {
         extents.add(new Vector3f(0.5F - HALF, 0.5F - HALF, 0.5F - HALF));
         extents.add(new Vector3f(0.5F + HALF, 0.5F + HALF, 0.5F + HALF));
      }
      *///?}

      @Override
      public Identifier extractArgument(ItemStack stack) {
         return textureOf(stack);
      }
   }
}
