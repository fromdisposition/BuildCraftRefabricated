/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import buildcraft.lib.compat.BcInteract;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BcTileBlock extends Block implements EntityBlock {
   @Nullable
   private final Supplier<? extends BlockEntityType<?>> tileType;
   @Nullable
   private final Consumer<?> serverTick;
   @Nullable
   private final Consumer<?> clientTick;
   private final boolean opensMenu;

   protected BcTileBlock(Properties properties) {
      this(properties, null, null, null, false);
   }

   public <T extends BlockEntity> BcTileBlock(
      Properties properties,
      @Nullable Supplier<BlockEntityType<T>> tileType,
      @Nullable Consumer<T> serverTick,
      @Nullable Consumer<T> clientTick,
      boolean opensMenu
   ) {
      super(properties);
      this.tileType = tileType;
      this.serverTick = serverTick;
      this.clientTick = clientTick;
      this.opensMenu = opensMenu;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.tileType == null ? null : this.tileType.get().create(pos, state);
   }

   @Nullable
   @Override
   @SuppressWarnings("unchecked")
   public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level level, BlockState state, BlockEntityType<A> type) {
      Consumer<?> tick = level.isClientSide() ? this.clientTick : this.serverTick;
      if (tick == null || this.tileType == null || type != this.tileType.get()) {
         return null;
      }

      Consumer<A> typed = (Consumer<A>) tick;
      return (lvl, pos, st, tile) -> typed.accept(tile);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return this.opensMenu ? this.openMenu(level, pos, player) : super.useWithoutItem(state, level, pos, player, hitResult);
   }

   @Nullable
   protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
      BlockEntityType<A> given, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker
   ) {
      return BcTileBlocks.ticker(given, expected, ticker);
   }

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      BcTileBlocks.onPlacedBy(level, pos, placer, stack);
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return BcInteract.toItem(this.bcUseItemOn(stack, state, level, pos, player, hand, hitResult));
   }

   protected InteractionResult bcUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return BcInteract.TRY_WITH_EMPTY_HAND;
   }

   protected InteractionResult openMenu(Level level, BlockPos pos, Player player) {
      return BcTileBlocks.openMenu(level, pos, player);
   }

   //? if < 1.21.10 {
   /*protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      BcTileBlocks.preRemove(state, level, pos, newState);
      super.onRemove(state, level, pos, newState, movedByPiston);
   }
   *///?}
}
