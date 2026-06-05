/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.pipe.IPipeHolder;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.StatementWrapper;
import buildcraft.lib.statement.TriggerWrapper;

import buildcraft.silicon.gate.GateContext;
import buildcraft.silicon.gate.GateContext.GateGroup;
import buildcraft.silicon.gate.GateLogic;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.lib.fabric.menu.GateMenuKey;
import buildcraft.silicon.BCSiliconMenuTypes;

@SuppressWarnings("this-escape")
public class ContainerGate extends ContainerBC_Neptune {
    public static final int ID_CONNECTION = 1;
    public static final int ID_VALID_STATEMENTS = 2;
    public static final int ID_STATEMENT_CHANGE = 3;

    @javax.annotation.Nullable
    public final GateLogic gate;
    @javax.annotation.Nullable
    public final IPipeHolder pipeHolder;

    public final int slotHeight;

    public final SortedSet<TriggerWrapper> possibleTriggers;
    public final SortedSet<ActionWrapper> possibleActions;

    public final GateContext<TriggerWrapper> possibleTriggersContext;
    public final GateContext<ActionWrapper> possibleActionsContext;

    public ContainerGate(int containerId, Inventory playerInv, GateMenuKey key) {
        this(containerId, playerInv, getPluggableGate(playerInv, key));
    }

    private static PluggableGate getPluggableGate(Inventory inv, GateMenuKey key) {
        BlockPos pos = key.pos();
        Direction side = key.side();
        if (inv.player.level() != null) {
            var be = inv.player.level().getBlockEntity(pos);
            if (be instanceof buildcraft.transport.tile.TilePipeHolder holder) {
                var plug = holder.getPluggable(side);
                if (plug instanceof PluggableGate gatePlug) {
                    return gatePlug;
                }
            }
        }
        BCLog.logger.warn("[silicon.gui] No gate pluggable at {} side {}", pos, side);
        return null;
    }

    public ContainerGate(int containerId, Inventory playerInv, PluggableGate pluggable) {
        super(BCSiliconMenuTypes.GATE, containerId, playerInv.player);
        if (pluggable == null) {
            this.gate = null;
            this.pipeHolder = null;
            this.slotHeight = 0;
            this.possibleTriggers = new TreeSet<>();
            this.possibleActions = new TreeSet<>();
            this.possibleTriggersContext = new GateContext<>(new ArrayList<>());
            this.possibleActionsContext = new GateContext<>(new ArrayList<>());
            return;
        }
        this.gate = pluggable.logic;
        this.pipeHolder = pluggable.holder;

        this.pipeHolder.onPlayerOpen(player);

        boolean split = gate.isSplitInTwo();
        int s = gate.variant.numSlots;
        if (split) {
            s = (int) Math.ceil(s / 2.0);
        }
        slotHeight = s;

        if (this.pipeHolder.getPipeWorld().isClientSide()) {
            possibleTriggers = new TreeSet<>();
            possibleActions = new TreeSet<>();
            gate.guiMessageOverride = writer -> sendMessage(ID_STATEMENT_CHANGE, writer);
        } else {
            possibleTriggers = gate.getAllValidTriggers();
            possibleActions = gate.getAllValidActions();
        }

        possibleTriggersContext = new GateContext<>(new ArrayList<>());
        possibleActionsContext = new GateContext<>(new ArrayList<>());

        refreshPossibleGroups();

        addFullPlayerInventory(8, 33 + slotHeight * 18);
    }

