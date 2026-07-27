/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.crops;

import buildcraft.api.crops.ICropHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public enum CropHandlerPlantable implements ICropHandler {
   INSTANCE;

   @Override
   public boolean isSeed(ItemStack stack) {
      if (stack.getItem() instanceof BlockItem blockItem) {
         Block block = blockItem.getBlock();
         if (block instanceof BushBlock && block != Blocks.SUGAR_CANE) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean canSustainPlant(Level world, ItemStack seed, BlockPos pos) {
      if (seed.getItem() instanceof BlockItem blockItem) {
         Block var8 = blockItem.getBlock();
         BlockPos placePos = pos.above();
         if (!world.isEmptyBlock(placePos)) {
            return false;
         }

         BlockState cropState = var8.defaultBlockState();
         return cropState.canSurvive(world, placePos);
      } else {
         return false;
      }
   }

   @Override
   public boolean plantCrop(Level world, Player player, ItemStack seed, BlockPos pos) {
      BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
      player.setItemInHand(InteractionHand.MAIN_HAND, seed);
      UseOnContext ctx = new UseOnContext(world, player, InteractionHand.MAIN_HAND, seed, hit);
      return seed.useOn(ctx).consumesAction();
   }

   @Override
   public boolean isMature(BlockGetter blockAccess, BlockState state, BlockPos pos) {
      Block block = state.getBlock();
      if (block instanceof FlowerBlock
         || block instanceof TallGrassBlock
         || block == Blocks.MELON
         || block instanceof MushroomBlock
         || block instanceof DoublePlantBlock
         || block == Blocks.PUMPKIN) {
         return true;
      } else if (block instanceof CropBlock cropBlock) {
         return cropBlock.isMaxAge(state);
      } else {
         return block instanceof NetherWartBlock
            ? (Integer)state.getValue(NetherWartBlock.AGE) == 3
            : block instanceof BushBlock && blockAccess.getBlockState(pos.below()).getBlock() == block;
      }
   }

   @Override
   public boolean harvestCrop(Level world, BlockPos pos, NonNullList<ItemStack> drops) {
      if (!world.isClientSide() && world instanceof ServerLevel serverLevel) {
         BlockState state = world.getBlockState(pos);
         Block.getDrops(state, serverLevel, pos, world.getBlockEntity(pos)).forEach(drops::add);
         world.destroyBlock(pos, false);
         return !drops.isEmpty();
      } else {
         return false;
      }
   }
}
