/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.robots;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;
import java.util.Arrays;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;

public abstract class DockingStation {
   public Direction side;
   public Level world;
   private long robotTakingId = Long.MAX_VALUE;
   private EntityRobotBase robotTaking;
   private boolean linkIsMain = false;
   private BlockPos pos;

   public DockingStation(BlockPos iIndex, Direction iSide) {
      this.pos = iIndex;
      this.side = iSide;
   }

   public DockingStation() {
   }

   public boolean isMainStation() {
      return this.linkIsMain;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Direction side() {
      return this.side;
   }

   public EntityRobotBase robotTaking() {
      if (this.robotTakingId == Long.MAX_VALUE || this.world == null) {
         return null;
      }

      if (this.robotTaking == null) {
         this.robotTaking = RobotManager.registryProvider.getRegistry(this.world).getLoadedRobot(this.robotTakingId);
      }

      return this.robotTaking;
   }

   public void invalidateRobotTakingEntity() {
      this.robotTaking = null;
   }

   public long linkedId() {
      return this.robotTakingId;
   }

   /**
    * The single source of truth for occupancy: this station is taken only while a robot that <b>still exists</b>
    * holds it. {@link #robotTaking()} resolves the taking id through the registry and returns {@code null} if that
    * robot is no longer loaded, so a station left reserved by a robot that has since been removed -- a "ghost" take
    * that used to permanently block re-deploying -- is treated as free again. {@link #isTaken}, {@link #takeAsMain}
    * and {@link #take} all route through this one check so they can never disagree.
    */
   private boolean isOccupiedByLiveRobot() {
      return this.robotTakingId != Long.MAX_VALUE && this.robotTaking() != null;
   }

   public boolean takeAsMain(EntityRobotBase robot) {
      if (this.world == null) {
         return false;
      }

      if (!this.isOccupiedByLiveRobot()) {
         IRobotRegistry registry = RobotManager.registryProvider.getRegistry(this.world);
         this.linkIsMain = true;
         this.robotTaking = robot;
         this.robotTakingId = robot.getRobotId();
         registry.registryMarkDirty();
         robot.setMainStation(this);
         registry.take(this, robot.getRobotId());
         return true;
      } else {
         return this.robotTakingId == robot.getRobotId();
      }
   }

   public boolean take(EntityRobotBase robot) {
      if (this.world == null) {
         return false;
      }

      if (!this.isOccupiedByLiveRobot()) {
         IRobotRegistry registry = RobotManager.registryProvider.getRegistry(this.world);
         this.linkIsMain = false;
         this.robotTaking = robot;
         this.robotTakingId = robot.getRobotId();
         registry.registryMarkDirty();
         registry.take(this, robot.getRobotId());
         return true;
      } else {
         return robot.getRobotId() == this.robotTakingId;
      }
   }

   public void release(EntityRobotBase robot) {
      if (this.world == null) {
         this.unsafeRelease(robot);
         return;
      }

      if (this.robotTaking == robot && !this.linkIsMain) {
         IRobotRegistry registry = RobotManager.registryProvider.getRegistry(this.world);
         this.unsafeRelease(robot);
         registry.registryMarkDirty();
         registry.release(this, robot.getRobotId());
      }
   }

   public void unsafeRelease(EntityRobotBase robot) {
      if (this.robotTaking == robot) {
         this.linkIsMain = false;
         this.robotTaking = null;
         this.robotTakingId = Long.MAX_VALUE;
      }
   }

   public void writeToNBT(CompoundTag nbt) {
      nbt.putIntArray("pos", new int[]{this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()});
      nbt.putByte("side", (byte)this.side.ordinal());
      nbt.putBoolean("isMain", this.linkIsMain);
      nbt.putLong("robotId", this.robotTakingId);
   }

   public void readFromNBT(CompoundTag nbt) {
      if (nbt.contains("index")) {
         CompoundTag indexNBT = BcNbt.getCompound(nbt, "index");
         int x = BcNbt.getInt(indexNBT, "i", 0);
         int y = BcNbt.getInt(indexNBT, "j", 0);
         int z = BcNbt.getInt(indexNBT, "k", 0);
         this.pos = new BlockPos(x, y, z);
      } else {
         int[] array = BcNbt.getIntArray(nbt, "pos");
         if (array.length == 3) {
            this.pos = new BlockPos(array[0], array[1], array[2]);
         } else if (array.length != 0) {
            BCLog.logger.warn("Found an integer array that was not the right length! (" + Arrays.toString(array) + ")");
         } else {
            BCLog.logger.warn("Did not find any integer positions! This is a bug!");
         }
      }

      this.side = Direction.values()[BcNbt.getByte(nbt, "side", (byte)0)];
      this.linkIsMain = BcNbt.getBoolean(nbt, "isMain", false);
      this.robotTakingId = BcNbt.getLong(nbt, "robotId", Long.MAX_VALUE);
   }

   public boolean isTaken() {
      return this.isOccupiedByLiveRobot();
   }

   public long robotIdTaking() {
      return this.robotTakingId;
   }

   public BlockPos index() {
      return this.pos;
   }

   @Override
   public String toString() {
      return "{" + this.pos + ", " + this.side + " :" + this.robotTakingId + "}";
   }

   public boolean linkIsDocked() {
      return this.robotTaking() != null ? this.robotTaking().getDockingStation() == this : false;
   }

   public boolean canRelease() {
      return !this.isMainStation() && !this.linkIsDocked();
   }

   public boolean isInitialized() {
      return true;
   }

   public abstract Iterable<StatementSlot> getActiveActions();

   /** True if any active gate action on this station carries the given unique tag. A sleeping docked robot polls
    * this every tick for the wakeup action (a gate pulse can be active for a single tick, so sampling less often
    * would miss it) -- subclasses should override it allocation-free instead of building the full action list. */
   public boolean hasActiveAction(String uniqueTag) {
      for (StatementSlot slot : this.getActiveActions()) {
         if (slot.statement != null && uniqueTag.equals(slot.statement.getUniqueTag())) {
            return true;
         }
      }

      return false;
   }

   public IInjectable getItemOutput() {
      return null;
   }

   public EnumPipePart getItemOutputSide() {
      return EnumPipePart.CENTER;
   }

   public Container getItemInput() {
      return null;
   }

   public EnumPipePart getItemInputSide() {
      return EnumPipePart.CENTER;
   }

   public Storage<FluidVariant> getFluidOutput() {
      return null;
   }

   public EnumPipePart getFluidOutputSide() {
      return EnumPipePart.CENTER;
   }

   public Storage<FluidVariant> getFluidInput() {
      return null;
   }

   public EnumPipePart getFluidInputSide() {
      return EnumPipePart.CENTER;
   }

   public boolean providesPower() {
      return false;
   }

   public IRequestProvider getRequestProvider() {
      return null;
   }

   public void onChunkUnload() {
   }
}
