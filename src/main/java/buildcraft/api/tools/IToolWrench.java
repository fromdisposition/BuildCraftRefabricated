/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.tools;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;

public interface IToolWrench {

    boolean canWrench(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace);

    void wrenchUsed(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace);
}
