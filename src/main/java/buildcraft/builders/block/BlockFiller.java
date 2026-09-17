/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.lib.block.BcHorizontalTileBlock;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.tile.TileFiller;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockFiller extends BcHorizontalTileBlock {
   public BlockFiller(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileFiller(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return blockEntityType != BCBuildersBlockEntities.FILLER ? null : (lvl, pos, st, be) -> {
         if (be instanceof TileFiller filler) {
            filler.tick();
         }
      };
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return this.openGui(level, pos, player);
   }

   @Override
   protected InteractionResult bcUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return stack.isEmpty() ? this.useWithoutItem(state, level, pos, player, hitResult) : this.openGui(level, pos, player);
   }

   private InteractionResult openGui(Level level, BlockPos pos, Player player) {
      if (level.getBlockEntity(pos) instanceof TileFiller filler) {
         if (!filler.hasBox()) {
            return InteractionResult.PASS;
         }

         if (!level.isClientSide()) {
            player.openMenu(filler);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

}
