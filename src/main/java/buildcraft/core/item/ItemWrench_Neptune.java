/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.lib.misc.BreakEventCompat;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.registries.BuiltInRegistries;

public class ItemWrench_Neptune extends Item {
   public ItemWrench_Neptune(Properties properties) {
      super(properties);
   }

   public InteractionResult useOn(UseOnContext context) {
      InteractionResult result = applyWrench(context);
      // PASS would fall through to the off-hand item and place a block. Only this item consumes: applyWrench
      // keeps PASS so a foreign wrench's own block interaction still runs.
      return result == InteractionResult.PASS ? InteractionResult.CONSUME : result;
   }

   /** Also serves foreign c:tools/wrench items; returns PASS when nothing was done so the interaction continues. */
   public static InteractionResult applyWrench(UseOnContext context) {
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Player player = context.getPlayer();
      InteractionHand hand = context.getHand();
      Direction side = context.getClickedFace();
      BlockState state = world.getBlockState(pos);

      if (player != null && player.isShiftKeyDown()) {
         return tryDismantle(context, world, pos, player, hand, side, state);
      }

      InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
      if (result == InteractionResult.SUCCESS && player != null) {
         BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), side, pos, context.isInside());
         EntityUtil.wrenchUsed(player, hand, context.getItemInHand(), hitResult);
      }

      SoundUtil.playSlideSound(world, pos, state, result);
      return result;
   }

   private static InteractionResult tryDismantle(
      UseOnContext context, Level world, BlockPos pos, Player player, InteractionHand hand, Direction side, BlockState state
   ) {
      Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
      if (blockId == null || !blockId.getNamespace().startsWith("buildcraft")) {
         return InteractionResult.PASS;
      }
      if (blockId.getPath().contains("pipe")) {
         return InteractionResult.PASS;
      }
      if (blockId.getPath().equals("frame")) {
         // The quarry frame is a technical block that must stay non-obtainable.
         return InteractionResult.PASS;
      }
      if (blockId.getPath().startsWith("spring")) {
         // Springs are hidden worldgen technical blocks; never obtainable.
         return InteractionResult.PASS;
      }
      if (!(state.getBlock() instanceof EntityBlock)) {
         // Non-machine blocks must break normally so their own loot applies; some, like gel, have no item form.
         return InteractionResult.PASS;
      }

      if (!world.isClientSide()) {
         // Dismantling is a break and must clear the same claim protection. The real player is passed, not
         // canMachineBreak's fake one, which is for machines mining someone else's land.
         if (!world.mayInteract(player, pos)
            || world instanceof ServerLevel serverLevel && !BreakEventCompat.canBreak(serverLevel, pos, state, player)) {
            return InteractionResult.PASS;
         }

         // removeBlock skips playerWillDestroy, which drops machine contents and tears down quarry frames.
         state.getBlock().playerWillDestroy(world, pos, state, player);
         ItemStack drop = new ItemStack(state.getBlock().asItem());
         if (!drop.isEmpty()) {
            Block.popResource(world, pos, drop);
         }
         world.removeBlock(pos, false);
         SoundUtil.playSlideSound(world, pos, state, InteractionResult.SUCCESS);
      }

      BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), side, pos, context.isInside());
      EntityUtil.wrenchUsed(player, hand, context.getItemInHand(), hitResult);
      return InteractionResult.CONSUME;
   }
}
