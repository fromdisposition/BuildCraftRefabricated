/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.interaction;

import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FluidWorldFeedback {
   private FluidWorldFeedback() {
   }

   public static void triggerSoundAndGameEvent(FluidStack stack, Level level, Vec3 position, @Nullable Player player, boolean pickup) {
      if (pickup) {
         SoundUtil.playBucketFill(level, BlockPos.containing(position), stack);
      } else {
         SoundUtil.playBucketEmpty(level, BlockPos.containing(position), stack);
      }

      level.gameEvent(player, pickup ? GameEvent.FLUID_PICKUP : GameEvent.FLUID_PLACE, position);
   }

   public static void playAtBlockOrPlayer(FluidStack stack, Level level, @Nullable BlockPos blockPos, @Nullable Player player, boolean pickup) {
      if (player == null && blockPos == null) {
         throw new IllegalArgumentException("Either player or blockPos must be provided.");
      }

      Vec3 position = blockPos != null ? Vec3.atCenterOf(blockPos) : new Vec3(player.getX(), player.getY() + 0.5, player.getZ());
      triggerSoundAndGameEvent(stack, level, position, player, pickup);
   }
}
