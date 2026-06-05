/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond.FilterMode;
import buildcraft.transport.tile.TilePipeHolder;

@SuppressWarnings("this-escape")
public class ContainerDiamondWoodPipe extends ContainerBC_Neptune {
    private static final int NET_FILTER_MODE = 1;

    @javax.annotation.Nullable
    private final IPipeHolder pipeHolder;
    @javax.annotation.Nullable
    public final PipeBehaviourWoodDiamond behaviour;

    public ContainerDiamondWoodPipe(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getBehaviour(playerInv, pos));
    }

    public ContainerDiamondWoodPipe(int containerId, Inventory playerInv, PipeBehaviourWoodDiamond behaviour) {
        super(BCTransportMenuTypes.DIAMOND_WOOD_PIPE, containerId, playerInv.player);
        this.behaviour = behaviour;
        if (behaviour == null) {
            this.pipeHolder = null;
            addFullPlayerInventory(8, 79);
            return;
        }
        this.pipeHolder = behaviour.pipe.getHolder();

        for (int i = 0; i < 9; i++) {
            addSlot(new SlotPhantom(behaviour.filters, i, 8 + i * 18, 18));
        }

        addFullPlayerInventory(8, 79);
    }

    private static PipeBehaviourWoodDiamond getBehaviour(Inventory playerInv, BlockPos pos) {
        if (playerInv.player.level() != null) {
            var be = playerInv.player.level().getBlockEntity(pos);
            if (be instanceof TilePipeHolder holder && holder.getPipe() != null) {
                if (holder.getPipe().getBehaviour() instanceof PipeBehaviourWoodDiamond wd) {
                    return wd;
                }
            }
        }
        BCLog.logger.warn("[transport.gui] No wood-diamond pipe behaviour at {}", pos);
        return null;
    }

    public void sendNewFilterMode(FilterMode newFilterMode) {
        this.sendMessage(NET_FILTER_MODE, (buffer) -> buffer.writeEnum(newFilterMode));
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient,
            buildcraft.fabric.network.BCPayloadContext ctx) {
        super.readMessage(id, buffer, isClient, ctx);
        if (id == NET_FILTER_MODE && !isClient && behaviour != null) {
            behaviour.filterMode = buffer.readEnum(FilterMode.class);
            behaviour.pipe.getHolder().scheduleNetworkUpdate(
                    buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return pipeHolder != null && pipeHolder.canPlayerInteract(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {

        return ItemStack.EMPTY;
    }
}
