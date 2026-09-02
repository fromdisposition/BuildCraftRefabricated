/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.lib.block.BcHorizontalTileBlock;

import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.tile.TileConstructionMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockConstructionMarker extends BcHorizontalTileBlock {
   public BlockConstructionMarker(Properties properties) {
      super(properties);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileConstructionMarker(pos, state);
   }

   protected InteractionResult bcUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (!(stack.getItem() instanceof ItemSnapshot snapshot) || !snapshot.isUsed()) {
         return InteractionResult.PASS;
      }

      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileConstructionMarker marker && !marker.hasBlueprint()) {
         marker.setBlueprint(stack.copyWithCount(1));
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
      }

      return InteractionResult.SUCCESS;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileConstructionMarker marker && marker.hasBlueprint()) {
         ItemStack removed = marker.removeBlueprint();
         if (!removed.isEmpty() && !player.addItem(removed)) {
            Block.popResource(level, pos, removed);
         }
      }

      return InteractionResult.SUCCESS;
   }

}
