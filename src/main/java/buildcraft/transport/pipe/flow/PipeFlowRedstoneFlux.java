/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowRedstoneFlux;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeEventRedstoneFlux;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.lib.fabric.transfer.EnergyStorageOps;
import buildcraft.lib.misc.data.AverageInt;
import buildcraft.transport.tile.TilePipeHolder;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.ToIntFunction;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;

public class PipeFlowRedstoneFlux extends PipeEnergyFlowBase implements IFlowRedstoneFlux, IDebuggable {
   private static final int DEFAULT_MAX_POWER = 100;
   public static final int NET_POWER_AMOUNTS = 2;
   private int maxPower = -1;
   private boolean disabled = false;
   private long currentWorldTime;
   private boolean isReceiver = false;
   private final EnumMap<Direction, PipeFlowRedstoneFlux.Section> sections;

   public PipeFlowRedstoneFlux(IPipe pipe) {
      super(pipe);
      this.sections = new EnumMap<>(Direction.class);

      for (Direction face : Direction.values()) {
         this.sections.put(face, new PipeFlowRedstoneFlux.Section(face));
      }
   }

   public PipeFlowRedstoneFlux(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      this.isReceiver = BcNbt.getBoolean(nbt, "isReceiver", false);
      this.sections = new EnumMap<>(Direction.class);

      for (Direction face : Direction.values()) {
         this.sections.put(face, new PipeFlowRedstoneFlux.Section(face));
      }
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.putBoolean("isReceiver", this.isReceiver);
      return nbt;
   }

   @Override
   public void writePayload(int id, FriendlyByteBuf buffer) {
      super.writePayload(id, buffer);
      if (id == 2 || id == 0) {
         PipeEnergyDisplaySupport.writeDisplayState(buffer, this.sections);
      }
   }

   @Override
   public void readPayload(int id, FriendlyByteBuf buffer) {
      super.readPayload(id, buffer);
      if (id == 2 || id == 0) {
         PipeEnergyDisplaySupport.readDisplayState(buffer, this.sections);
      }
   }

   @Override
   public boolean canConnect(Direction face, PipeFlow other) {
      return other instanceof PipeFlowRedstoneFlux;
   }

   @Override
   public boolean canConnect(Direction face, BlockEntity oTile) {
      return PipeNeighborEnergyAccess.canConnect(this.pipe.getHolder(), face);
   }

   @Override
   public void reconfigure() {
      PipeEventRedstoneFlux.Configure configure = new PipeEventRedstoneFlux.Configure(this.pipe.getHolder(), this);
      PipeApi.RedstoneFluxTransferInfo pti = PipeApi.getRfTransferInfo(this.pipe.getDefinition());
      configure.setReceiver(pti.isReceiver);
      configure.setMaxPower(pti.transferPerTick);
      this.pipe.getHolder().fireEvent(configure);
      boolean wasReceiver = this.isReceiver;
      this.isReceiver = configure.isReceiver();
      if (wasReceiver != this.isReceiver) {
         this.pipe.markForUpdate();
      }

      this.maxPower = configure.getMaxPower();
      this.disabled = configure.isTransferDisabled();
      if (this.maxPower <= 0) {
         this.maxPower = 100;
      }
   }

   @Override
   public int tryExtractPower(int maxExtracted, Direction from) {
      return this.isReceiver && !this.disabled
         ? EnergyStorageOps.extract(PipeNeighborEnergyAccess.storage(this.pipe.getHolder(), from), maxExtracted, true)
         : 0;
   }

   public EnumMap<Direction, PipeFlowRedstoneFlux.Section> getSections() {
      return this.sections;
   }

   public PipeFlowRedstoneFlux.Section getSection(Direction side) {
      return this.sections.get(side);
   }

   @Nullable
   public PipeFlowRedstoneFlux.Section getEnergySection(@Nullable Direction facing) {
      return facing == null ? null : this.sections.get(facing);
   }

   @Nullable
   public EnergyStorage getEnergyStorage(@Nullable Direction facing) {
      PipeFlowRedstoneFlux.Section section = this.getEnergySection(facing);
      return section == null ? null : section.storage;
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("maxPower = " + this.maxPower);
      left.add("isReceiver = " + this.isReceiver);
      left.add("internalPower = " + this.arrayToString(s -> s.internalPower) + " <- " + this.arrayToString(s -> s.internalNextPower));
      left.add("- powerQuery: " + this.arrayToString(s -> s.powerQuery) + " <- " + this.arrayToString(s -> s.nextPowerQuery));
      left.add("- power: OUT " + this.arrayToString(s -> s.debugPowerOutput));
      left.add("- power: OFFERED " + this.arrayToString(s -> s.debugPowerOffered));
   }

