/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileChute;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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

public class BlockChute extends BaseEntityBlock {
   public static final MapCodec<BlockChute> CODEC = simpleCodec(BlockChute::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
   public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

   public BlockChute(Properties properties) {
      super(properties);
      BlockState defaultState = (BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.DOWN);

      for (Property<Boolean> prop : CONNECTED_MAP.values()) {
         defaultState = (BlockState)defaultState.setValue(prop, false);
      }

      this.registerDefaultState(defaultState);
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(FACING);
      for (Property<Boolean> prop : CONNECTED_MAP.values()) {
         builder.add(prop);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = (BlockState)this.defaultBlockState().setValue(FACING, context.getClickedFace());
      return computeAllConnections(context.getLevel(), context.getClickedPos(), state);
   }

   protected BlockState updateShape(
      BlockState state,
      LevelReader level,
      ScheduledTickAccess scheduledTickAccess,
      BlockPos pos,
      Direction direction,
      BlockPos neighborPos,
      BlockState neighborState,
      RandomSource random
   ) {
      Property<Boolean> prop = CONNECTED_MAP.get(direction);
      return prop != null ? (BlockState)state.setValue(prop, shouldConnect(level, pos, state, direction)) : state;
   }

   private static BlockState computeAllConnections(LevelReader level, BlockPos pos, BlockState state) {
      BlockState updated = state;

      for (Entry<Direction, Property<Boolean>> entry : CONNECTED_MAP.entrySet()) {
         updated = (BlockState)updated.setValue(entry.getValue(), shouldConnect(level, pos, state, entry.getKey()));
      }

      return updated;
   }

   private static boolean shouldConnect(LevelReader level, BlockPos pos, BlockState state, Direction direction) {
      if (direction == state.getValue(FACING)) {
         return false;
      } else {
         BlockPos neighborPos = pos.relative(direction);
         Direction toNeighbourFace = direction.getOpposite();
         if (level instanceof Level realLevel && BcTransfers.item(realLevel, neighborPos, toNeighbourFace) != null) {
            return true;
         } else {
            if (level.getBlockEntity(neighborPos) instanceof IPipeHolder holder) {
               IPipe pipe = holder.getPipe();
               if (pipe != null && pipe.getFlow() instanceof PipeFlowItems items && items.getInjectable(toNeighbourFace) != null) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileChute(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null : createTickerHelper(type, BCFactoryBlockEntities.CHUTE, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileChute chute) {
         chute.onPlacedBy(placer, stack);
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!level.isClientSide()) {
         BlockEntity be = level.getBlockEntity(pos);
         if (be instanceof TileChute) {
            player.openMenu((TileChute)be);
         }
      }

      return InteractionResult.SUCCESS;
   }
}