    @Override
    public boolean stillValid(Player player) {
        return pipeHolder != null && pipeHolder.canPlayerInteract(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (pipeHolder != null) {
            pipeHolder.onPlayerClose(player);
        }
    }

    private void refreshPossibleGroups() {
        refresh(possibleActions, possibleActionsContext);
        refresh(possibleTriggers, possibleTriggersContext);
    }

    private static <T extends StatementWrapper> void refresh(SortedSet<T> from, GateContext<T> to) {
        to.groups.clear();
        Map<EnumPipePart, List<T>> parts = new EnumMap<>(EnumPipePart.class);
        for (T val : from) {
            parts.computeIfAbsent(val.sourcePart, p -> new ArrayList<>()).add(val);
        }
        List<T> list = parts.get(EnumPipePart.CENTER);
        if (list == null) {
            list = new ArrayList<>(1);
            list.add(null);
        } else {
            list.add(0, null);
        }
        to.groups.add(new GateGroup<>(EnumPipePart.CENTER, list));
        for (EnumPipePart part : EnumPipePart.FACES) {
            list = parts.get(part);
            if (list != null) {
                to.groups.add(new GateGroup<>(part, list));
            }
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
        super.readMessage(id, buffer, isClient, ctx);
        if (gate == null || pipeHolder == null) {
            return;
        }
        if (!isClient) {
            if (id == ID_CONNECTION) {
                int index = buffer.readUnsignedByte();
                boolean to = buffer.readBoolean();
                if (index < gate.connections.length) {
                    gate.connections[index] = to;
                    if (pipeHolder instanceof net.minecraft.world.level.block.entity.BlockEntity be) {
                        be.setChanged();
                        pipeHolder.scheduleRenderUpdate();
                    }
                    gate.sendResolveData();
                }
            } else if (id == ID_VALID_STATEMENTS) {

                sendStatementsToClient();
            } else if (id == ID_STATEMENT_CHANGE) {
                try {
                    gate.readPayload(buffer, false);
                    if (pipeHolder instanceof net.minecraft.world.level.block.entity.BlockEntity be) {
                        be.setChanged();
                        pipeHolder.scheduleRenderUpdate();
                    }
                    gate.sendResolveData();
                } catch (Exception e) {
                    BCLog.logger.error("[gate.sync] Error handling statement change", e);
                }
            }
        } else {
            if (id == ID_VALID_STATEMENTS) {
                possibleTriggers.clear();
                possibleActions.clear();
                int numTriggers = buffer.readInt();
                int numActions = buffer.readInt();
                for (int i = 0; i < numTriggers; i++) {
                    String tag = buffer.readUtf();
                    EnumPipePart part = buffer.readEnum(EnumPipePart.class);
                    var state = StatementManager.statements.get(tag);
                    if (state == null) {
                        BCLog.logger.warn("Gate received invalid trigger tag from server: " + tag);
                        continue;
                    }
                    TriggerWrapper wrapper = TriggerWrapper.wrap(state, part.face);
                    if (gate.isValidTrigger(wrapper)) {
                        possibleTriggers.add(wrapper);
                    }
                }
                for (int i = 0; i < numActions; i++) {
                    String tag = buffer.readUtf();
                    EnumPipePart part = buffer.readEnum(EnumPipePart.class);
                    var state = StatementManager.statements.get(tag);
                    if (state == null) {
                        BCLog.logger.warn("Gate received invalid action tag: " + tag);
                        continue;
                    }
                    ActionWrapper wrapper = ActionWrapper.wrap(state, part.face);
                    if (gate.isValidAction(wrapper)) {
                        possibleActions.add(wrapper);
                    }
                }
                refreshPossibleGroups();

                for (int i = 0; i < gate.connections.length; i++) {
                    gate.connections[i] = buffer.readBoolean();
                }
            }
        }
    }

    private void sendStatementsToClient() {
        sendMessage(ID_VALID_STATEMENTS, (buffer) -> {
            buffer.writeInt(possibleTriggers.size());
            buffer.writeInt(possibleActions.size());
            for (TriggerWrapper wrapper : possibleTriggers) {
                buffer.writeUtf(wrapper.getUniqueTag());
                buffer.writeEnum(wrapper.sourcePart);
            }

            for (ActionWrapper wrapper : possibleActions) {
                buffer.writeUtf(wrapper.getUniqueTag());
                buffer.writeEnum(wrapper.sourcePart);
            }

            for (int i = 0; i < gate.connections.length; i++) {
                buffer.writeBoolean(gate.connections[i]);
            }
        });
    }

    public void requestValidStatements() {
        sendMessage(ID_VALID_STATEMENTS, buffer -> {});
    }

    public void setConnected(int index, boolean to) {
        sendMessage(ID_CONNECTION, (buffer) -> {
            buffer.writeByte(index);
            buffer.writeBoolean(to);
        });
    }
}
