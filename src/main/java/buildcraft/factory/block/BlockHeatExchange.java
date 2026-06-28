/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.lib.compat.BcInteract;


import buildcraft.lib.fabric.transfer.fluid.FluidStorageInteractions;
import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.tools.IToolWrench;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileHeatExchange;
import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
//? if >= 1.21.10 {
import net.minecraft.world.level.ScheduledTickAccess;
//?}
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
//? if >= 1.21.10 {
import net.minecraft.world.level.redstone.Orientation;
//?}
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockHeatExchange extends BaseEntityBlock implements ICustomRotationHandler {
   public static final MapCodec<BlockHeatExchange> CODEC = simpleCodec(BlockHeatExchange::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<BlockHeatExchange.EnumExchangePart> PART = EnumProperty.create("part", BlockHeatExchange.EnumExchangePart.class);
   public static final BooleanProperty CONNECTED_LEFT = BooleanProperty.create("connected_left");
   public static final BooleanProperty CONNECTED_RIGHT = BooleanProperty.create("connected_right");

   public BlockHeatExchange(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
                  .setValue(PART, BlockHeatExchange.EnumExchangePart.MIDDLE))
               .setValue(CONNECTED_LEFT, false))
            .setValue(CONNECTED_RIGHT, false)
      );
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, PART, CONNECTED_LEFT, CONNECTED_RIGHT});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction facing = context.getHorizontalDirection().getOpposite();
      BlockPos pos = context.getClickedPos();
      if (chainLengthIfPlaced(context.getLevel(), pos, facing) > TileHeatExchange.MAX_CHAIN_LENGTH) {
         return null;
      }

      BlockState state = (BlockState)this.defaultBlockState().setValue(FACING, facing);
      return this.updateConnections(state, context.getLevel(), pos, facing);
   }

   private static int chainLengthIfPlaced(LevelReader level, BlockPos pos, Direction facing) {
      return 1
         + countRun(level, pos, facing, facing.getCounterClockWise())
         + countRun(level, pos, facing, facing.getClockWise());
   }

   private static int countRun(LevelReader level, BlockPos pos, Direction facing, Direction along) {
      int count = 0;

      for (int i = 1; ; i++) {
         BlockState state = level.getBlockState(pos.relative(along, i));
         if (!(state.getBlock() instanceof BlockHeatExchange) || state.getValue(FACING) != facing) {
            break;
         }

         count++;
      }

      return count;
   }

   //? if >= 1.21.10 {
   protected BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos,
      Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource randomSource
   ) {
   //?} else {
   /*protected BlockState updateShape(
      BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level,
      BlockPos pos, BlockPos neighborPos
   ) {
   *///?}
      if (direction.getAxis().isVertical()) {
         return state;
      }

      Direction facing = (Direction)state.getValue(FACING);
      return this.updateConnections(state, level, pos, facing);
   }

   private BlockState updateConnections(BlockState state, LevelReader level, BlockPos pos, Direction facing) {
      boolean connectLeft = doesNeighbourConnect(level, pos, facing, facing.getCounterClockWise());
      boolean connectRight = doesNeighbourConnect(level, pos, facing, facing.getClockWise());
      return (BlockState)((BlockState)state.setValue(CONNECTED_LEFT, connectLeft)).setValue(CONNECTED_RIGHT, connectRight);
   }

   private static boolean doesNeighbourConnect(LevelReader level, BlockPos pos, Direction thisFacing, Direction dir) {
      BlockState neighbour = level.getBlockState(pos.relative(dir));
      return neighbour.getBlock() instanceof BlockHeatExchange ? neighbour.getValue(FACING) == thisFacing : false;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileHeatExchange(pos, state);
   }

   // 1.21.1: vanilla never calls the BE hook preRemoveSideEffects (1.21.2+); drop the fluid shards from the
   // classic Block.onRemove (BE still present before super removes it). 1.21.10+ uses the BE hook directly.
   //? if < 1.21.10 {
   /*@Override
   protected void onRemove(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof buildcraft.factory.tile.TileHeatExchange tile) {
         tile.preRemoveSideEffects(pos, state);
      }
      super.onRemove(state, level, pos, newState, movedByPiston);
   }
   *///?}

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide()
         ? createTickerHelper(type, BCFactoryBlockEntities.HEAT_EXCHANGE, (lvl, pos, st, tile) -> tile.clientTick())
         : createTickerHelper(type, BCFactoryBlockEntities.HEAT_EXCHANGE, (lvl, pos, st, tile) -> tile.serverTick());
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

      if (stack.getItem() instanceof IToolWrench) {
         if (player.isShiftKeyDown()) {
            return (InteractionResult)(level.getBlockEntity(pos) instanceof TileHeatExchange exchange
               ? openExchangeMenu(level, exchange, player)
               : InteractionResult.PASS);
         } else {
            return InteractionResult.PASS;
         }
      } else if (level.getBlockEntity(pos) instanceof TileHeatExchange exchange) {
         Storage<FluidVariant> storage = exchange.getSidedFluidStorage(hitResult.getDirection());
         boolean didChange = storage != null && FluidStorageInteractions.onTankActivated(player, pos, hand, storage);

         if (didChange) {
            return InteractionResult.SUCCESS;
         }

         return (InteractionResult)(FluidStorageInteractions.isFluidContainerInHand(player, hand) ? InteractionResult.SUCCESS : openExchangeMenu(level, exchange, player));
      } else {
         return InteractionResult.PASS;
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return (InteractionResult)(level.getBlockEntity(pos) instanceof TileHeatExchange exchange
         ? openExchangeMenu(level, exchange, player)
         : InteractionResult.PASS);
   }

   @Override
   public InteractionResult attemptRotation(Level level, BlockPos pos, BlockState state, Direction sideWrenched) {
      return (InteractionResult)(level.getBlockEntity(pos) instanceof TileHeatExchange exchange && exchange.rotate()
         ? InteractionResult.SUCCESS
         : InteractionResult.PASS);
   }

   private static InteractionResult openExchangeMenu(Level level, TileHeatExchange exchange, Player player) {
      TileHeatExchange start = exchange.findStart();
      if (start == null) {
         return InteractionResult.PASS;
      }

      if (!level.isClientSide()) {
         player.openMenu(start);
      }

      return InteractionResult.SUCCESS;
   }

   //? if >= 1.21.10 {
   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
      super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
   //?} else {
   /*protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
      super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
   *///?}
      if (level.getBlockEntity(pos) instanceof TileHeatExchange exchange) {
         exchange.markCheckNeighbours();
      }
   }

   public enum EnumExchangePart implements StringRepresentable {
      START,
      MIDDLE,
      END;

      private final String lowerCaseName = this.name().toLowerCase(Locale.ROOT);

      public String getSerializedName() {
         return this.lowerCaseName;
      }
   }
}
