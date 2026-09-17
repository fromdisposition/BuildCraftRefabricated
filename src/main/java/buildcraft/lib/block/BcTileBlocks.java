/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import buildcraft.lib.tile.BcBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

final class BcTileBlocks {
   static void onPlacedBy(Level level, BlockPos pos, @Nullable LivingEntity placer, ItemStack stack) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BcBlockEntity tile) {
         tile.onPlacedBy(placer, stack);
      }
   }

   static InteractionResult openMenu(Level level, BlockPos pos, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider provider) {
         player.openMenu(provider);
      }

      return InteractionResult.SUCCESS;
   }

   static void preRemove(BlockState state, Level level, BlockPos pos, BlockState newState) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BcBlockEntity tile) {
         tile.preRemoveSideEffects(pos, state);
      }
   }

   @Nullable
   @SuppressWarnings("unchecked")
   static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> ticker(BlockEntityType<A> given, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker) {
      return expected == given ? (BlockEntityTicker<A>) ticker : null;
   }

   private BcTileBlocks() {
   }
}
