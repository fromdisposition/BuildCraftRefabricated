/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.lib.block.BcTileBlock;


import buildcraft.lib.fabric.transfer.fluid.FluidStorageInteractions;
import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileDistiller;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockDistiller extends BcTileBlock implements ICustomRotationHandler {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

   public BlockDistiller(Properties properties) {
      super(properties);
      this.registerDefaultState((this.stateDefinition.any()).setValue(FACING, Direction.WEST));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TileDistiller distiller) {
         distiller.onPlacedBy(placer);
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileDistiller(pos, state);
   }

   // Vanilla only calls the BE hook preRemoveSideEffects from 1.21.2; below that, drop the fluid shards here instead.
   //? if < 1.21.10 {
   /*@Override
   protected void onRemove(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof buildcraft.factory.tile.TileDistiller tile) {
         tile.preRemoveSideEffects(pos, state);
      }
      super.onRemove(state, level, pos, newState, movedByPiston);
   }
   *///?}

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide()
         ? createTickerHelper(type, BCFactoryBlockEntities.DISTILLER, (lvl, pos, st, tile) -> tile.clientTick())
         : createTickerHelper(type, BCFactoryBlockEntities.DISTILLER, (lvl, pos, st, tile) -> tile.serverTick());
   }

   @Override
   protected InteractionResult bcUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (stack.isEmpty()) {
         return this.useWithoutItem(state, level, pos, player, hitResult);
      }

      if (EntityUtil.isWrench(stack)) {
         if (player.isShiftKeyDown()) {
            openMenuOnServer(level, player, pos);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      } else if (level.getBlockEntity(pos) instanceof TileDistiller distiller) {
         Storage<FluidVariant> storage = distiller.getSidedFluidStorage(hitResult.getDirection());
         boolean didChange = storage != null && FluidStorageInteractions.onTankActivated(player, pos, hand, storage);

         if (didChange) {
            return InteractionResult.SUCCESS;
         }

         if (!FluidStorageInteractions.isFluidContainerInHand(player, hand)) {
            openMenuOnServer(level, player, pos);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public InteractionResult attemptRotation(Level level, BlockPos pos, BlockState state, Direction sideWrenched) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      Direction current = state.getValue(FACING);
      level.setBlock(pos, state.setValue(FACING, current.getClockWise()), 3);
      return InteractionResult.SUCCESS;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      openMenuOnServer(level, player, pos);
      return InteractionResult.SUCCESS;
   }

   private static void openMenuOnServer(Level level, Player player, BlockPos pos) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileDistiller distiller) {
         player.openMenu(distiller);
      }
   }
}
