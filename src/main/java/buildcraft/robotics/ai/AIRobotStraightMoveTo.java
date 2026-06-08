/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.nbt.CompoundTag;

public class AIRobotStraightMoveTo extends AIRobotGoto {
   private double finalX;
   private double finalY;
   private double finalZ;

   public AIRobotStraightMoveTo(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotStraightMoveTo(EntityRobotBase robot, double x, double y, double z) {
      this(robot);
      this.finalX = x;
      this.finalY = y;
      this.finalZ = z;
   }

   @Override
   public void start() {
      this.setDestination(this.robot, this.finalX, this.finalY, this.finalZ);
   }

   @Override
   public void update() {
      if (!this.robot.isMoving()) {
         this.terminate();
      }
   }

   @Override
   public void end() {
      this.clearDestination(this.robot);
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      nbt.putDouble("x", this.finalX);
      nbt.putDouble("y", this.finalY);
      nbt.putDouble("z", this.finalZ);
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      this.finalX = nbt.getDouble("x").orElse(0.0);
      this.finalY = nbt.getDouble("y").orElse(0.0);
      this.finalZ = nbt.getDouble("z").orElse(0.0);
   }
}
