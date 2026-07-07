/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AIRobotFetchItem extends AIRobot {
   
   public static final Set<Integer> targettedItems = Collections.newSetFromMap(new ConcurrentHashMap<>());

   private ItemEntity target;
   private float maxRange;
   private IStackFilter stackFilter;
   private IZone zone;
   private int pickTime = -1;

   public AIRobotFetchItem(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotFetchItem(EntityRobotBase robot, float maxRange, IStackFilter stackFilter, IZone zone) {
      this(robot);
      this.maxRange = maxRange;
      this.stackFilter = stackFilter;
      this.zone = zone;
   }

   @Override
   public void preempt(AIRobot ai) {
      if (this.target != null && !this.target.isAlive()) {
         this.terminate();
      }
   }

   @Override
   public void update() {
      if (this.target == null) {
         this.scanForItem();
      } else {
         this.pickTime++;
         if (this.pickTime > 5) {
            if (this.robot instanceof EntityRobot entityRobot) {
               ItemStack remaining = entityRobot.receiveItem(null, this.target.getItem());
               if (remaining.isEmpty()) {
                  this.target.discard();
               } else {
                  this.target.setItem(remaining);
               }
            }

            this.terminate();
         }
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoBlock) {
         if (this.target == null) {
            this.setSuccess(false);
            this.terminate();
         } else if (!ai.success()) {
            this.robot.unreachableEntityDetected(this.target);
            this.setSuccess(false);
            this.terminate();
         }
      }
   }

   @Override
   public void end() {
      if (this.target != null) {
         targettedItems.remove(this.target.getId());
      }
   }

   private void scanForItem() {
      double best = Double.MAX_VALUE;
      // Same station leash as the mob search: no zone -> a fixed sphere around the home station; a Zone Planner area
      // overrides it (wider query box, zone.contains clamps). Nearest-to-robot wins among the eligible drops.
      Vec3 anchor = this.robot.getWorkAnchor();
      float queryRange = this.zone != null ? EntityRobotBase.ZONE_SEARCH_RANGE : this.maxRange;
      AABB box = new AABB(anchor, anchor).inflate(queryRange);

      for (ItemEntity e : this.robot.level().getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive)) {
         if (targettedItems.contains(e.getId()) || this.robot.isKnownUnreachable(e)) {
            continue;
         }

         if (this.zone != null) {
            if (!this.zone.contains(e.position())) {
               continue;
            }
         } else if (anchor.distanceToSqr(e.position()) >= (double) this.maxRange * this.maxRange) {
            continue;
         }

         if (this.stackFilter != null && !this.stackFilter.matches(e.getItem())) {
            continue;
         }

         double distance = this.robot.position().distanceToSqr(e.position());
         if (distance < best) {
            best = distance;
            this.target = e;
         }
      }

      if (this.target != null) {
         targettedItems.add(this.target.getId());
         if (Math.floor(this.target.getX()) != Math.floor(this.robot.getX())
            || Math.floor(this.target.getY()) != Math.floor(this.robot.getY())
            || Math.floor(this.target.getZ()) != Math.floor(this.robot.getZ())) {
            this.startDelegateAI(new AIRobotGotoBlock(this.robot,
               (int)Math.floor(this.target.getX()), (int)Math.floor(this.target.getY()), (int)Math.floor(this.target.getZ())));
         }
      } else {
         this.setSuccess(false);
         this.terminate();
      }
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ * 3L / 2L;
   }
}
