/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.robot;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjBattery;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.BlueprintBuilder;
import buildcraft.builders.snapshot.ITileForBlueprintBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.lib.fabric.transfer.MultiFluidTankStorage;
import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.builders.snapshot.EnumFluidHandlingMode;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.robotics.entity.EntityRobot;
import com.mojang.authlib.GameProfile;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class RobotBlueprintContext implements ITileForBlueprintBuilder {
   private final EntityRobot robot;
   private final BlockPos markerPos;
   private final Blueprint.BuildingInfo buildingInfo;
   @Nullable
   private final GameProfile owner;
   private final GatedMjBattery battery;
   private final RobotItemTransactor invResources;
   private final MultiFluidTankStorage fluidTanks;
   private final BlueprintBuilder builder;

   public RobotBlueprintContext(EntityRobot robot, BlockPos markerPos, Blueprint.BuildingInfo buildingInfo, @Nullable GameProfile owner) {
      this.robot = robot;
      this.markerPos = markerPos.immutable();
      this.buildingInfo = buildingInfo;
      this.owner = owner;
      this.battery = new GatedMjBattery(robot.getBattery());
      this.invResources = new RobotItemTransactor(robot);
      this.fluidTanks = new MultiFluidTankStorage(new SingleFluidTank[]{robot.getFluidStorage()});
      this.builder = new BlueprintBuilder(this);
   }

   public BlueprintBuilder getBlueprintBuilder() {
      return this.builder;
   }

   public BlockPos getMarkerPos() {
      return this.markerPos;
   }

   public void setInRange(boolean inRange) {
      this.battery.inRange = inRange;
   }

   @Override
   public Level getWorldBC() {
      return this.robot.level();
   }

   @Override
   public MjBattery getBattery() {
      return this.battery;
   }

   @Override
   public BlockPos getBuilderPos() {
      return this.markerPos;
   }

   @Override
   public boolean canExcavate() {
      return true;
   }

   @Override
   public SnapshotBuilder<?> getBuilder() {
      return this.builder;
   }

   @Override
   public GameProfile getOwner() {
      return this.owner;
   }

   @Override
   public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
      return this.buildingInfo;
   }

   @Override
   public IItemTransactor getInvResources() {
      return this.invResources;
   }

   @Override
   public MultiFluidTankStorage getFluidTanks() {
      return this.fluidTanks;
   }

   @Override
   public void onBlockBroken(BlockPos brokenPos, List<ItemStack> drops, int xp, FluidStack capturedFluid) {
      if (this.robot.level() instanceof ServerLevel serverLevel) {
         for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
               ItemStack remaining = this.invResources.insert(stack.copy(), false, false);
               if (!remaining.isEmpty()) {
                  Block.popResource(serverLevel, brokenPos, remaining);
               }
            }
         }

         if (xp > 0) {
            ExperienceOrb.award(serverLevel, Vec3.atCenterOf(this.robot.blockPosition()), xp);
         }

         if (!capturedFluid.isEmpty() && this.getFluidMode() == EnumFluidHandlingMode.CLEAR) {
            this.fluidTanks.insertMillibuckets(capturedFluid, capturedFluid.getAmount(), true);
         }
      }
   }
}
