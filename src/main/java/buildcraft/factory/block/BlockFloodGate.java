/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.lib.block.BcTileBlock;
import buildcraft.lib.compat.BcInteract;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileFloodGate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockFloodGate extends BcTileBlock {
   public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = new HashMap<>(BuildCraftProperties.CONNECTED_MAP);

   public BlockFloodGate(Properties properties) {
      super(properties);
      BlockState defaultState = this.stateDefinition.any();

      for (Property<Boolean> prop : CONNECTED_MAP.values()) {
         defaultState = defaultState.setValue(prop, true);
      }

      this.registerDefaultState(defaultState);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      for (Property<Boolean> prop : CONNECTED_MAP.values()) {
         builder.add(prop);
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileFloodGate(pos, state);
   }

   // Vanilla only calls the BE hook preRemoveSideEffects from 1.21.2; below that, drop the fluid shard here instead.
   //? if < 1.21.10 {
   /*@Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof buildcraft.factory.tile.TileFloodGate tile) {
         tile.preRemoveSideEffects(pos, state);
      }
      super.onRemove(state, level, pos, newState, movedByPiston);
   }
   *///?}

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TileFloodGate floodGate) {
         floodGate.onPlacedBy(placer);
      }
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null : createTickerHelper(type, BCFactoryBlockEntities.FLOOD_GATE, (lvl, pos, st, tile) -> tile.serverTick());
   }

   @Override
   protected InteractionResult bcUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (!(EntityUtil.isWrench(stack))) {
         return BcInteract.TRY_WITH_EMPTY_HAND;
      } else {
         Direction side = hitResult.getDirection();
         if (side != Direction.UP && CONNECTED_MAP.containsKey(side)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileFloodGate floodGate) {
               boolean nowOpen;
               if (!floodGate.openSides.remove(side)) {
                  floodGate.openSides.add(side);
                  nowOpen = true;
               } else {
                  nowOpen = false;
               }

               floodGate.onSidesToggled();
               BlockState newState = state;

               for (Entry<Direction, Property<Boolean>> entry : CONNECTED_MAP.entrySet()) {
                  newState = newState.setValue(entry.getValue(), floodGate.openSides.contains(entry.getKey()));
               }

               level.setBlock(pos, newState, 2);
               floodGate.setChanged();
               level.playSound(
                  null,
                  pos,
                  nowOpen ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                  SoundSource.BLOCKS,
                  1.0F,
                  level.getRandom().nextFloat() * 0.1F + 0.9F
               );
            }

            EntityUtil.wrenchUsed(player, hand, stack, hitResult);
            return InteractionResult.SUCCESS;
         } else {
            return BcInteract.TRY_WITH_EMPTY_HAND;
         }
      }
   }

   static {
      CONNECTED_MAP.remove(Direction.UP);
   }
}
