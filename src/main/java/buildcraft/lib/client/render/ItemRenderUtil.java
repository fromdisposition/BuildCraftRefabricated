/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemRenderUtil {
   private static final Random modelOffsetRandom = new Random(0L);
   private static PoseStack currentPoseStack;
   private static SubmitNodeCollector currentCollector;
   private static int currentLight;
   private static ItemStackRenderState renderState;

   public static void beginItemBatch(PoseStack poseStack, SubmitNodeCollector collector, int light) {
      currentPoseStack = poseStack;
      currentCollector = collector;
      currentLight = light;
      renderState = new ItemStackRenderState();
   }

   public static PoseStack getCurrentPoseStack() {
      return currentPoseStack;
   }

   public static void renderItemStack(double x, double y, double z, ItemStack stack, int lightc, Direction dir, VertexConsumer bb) {
      renderItemStack(x, y, z, stack, stack.getCount(), lightc, dir, bb);
   }

   public static void renderItemStack(double x, double y, double z, ItemStack stack, int stackCount, int lightc, Direction dir, VertexConsumer bb) {
      if (!stack.isEmpty() && currentPoseStack != null && currentCollector != null) {
         if (dir == null) {
            dir = Direction.EAST;
         }

         ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
         renderState.clear();
         resolver.updateForTopItem(renderState, stack, ItemDisplayContext.FIXED, Minecraft.getInstance().level, null, 0);
         if (!renderState.isEmpty()) {
            int itemModelCount = getStackModelCount(stackCount);
            if (itemModelCount > 1) {
               setupModelOffsetRandom(stack);
            }

            for (int i = 0; i < itemModelCount; i++) {
               currentPoseStack.pushPose();
               float dx = 0.0F;
               float dy = 0.0F;
               float dz = 0.0F;
               if (i > 0) {
                  dx = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                  dy = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                  dz = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
               }

               currentPoseStack.translate(x + dx, y + dy, z + dz);
               currentPoseStack.scale(0.6F, 0.6F, 0.6F);
               applyDirectionRotation(currentPoseStack, dir);
               renderState.submit(currentPoseStack, currentCollector, lightc, OverlayTexture.NO_OVERLAY, 0);
               currentPoseStack.popPose();
            }
         }
      }
   }

   private static void applyDirectionRotation(PoseStack ps, Direction dir) {
      switch (dir) {
         case NORTH:
            ps.mulPose(Axis.YP.rotationDegrees(180.0F));
            break;
         case EAST:
            ps.mulPose(Axis.YP.rotationDegrees(90.0F));
            break;
         case WEST:
            ps.mulPose(Axis.YP.rotationDegrees(-90.0F));
            break;
         case UP:
            ps.mulPose(Axis.XP.rotationDegrees(-90.0F));
            break;
         case DOWN:
            ps.mulPose(Axis.XP.rotationDegrees(90.0F));
         case SOUTH:
      }
   }

   private static void setupModelOffsetRandom(ItemStack stack) {
      long seed;
      if (stack.isEmpty()) {
         seed = 137L;
      } else {
         Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
         seed = key != null ? key.hashCode() & 2147483647L : 127L;
      }

      modelOffsetRandom.setSeed(seed);
   }

   private static int getStackModelCount(int stackCount) {
      if (stackCount > 48) {
         return 5;
      } else if (stackCount > 32) {
         return 4;
      } else if (stackCount > 16) {
         return 3;
      } else {
         return stackCount > 1 ? 2 : 1;
      }
   }

   public static void endItemBatch() {
      currentPoseStack = null;
      currentCollector = null;
   }
}
