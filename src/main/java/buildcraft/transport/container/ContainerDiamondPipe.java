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

import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import buildcraft.transport.tile.TilePipeHolder;

@SuppressWarnings("this-escape")
public class ContainerDiamondPipe extends ContainerBC_Neptune {
    @javax.annotation.Nullable
    private final IPipeHolder pipeHolder;
    @javax.annotation.Nullable
    private final PipeBehaviourDiamond behaviour;

    public ContainerDiamondPipe(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getBehaviour(playerInv, pos));
    }

    public ContainerDiamondPipe(int containerId, Inventory playerInv, PipeBehaviourDiamond behaviour) {
        super(BCTransportMenuTypes.DIAMOND_PIPE, containerId, playerInv.player);
        this.behaviour = behaviour;
        if (behaviour == null) {
            this.pipeHolder = null;
            addFullPlayerInventory(8, 140);
            return;
        }
        this.pipeHolder = behaviour.pipe.getHolder();

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new SlotPhantom(behaviour.filters, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }

        addFullPlayerInventory(8, 140);
    }

    private static PipeBehaviourDiamond getBehaviour(Inventory playerInv, BlockPos pos) {
        if (playerInv.player.level() != null) {
            var be = playerInv.player.level().getBlockEntity(pos);
            if (be instanceof TilePipeHolder holder && holder.getPipe() != null) {
                if (holder.getPipe().getBehaviour() instanceof PipeBehaviourDiamond diamond) {
                    return diamond;
                }
            }
        }
        BCLog.logger.warn("[transport.gui] No diamond pipe behaviour at {}", pos);
        return null;
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
