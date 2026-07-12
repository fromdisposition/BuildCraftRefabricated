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
import net.minecraft.client.renderer.SubmitNodeCollector;
//?} else if >= 1.21.10 {
/*import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
*///?}
//? if >= 1.21.10 {
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
//? if >= 1.21.10 {
import net.minecraft.util.ARGB;
//?}
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
         if (player != null
            && event.getLevel().getBlockEntity(event.getBlockPos()) instanceof TilePipeHolder tile
            && tile.getPipe() != null) {
            // Holding a pluggable/wire: draw a placement preview ON TOP of the vanilla pipe outline.
            VoxelShape placement = placementShape(tile, event.getHitResult(), player);
            if (placement != null) {
               event.addCustomRenderer(new PipePlacementHighlight.PreviewRenderer(placement));
               return;
            }

            // Otherwise: outline just the sub-part (pluggable/wire/arm/centre) under the crosshair instead of the
            // vanilla whole-pipe outline -- getShape now returns the full composed shape, so without this the
            // outline would wrap the entire pipe instead of the part the player is aiming at. NOTE: do NOT
            // setCanceled here -- cancelling nulls the outline state and drops custom renderers with it (nothing
            // renders at all); PreviewRenderer.render returning true already suppresses the vanilla outline.
            event.addCustomRenderer(new PipePlacementHighlight.PreviewRenderer(hoveredPartShape(tile, event.getHitResult())));
         }
      }
   }

   @Nullable
   static VoxelShape placementShape(TilePipeHolder tile, BlockHitResult hit, LocalPlayer player) {
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

   /** The tight shape of the single sub-part (pluggable, wire, arm or centre) under the crosshair. Never null. */
   static VoxelShape hoveredPartShape(TilePipeHolder tile, BlockHitResult hit) {
      BlockPos pos = hit.getBlockPos();
      return BlockPipeHolder.partShapeAt(
         tile, hit.getLocation().x - pos.getX(), hit.getLocation().y - pos.getY(), hit.getLocation().z - pos.getZ()
      );
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
   private record PreviewRenderer(VoxelShape shape) implements BlockOutlineRenderer {
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
   //?} else if >= 1.21.10 {
   /*private record PreviewRenderer(VoxelShape shape) implements BlockOutlineRenderer {
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
         /^// 1.21.10 ShapeRenderer.renderShape has no line-width parameter.
         ShapeRenderer.renderShape(poseStack, lines, this.shape, pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z, ARGB.black(102));
         ^///?}
         buffer.endLastBatch();
         return true;
      }
   }
   *///?} else {
   /*// 1.21.1: BlockOutlineRenderer is an empty marker (custom outlines absent); no render override.
   private record PreviewRenderer(VoxelShape shape) implements BlockOutlineRenderer {
   }
   *///?}
}
