/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.robots;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
//? if >= 26.1.3 {
/*import net.minecraft.world.entity.EntityTypes;*/
//?}
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class EntityRobotBase extends LivingEntity {
   public static final long MAX_POWER = 5000L * MjAPI.MJ;
   public static final long SAFETY_POWER = MAX_POWER / 5L;
   public static final long SHUTDOWN_POWER = 0L;
   public static final long NULL_ROBOT_ID = Long.MAX_VALUE;

   public EntityRobotBase(Level par1World) {
      //? if >= 26.1.3 {
/*      super(EntityTypes.PIG, par1World);*/
      //?} else {
super(EntityType.PIG, par1World);
      //?}
   }

   public EntityRobotBase(EntityType<? extends EntityRobotBase> type, Level par1World) {
      super(type, par1World);
   }

   public abstract void setItemInUse(ItemStack var1);

   public abstract ItemStack getHeldItem();

   public abstract void setItemActive(boolean var1);

   public abstract boolean isMoving();

   public abstract DockingStation getLinkedStation();

   public abstract RedstoneBoardRobot getBoard();

   public abstract void aimItemAt(float var1, float var2);

   public abstract void aimItemAt(BlockPos var1);

   public abstract float getAimYaw();

   public abstract float getAimPitch();

   public long getPower() {
      return this.getBattery().getStored();
   }

   public abstract MjBattery getBattery();

   public abstract DockingStation getDockingStation();

   public abstract void dock(DockingStation var1);

   public abstract void undock();

   public abstract IZone getZoneToWork();

   public abstract IZone getZoneToLoadUnload();

   public abstract boolean containsItems();

   public abstract boolean hasFreeSlot();

   public abstract void unreachableEntityDetected(Entity var1);

   public abstract boolean isKnownUnreachable(Entity var1);

   public abstract long getRobotId();

   public abstract void setUniqueRobotId(long var1);

   public abstract IRobotRegistry getRegistry();

   public abstract void releaseResources();

   public abstract void onChunkUnload();

   public abstract ItemStack receiveItem(BlockEntity var1, ItemStack var2);

   public abstract void setMainStation(DockingStation var1);

   public abstract Storage<FluidVariant> getFluidStorage();

   
   public boolean hasFluid() {
      for (StorageView<FluidVariant> view : this.getFluidStorage()) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            return true;
         }
      }

      return false;
   }
}
