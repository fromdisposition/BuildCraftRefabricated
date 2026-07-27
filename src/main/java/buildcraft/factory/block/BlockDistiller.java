/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.lib.compat.BcInteract;


import buildcraft.lib.fabric.transfer.fluid.FluidStorageInteractions;
import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileDistiller;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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

public class BlockDistiller extends BaseEntityBlock implements ICustomRotationHandler {
   public static final MapCodec<BlockDistiller> CODEC = simpleCodec(BlockDistiller::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

   public BlockDistiller(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.WEST));
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TileDistiller distiller) {
         distiller.onPlacedBy(placer);
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileDistiller(pos, state);
   }

   // 1.21.1: vanilla never calls the BE hook preRemoveSideEffects (1.21.2+); drop the fluid shards from the
   // classic Block.onRemove (BE still present before super removes it). 1.21.10+ uses the BE hook directly.
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
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide()
         ? createTickerHelper(type, BCFactoryBlockEntities.DISTILLER, (lvl, pos, st, tile) -> tile.clientTick())
         : createTickerHelper(type, BCFactoryBlockEntities.DISTILLER, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return BcInteract.toItem(bcUseItemOn(stack, state, level, pos, player, hand, hitResult));
   }

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

      Direction current = (Direction)state.getValue(FACING);
      level.setBlock(pos, (BlockState)state.setValue(FACING, current.getClockWise()), 3);
      return InteractionResult.SUCCESS;
   }

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
