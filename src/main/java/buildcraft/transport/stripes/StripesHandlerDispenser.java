/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public enum StripesHandlerDispenser implements IStripesHandlerItem {
   INSTANCE;

   @Override
   public boolean handle(Level world, BlockPos pos, Direction direction, ItemStack stack, Player player, IStripesActivator activator) {
      if (!(world instanceof ServerLevel)) {
         return false;
      }

      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      BlockPos target = pos.relative(direction);
      BlockHitResult hitDirect = new BlockHitResult(Vec3.atCenterOf(target), direction.getOpposite(), target, false);
      UseOnContext ctxDirect = new UseOnContext(world, player, InteractionHand.MAIN_HAND, stack, hitDirect);
      if (stack.useOn(ctxDirect).consumesAction()) {
         return true;
      }

      BlockHitResult hitFromPipe = new BlockHitResult(Vec3.atCenterOf(pos), direction, pos, false);
      UseOnContext ctxFromPipe = new UseOnContext(world, player, InteractionHand.MAIN_HAND, stack, hitFromPipe);
      //? if >= 1.21.10 {
      return stack.useOn(ctxFromPipe).consumesAction() ? true : stack.getItem().use(world, player, InteractionHand.MAIN_HAND).consumesAction();
      //?} else {
      /*return stack.useOn(ctxFromPipe).consumesAction() ? true : stack.getItem().use(world, player, InteractionHand.MAIN_HAND).getResult().consumesAction();
      *///?}
   }
}
