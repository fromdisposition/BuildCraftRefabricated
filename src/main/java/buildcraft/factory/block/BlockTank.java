/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.lib.block.BcTileBlock;


import buildcraft.lib.fabric.transfer.fluid.FluidStorageInteractions;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.misc.AdvancementUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
//? if >= 1.21.10 {
import net.minecraft.world.level.ScheduledTickAccess;
//?}
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockTank extends BcTileBlock implements ITankBlockConnector {
   public static final Property<Boolean> JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftfactory:fluid_storage");
   private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

   public BlockTank(Properties properties) {
      super(properties);
      this.registerDefaultState((this.stateDefinition.any()).setValue(JOINED_BELOW, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(JOINED_BELOW);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileTank(pos, state);
   }

   // Vanilla only calls the BE hook preRemoveSideEffects from 1.21.2; below that, drop the fluid shard here instead.
   //? if < 1.21.10 {
   /*@Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof TileTank tile) {
            tile.preRemoveSideEffects(pos, state);
         }
      }
      super.onRemove(state, level, pos, newState, movedByPiston);
   }
   *///?}

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide()
         ? createTickerHelper(type, BCFactoryBlockEntities.TANK, (lvl, pos, st, tile) -> tile.clientTick())
         : createTickerHelper(type, BCFactoryBlockEntities.TANK, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   protected boolean isPathfindable(BlockState state, PathComputationType type) {
      return false;
   }

   protected boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
      return level.getBlockEntity(pos) instanceof TileTank tank ? tank.getComparatorLevel() : 0;
   }

   protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
      return side.getAxis() == Axis.Y && adjacentState.getBlock() instanceof ITankBlockConnector
         || super.skipRendering(state, adjacentState, side);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      boolean isTankBelow = level.getBlockState(pos.below()).getBlock() instanceof ITankBlockConnector;
      return this.defaultBlockState().setValue(JOINED_BELOW, isTankBelow);
   }

   //? if >= 1.21.10 {
   protected BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos,
      Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
   //?} else {
   /*protected BlockState updateShape(
      BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level,
      BlockPos pos, BlockPos neighborPos
   ) {
   *///?}
      if (direction == Direction.DOWN) {
         boolean isTankBelow = neighborState.getBlock() instanceof ITankBlockConnector;
         return state.setValue(JOINED_BELOW, isTankBelow);
      }

      //? if >= 1.21.10 {
      return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
      //?} else {
      /*return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
      *///?}
   }

   protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
      super.onPlace(state, level, pos, oldState, movedByPiston);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileTank tank) {
         tank.balanceTankFluids();
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level.getBlockEntity(pos) instanceof TileTank tank) {
         if (!level.isClientSide()) {
            player.openMenu(tank);
         }

         return InteractionResult.SUCCESS;
      }

      return InteractionResult.PASS;
   }

   protected InteractionResult bcUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (player.getItemInHand(hand).isEmpty()) {
         return this.useWithoutItem(state, level, pos, player, hitResult);
      }

      if (level.getBlockEntity(pos) instanceof TileTank tank) {
         boolean didChange = FluidStorageInteractions.onTankActivated(player, pos, hand, tank.getColumnFluidStorage());
         if (didChange) {
            if (!level.isClientSide()) {
               tank.balanceTankFluids();
               AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
            }

            return InteractionResult.SUCCESS;
         }

         if (!FluidStorageInteractions.isFluidContainerInHand(player, hand) && !level.isClientSide()) {
            player.openMenu(tank);
         }

         return InteractionResult.SUCCESS;
      }

      return InteractionResult.PASS;
   }
}
