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
import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
//? if >= 26.2 {
import net.minecraft.world.entity.EntityTypes;
//?}
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class EntityRobotBase extends LivingEntity {
   public static final long MAX_POWER = 5000L * MjAPI.MJ;
   public static final long SAFETY_POWER = MAX_POWER / 5L;
   public static final long SHUTDOWN_POWER = 0L;
   public static final long NULL_ROBOT_ID = Long.MAX_VALUE;
   /**
    * Leash radius (blocks) from the robot's home station for every search that has no explicit zone -- mobs, animals,
    * dropped items and harvestable blocks alike. The classic 250 let a Knight/Butcher/Picker roam ~15 chunks and clear
    * half the world; worse, searches centred on the robot's <em>current</em> position, so it ratcheted outward chasing
    * a trail. Measuring from the station instead makes this a true leash the robot cannot drift past. A Zone Planner
    * "work area" overrides it (see {@link #ZONE_SEARCH_RANGE}), so a larger patrol is an explicit choice.
    */
   public static final float DEFAULT_SEARCH_RANGE = 32.0F;
   /**
    * Coarse query bound (blocks) used only when a Zone Planner work area is set: the actual clamp is {@code zone
    * contains()}, but entity/item lookups still need a finite box around the station to gather candidates, so a large
    * drawn zone is not silently clipped to {@link #DEFAULT_SEARCH_RANGE}.
    */
   public static final float ZONE_SEARCH_RANGE = 128.0F;

   public EntityRobotBase(Level par1World) {
      //? if >= 26.2 {
      super(EntityTypes.PIG, par1World);
      //?} else {
/*super(EntityType.PIG, par1World);
      *///?}
   }

   public EntityRobotBase(EntityType<? extends EntityRobotBase> type, Level par1World) {
      super(type, par1World);
   }

   /**
    * The profile of the player who deployed this robot, or {@code null} for legacy robots saved
    * before owner tracking existed. The owner is threaded into every world-modifying action the
    * robot performs so that land-claim protection (e.g. Open Parties and Claims, via the standard
    * Fabric break/use events) can authorize the robot exactly as it would the owning player. A
    * {@code null} owner is treated as the generic [BuildCraft] fake player by
    * {@code BuildCraftAPI.fakePlayerProvider}, which fails closed inside any claim.
    */
   @Nullable
   private GameProfile owner;

   @Nullable
   public GameProfile getOwner() {
      return this.owner;
   }

   public void setOwner(@Nullable GameProfile owner) {
      this.owner = owner;
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

   /** Centre of the robot's home (linked) station -- the leash origin for all searches. Falls back to the robot's own
    *  position if it has no linked station yet (e.g. mid-reload), which just reproduces the old robot-centred behaviour. */
   public Vec3 getWorkAnchor() {
      DockingStation station = this.getLinkedStation();
      return station != null ? Vec3.atCenterOf(station.getPos()) : this.position();
   }

   /** Block form of {@link #getWorkAnchor()} for the block scanners. */
   public BlockPos getWorkAnchorPos() {
      DockingStation station = this.getLinkedStation();
      return station != null ? station.getPos() : this.blockPosition();
   }

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
