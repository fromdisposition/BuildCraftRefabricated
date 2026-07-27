/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.IPayloadWriter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

/** Marker interface shared by {@link BcMenu} and {@link BcMenuRecipeBook}. */
public interface IBcMenu {
   Player getPlayer();
   void sendMessage(int id, IPayloadWriter writer);
   void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx);
   boolean stillValid(Player player);
   void sendWidgetData(Widget_Neptune<?> widget, IPayloadWriter writer);
}
