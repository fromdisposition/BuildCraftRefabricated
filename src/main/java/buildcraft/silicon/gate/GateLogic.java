/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.IPayloadWriter;
import net.minecraft.network.FriendlyByteBuf;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.FullStatement;
import buildcraft.lib.statement.TriggerWrapper;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.silicon.statement.TriggerTimer;
import buildcraft.transport.statements.TriggerParameterSignal;
import buildcraft.transport.statements.TriggerPipeEmpty;
import buildcraft.transport.statements.TriggerPipeSignal;
import buildcraft.transport.wire.SavedDataWireSystems;
import buildcraft.transport.wire.WireSystem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GateLogic implements IGate, IWireEmitter, IRedstoneStatementContainer {
   protected static final IdAllocator ID_ALLOC = new IdAllocator("GateLogic");
   public static final int NET_ID_RESOLVE = ID_ALLOC.allocId("RESOLVE");
   public static final int NET_ID_CHANGE = ID_ALLOC.allocId("STATEMENT_CHANGE");
   public static final int NET_ID_GLOWING = ID_ALLOC.allocId("GLOWING");
   public static final int NET_ID_DARK = ID_ALLOC.allocId("DARK");
   @Deprecated
   public final PluggableGate pluggable;
   public final GateVariant variant;
   public final GateLogic.StatementPair[] statements;
   public final List<StatementSlot> activeActions = new ArrayList<>();
   public Consumer<IPayloadWriter> guiMessageOverride;
   public final boolean[] connections;
   public final boolean[] triggerOn;
   public final boolean[] actionOn;
   private final EnumSet<DyeColor> wireBroadcasts;
   public boolean isOn;
   private boolean resolveDirty = true;
   private int gateWakeTicks;

   public GateLogic(PluggableGate pluggable, GateVariant variant) {
      this.pluggable = pluggable;
      this.variant = variant;
      this.statements = new GateLogic.StatementPair[variant.numSlots];

      for (int s = 0; s < variant.numSlots; s++) {
         this.statements[s] = new GateLogic.StatementPair(s);
      }

      this.connections = new boolean[variant.numSlots - 1];
      this.triggerOn = new boolean[variant.numSlots];
      this.actionOn = new boolean[variant.numSlots];
      this.wireBroadcasts = EnumSet.noneOf(DyeColor.class);
   }

   public GateLogic(PluggableGate pluggable, CompoundTag nbt) {
      this(pluggable, new GateVariant(nbt.getCompound("variant").orElse(new CompoundTag())));
      this.readConfigData(nbt);
      if (nbt.contains("wireBroadcasts")) {
         int[] arr = nbt.getIntArray("wireBroadcasts").orElse(new int[0]);

         for (int i : arr) {
            if (i >= 0 && i < DyeColor.values().length) {
               this.wireBroadcasts.add(DyeColor.values()[i]);
            }
         }
      }
   }

   public void readConfigData(CompoundTag nbt) {
      short c = nbt.getShort("connections").orElse((short)0);

      for (int i = 0; i < this.connections.length; i++) {
         this.connections[i] = (c >>> i & 1) == 1;
      }

      for (int i = 0; i < this.statements.length; i++) {
         String tName = "trigger[" + i + "]";
         String aName = "action[" + i + "]";
         if (nbt.contains(tName)) {
            CompoundTag existing = nbt.getCompound(tName).orElse(new CompoundTag());
            if (existing.contains("kind") && !existing.contains("s")) {
               CompoundTag nbt2 = new CompoundTag();
               CompoundTag sTag = new CompoundTag();
               sTag.putString("kind", existing.getString("kind").orElse(""));
               sTag.putByte("side", existing.getByte("side").orElse((byte)6));
               nbt2.put("s", sTag);
               nbt.put(tName, nbt2);
            }
         }

         if (nbt.contains(aName)) {
            CompoundTag existing = nbt.getCompound(aName).orElse(new CompoundTag());
            if (existing.contains("kind") && !existing.contains("s")) {
               CompoundTag nbt2 = new CompoundTag();
               CompoundTag sTag = new CompoundTag();
               sTag.putString("kind", existing.getString("kind").orElse(""));
               sTag.putByte("side", existing.getByte("side").orElse((byte)6));
               nbt2.put("s", sTag);
               nbt.put(aName, nbt2);
            }
         }

         this.statements[i].trigger.readFromNbt(nbt.getCompound(tName).orElse(new CompoundTag()));
         this.statements[i].action.readFromNbt(nbt.getCompound(aName).orElse(new CompoundTag()));
      }
   }

   public boolean hasConfiguration() {
      for (boolean connected : this.connections) {
         if (connected) {
            return true;
         }
      }

      for (GateLogic.StatementPair pair : this.statements) {
         if (pair.trigger.get() != null || pair.action.get() != null) {
            return true;
         }
      }

      return false;
   }

   public CompoundTag writeToNbt() {
      CompoundTag nbt = new CompoundTag();
      nbt.put("variant", this.variant.writeToNBT());
      short c = 0;

      for (int i = 0; i < this.connections.length; i++) {
         if (this.connections[i]) {
            c |= (short)(1 << i);
         }
      }

      nbt.putShort("connections", c);

      for (int s = 0; s < this.statements.length; s++) {
         if (this.statements[s].trigger.get() != null) {
            nbt.put("trigger[" + s + "]", this.statements[s].trigger.writeToNbt());
         }

         if (this.statements[s].action.get() != null) {
            nbt.put("action[" + s + "]", this.statements[s].action.writeToNbt());
         }
      }

      int[] arr = new int[this.wireBroadcasts.size()];
      int idx = 0;

      for (DyeColor color : this.wireBroadcasts) {
         arr[idx++] = color.ordinal();
      }

      nbt.putIntArray("wireBroadcasts", arr);
      return nbt;
   }

   public CompoundTag writeClientState() {
      CompoundTag nbt = new CompoundTag();
      short tOn = 0;

      for (int i = 0; i < this.triggerOn.length; i++) {
         if (this.triggerOn[i]) {
            tOn |= (short)(1 << i);
         }
      }

      nbt.putShort("triggerOn", tOn);
      short aOn = 0;

      for (int i = 0; i < this.actionOn.length; i++) {
         if (this.actionOn[i]) {
            aOn |= (short)(1 << i);
         }
      }

      nbt.putShort("actionOn", aOn);
      nbt.putBoolean("isOn", this.isOn);
      return nbt;
   }

   public void readClientState(CompoundTag nbt) {
      short tOn = nbt.getShort("triggerOn").orElse((short)0);

      for (int i = 0; i < this.triggerOn.length; i++) {
         this.triggerOn[i] = (tOn >>> i & 1) == 1;
      }

      short aOn = nbt.getShort("actionOn").orElse((short)0);

      for (int i = 0; i < this.actionOn.length; i++) {
         this.actionOn[i] = (aOn >>> i & 1) == 1;
      }

      this.isOn = nbt.getBoolean("isOn").orElse(false);
   }

   public GateLogic(PluggableGate pluggable, FriendlyByteBuf buffer) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buffer);
      this(pluggable, new GateVariant(bc));
      readBoolArray(bc, this.triggerOn);
      readBoolArray(bc, this.actionOn);
      readBoolArray(bc, this.connections);

      try {
         for (GateLogic.StatementPair pair : this.statements) {
            pair.trigger.readFromBuffer(bc);
            pair.action.readFromBuffer(bc);
         }
      } catch (IOException io) {
         throw new Error(io);
      }

      boolean on = false;

      for (int i = 0; i < this.statements.length; i++) {
         boolean b = this.actionOn[i];
         on |= b && this.statements[i].action.get() != null;
      }

      this.isOn = on;
   }

   public void writeCreationToBuf(FriendlyByteBuf buffer) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buffer);
      this.variant.writeToBuffer(bc);
      writeBoolArray(bc, this.triggerOn);
      writeBoolArray(bc, this.actionOn);
      writeBoolArray(bc, this.connections);

      for (GateLogic.StatementPair pair : this.statements) {
         pair.trigger.writeToBuffer(bc);
         pair.action.writeToBuffer(bc);
      }
   }

   public void readPayload(FriendlyByteBuf buffer, boolean isClientSide) throws IOException {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buffer);
      int id = bc.readUnsignedByte();
      if (id == NET_ID_CHANGE) {
         boolean isAction = bc.readBoolean();
         int slot = bc.readUnsignedByte();
         if (slot >= 0 && slot < this.statements.length) {
            GateLogic.StatementPair s = this.statements[slot];
            (isAction ? s.action : s.trigger).readFromBuffer(bc);
         } else {
            throw new InvalidInputDataException("Slot index out of range! (" + slot + ", must be within " + this.statements.length + ")");
         }
      } else {
         if (isClientSide) {
            if (id == NET_ID_RESOLVE) {
               readBoolArray(bc, this.triggerOn);
               readBoolArray(bc, this.actionOn);
               readBoolArray(bc, this.connections);
            } else if (id == NET_ID_GLOWING) {
               this.isOn = true;
            } else if (id == NET_ID_DARK) {
               this.isOn = false;
            } else {
               BCLog.logger.warn("Unknown ID " + ID_ALLOC.getNameFor(id));
            }
         } else {
            BCLog.logger.warn("Unknown server ID " + ID_ALLOC.getNameFor(id));
         }
      }
   }

   public void sendStatementUpdate(boolean isAction, int slot) {
      IPayloadWriter writer = buffer -> {
         PacketBufferBC buf = BcPayloadBuffers.ensure(buffer);
         buf.writeByte(NET_ID_CHANGE);
         buf.writeBoolean(isAction);
         buf.writeByte(slot);
         GateLogic.StatementPair s = this.statements[slot];
         (isAction ? s.action : s.trigger).writeToBuffer(buf);
      };
      if (this.guiMessageOverride != null) {
         this.guiMessageOverride.accept(writer);
      } else {
         this.pluggable.sendGuiMessage(writer);
      }
   }

   public void sendResolveData() {
      this.pluggable.sendGuiMessage(buffer -> {
         PacketBufferBC buf = BcPayloadBuffers.ensure(buffer);
         buf.writeByte(NET_ID_RESOLVE);
         writeBoolArray(buf, this.triggerOn);
         writeBoolArray(buf, this.actionOn);
         writeBoolArray(buf, this.connections);
      });
   }

   private static void readBoolArray(PacketBufferBC buf, boolean[] arr) {
      for (int i = 0; i < arr.length; i++) {
         arr[i] = buf.readBoolean();
      }
   }

   private static void writeBoolArray(PacketBufferBC buf, boolean[] arr) {
      for (int i = 0; i < arr.length; i++) {
         buf.writeBoolean(arr[i]);
      }
   }

   public void sendIsOn() {
      this.pluggable.sendMessage(buffer -> buffer.writeByte(this.isOn ? NET_ID_GLOWING : NET_ID_DARK));
   }

   @Override
   public Direction getSide() {
      return this.pluggable.side;
   }

   @Override
   public BlockEntity getTile() {
      return this.getPipeHolder().getPipeTile();
   }

   @Override
   public BlockEntity getNeighbourTile(Direction side) {
      return this.getPipeHolder().getNeighbourTile(side);
   }

   @Override
   public IPipeHolder getPipeHolder() {
      return this.pluggable.holder;
   }

   @Override
   public List<IStatement> getTriggers() {
      List<IStatement> list = new ArrayList<>(this.statements.length);

      for (GateLogic.StatementPair pair : this.statements) {
         TriggerWrapper e = pair.trigger.get();
         list.add(e == null ? e : e.delegate);
      }

      return list;
   }

   @Override
   public List<IStatement> getActions() {
      List<IStatement> list = new ArrayList<>(this.statements.length);

      for (GateLogic.StatementPair pair : this.statements) {
         ActionWrapper e = pair.action.get();
         list.add(e == null ? e : e.delegate);
      }

      return list;
   }

   @Override
   public List<StatementSlot> getActiveActions() {
      return this.activeActions;
   }

   @Override
   public List<IStatementParameter> getTriggerParameters(int slot) {
      return Arrays.asList(this.statements[slot].trigger.getParameters());
   }

   @Override
   public List<IStatementParameter> getActionParameters(int slot) {
      return Arrays.asList(this.statements[slot].action.getParameters());
   }

   @Override
   public int getRedstoneInput(Direction side) {
      return this.getPipeHolder().getRedstoneInput(side);
   }

   @Override
   public boolean setRedstoneOutput(Direction side, int value) {
      return this.getPipeHolder().setRedstoneOutput(side, value);
   }

   @Override
   public boolean isEmitting(DyeColor colour) {
      BlockEntity tile = this.getPipeHolder().getPipeTile();
      return tile.isRemoved() ? false : this.wireBroadcasts.contains(colour);
   }

   @Override
   public void emitWire(DyeColor colour) {
      this.wireBroadcasts.add(colour);
      this.markResolveDirty();
   }

   public void markResolveDirty() {
      this.resolveDirty = true;
      this.gateWakeTicks = Math.max(this.gateWakeTicks, 2);
      this.pluggable.holder.wakePipe();
   }

   public boolean needsPeriodicTick() {
      return !this.resolveDirty && this.gateWakeTicks <= 0 ? this.hasPollingTrigger() : true;
   }

   private boolean hasPollingTrigger() {
      for (GateLogic.StatementPair pair : this.statements) {
         TriggerWrapper trigger = pair.trigger.get();
         if (trigger != null && triggerRequiresPolling(trigger.delegate, pair.trigger.getParameters())) {
            return true;
         }
      }

      return false;
   }

   private static boolean triggerRequiresPolling(IStatement delegate, IStatementParameter[] parameters) {
      if (delegate instanceof TriggerTimer || delegate instanceof TriggerPipeSignal || delegate instanceof TriggerPipeEmpty) {
         return true;
      }

      for (IStatementParameter parameter : parameters) {
         if (parameter instanceof TriggerParameterSignal signal && signal.colour != null) {
            return true;
         }
      }

      return false;
   }

   public boolean isSplitInTwo() {
      return this.variant.numSlots > 4;
   }

   public void resolveActions() {
      ProfilerFiller _profiler = Profiler.get();
      _profiler.push("buildcraft:gate_resolveActions");

      try {
         boolean prevIsOn = this.isOn;
         boolean[] prevTriggers = Arrays.copyOf(this.triggerOn, this.triggerOn.length);
         boolean[] prevActions = Arrays.copyOf(this.actionOn, this.actionOn.length);
         Arrays.fill(this.triggerOn, false);
         Arrays.fill(this.actionOn, false);
         this.activeActions.clear();
         EnumSet<DyeColor> previousBroadcasts = EnumSet.copyOf(this.wireBroadcasts);
         this.wireBroadcasts.clear();
         GateTriggerGroupEvaluator.StateHolder state = new GateTriggerGroupEvaluator.StateHolder();
         GateTriggerGroupEvaluator.evaluateGroups(
            this, this.getPipeHolder(), this.variant, this.statements, this.connections, prevActions, this.triggerOn, this.actionOn, this.activeActions, state
         );
         this.isOn = state.isOn;
         if (!previousBroadcasts.equals(this.wireBroadcasts) && !this.getPipeHolder().getPipeWorld().isClientSide()) {
            SavedDataWireSystems.get(this.getPipeHolder().getPipeWorld())
               .markEmitterDirty(new WireSystem.WireElement(this.getPipeHolder().getPipePos(), this.getSide()));
         }

         if (this.isOn != prevIsOn) {
            this.sendIsOn();
         }

         if (!Arrays.equals(prevTriggers, this.triggerOn) || !Arrays.equals(prevActions, this.actionOn)) {
            this.sendResolveData();
         }
      } finally {
         _profiler.pop();
      }
   }

   public void onTick() {
      if (!this.getPipeHolder().getPipeWorld().isClientSide()) {
         if (this.needsPeriodicTick()) {
            this.resolveActions();
            this.resolveDirty = false;
            if (this.gateWakeTicks > 0) {
               this.gateWakeTicks--;
            }
         }
      }
   }

   public SortedSet<TriggerWrapper> getAllValidTriggers() {
      SortedSet<TriggerWrapper> set = new TreeSet<>();

      for (ITriggerInternal trigger : StatementManager.getInternalTriggers(this)) {
         if (this.isValidTrigger(trigger)) {
            set.add(new TriggerWrapper.TriggerWrapperInternal(trigger));
         }
      }

      for (Direction face : Direction.values()) {
         for (ITriggerInternalSided trigger : StatementManager.getInternalSidedTriggers(this, face)) {
            if (this.isValidTrigger(trigger)) {
               set.add(new TriggerWrapper.TriggerWrapperInternalSided(trigger, face));
            }
         }

         BlockEntity neighbour = this.getNeighbourTile(face);
         if (neighbour != null) {
            for (ITriggerExternal trigger : StatementManager.getExternalTriggers(face, neighbour)) {
               if (this.isValidTrigger(trigger)) {
                  set.add(new TriggerWrapper.TriggerWrapperExternal(trigger, face));
               }
            }
         }
      }

      return set;
   }

   public SortedSet<ActionWrapper> getAllValidActions() {
      SortedSet<ActionWrapper> set = new TreeSet<>();

      for (IActionInternal trigger : StatementManager.getInternalActions(this)) {
         if (this.isValidAction(trigger)) {
            set.add(new ActionWrapper.ActionWrapperInternal(trigger));
         }
      }

      for (Direction face : Direction.values()) {
         for (IActionInternalSided trigger : StatementManager.getInternalSidedActions(this, face)) {
            if (this.isValidAction(trigger)) {
               set.add(new ActionWrapper.ActionWrapperInternalSided(trigger, face));
            }
         }

         BlockEntity neighbour = this.getNeighbourTile(face);
         if (neighbour != null) {
            for (IActionExternal trigger : StatementManager.getExternalActions(face, neighbour)) {
               if (this.isValidAction(trigger)) {
                  set.add(new ActionWrapper.ActionWrapperExternal(trigger, face));
               }
            }
         }
      }

      return set;
   }

   public boolean isValidTrigger(IStatement statement) {
      return statement != null && statement.minParameters() <= this.variant.numTriggerArgs;
   }

   public boolean isValidAction(IStatement statement) {
      return statement != null && statement.minParameters() <= this.variant.numActionArgs;
   }

   public class StatementPair {
      public final FullStatement<TriggerWrapper> trigger;
      public final FullStatement<ActionWrapper> action;

      public StatementPair(int index) {
         FullStatement.IStatementChangeListener tChange = (s, i) -> {
            GateLogic.this.markResolveDirty();
            GateLogic.this.sendStatementUpdate(false, index);
         };
         FullStatement.IStatementChangeListener aChange = (s, i) -> {
            GateLogic.this.markResolveDirty();
            GateLogic.this.sendStatementUpdate(true, index);
         };
         this.trigger = new FullStatement<>(TriggerType.INSTANCE, GateLogic.this.variant.numTriggerArgs, tChange);
         this.action = new FullStatement<>(ActionType.INSTANCE, GateLogic.this.variant.numActionArgs, aChange);
      }
   }
}
