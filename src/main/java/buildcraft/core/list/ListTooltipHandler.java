/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import buildcraft.api.items.IList;

public final class ListTooltipHandler {
    private ListTooltipHandler() {}

    public static void register() {
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register(ListTooltipHandler::onItemTooltip);
    }

    private static void onItemTooltip(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
            TooltipFlag flag, java.util.List<Component> lines) {
        Player player = net.minecraft.client.Minecraft.getInstance().player;
        if (stack.isEmpty() || player == null || !(player.containerMenu instanceof ContainerList containerList)) {
            return;
        }
        ItemStack list = containerList.getListItemStack();
        if (!list.isEmpty() && list.getItem() instanceof IList listItem && listItem.matches(list, stack)) {
            lines.add(Component.translatable("tip.list.matches").withStyle(ChatFormatting.GREEN));
        }
    }
}
