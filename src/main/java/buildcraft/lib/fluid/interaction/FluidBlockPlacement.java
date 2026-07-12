/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.interaction;

import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FluidBlockPlacement {
   private FluidBlockPlacement() {
   }

   public static boolean tryPlaceFluid(FluidStack resource, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
      FluidStack stack = resource.copyWithAmount(1000);
      if (stack.isEmpty() || stack.getFluid().isSame(Fluids.EMPTY)) {
         return false;
      }

      if (level instanceof ServerLevel serverLevel
         && player != null
         && !BlockUtil.canMachinePlace(serverLevel, pos, player.getGameProfile(), player.blockPosition())) {
         return false;
      }

      ItemStack handItem = player == null ? ItemStack.EMPTY : player.getItemInHand(hand);
      BlockPlaceContext context = new BlockPlaceContext(level, player, hand, handItem, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
      BlockState destBlockState = level.getBlockState(pos);
      boolean isDestReplaceable = destBlockState.canBeReplaced(context);
      boolean canDestContainFluid = destBlockState.getBlock() instanceof LiquidBlockContainer lbc
         && lbc.canPlaceLiquid(player, level, pos, destBlockState, stack.getFluid());
      if (!destBlockState.isAir() && !isDestReplaceable && !canDestContainFluid) {
         return false;
      }

      if (stack.is(Fluids.WATER) && level.dimension() == Level.NETHER) {
         level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F);
         return true;
      }

      if (canDestContainFluid) {
         LiquidBlockContainer lbc = (LiquidBlockContainer)destBlockState.getBlock();
         lbc.placeLiquid(level, pos, destBlockState, stack.getFluid().defaultFluidState());
      } else {
         if (!level.isClientSide() && isDestReplaceable && !BlockUtil.isLiquid(destBlockState)) {
            level.destroyBlock(pos, true);
         }

         BlockState state = stack.getFluid().defaultFluidState().createLegacyBlock();
         level.setBlock(pos, state, 11);
      }

      FluidWorldFeedback.playAtBlockOrPlayer(stack, level, pos, player, false);
      return true;
   }
}