   private String arrayToString(ToIntFunction<PipeFlowRedstoneFlux.Section> getter) {
      long[] arr = new long[6];

      for (Direction face : Direction.values()) {
         arr[face.ordinal()] = getter.applyAsInt(this.sections.get(face));
      }

      return Arrays.toString(arr);
   }

   @Override
   public boolean hasSimulationWork() {
      if (this.maxPower == -1) {
         return true;
      }

      // A pipe touching a machine must keep ticking even when idle. Transfer is demand-driven: the
      // consumer-adjacent pipe has to run requestFromConnectedTiles every tick to (re)create the query that
      // propagates upstream, and the source-adjacent pipe has to be ready to receive. Otherwise, once a consumer
      // stops asking for a moment the query decays, the pipe sleeps, and nothing wakes it when the machine wants
      // power again (machines don't call wakePipe) -- transfer latches off. For an energy pipe a TILE connection
      // is always an energy machine, so this is the exact "adjacent to a machine" test: a cached EnumMap read,
      // no per-tick world/capability lookup. Pure transport pipes with nothing attached still sleep.
      for (Direction face : Direction.values()) {
         if (this.pipe.getConnectedType(face) == IPipe.ConnectedType.TILE) {
            return true;
         }
      }

      for (PipeFlowRedstoneFlux.Section section : this.sections.values()) {
         if (section.internalPower > 0 || section.powerQuery > 0 || section.internalNextPower > 0 || section.nextPowerQuery > 0) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean hasClientSimulationWork() {
      for (PipeFlowRedstoneFlux.Section section : this.sections.values()) {
         if (section.displayPower > 0 || section.displayFlow != PipeEnergyEnumFlow.STATIONARY) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void onTick() {
      if (this.maxPower == -1) {
         this.reconfigure();
      }

      if (!this.tickClientDisplay(this.sections)) {
         this.captureDisplaySnapshot(this.sections);
         this.step();
         PipeEnergySimulation.distributeInternalPower(this.sections, (targetFace, watts) -> {
            int sent = (int)Math.min(watts, 2147483647L);
            IPipe neighbour = this.pipe.getConnectedPipe(targetFace);
            if (neighbour != null && neighbour.getFlow() instanceof PipeFlowRedstoneFlux && neighbour.isConnected(targetFace.getOpposite())) {
               PipeFlowRedstoneFlux oFlow = (PipeFlowRedstoneFlux)neighbour.getFlow();
               return oFlow.sections.get(targetFace.getOpposite()).receivePowerInternal(sent);
            } else {
               int accepted = EnergyStorageOps.insert(PipeNeighborEnergyAccess.storage(this.pipe.getHolder(), targetFace), sent, true);
               return accepted > 0 ? sent - accepted : sent;
            }
         });
         PipeEnergySimulation.updateDisplayPower(this.sections, this.maxPower);
         PipeEnergySimulation.requestFromConnectedTiles(this.pipe, face -> {
            EnergyStorage recv = PipeNeighborEnergyAccess.storage(this.pipe.getHolder(), face);
            return !EnergyStorageOps.canAccept(recv) ? 0L : EnergyStorageOps.spareCapacity(recv);
         }, (face, amount) -> this.requestPower(face, (int)amount));
         long[] transferQuery = PipeEnergySimulation.buildTransferQuery(this.pipe, this.sections);
         PipeEnergyDisplaySupport.propagateQueriesToNeighbourPipes(
            this.pipe,
            transferQuery,
            this.disabled,
            PipeFlowRedstoneFlux.class,
            (neighbourFlow, from, amount) -> ((PipeFlowRedstoneFlux)neighbourFlow).requestPower(from, (int)amount)
         );
         this.sendDisplayIfChanged(this.sections, 2);
      }
   }

   private void step() {
      long now = this.pipe.getHolder().getPipeWorld().getGameTime();
      PipeEnergySimulation.stepOnce(now, this.currentWorldTime, () -> {
         this.currentWorldTime = now;
         this.sections.values().forEach(PipeFlowRedstoneFlux.Section::step);
      });
   }

   private void requestPower(Direction from, int amount) {
      this.step();
      PipeFlowRedstoneFlux.Section s = this.sections.get(from);
      s.nextPowerQuery += amount;
      s.nextPowerQuery = Math.min(s.nextPowerQuery, this.maxPower);
   }

   @Override
   public long getPowerRequested() {
      return this.getPowerRequested(null);
   }

   public int getPowerRequested(@Nullable Direction side) {
      int req = 0;

      for (Direction face : Direction.values()) {
         if (side == null || face != side) {
            req += this.sections.get(face).powerQuery;
         }
      }

      return req;
   }

   public class Section implements PipeEnergySimulation.SimSectionWithAverage {
      public final Direction side;
      public double clientDisplayFlow;
      public double clientDisplayFlowLast;
      public int displayPower;
      public PipeEnergyEnumFlow displayFlow = PipeEnergyEnumFlow.STATIONARY;
      public int nextPowerQuery;
      public int internalNextPower;
      public final AverageInt powerAverage = new AverageInt(1);
      private final EnergyStorage storage = new PipeEnergySectionStorage(this);
      int powerQuery;
      int internalPower;
      int debugPowerOutput;
      int debugPowerOffered;
      private final SnapshotParticipant<Integer> powerJournal = new SnapshotParticipant<Integer>() {
         protected Integer createSnapshot() {
            return Section.this.internalNextPower;
         }

         protected void readSnapshot(Integer snapshot) {
            Section.this.internalNextPower = snapshot;
         }
      };

      public Section(Direction side) {
         this.side = side;
         this.clientDisplayFlow = (side.getAxisDirection() == AxisDirection.POSITIVE ? 7 : 1) / 8.0;
      }

      void step() {
         this.powerQuery = this.nextPowerQuery;
         this.nextPowerQuery = 0;
         this.internalPower = this.internalPower + this.internalNextPower;
         this.internalNextPower = 0;
      }

      public int insert(int maxReceive, TransactionContext transaction) {
         if (PipeFlowRedstoneFlux.this.isReceiver && maxReceive > 0) {
            PipeFlowRedstoneFlux.this.step();
            int maxCanAccept = PipeFlowRedstoneFlux.this.maxPower - (this.internalPower + this.internalNextPower);
            if (maxCanAccept <= 0) {
               return 0;
            }

            int accepted = Math.min(maxCanAccept, maxReceive);
            if (accepted > 0) {
               this.powerJournal.updateSnapshots(transaction);
               this.debugPowerOffered += accepted;
               this.internalNextPower += accepted;
               return accepted;
            }
         }

         return 0;
      }

      public int extract(int maxExtract, TransactionContext transaction) {
         return 0;
      }

      public long getAmountAsLong() {
         return this.internalPower + this.internalNextPower;
      }

      public long getCapacityAsLong() {
         return PipeFlowRedstoneFlux.this.maxPower;
      }

      int receivePowerInternal(int sent) {
         if (sent > 0) {
            if (PipeFlowRedstoneFlux.this.pipe.getHolder().getPipeTile() instanceof TilePipeHolder holder) {
               holder.wakePipe();
            }

            PipeFlowRedstoneFlux.this.step();
            int max = PipeFlowRedstoneFlux.this.maxPower - (this.internalPower + this.internalNextPower);
            if (max <= 0) {
               return sent;
            }

            int accepted = Math.min(max, sent);
            this.debugPowerOffered += accepted;
            this.internalNextPower += accepted;
            return sent - accepted;
         } else {
            return sent;
         }
      }

      @Override
      public long getInternalPower() {
         return this.internalPower;
      }

      @Override
      public void subtractInternalPower(long amount) {
         this.internalPower -= (int)amount;
      }

      @Override
      public long getPowerQuery() {
         return this.powerQuery;
      }

      @Override
      public void pushPowerAverage(int amount) {
         this.powerAverage.push(amount);
      }

      @Override
      public void addDebugOutput(long amount) {
         this.debugPowerOutput += (int)amount;
      }

      @Override
      public AverageInt getPowerAverage() {
         return this.powerAverage;
      }

      @Override
      public int getDisplayPower() {
         return this.displayPower;
      }

      @Override
      public void setDisplayPower(int power) {
         this.displayPower = power;
      }

      @Override
      public PipeEnergyEnumFlow getDisplayFlow() {
         return this.displayFlow;
      }

      @Override
      public void setDisplayFlow(PipeEnergyEnumFlow flow) {
         this.displayFlow = flow;
      }

      @Override
      public double getClientDisplayFlow() {
         return this.clientDisplayFlow;
      }

      @Override
      public double getClientDisplayFlowLast() {
         return this.clientDisplayFlowLast;
      }

      @Override
      public void setClientDisplayFlow(double value) {
         this.clientDisplayFlow = value;
      }

      @Override
      public void setClientDisplayFlowLast(double value) {
         this.clientDisplayFlowLast = value;
      }
   }
}
