/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.tools;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public interface IToolWrench {
   boolean canWrench(Player var1, InteractionHand var2, ItemStack var3, HitResult var4);

   void wrenchUsed(Player var1, InteractionHand var2, ItemStack var3, HitResult var4);
}
