/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.robot;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjRfConversion;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IFlowPower;
import buildcraft.api.transport.pipe.IFlowRedstoneFlux;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.robotics.BCRoboticsStatements;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.silicon.gate.GateLogic;
import buildcraft.silicon.plug.PluggableGate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DockingStationPipe extends DockingStation implements IRequestProvider {
   private IPipeHolder pipe;

   public DockingStationPipe() {
   }

   public DockingStationPipe(IPipeHolder pipe, Direction side) {
      super(pipe.getPipePos(), side);
      this.pipe = pipe;
      this.world = pipe.getPipeWorld();
   }

   /** Re-attach a station that was loaded detached (no-arg ctor, e.g. reused from the registry/NBT) to the live
    * pipe holder, so its {@code world} and {@code pipe} are valid for registry lookups (robotTaking, powerRoom,
    * tickPower). Idempotent. */
   public void bindToPipe(IPipeHolder holder) {
      this.pipe = holder;
      this.world = holder.getPipeWorld();
   }

   public IPipeHolder getPipe() {
      if (this.pipe == null && this.world != null) {
         BlockEntity tile = this.world.getBlockEntity(this.getPos());
         if (tile instanceof IPipeHolder holder) {
            this.pipe = holder;
         }
      }

      if (this.pipe == null || ((BlockEntity) this.pipe).isRemoved()) {
         if (this.world != null && !this.world.isClientSide()) {
            RobotManager.registryProvider.getRegistry(this.world).removeStation(this);
         }

         this.pipe = null;
      }

      return this.pipe;
   }

   @Override
   public Iterable<StatementSlot> getActiveActions() {
      List<StatementSlot> actions = new ArrayList<>();
      IPipeHolder holder = this.getPipe();
      if (holder != null) {
         for (Direction face : Direction.values()) {
            PipePluggable plug = holder.getPluggable(face);
            if (plug instanceof PluggableGate gate) {
               GateLogic logic = gate.logic;
               if (logic != null) {
                  actions.addAll(logic.getActiveActions());
               }
            }
         }
      }

      return actions;
   }

   @Override
   public IInjectable getItemOutput() {
      IPipeHolder holder = this.getPipe();
      if (holder == null) {
         return null;
      }

      IPipe ipipe = holder.getPipe();
      return ipipe != null && ipipe.getFlow() instanceof IFlowItems flow ? flow : null;
   }

   @Override
   public EnumPipePart getItemOutputSide() {
      return EnumPipePart.fromFacing(this.side().getOpposite());
   }

   @Override
   public Container getItemInput() {
      IPipeHolder holder = this.getPipe();
      if (holder == null) {
         return null;
      }

      BlockEntity neighbour = holder.getNeighbourTile(this.side());
      return neighbour instanceof Container container ? container : null;
   }

   @Override
   public EnumPipePart getItemInputSide() {
      return EnumPipePart.fromFacing(this.side().getOpposite());
   }

   @Override
   public net.fabricmc.fabric.api.transfer.v1.storage.Storage<net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant> getFluidInput() {
      if (this.getPipe() == null || this.world == null) {
         return null;
      }

      BlockPos neighbourPos = this.getPos().relative(this.side());
      return buildcraft.lib.fabric.transfer.BcTransfers.fluid(this.world, neighbourPos, this.side().getOpposite());
   }

   @Override
   public EnumPipePart getFluidInputSide() {
      return EnumPipePart.fromFacing(this.side().getOpposite());
   }

   @Override
   public net.fabricmc.fabric.api.transfer.v1.storage.Storage<net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant> getFluidOutput() {
      if (this.getPipe() == null || this.world == null) {
         return null;
      }

      return buildcraft.lib.fabric.transfer.BcTransfers.fluid(this.world, this.getPos(), this.side());
   }

   @Override
   public EnumPipePart getFluidOutputSide() {
      return EnumPipePart.fromFacing(this.side());
   }

   @Override
   public boolean providesPower() {
      IPipeHolder holder = this.getPipe();
      if (holder == null) {
         return false;
      }

      IPipe ipipe = holder.getPipe();
      if (ipipe == null) {
         return false;
      }

      // A station charges its robot from any energy pipe it sits on: kinesis (MJ, IFlowPower) or RF (IFlowRedstoneFlux).
      Object flow = ipipe.getFlow();
      return flow instanceof IFlowPower || flow instanceof IFlowRedstoneFlux;
   }

   @Override
   public IRequestProvider getRequestProvider() {
      IPipeHolder holder = this.getPipe();
      if (holder != null) {
         for (Direction dir : Direction.values()) {
            BlockEntity nearby = holder.getNeighbourTile(dir);
            if (nearby instanceof IRequestProvider provider) {
               return provider;
            }
         }
      }

      return this;
   }

   @Override
   public boolean take(EntityRobotBase robot) {
      if (this.getPipe() == null) {
         return false;
      }

      boolean result = super.take(robot);
      if (result) {
         this.pipe.scheduleRenderUpdate();
      }

      return result;
   }

   @Override
   public boolean takeAsMain(EntityRobotBase robot) {
      if (this.getPipe() == null) {
         return false;
      }

      boolean result = super.takeAsMain(robot);
      if (result) {
         this.pipe.scheduleRenderUpdate();
      }

      return result;
   }

   @Override
   public void unsafeRelease(EntityRobotBase robot) {
      super.unsafeRelease(robot);
      if (this.robotTaking() == null && this.getPipe() != null) {
         this.pipe.scheduleRenderUpdate();
      }
   }

   @Override
   public void onChunkUnload() {
      this.pipe = null;
   }

   @Override
   public int getRequestsCount() {
      return 127;
   }

   @Override
   public ItemStack getRequest(int slot) {
      int facing = (slot & 0x70) >> 4;
      int action = (slot & 0xc) >> 2;
      int param = slot & 0x3;

      if (facing >= 6) {
         return ItemStack.EMPTY;
      }

      IPipeHolder holder = this.getPipe();
      if (holder == null) {
         return ItemStack.EMPTY;
      }

      Direction gateSide = Direction.from3DDataValue(facing);
      PipePluggable plug = holder.getPluggable(gateSide);
      if (!(plug instanceof PluggableGate gate) || gate.logic == null) {
         return ItemStack.EMPTY;
      }

      GateLogic logic = gate.logic;
      List<IStatement> actions = logic.getActions();
      if (actions.size() <= action) {
         return ItemStack.EMPTY;
      }

      IStatement targetAction = actions.get(action);
      if (targetAction == null || !BCRoboticsStatements.ACTION_STATION_REQUEST_ITEMS.getUniqueTag().equals(targetAction.getUniqueTag())) {
         return ItemStack.EMPTY;
      }

      for (StatementSlot slotStmt : logic.getActiveActions()) {
         if (slotStmt.statement == targetAction) {
            if (slotStmt.parameters.length <= param || slotStmt.parameters[param] == null) {
               return ItemStack.EMPTY;
            }

            return slotStmt.parameters[param].getItemStack();
         }
      }

      return ItemStack.EMPTY;
   }

   // --- Robot power ---------------------------------------------------------------------------------------------
   // The station is a small energy buffer that the pipe pushes into like any machine, and that drip-feeds the docked
   // robot. It exposes an MJ receiver (kinesis pipes) and an RF storage (RF pipes) to the pipe via the station
   // pluggable's capability hooks, so the pipe's normal push logic delivers here; and it posts its demand each tick
   // so upstream generators route power to this face. Charging the robot is then just draining the buffer -- no
   // fragile "extract from a push-based pipe" that never actually yields power.

   static final long POWER_BUFFER_CAP = MjAPI.MJ * 512L;
   private long powerBuffer;
   /** Cached RF/E adapter, typed as Object so DockingStationPipe itself never carries team.reborn.energy.EnergyStorage
    *  in its own signature (see {@link DockingStationRfStorage}). Created lazily by {@link #getEnergyStorage()}, which
    *  is only reached while the energy mod is installed. */
   private Object rfStorageCache;

   private final IMjReceiver mjReceiver = new IMjReceiver() {
      @Override
      public boolean canConnect(IMjConnector other) {
         return true;
      }

      @Override
      public long getPowerRequested() {
         return DockingStationPipe.this.powerRoom();
      }

      @Override
      public long receivePower(long microJoules, boolean simulate) {
         long accepted = Math.min(DockingStationPipe.this.powerRoom(), microJoules);
         if (!simulate) {
            DockingStationPipe.this.powerBuffer += accepted;
         }

         return microJoules - accepted;
      }
   };

   private final SnapshotParticipant<Long> rfJournal = new SnapshotParticipant<Long>() {
      @Override
      protected Long createSnapshot() {
         return DockingStationPipe.this.powerBuffer;
      }

      @Override
      protected void readSnapshot(Long snapshot) {
         DockingStationPipe.this.powerBuffer = snapshot;
      }
   };

   /** The MJ receiver the pipe delivers power to (exposed via the station pluggable's {@code getCapability}). */
   public IMjReceiver getMjReceiver() {
      return this.mjReceiver;
   }

   /** The RF storage the pipe delivers power to (exposed via the station pluggable's {@code energyStorage}). Lazily
    *  built so the {@link EnergyStorage} class is only touched when the energy mod is present -- see the field note. */
   public EnergyStorage getEnergyStorage() {
      if (this.rfStorageCache == null) {
         this.rfStorageCache = new DockingStationRfStorage(this);
      }

      return (EnergyStorage) this.rfStorageCache;
   }

   /** Current stored buffer, in micro-joules. Package-private for {@link DockingStationRfStorage}. */
   long getPowerBuffer() {
      return this.powerBuffer;
   }

   /** Add RF-sourced power (already converted to micro-joules) to the buffer inside a transaction, journalling so a
    *  rolled-back RF transfer does not leak power. Package-private for {@link DockingStationRfStorage}. */
   void rfInsert(TransactionContext transaction, long microJoules) {
      this.rfJournal.updateSnapshots(transaction);
      this.powerBuffer += microJoules;
   }

   /** Free MJ the buffer can still take -- but only "wanted" while a live robot bound to this station needs charge,
    * so an idle station never drains the network. Package-private for {@link DockingStationRfStorage}. */
   long powerRoom() {
      // robotTaking() resolves through the registry keyed by world; a detached station (world null) would NPE.
      if (this.world == null) {
         return 0L;
      }

      EntityRobotBase robot = this.robotTaking();
      if (robot == null) {
         return 0L;
      }

      long need = robot.getBattery().getCapacity() - robot.getBattery().getStored();
      if (need <= 0L) {
         return 0L;
      }

      return Math.max(0L, Math.min(POWER_BUFFER_CAP - this.powerBuffer, need));
   }

   /** Post this station's power demand into the pipe each tick so upstream generators route power to this face.
    * A blocking pluggable is skipped by the pipe's own {@code requestFromConnectedTiles}, so we drive the query
    * directly. The pipe never sleeps while a station is present ({@code needsTick() == true}). */
   public void tickPower() {
      long room = this.powerRoom();
      if (room <= 0L) {
         return;
      }

      IPipeHolder holder = this.getPipe();
      IPipe ipipe = holder == null ? null : holder.getPipe();
      Object flow = ipipe == null ? null : ipipe.getFlow();
      if (flow instanceof PipeFlowPower power) {
         PipeFlowPower.Section section = power.getSection(this.side);
         if (section != null) {
            section.nextPowerQuery += room;
         }
      } else if (flow instanceof PipeFlowRedstoneFlux rf) {
         PipeFlowRedstoneFlux.Section section = rf.getSection(this.side);
         if (section != null) {
            section.nextPowerQuery += (int) Math.min(Integer.MAX_VALUE, room / MjRfConversion.DEFAULT_MJ_PER_RF);
         }
      }
   }

   public long tryChargeRobot(EntityRobotBase robot) {
      if (robot == null || robot.getDockingStation() != this || !(robot instanceof EntityRobot entityRobot)) {
         return 0L;
      }

      long need = robot.getBattery().getCapacity() - robot.getBattery().getStored();
      long give = Math.min(need, this.powerBuffer);
      if (give <= 0L) {
         return 0L;
      }

      this.powerBuffer -= give;
      return entityRobot.receivePower(give, false);
   }

   @Override
   public ItemStack offerItem(int slot, ItemStack stack) {
      IInjectable output = this.getItemOutput();
      if (output == null) {
         return stack;
      }

      Direction from = this.side().getOpposite();
      ItemStack remaining = output.injectItem(stack.copy(), false, from, null, 0.0);
      return remaining;
   }
}
