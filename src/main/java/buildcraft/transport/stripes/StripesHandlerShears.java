/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import buildcraft.lib.common.IShearable;
import buildcraft.lib.misc.BlockUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum StripesHandlerShears implements IStripesHandlerItem {
   INSTANCE;

   @Override
   public boolean handle(Level world, BlockPos pos, Direction direction, ItemStack stack, Player player, IStripesActivator activator) {
      if (!(stack.getItem() instanceof ShearsItem)) {
         return false;
      } else {
         BlockPos target = pos.relative(direction);
         BlockState state = world.getBlockState(target);
         if (!(state.getBlock() instanceof IShearable shearableBlock && shearableBlock.isShearable(player, stack, world, target))) {
            return false;
         } else if (world instanceof ServerLevel serverLevel && !BlockUtil.canMachineBreak(serverLevel, target, player.getGameProfile())) {
            return false;
         } else {
            List<ItemStack> drops = shearableBlock.onSheared(player, stack, world, target);
            stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
            world.setBlock(target, Blocks.AIR.defaultBlockState(), 3);

            for (ItemStack dropStack : drops) {
               activator.sendItem(dropStack, direction);
            }

            return true;
         }
      }
   }
}
