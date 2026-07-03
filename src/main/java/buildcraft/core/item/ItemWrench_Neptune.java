/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.tools.IToolWrench;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.registries.BuiltInRegistries;

public class ItemWrench_Neptune extends Item implements IToolWrench {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:wrenched");

   public ItemWrench_Neptune(Properties properties) {
      super(properties);
   }

   @Override
   public boolean canWrench(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
      return true;
   }

   @Override
   public void wrenchUsed(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
      AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
      player.swing(hand);
   }

   public InteractionResult useOn(UseOnContext context) {
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Player player = context.getPlayer();
      InteractionHand hand = context.getHand();
      Direction side = context.getClickedFace();
      BlockState state = world.getBlockState(pos);

      // Classic BuildCraft semantics: a plain right-click rotates the block, sneak + right-click dismantles it.
      if (player != null && player.isShiftKeyDown()) {
         return this.tryDismantle(context, world, pos, player, hand, side, state);
      }

      // Plain right-click: rotate. attemptRotateBlock handles ICustomRotationHandler blocks (e.g. engines) and any
      // block with a registered rotation handler, and returns PASS for everything else.
      InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
      if (result == InteractionResult.SUCCESS && player != null) {
         BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), side, pos, context.isInside());
         this.wrenchUsed(player, hand, context.getItemInHand(), hitResult);
      }

      SoundUtil.playSlideSound(world, pos, state, result);
      return result;
   }

   private InteractionResult tryDismantle(
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
         // The quarry frame is a technical block the quarry builds and removes itself — don't let the wrench
         // dismantle it into an item (it also drops nothing when broken), so it stays non-obtainable in play.
         return InteractionResult.PASS;
      }

      if (!world.isClientSide()) {
         // Run the block's own break logic first: a machine drops its inventory and the quarry tears down its
         // frame lattice inside playerWillDestroy, which world.removeBlock skips. Without this the wrench would
         // silently void machine contents and orphan quarry frames.
         state.getBlock().playerWillDestroy(world, pos, state, player);
         ItemStack drop = new ItemStack(state.getBlock().asItem());
         if (!drop.isEmpty()) {
            Block.popResource(world, pos, drop);
         }
         world.removeBlock(pos, false);
         SoundUtil.playSlideSound(world, pos, state, InteractionResult.SUCCESS);
      }

      BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), side, pos, context.isInside());
      this.wrenchUsed(player, hand, context.getItemInHand(), hitResult);
      return InteractionResult.CONSUME;
   }
}
