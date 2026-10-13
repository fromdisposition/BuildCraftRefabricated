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

            // Do not setCanceled here: it nulls the outline state and drops custom renderers with it (nothing
            // renders); PreviewRenderer.render returning true already suppresses the vanilla outline.
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
         boolean highContrast = renderState.highContrast();
         boolean translucent = renderState.isTranslucent();
         poseStack.pushPose();
         poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
         if (highContrast) {
            submitNodeCollector.submitShapeOutline(poseStack, this.shape, BCLibRenderTypes.secondaryBlockOutline(), 0xFF000000, 7.0F, translucent);
         }

         int colour = highContrast ? 0xFF57FF61 : ARGB.black(102);
         submitNodeCollector.submitShapeOutline(
            poseStack, this.shape, BCLibRenderTypes.blockOutline(highContrast), colour, BCLibRenderTypes.blockOutlineWidth(), translucent
         );
         poseStack.popPose();
         return true;
      }
   }
   //?} else if >= 1.21.10 {
   /*private record PreviewRenderer(VoxelShape shape) implements BlockOutlineRenderer {
      @Override
      public boolean render(
         BlockOutlineRenderState renderState, BufferSource buffer, PoseStack poseStack, LevelRenderState levelRenderState
      ) {
         Vec3 cam = levelRenderState.cameraRenderState.pos;
         BlockPos pos = renderState.pos();
         double x = pos.getX() - cam.x;
         double y = pos.getY() - cam.y;
         double z = pos.getZ() - cam.z;
         boolean highContrast = renderState.highContrast();
         int colour = highContrast ? 0xFF57FF61 : ARGB.black(102);
         //? if >= 1.21.11 {
         if (highContrast) {
            ShapeRenderer.renderShape(poseStack, buffer.getBuffer(BCLibRenderTypes.secondaryBlockOutline()), this.shape, x, y, z, 0xFF000000, 7.0F);
         }

         ShapeRenderer.renderShape(poseStack, buffer.getBuffer(BCLibRenderTypes.lines()), this.shape, x, y, z, colour, BCLibRenderTypes.blockOutlineWidth());
         //?} else {
         /^if (highContrast) {
            ShapeRenderer.renderShape(poseStack, buffer.getBuffer(BCLibRenderTypes.secondaryBlockOutline()), this.shape, x, y, z, 0xFF000000);
         }

         ShapeRenderer.renderShape(poseStack, buffer.getBuffer(BCLibRenderTypes.lines()), this.shape, x, y, z, colour);
         ^///?}
         buffer.endLastBatch();
         return true;
      }
   }
   *///?} else {
   /*private record PreviewRenderer(VoxelShape shape) implements BlockOutlineRenderer {
   }
   *///?}
}
