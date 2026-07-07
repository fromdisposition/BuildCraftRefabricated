/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.IEntityFilter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AIRobotSearchEntity extends AIRobot {
   public Entity target;
   private float maxRange;
   private IZone zone;
   private IEntityFilter filter;

   public AIRobotSearchEntity(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotSearchEntity(EntityRobotBase robot, IEntityFilter filter, float maxRange, IZone zone) {
      this(robot);
      this.filter = filter;
      this.maxRange = maxRange;
      this.zone = zone;
   }

   @Override
   public void start() {
      double best = Double.MAX_VALUE;
      // Leash the search to the home station, not the robot's drifting position: with no zone we scan a fixed sphere
      // of DEFAULT_SEARCH_RANGE around the station; a Zone Planner area overrides it (wider query box, zone.contains
      // does the real clamp). Nearest-to-robot still wins among the eligible targets so the robot heads to the closest.
      Vec3 anchor = this.robot.getWorkAnchor();
      float queryRange = this.zone != null ? EntityRobotBase.ZONE_SEARCH_RANGE : this.maxRange;
      AABB box = new AABB(anchor, anchor).inflate(queryRange);

      for (Entity e : this.robot.level().getEntitiesOfClass(Entity.class, box, e -> e.isAlive() && this.filter.matches(e))) {
         if (this.robot.isKnownUnreachable(e)) {
            continue;
         }

         if (this.zone != null) {
            if (!this.zone.contains(e.position())) {
               continue;
            }
         } else if (anchor.distanceToSqr(e.position()) >= (double) this.maxRange * this.maxRange) {
            continue;
         }

         double distance = this.robot.position().distanceToSqr(e.position());
         if (distance < best) {
            best = distance;
            this.target = e;
         }
      }

      this.terminate();
   }

   @Override
   public boolean success() {
      return this.target != null;
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ / 5L;
   }
}
