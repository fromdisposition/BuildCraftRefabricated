/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.IPayloadWriter;
import net.minecraft.network.FriendlyByteBuf;

public abstract class Widget_Neptune<C extends BcMenu> {
   public final C container;

   public Widget_Neptune(C container) {
      this.container = container;
   }

   public boolean isRemote() {
      return this.container.player.level().isClientSide();
   }

   protected final void sendWidgetData(IPayloadWriter writer) {
      this.container.sendWidgetData(this, writer);
   }

   public void handleWidgetDataServer(BCPayloadContext ctx, FriendlyByteBuf buffer) {
   }

   public void handleWidgetDataClient(BCPayloadContext ctx, FriendlyByteBuf buffer) {
   }
}
