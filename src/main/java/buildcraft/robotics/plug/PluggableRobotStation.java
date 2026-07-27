/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.plug;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.robot.DockingStationPipe;
// Client-only model key; only referenced from getModelRenderKey, which is invoked exclusively client-side during
// model baking -- so the class is never loaded on a dedicated server (same pattern as PluggableBlocker).
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import team.reborn.energy.api.EnergyStorage;

public class PluggableRobotStation extends PipePluggable implements IDockingStationProvider {
   private static final AABB[] BOXES = new AABB[6];

   public enum RobotStationState {
      None,
      Available,
      Reserved,
      Linked;
   }

   private DockingStationPipe station;
   private RobotStationState renderState = RobotStationState.None;

   public PluggableRobotStation(PluggableDefinition definition, IPipeHolder holder, Direction side) {
      super(definition, holder, side);
   }

   @Override
   public AABB getBoundingBox() {
      return BOXES[this.side.ordinal()];
   }

   @Override
   public PluggableModelKey getModelRenderKey(Object layer) {
      // The port never gave the docking station an in-world model, so it was invisible. Render it as the standard
      // plug box (reusing the already-registered blocker baker) so it shows on the pipe and can be aimed at.
      return "cutout".equals(layer) ? new KeyPlugBlocker(this.side) : null;
   }

   @Override
   public boolean isBlocking() {
      return true;
   }

   @Override
   public boolean canBeConnected() {
      return false;
   }

   @Override
   public ItemStack getPickStack() {
      return new ItemStack(BCRoboticsItems.ROBOT_STATION);
   }

   @Override
   public DockingStation getStation() {
      Level world = this.holder.getPipeWorld();
      if (this.station == null && world != null && !world.isClientSide()) {
         DockingStation existing = RobotManager.registryProvider.getRegistry(world).getStation(this.holder.getPipePos(), this.side);
         if (existing instanceof DockingStationPipe pipeStation) {
            // A station reused from the registry may have been loaded detached (world/pipe null); bind it to this
            // live holder so robotTaking()/powerRoom() can resolve the registry without NPEing.
            pipeStation.bindToPipe(this.holder);
            this.station = pipeStation;
         } else {
            this.station = new DockingStationPipe(this.holder, this.side);
            RobotManager.registryProvider.getRegistry(world).registerStation(this.station);
         }
      }

      return this.station;
   }

   @Override
   public void onPlacedBy(Player player) {
      super.onPlacedBy(player);
      if (!this.holder.getPipeWorld().isClientSide()) {
         this.getStation();
      }
   }

   @Override
   public void onRemove() {
      Level world = this.holder.getPipeWorld();
      if (world == null || world.isClientSide()) {
         return;
      }

      BlockPos pos = this.holder.getPipePos();
      var registry = RobotManager.registryProvider.getRegistry(world);
      // this.station is a lazily-populated transient (can be null right after a reload); fall back to the
      // authoritative registry station for this position.
      DockingStation station = this.station != null ? this.station : registry.getStation(pos, this.side);
      if (station instanceof DockingStationPipe pipeStation) {
         pipeStation.bindToPipe(this.holder);
      }

      // Breaking the station destroys the robot that calls it home: drop it as its board (with stored energy and
      // carried tool/loot) where it stood -- robots are otherwise invulnerable/unremovable. Try the direct
      // station->robot link first, then scan nearby robot entities matched by their persisted home-station pos/side.
      // The scan is the robust path: the registry link can be momentarily unresolved on the single-plug removal
      // path, which is why a plain robotTaking() check silently missed and left the robot hanging.
      if (station != null && station.isMainStation() && station.robotTaking() instanceof EntityRobot linked) {
         linked.dropAsItemAndDiscard();
      }

      for (EntityRobot robot : world.getEntitiesOfClass(EntityRobot.class, new AABB(pos).inflate(2.0))) {
         if (!robot.isRemoved() && robot.isHomedAt(pos, this.side)) {
            robot.dropAsItemAndDiscard();
         }
      }

      if (station != null) {
         registry.removeStation(station);
      }

      this.station = null;
   }

