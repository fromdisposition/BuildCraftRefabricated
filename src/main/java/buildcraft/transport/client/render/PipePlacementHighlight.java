/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.transport.client.render;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import buildcraft.lib.client.render.BCLibRenderTypes;

import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;

import net.minecraft.client.renderer.state.level.LevelRenderState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraft.fabric.client.render.BlockOutlineRenderer;
import buildcraft.fabric.client.event.ExtractBlockOutlineRenderStateEvent;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IItemPluggable;

import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.item.ItemWire;
import buildcraft.transport.tile.TilePipeHolder;

public final class PipePlacementHighlight {
    private PipePlacementHighlight() {}

    public static void onExtractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (!(event.getBlockState().getBlock() instanceof BlockPipeHolder)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        BlockEntity be = event.getLevel().getBlockEntity(event.getBlockPos());
        if (!(be instanceof TilePipeHolder tile) || tile.getPipe() == null) {
            return;
        }
        VoxelShape preview = previewShape(tile, event.getHitResult(), player);
        if (preview != null) {
            event.addCustomRenderer(new PreviewRenderer(preview));
        }
    }

    @Nullable
    private static VoxelShape previewShape(TilePipeHolder tile, BlockHitResult hit, LocalPlayer player) {
        ItemStack pluggableStack = heldStackOf(player, IItemPluggable.class);
        if (pluggableStack != null) {
            Direction face = BlockPipeHolder.resolveTargetFace(tile, hit);

            if (tile.getPluggable(face) != null) {
                return null;
            }
            IItemPluggable item = (IItemPluggable) pluggableStack.getItem();
            return Shapes.create(item.getPlacementBoundingBox(pluggableStack, face));
        }
        if (heldStackOf(player, ItemWire.class) != null) {
            EnumWirePart part = BlockPipeHolder.resolveTargetWirePart(hit);
            if (tile.getWireManager().parts.containsKey(part)) {
                return null;
            }

            return Shapes.create(part.boundingBox.inflate(BlockPipeHolder.WIRE_HIT_INFLATE));
        }
        return null;
    }

    @Nullable
    private static ItemStack heldStackOf(LocalPlayer player, Class<?> itemType) {
        ItemStack main = player.getMainHandItem();
        if (itemType.isInstance(main.getItem())) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (itemType.isInstance(off.getItem())) {
            return off;
        }
        return null;
    }

    private record PreviewRenderer(VoxelShape shape) implements BlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource buffer,
                PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {

            if (translucentPass != renderState.isTranslucent()) {
                return false;
            }
            Vec3 cam = levelRenderState.cameraRenderState.pos;
            BlockPos pos = renderState.pos();
            VertexConsumer lines = buffer.getBuffer(BCLibRenderTypes.lines());
            ShapeRenderer.renderShape(poseStack, lines, shape,
                    pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z,

                    ARGB.black(102), 2.5F);

            buffer.endLastBatch();
            return true;
        }
    }
}

