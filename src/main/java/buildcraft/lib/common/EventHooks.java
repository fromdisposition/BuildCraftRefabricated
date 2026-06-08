/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.common;

import buildcraft.lib.fluids.FluidStack;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class EventHooks {
   private EventHooks() {
   }

   public static void onFluidTooltip(FluidStack stack, Player player, List<Component> tooltips, TooltipFlag flag, TooltipContext context) {
   }
}
