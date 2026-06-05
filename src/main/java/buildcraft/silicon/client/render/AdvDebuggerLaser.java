/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.debug.DebugRenderHelper;
import buildcraft.lib.misc.VolumeUtil;

import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.tile.TileLaser;

public final class AdvDebuggerLaser {

    private static final int DISTANCE = 6;
    private static final int COLOUR_VISIBLE = 0xFF_99FF99;

    private static final int COLOUR_BLOCKED = 0xFF_991111;

    private static final double CUBE_RADIUS = 0.15;

    private AdvDebuggerLaser() {}

    public static void render(TileLaser tile, PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos) {
        Level level = tile.getLevel();
        if (level == null) {
            return;
        }
        BlockState state = level.getBlockState(tile.getBlockPos());
        if (state.getBlock() != BCSiliconBlocks.LASER.get()) {
            return;
        }
        Direction face = state.getValue(BlockLaser.FACING);
        VolumeUtil.iterateCone(level, tile.getBlockPos(), face, DISTANCE, true, (w, start, p, visible) -> {
            AABB box = new AABB(
                p.getX() + 0.5 - CUBE_RADIUS, p.getY() + 0.5 - CUBE_RADIUS, p.getZ() + 0.5 - CUBE_RADIUS,
                p.getX() + 0.5 + CUBE_RADIUS, p.getY() + 0.5 + CUBE_RADIUS, p.getZ() + 0.5 + CUBE_RADIUS
            );
            DebugRenderHelper.renderSolidBox(poseStack, bufferSource, box, cameraPos,
                visible ? COLOUR_VISIBLE : COLOUR_BLOCKED);
        });
    }
}
