/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.render;

import buildcraft.lib.debug.DebugRenderHelper;
import buildcraft.lib.misc.VolumeUtil;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.tile.TileLaser;
import com.mojang.blaze3d.vertex.PoseStack;
//? if >= 26.1.3 {
//?} else {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AdvDebuggerLaser {
   private static final int DISTANCE = 6;
   private static final int COLOUR_VISIBLE = -6684775;
   private static final int COLOUR_BLOCKED = -6745839;
   private static final double CUBE_RADIUS = 0.15;

   private AdvDebuggerLaser() {
   }

   //? if >= 26.1.3 {
   //?} else {
   public static void render(TileLaser tile, PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos) {
      Level level = tile.getLevel();
      if (level != null) {
         BlockState state = level.getBlockState(tile.getBlockPos());
         if (state.getBlock() == BCSiliconBlocks.LASER) {
            Direction face = (Direction)state.getValue(BlockLaser.FACING);
            VolumeUtil.iterateCone(
               level,
               tile.getBlockPos(),
               face,
               6,
               true,
               (w, start, p, visible) -> {
                  AABB box = new AABB(
                     p.getX() + 0.5 - 0.15, p.getY() + 0.5 - 0.15, p.getZ() + 0.5 - 0.15, p.getX() + 0.5 + 0.15, p.getY() + 0.5 + 0.15, p.getZ() + 0.5 + 0.15
                  );
                  DebugRenderHelper.renderSolidBox(poseStack, bufferSource, box, cameraPos, visible ? -6684775 : -6745839);
               }
            );
         }
      }
   }
   //?}
}
