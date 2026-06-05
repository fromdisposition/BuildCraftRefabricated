/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.fabric.network.BCPayloadContext;

public abstract class Widget_Neptune<C extends ContainerBC_Neptune> {
    public final C container;

    public Widget_Neptune(C container) {
        this.container = container;
    }

    public boolean isRemote() {
        return container.player.level().isClientSide();
    }

    protected final void sendWidgetData(IPayloadWriter writer) {
        container.sendWidgetData(this, writer);
    }

    public void handleWidgetDataServer(BCPayloadContext ctx, PacketBufferBC buffer) {

    }

    public void handleWidgetDataClient(BCPayloadContext ctx, PacketBufferBC buffer) {

    }
}
