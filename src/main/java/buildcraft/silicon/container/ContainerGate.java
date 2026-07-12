/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.fabric.menu.GateMenuKey;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.StatementWrapper;
import buildcraft.lib.statement.TriggerWrapper;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.gate.GateContext;
import buildcraft.silicon.gate.GateLogic;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.transport.tile.TilePipeHolder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerGate extends BcMenu {
   public static final int ID_CONNECTION = 1;
   public static final int ID_VALID_STATEMENTS = 2;
   public static final int ID_STATEMENT_CHANGE = 3;
   @Nullable
   public final GateLogic gate;
   @Nullable
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
      if (inv.player.level() != null
         && inv.player.level().getBlockEntity(pos) instanceof TilePipeHolder holder
         && holder.getPluggable(side) instanceof PluggableGate gatePlug) {
         return gatePlug;
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
      } else {
         this.gate = pluggable.logic;
         this.pipeHolder = pluggable.holder;
         this.pipeHolder.onPlayerOpen(this.player);
         boolean split = this.gate.isSplitInTwo();
         int s = this.gate.variant.numSlots;
         if (split) {
            s = (int)Math.ceil(s / 2.0);
         }

         this.slotHeight = s;
         if (this.pipeHolder.getPipeWorld().isClientSide()) {
            this.possibleTriggers = new TreeSet<>();
            this.possibleActions = new TreeSet<>();
            this.gate.guiMessageOverride = writer -> this.sendMessage(3, writer);
         } else {
            this.possibleTriggers = this.gate.getAllValidTriggers();
            this.possibleActions = this.gate.getAllValidActions();
         }

         this.possibleTriggersContext = new GateContext<>(new ArrayList<>());
         this.possibleActionsContext = new GateContext<>(new ArrayList<>());
         this.refreshPossibleGroups();
         if (!this.pipeHolder.getPipeWorld().isClientSide()) {
            this.gate.sendResolveData();
         }

         this.addFullPlayerInventory(8, 32 + this.slotHeight * 18);
      }
   }

   @Override
   public boolean stillValid(Player player) {
      return this.pipeHolder != null && this.pipeHolder.canPlayerInteract(player);
   }

   public void removed(Player player) {
      super.removed(player);
      if (this.pipeHolder != null) {
         this.pipeHolder.onPlayerClose(player);
      }
   }

   private void refreshPossibleGroups() {
      refresh(this.possibleActions, this.possibleActionsContext);
      refresh(this.possibleTriggers, this.possibleTriggersContext);
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

      to.groups.add(new GateContext.GateGroup<>(EnumPipePart.CENTER, list));

      for (EnumPipePart part : EnumPipePart.FACES) {
         list = parts.get(part);
         if (list != null) {
            to.groups.add(new GateContext.GateGroup<>(part, list));
         }
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      super.readMessage(id, buffer, isClient, ctx);
      if (this.gate != null && this.pipeHolder != null) {
         if (!isClient) {
            if (id == 1) {
               int index = buffer.readUnsignedByte();
               boolean to = buffer.readBoolean();
               if (index < this.gate.connections.length) {
                  this.gate.connections[index] = to;
                  if (this.pipeHolder instanceof BlockEntity be) {
                     be.setChanged();
                     this.pipeHolder.scheduleRenderUpdate();
                  }

                  this.gate.sendResolveData();
               }
            } else if (id == 2) {
               this.sendStatementsToClient();
            } else if (id == 3) {
               try {
                  this.gate.readPayload(buffer, false);
                  if (this.pipeHolder instanceof BlockEntity be) {
                     be.setChanged();
                     this.pipeHolder.scheduleRenderUpdate();
                  }

                  this.gate.sendResolveData();
               } catch (Exception e) {
                  BCLog.logger.error("[gate.sync] Error handling statement change", e);
               }
            }
         } else if (id == 2) {
            this.possibleTriggers.clear();
            this.possibleActions.clear();
            int numTriggers = buffer.readInt();
            int numActions = buffer.readInt();

            for (int i = 0; i < numTriggers; i++) {
               String tag = buffer.readUtf();
               EnumPipePart part = (EnumPipePart)buffer.readEnum(EnumPipePart.class);
               IStatement state = StatementManager.statements.get(tag);
               if (state == null) {
                  BCLog.logger.warn("Gate received invalid trigger tag from server: " + tag);
               } else {
                  TriggerWrapper wrapper = TriggerWrapper.wrap(state, part.face);
                  if (this.gate.isValidTrigger(wrapper)) {
                     this.possibleTriggers.add(wrapper);
                  }
               }
            }

            for (int i = 0; i < numActions; i++) {
               String tag = buffer.readUtf();
               EnumPipePart part = (EnumPipePart)buffer.readEnum(EnumPipePart.class);
               IStatement state = StatementManager.statements.get(tag);
               if (state == null) {
                  BCLog.logger.warn("Gate received invalid action tag: " + tag);
               } else {
                  ActionWrapper wrapper = ActionWrapper.wrap(state, part.face);
                  if (this.gate.isValidAction(wrapper)) {
                     this.possibleActions.add(wrapper);
                  }
               }
            }

            this.refreshPossibleGroups();

            for (int i = 0; i < this.gate.connections.length; i++) {
               this.gate.connections[i] = buffer.readBoolean();
            }
         }
      }
   }

   private void sendStatementsToClient() {
      this.sendMessage(2, buffer -> {
         buffer.writeInt(this.possibleTriggers.size());
         buffer.writeInt(this.possibleActions.size());

         for (TriggerWrapper wrapper : this.possibleTriggers) {
            buffer.writeUtf(wrapper.getUniqueTag());
            buffer.writeEnum(wrapper.sourcePart);
         }

         for (ActionWrapper wrapper : this.possibleActions) {
            buffer.writeUtf(wrapper.getUniqueTag());
            buffer.writeEnum(wrapper.sourcePart);
         }

         for (int i = 0; i < this.gate.connections.length; i++) {
            buffer.writeBoolean(this.gate.connections[i]);
         }
      });
   }

   public void requestValidStatements() {
      this.sendMessage(2, buffer -> {});
   }

   public void setConnected(int index, boolean to) {
      this.sendMessage(1, buffer -> {
         buffer.writeByte(index);
         buffer.writeBoolean(to);
      });
   }
}
