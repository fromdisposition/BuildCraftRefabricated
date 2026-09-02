/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.blocks;


import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockEngineStone_BC8 extends BlockEngineBase_BC8 {
   public BlockEngineStone_BC8(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEngineStone_BC8(pos, state);
   }

   protected InteractionResult bcUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      boolean isWrench = EntityUtil.isWrench(stack);
      TileEngineBase_BC8 engine = level.getBlockEntity(pos) instanceof TileEngineBase_BC8 e ? e : null;
      if (isWrench && engine != null && engine.getPowerStage() == EnumPowerStage.OVERHEAT) {
         if (!level.isClientSide()) {
            engine.clearOverheat(player);
            SoundUtil.playSlideSound(level, pos, state, InteractionResult.SUCCESS);
         }

         player.swing(hand, player.getItemInHand(hand).getOrDefault(net.minecraft.core.component.DataComponents.INTERACT_ANIMATION, net.minecraft.world.item.component.SwingAnimation.DEFAULT), false);
         return InteractionResult.CONSUME;
      } else if (stack.getItem() instanceof IItemPipe pipe) {
         InteractionResult placed = EnginePipeInteraction.tryPlacePipe(pipe, stack, level, player, hand, hitResult, PipeApi.flowItems, PipeApi.flowPower);
         return placed != null ? placed : this.openGui(state, level, pos, player);
      } else {
         if (player.isShiftKeyDown()) {
            return this.openGui(state, level, pos, player);
         }

         if (isWrench) {
            if (engine != null && engine.hasAlternateReceiver()) {
               return InteractionResult.PASS;
            }

            if (!level.isClientSide()) {
               level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4F, 1.3F);
            }

            player.swing(hand, player.getItemInHand(hand).getOrDefault(net.minecraft.core.component.DataComponents.INTERACT_ANIMATION, net.minecraft.world.item.component.SwingAnimation.DEFAULT), false);
            return InteractionResult.CONSUME;
         } else {
            return this.openGui(state, level, pos, player);
         }
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return this.openGui(state, level, pos, player);
   }

   private InteractionResult openGui(BlockState state, Level level, BlockPos pos, Player player) {
      return EngineBlockGui.open(level, pos, player, TileEngineStone_BC8.class);
   }

   //? if < 1.21.10 {
   /*@Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileEngineStone_BC8 tile) {
         tile.preRemoveSideEffects(pos, state);
      }
      super.onRemove(state, level, pos, newState, movedByPiston);
   }
   *///?}
}
