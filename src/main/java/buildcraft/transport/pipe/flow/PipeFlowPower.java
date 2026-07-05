/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowPower;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeEventPower;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.data.AverageInt;
import buildcraft.transport.tile.TilePipeHolder;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.ToLongFunction;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PipeFlowPower extends PipeEnergyFlowBase implements IFlowPower, IDebuggable {
   private static final long DEFAULT_MAX_POWER = MjAPI.MJ * 10L;
   public static final int NET_POWER_AMOUNTS = 2;
   private long maxPower = -1L;
   private long powerLoss = -1L;
   private long powerResistance = -1L;
   private boolean disabled = false;
   private long currentWorldTime;
   private boolean isReceiver = false;
   private final EnumMap<Direction, PipeFlowPower.Section> sections;

   public PipeFlowPower(IPipe pipe) {
      super(pipe);
      this.sections = new EnumMap<>(Direction.class);

      for (Direction face : Direction.values()) {
         this.sections.put(face, new PipeFlowPower.Section(face));
      }
   }

   public PipeFlowPower(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      this.isReceiver = BcNbt.getBoolean(nbt, "isReceiver", false);
      this.sections = new EnumMap<>(Direction.class);

      for (Direction face : Direction.values()) {
         this.sections.put(face, new PipeFlowPower.Section(face));
      }
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.putBoolean("isReceiver", this.isReceiver);
      int[] powers = new int[6];
      int[] flows = new int[6];

      for (Direction face : Direction.values()) {
         PipeFlowPower.Section s = this.sections.get(face);
         powers[face.ordinal()] = s.displayPower;
         flows[face.ordinal()] = s.displayFlow.ordinal();
      }

      nbt.putIntArray("displayPower", powers);
      nbt.putIntArray("displayFlow", flows);
      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      this.isReceiver = BcNbt.getBoolean(nbt, "isReceiver", false);
      int[] powers = BcNbt.getIntArray(nbt, "displayPower");
      int[] flows = BcNbt.getIntArray(nbt, "displayFlow");

      for (Direction face : Direction.values()) {
         int i = face.ordinal();
         PipeFlowPower.Section s = this.sections.get(face);
         if (i < powers.length) {
            s.displayPower = powers[i];
         }

         if (i < flows.length) {
            int flowIdx = flows[i];
            PipeEnergyEnumFlow[] vals = PipeEnergyEnumFlow.values();
            s.displayFlow = flowIdx >= 0 && flowIdx < vals.length ? vals[flowIdx] : PipeEnergyEnumFlow.STATIONARY;
         }
      }
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
      return other instanceof PipeFlowPower;
   }

   @Override
   public boolean canConnect(Direction face, BlockEntity oTile) {
      return PipeNeighborMjAccess.receiver(this.pipe.getHolder(), face) != null
         || PipeNeighborMjAccess.connector(this.pipe.getHolder(), face) != null
         || PipeNeighborMjAccess.passiveProvider(this.pipe.getHolder(), face) != null;
   }

   @Override
   public void reconfigure() {
      PipeEventPower.Configure configure = new PipeEventPower.Configure(this.pipe.getHolder(), this);
      PipeApi.PowerTransferInfo pti = PipeApi.getPowerTransferInfo(this.pipe.getDefinition());
      configure.setReceiver(pti.isReceiver);
      configure.setMaxPower(pti.transferPerTick);
      configure.setPowerLoss(pti.lossPerTick);
      configure.setPowerResistance(pti.resistancePerTick);
      this.pipe.getHolder().fireEvent(configure);
      boolean wasReceiver = this.isReceiver;
      this.isReceiver = configure.isReceiver();
      if (wasReceiver != this.isReceiver) {
         this.pipe.markForUpdate();
      }

      this.maxPower = configure.getMaxPower();
      this.disabled = configure.isTransferDisabled();
      if (this.maxPower <= 0L) {
         this.maxPower = DEFAULT_MAX_POWER;
      }

      this.powerLoss = MathUtil.clamp(configure.getPowerLoss(), -1L, this.maxPower);
      this.powerResistance = MathUtil.clamp(configure.getPowerResistance(), -1L, MjAPI.MJ);
      if (this.powerLoss < 0L) {
         if (this.powerResistance < 0L) {
            this.powerResistance = MjAPI.MJ / 100L;
         }

         this.powerLoss = this.maxPower * this.powerResistance / MjAPI.MJ;
      } else if (this.powerResistance < 0L) {
         this.powerResistance = this.powerLoss * MjAPI.MJ / this.maxPower;
      }
   }

   @Override
   public long tryExtractPower(long maxExtracted, Direction from) {
      if (this.isReceiver && !this.disabled) {
         IMjPassiveProvider provider = PipeNeighborMjAccess.passiveProvider(this.pipe.getHolder(), from);
         return provider == null ? 0L : provider.extractPower(0L, maxExtracted, false);
      } else {
         return 0L;
      }
   }

   public EnumMap<Direction, PipeFlowPower.Section> getSections() {
      return this.sections;
   }

   public PipeFlowPower.Section getSection(Direction side) {
      return this.sections.get(side);
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> T getCapability(@Nonnull Object capability, Direction facing) {
      if (facing == null) {
         return null;
      } else if (capability == MjAPI.CAP_RECEIVER) {
         return (T)(this.isReceiver ? this.sections.get(facing) : null);
      } else {
         return (T)(capability == MjAPI.CAP_CONNECTOR ? this.sections.get(facing) : null);
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("maxPower = " + LocaleUtil.localizeMj(this.maxPower));
      left.add("isReceiver = " + this.isReceiver);
      left.add("internalPower = " + this.arrayToString(s -> s.internalPower) + " <- " + this.arrayToString(s -> s.internalNextPower));
      left.add("- powerQuery: " + this.arrayToString(s -> s.powerQuery) + " <- " + this.arrayToString(s -> s.nextPowerQuery));
      left.add("- power: OUT " + this.arrayToString(s -> s.debugPowerOutput));
      left.add("- power: OFFERED " + this.arrayToString(s -> s.debugPowerOffered));
   }

   private String arrayToString(ToLongFunction<PipeFlowPower.Section> getter) {
      long[] arr = new long[6];

      for (Direction face : Direction.values()) {
         arr[face.ordinal()] = getter.applyAsLong(this.sections.get(face)) / MjAPI.MJ;
      }

      return Arrays.toString(arr);
   }

   @Override
   public boolean hasSimulationWork() {
      if (this.maxPower == -1L) {
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

      for (PipeFlowPower.Section section : this.sections.values()) {
         if (section.internalPower > 0L || section.powerQuery > 0L || section.internalNextPower > 0L || section.nextPowerQuery > 0L) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean hasClientSimulationWork() {
      for (PipeFlowPower.Section section : this.sections.values()) {
         if (section.displayPower > 0 || section.displayFlow != PipeEnergyEnumFlow.STATIONARY) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void onTick() {
      if (this.maxPower == -1L) {
         this.reconfigure();
      }

      if (!this.tickClientDisplay(this.sections)) {
         this.captureDisplaySnapshot(this.sections);
         this.step();
         PipeEnergySimulation.distributeInternalPower(this.sections, (targetFace, watts) -> {
            IPipe neighbour = this.pipe.getConnectedPipe(targetFace);
            if (neighbour != null && neighbour.getFlow() instanceof PipeFlowPower && neighbour.isConnected(targetFace.getOpposite())) {
               PipeFlowPower oFlow = (PipeFlowPower)neighbour.getFlow();
               return oFlow.sections.get(targetFace.getOpposite()).receivePowerInternal(watts);
            } else {
               IMjReceiver receiver = this.getReceiver(targetFace);
               return receiver != null && receiver.canReceive() ? receiver.receivePower(watts, false) : watts;
            }
         });
         PipeEnergySimulation.updateDisplayPower(this.sections, this.maxPower);
         PipeEnergySimulation.requestFromConnectedTiles(this.pipe, face -> {
            IMjReceiver recv = this.getReceiver(face);
            return recv != null && recv.canReceive() ? recv.getPowerRequested() : 0L;
         }, this::requestPower);
         long[] transferQuery = PipeEnergySimulation.buildTransferQuery(this.pipe, this.sections);
         PipeEnergyDisplaySupport.propagateQueriesToNeighbourPipes(
            this.pipe,
            transferQuery,
            this.disabled,
            PipeFlowPower.class,
            (neighbourFlow, from, amount) -> ((PipeFlowPower)neighbourFlow).requestPower(from, amount)
         );
         this.sendDisplayIfChanged(this.sections, 2);
      }
   }

   private IMjReceiver getReceiver(Direction side) {
      return PipeNeighborMjAccess.receiver(this.pipe.getHolder(), side);
   }

   private void step() {
      long now = this.pipe.getHolder().getPipeWorld().getGameTime();
      PipeEnergySimulation.stepOnce(now, this.currentWorldTime, () -> {
         this.currentWorldTime = now;
         this.sections.values().forEach(PipeFlowPower.Section::step);
      });
   }

   private void requestPower(Direction from, long amount) {
      this.step();
      PipeFlowPower.Section s = this.sections.get(from);
      s.nextPowerQuery += amount;
      s.nextPowerQuery = Math.min(s.nextPowerQuery, this.maxPower);
   }

   @Override
   public long getPowerRequested() {
      return this.getPowerRequested(null);
   }

   public long getPowerRequested(@Nullable Direction side) {
      long req = 0L;

      for (Direction face : Direction.values()) {
         if (side == null || face != side) {
            req += this.sections.get(face).powerQuery;
         }
      }

      return req;
   }

   public class Section implements IMjReceiver, PipeEnergySimulation.SimSectionWithAverage {
      public final Direction side;
      public double clientDisplayFlow;
      public double clientDisplayFlowLast;
      public int displayPower;
      public PipeEnergyEnumFlow displayFlow = PipeEnergyEnumFlow.STATIONARY;
      public long nextPowerQuery;
      public long internalNextPower;
      public final AverageInt powerAverage = new AverageInt(1);
      long powerQuery;
      long internalPower;
      long debugPowerOutput;
      long debugPowerOffered;

      public Section(Direction side) {
         this.side = side;
         this.clientDisplayFlow = (side.getAxisDirection() == AxisDirection.POSITIVE ? 7 : 1) / 8.0;
      }

      void step() {
         this.powerQuery = this.nextPowerQuery;
         this.nextPowerQuery = 0L;
         this.internalPower = this.internalPower + this.internalNextPower;
         this.internalNextPower = 0L;
      }

      @Override
      public boolean canConnect(@Nonnull IMjConnector other) {
         return true;
      }

      @Override
      public long getPowerRequested() {
         return PipeFlowPower.this.getPowerRequested(this.side);
      }

      long receivePowerInternal(long sent) {
         if (sent > 0L) {
            PipeFlowPower.this.step();
            this.debugPowerOffered += sent;
            this.internalNextPower += sent;
            if (PipeFlowPower.this.pipe.getHolder().getPipeTile() instanceof TilePipeHolder holder) {
               holder.wakePipe();
            }

            return 0L;
         } else {
            return sent;
         }
      }

      @Override
      public long receivePower(long microJoules, boolean simulate) {
         if (PipeFlowPower.this.isReceiver) {
            return !simulate ? this.receivePowerInternal(microJoules) : 0L;
         } else {
            return microJoules;
         }
      }

      @Override
      public boolean canReceive() {
         return PipeFlowPower.this.isReceiver;
      }

      @Override
      public long getInternalPower() {
         return this.internalPower;
      }

      @Override
      public void subtractInternalPower(long amount) {
         this.internalPower -= amount;
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
         this.debugPowerOutput += amount;
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