   @Override
   public void onChunkUnload() {
      // The station outlives this pluggable in the robot registry; drop its reference to the now-removed holder
      // so the next getPipe() re-resolves through the level instead of self-deleting on a stale isRemoved() hit.
      if (this.station != null) {
         this.station.onChunkUnload();
      }
   }

   /** Set once the station throws while ticking (usually a legacy/corrupt state); it then stops ticking instead of
    * crashing the pipe every tick. */
   private boolean tickFaulted;

   @Override
   public boolean needsTick() {
      return !this.tickFaulted;
   }

   @Override
   public void onTick() {
      Level world = this.holder.getPipeWorld();
      if (world == null || world.isClientSide() || this.tickFaulted) {
         return;
      }

      try {
         this.getStation();
         if (this.station != null) {
            this.station.tickPower();
         }

         RobotStationState newState = this.computeState();
         if (newState != this.renderState) {
            this.renderState = newState;
            this.scheduleNetworkUpdate();
         }
      } catch (Exception e) {
         // A broken docking station must not crash the pipe tick; disable it and keep the world alive.
         this.tickFaulted = true;
         buildcraft.api.core.BCLog.logger.warn(
            "[robots] Docking station at " + this.holder.getPipePos() + " threw while ticking; disabling it to keep the world stable", e);
      }
   }

   // The pipe delivers power to the station like any machine: kinesis pipes push MJ into the receiver, RF pipes
   // insert into the energy storage. Both feed the station's buffer, which drip-feeds the docked robot.
   @Override
   @SuppressWarnings("unchecked")
   public <T> T getCapability(Object cap) {
      if (cap == MjAPI.CAP_RECEIVER) {
         this.getStation();
         if (this.station != null) {
            return (T) this.station.getMjReceiver();
         }
      }

      return null;
   }

   @Override
   public EnergyStorage energyStorage() {
      this.getStation();
      return this.station != null ? this.station.getEnergyStorage() : null;
   }

   private RobotStationState computeState() {
      DockingStation s = this.getStation();
      if (s == null) {
         return RobotStationState.None;
      } else if (s.isMainStation()) {
         return RobotStationState.Linked;
      } else {
         return s.isTaken() ? RobotStationState.Reserved : RobotStationState.Available;
      }
   }

   public RobotStationState getRenderState() {
      return this.renderState;
   }

   @Override
   public CompoundTag writeClientUpdateData() {
      CompoundTag nbt = super.writeClientUpdateData();
      nbt.putByte("state", (byte) this.renderState.ordinal());
      return nbt;
   }

   @Override
   public void readClientUpdateData(CompoundTag nbt) {
      super.readClientUpdateData(nbt);
      int ordinal = BcNbt.getByte(nbt, "state", (byte) 0);
      RobotStationState[] values = RobotStationState.values();
      this.renderState = ordinal >= 0 && ordinal < values.length ? values[ordinal] : RobotStationState.None;
   }

   @Override
   public void writeCreationPayload(FriendlyByteBuf buffer) {
      super.writeCreationPayload(buffer);
      buffer.writeByte(this.renderState.ordinal());
   }

   static {
      double min = 0.25;
      double max = 0.75;
      double l = 0.0;
      double h = 0.25;
      double ll = 0.75;
      double hh = 1.0;
      BOXES[Direction.DOWN.ordinal()] = new AABB(min, l, min, max, h, max);
      BOXES[Direction.UP.ordinal()] = new AABB(min, ll, min, max, hh, max);
      BOXES[Direction.NORTH.ordinal()] = new AABB(min, min, l, max, max, h);
      BOXES[Direction.SOUTH.ordinal()] = new AABB(min, min, ll, max, max, hh);
      BOXES[Direction.WEST.ordinal()] = new AABB(l, min, min, h, max, max);
      BOXES[Direction.EAST.ordinal()] = new AABB(ll, min, min, hh, max, max);
   }
}
