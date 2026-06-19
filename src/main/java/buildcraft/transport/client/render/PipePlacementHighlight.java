/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.fabric.client.event.ExtractBlockOutlineRenderStateEvent;
import buildcraft.fabric.client.render.BlockOutlineRenderer;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.item.ItemWire;
import buildcraft.transport.tile.TilePipeHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
//? if >= 26.2 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
*///?} else {
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
//?}
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class PipePlacementHighlight {
   private PipePlacementHighlight() {
   }

   public static void onExtractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
      if (event.getBlockState().getBlock() instanceof BlockPipeHolder) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null) {
            if (event.getLevel().getBlockEntity(event.getBlockPos()) instanceof TilePipeHolder tile && tile.getPipe() != null) {
               VoxelShape preview = previewShape(tile, event.getHitResult(), player);
               if (preview != null) {
                  event.addCustomRenderer(new PipePlacementHighlight.PreviewRenderer(preview));
               }
            }
         }
      }
   }

   @Nullable
   static VoxelShape previewShape(TilePipeHolder tile, BlockHitResult hit, LocalPlayer player) {
      ItemStack pluggableStack = heldStackOf(player, IItemPluggable.class);
      if (pluggableStack != null) {
         Direction face = BlockPipeHolder.resolveTargetFace(tile, hit);
         if (tile.getPluggable(face) != null) {
            return null;
         }

         IItemPluggable item = (IItemPluggable)pluggableStack.getItem();
         return Shapes.create(item.getPlacementBoundingBox(pluggableStack, face));
      } else if (heldStackOf(player, ItemWire.class) != null) {
         EnumWirePart part = BlockPipeHolder.resolveTargetWirePart(hit);
         return tile.getWireManager().parts.containsKey(part) ? null : Shapes.create(part.boundingBox.inflate(0.0625));
      } else {
         return null;
      }
   }

   @Nullable
   private static ItemStack heldStackOf(LocalPlayer player, Class<?> itemType) {
      ItemStack main = player.getMainHandItem();
      if (itemType.isInstance(main.getItem())) {
         return main;
      }

      ItemStack off = player.getOffhandItem();
      return itemType.isInstance(off.getItem()) ? off : null;
   }

   //? if >= 26.2 {
   /*private record PreviewRenderer(VoxelShape shape) implements BlockOutlineRenderer {
      @Override
      public boolean render(
         BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState
      ) {
         Vec3 cam = levelRenderState.cameraRenderState.pos;
         BlockPos pos = renderState.pos();
         poseStack.pushPose();
         poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
         submitNodeCollector.submitShapeOutline(poseStack, this.shape, BCLibRenderTypes.lines(), ARGB.black(102), 2.5F, renderState.isTranslucent());
         poseStack.popPose();
         return true;
      }
   }
   *///?} else {
   private record PreviewRenderer(VoxelShape shape) implements BlockOutlineRenderer {
      @Override
      public boolean render(
         BlockOutlineRenderState renderState, BufferSource buffer, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState
      ) {
         if (translucentPass != renderState.isTranslucent()) {
            return false;
         }

         Vec3 cam = levelRenderState.cameraRenderState.pos;
         BlockPos pos = renderState.pos();
         VertexConsumer lines = buffer.getBuffer(BCLibRenderTypes.lines());
         //? if >= 1.21.11 {
         ShapeRenderer.renderShape(poseStack, lines, this.shape, pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z, ARGB.black(102), 2.5F);
         //?} else {
         /*// 1.21.10 ShapeRenderer.renderShape has no line-width parameter.
         ShapeRenderer.renderShape(poseStack, lines, this.shape, pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z, ARGB.black(102));
         *///?}
         buffer.endLastBatch();
         return true;
      }
   }
   //?}
}
