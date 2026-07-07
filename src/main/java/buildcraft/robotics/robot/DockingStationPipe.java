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
import buildcraft.api.mj.MjRfConversion;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IFlowPower;
import buildcraft.api.transport.pipe.IFlowRedstoneFlux;
import buildcraft.api.transport.pipe.IPipe;
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

   public long tryChargeRobot(EntityRobotBase robot) {
      if (robot == null || robot.getDockingStation() != this || !(robot instanceof EntityRobot entityRobot)) {
         return 0L;
      }

      IPipeHolder holder = this.getPipe();
      if (holder == null) {
         return 0L;
      }

      IPipe ipipe = holder.getPipe();
      if (ipipe == null) {
         return 0L;
      }

      long needed = robot.getBattery().getCapacity() - robot.getBattery().getStored();
      if (needed <= 0L) {
         return 0L;
      }

      Direction from = this.side.getOpposite();
      Object flow = ipipe.getFlow();
      long extractedMj;
      if (flow instanceof IFlowPower power) {
         // Kinesis pipe: MJ directly.
         extractedMj = power.tryExtractPower(needed, from);
      } else if (flow instanceof IFlowRedstoneFlux rf) {
         // RF pipe: pull the equivalent amount of RF and convert it back to MJ (standard BuildCraft ratio).
         long mjPerRf = MjRfConversion.DEFAULT_MJ_PER_RF;
         // Floor, so we never pull more RF than the robot can bank (any sub-RF remainder tops up next tick).
         int neededRf = (int) Math.min(Integer.MAX_VALUE, needed / mjPerRf);
         if (neededRf <= 0) {
            return 0L;
         }

         int extractedRf = rf.tryExtractPower(neededRf, from);
         extractedMj = extractedRf * mjPerRf;
      } else {
         return 0L;
      }

      if (extractedMj > 0L) {
         return entityRobot.receivePower(extractedMj, false);
      }

      return 0L;
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
