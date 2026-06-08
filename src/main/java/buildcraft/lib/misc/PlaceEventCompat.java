/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PlaceEventCompat {
   private PlaceEventCompat() {
   }

   public static boolean canPlace(ServerLevel level, BlockPos pos, Player player) {
      BlockState state = level.getBlockState(pos);
      BlockPos hitPos;
      Direction face;
      if (state.isAir()) {
         hitPos = pos.below();
         face = Direction.UP;
         if (level.isOutsideBuildHeight(hitPos)) {
            hitPos = pos;
            face = Direction.DOWN;
         }
      } else {
         hitPos = pos;
         face = Direction.UP;
      }

      BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), face, hitPos, false);
      InteractionResult result = ((UseBlockCallback)UseBlockCallback.EVENT.invoker()).interact(player, level, InteractionHand.MAIN_HAND, hit);
      return result != InteractionResult.FAIL;
   }
}
