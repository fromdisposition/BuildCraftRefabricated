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
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AIRobotFetchItem extends AIRobot {
   /** Drops another robot is already on its way to. Keyed per level, because entity ids are only unique within
    *  one, and every claim carries a deadline, because a robot discarded mid-fetch never gets to release it. */
   private static final Map<Level, Map<Integer, Long>> TARGETTED_ITEMS = Collections.synchronizedMap(new WeakHashMap<>());
   private static final long CLAIM_TTL_TICKS = 30L * 20L;

   private static Map<Integer, Long> claims(Level level) {
      return TARGETTED_ITEMS.computeIfAbsent(level, l -> new HashMap<>());
   }

   private static boolean isClaimed(Level level, int entityId) {
      Long deadline = claims(level).get(entityId);
      return deadline != null && deadline > level.getGameTime();
   }

   private static void claim(Level level, int entityId) {
      Map<Integer, Long> claims = claims(level);
      long now = level.getGameTime();
      claims.values().removeIf(deadline -> deadline <= now);
      claims.put(entityId, now + CLAIM_TTL_TICKS);
   }

   private static void unclaim(Level level, int entityId) {
      claims(level).remove(entityId);
   }

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
         unclaim(this.robot.level(), this.target.getId());
      }
   }

   private void scanForItem() {
      double best = Double.MAX_VALUE;
      // Same station leash as the mob search: no zone -> a fixed sphere around the home station; a Zone Planner area
      // overrides it (wider query box, zone.contains clamps). Nearest-to-robot wins among the eligible drops.
      Vec3 anchor = this.robot.getWorkAnchor();
      AABB box = queryBox(anchor, this.zone, this.maxRange);

      for (ItemEntity e : this.robot.level().getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive)) {
         if (isClaimed(this.robot.level(), e.getId()) || this.robot.isKnownUnreachable(e)) {
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
         claim(this.robot.level(), this.target.getId());
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

   /** The candidate-gathering box: the zone's own footprint when it has one, otherwise the leash sphere. */
   static AABB queryBox(Vec3 anchor, IZone zone, float range) {
      if (zone == null) {
         return new AABB(anchor, anchor).inflate(range);
      }

      AABB bounds = zone.horizontalBounds();
      if (bounds == null) {
         return new AABB(anchor, anchor).inflate(EntityRobotBase.ZONE_SEARCH_RANGE);
      }

      double reach = EntityRobotBase.ZONE_SEARCH_RANGE;
      return new AABB(bounds.minX, anchor.y - reach, bounds.minZ, bounds.maxX, anchor.y + reach, bounds.maxZ);
   }
}
