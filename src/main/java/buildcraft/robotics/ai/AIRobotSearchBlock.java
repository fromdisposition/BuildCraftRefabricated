/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.robotics.path.BlockScannerExpanding;
import buildcraft.robotics.path.IBlockFilter;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class AIRobotSearchBlock extends AIRobot {
   /** Cheap positions walked per expensive check allowed: most of an expanding cube is sky, void, out-of-zone or
    * unloaded, all rejected by pure math (no chunk access), so skipping them must not eat the expensive-check
    * budget -- otherwise a zoned sweep (radius 64 = ~2.1M positions) took ~9 minutes to notice new work. Still
    * bounded so a tiny zone inside a big cube cannot spin the loop unbounded in one tick. The expensive budget
    * itself (block-state + filter checks per tick) comes from {@code BCRoboticsConfig.scanBudgetPerTick}. */
   private static final int ITERATIONS_PER_CHECK = 20;
   /** Expanding-scan reach (blocks) when a Zone Planner area is set -- the classic BuildCraft cap, kept so zoned
    *  harvesters still reach as far as before; zone.contains() does the precise clamp inside it. */
   private static final int ZONE_SCAN_RADIUS = 64;
   private static final java.util.Random RANDOM = new java.util.Random();

   public BlockPos blockFound;
   private IBlockFilter filter;
   private IZone zone;
   private Iterator<BlockPos> blockIter;
   private BlockPos origin;
   private Vec3 anchor;
   private boolean random;
   private double maxDistanceToEnd;
   private int randomAttempts;
   /** Scratch position handed to the expensive checks; {@link #blockFound} always stores an immutable copy. */
   private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

   public AIRobotSearchBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotSearchBlock(EntityRobotBase robot, boolean random, IBlockFilter filter, double maxDistanceToEnd) {
      super(robot);
      this.filter = filter;
      this.random = random;
      this.maxDistanceToEnd = maxDistanceToEnd;
      this.zone = robot.getZoneToWork();
   }

   @Override
   public void start() {
      // Leash block searches to the home station, not the robot's position, so a harvester cannot ratchet across the
      // world following a trail of targets. No zone -> expand only DEFAULT_SEARCH_RANGE from the station; a Zone
      // Planner area overrides it (walk out to the classic reach, then zone.contains() clamps to the drawn region).
      this.origin = this.robot.getWorkAnchorPos();
      this.anchor = Vec3.atCenterOf(this.origin);
      if (!this.random) {
         int cap = this.zone != null ? ZONE_SCAN_RADIUS : (int) EntityRobotBase.DEFAULT_SEARCH_RANGE;
         this.blockIter = new BlockScannerExpanding(cap).iterator();
      }
   }

   @Override
   public void update() {
      if (this.filter == null) {
         this.terminate();
         return;
      }

      int scanBudget = buildcraft.robotics.BCRoboticsConfig.scanBudgetPerTick.get();
      if (this.random) {
         this.scanRandom(scanBudget);
      } else {
         this.scanExpanding(scanBudget);
      }
   }

   private void scanExpanding(int scanBudget) {
      int iterationBudget = scanBudget * ITERATIONS_PER_CHECK;
      int checks = 0;
      for (int i = 0; i < iterationBudget && checks < scanBudget; i++) {
         if (this.blockIter == null || !this.blockIter.hasNext()) {
            this.terminate();
            return;
         }

         // The scanner's iterator reuses one mutable position; consume its coordinates immediately.
         BlockPos step = this.blockIter.next();
         int bx = this.origin.getX() + step.getX();
         int by = this.origin.getY() + step.getY();
         int bz = this.origin.getZ() + step.getZ();
         if (!this.cheapAccept(bx, by, bz)) {
            continue;
         }

         checks++;
         if (this.fullAccept(this.cursor.set(bx, by, bz))) {
            this.blockFound = this.cursor.immutable();
            this.terminate();
            return;
         }
      }
   }

   private void scanRandom(int scanBudget) {
      for (int i = 0; i < scanBudget; i++) {
         this.randomAttempts++;
         if (this.randomAttempts > 4096) {
            this.terminate();
            return;
         }

         int bx;
         int by;
         int bz;
         if (this.zone != null) {
            BlockPos candidate = this.zone.getRandomBlockPos(RANDOM);
            if (candidate == null) {
               this.terminate();
               return;
            }

            // The zone only knows chunk-local X/Z; its Y is a world-agnostic placeholder (legacy 0..254). Re-roll
            // Y across the ACTUAL build height here, where the level is known, so zoned random search can reach the
            // negative-Y region of modern worlds instead of being clamped above y=0.
            net.minecraft.world.level.Level level = this.robot.level();
            bx = candidate.getX();
            by = level.getMinY() + RANDOM.nextInt(level.getHeight());
            bz = candidate.getZ();
         } else {
            double r = this.robot.level().getRandom().nextFloat() * EntityRobotBase.DEFAULT_SEARCH_RANGE;
            float a = this.robot.level().getRandom().nextFloat() * 2.0F * (float)Math.PI;
            bx = (int)(Math.cos(a) * r + this.origin.getX());
            by = this.origin.getY();
            bz = (int)(Math.sin(a) * r + this.origin.getZ());
         }

         if (this.cheapAccept(bx, by, bz) && this.fullAccept(this.cursor.set(bx, by, bz))) {
            this.blockFound = this.cursor.immutable();
            this.terminate();
            return;
         }
      }
   }

   /** Pure-math rejects, allocation-free (runs for every walked position): build height, zone / station leash,
    * caller's reach limit, and whether the chunk is even loaded. Touching an unloaded chunk in {@code fullAccept}
    * would sync-load it -- both a lag spike and a world-load side effect a scan must never have. */
   private boolean cheapAccept(int bx, int by, int bz) {
      net.minecraft.world.level.Level level = this.robot.level();
      if (by < level.getMinY() || by >= level.getMinY() + level.getHeight()) {
         return false;
      }

      double cx = bx + 0.5;
      double cy = by + 0.5;
      double cz = bz + 0.5;
      if (this.zone != null) {
         if (!this.zone.contains(cx, cy, cz)) {
            return false;
         }
      } else if (this.anchor.distanceToSqr(cx, cy, cz) > (double) EntityRobotBase.DEFAULT_SEARCH_RANGE * EntityRobotBase.DEFAULT_SEARCH_RANGE) {
         // No zone: keep the target inside the station leash sphere.
         return false;
      }

      if (this.maxDistanceToEnd > 0.0 && this.robot.position().distanceToSqr(cx, cy, cz) > this.maxDistanceToEnd * this.maxDistanceToEnd) {
         return false;
      }

      // Same check BcRegistryUtil.isChunkLoaded does, without materialising a BlockPos for it.
      return level.getChunkSource().hasChunk(bx >> 4, bz >> 4);
   }

   /** The per-candidate work worth budgeting: reservation lookup + the board's block filter (block-state reads). */
   private boolean fullAccept(BlockPos pos) {
      if (this.robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
         return false;
      }

      return this.filter.matches(this.robot.level(), pos);
   }

   public boolean takeResource() {
      return this.robot.getRegistry().take(new ResourceIdBlock(this.blockFound), this.robot);
   }

   public void releaseResource() {
      if (this.blockFound != null) {
         this.robot.getRegistry().release(new ResourceIdBlock(this.blockFound));
      }
   }

   @Override
   public boolean success() {
      return this.blockFound != null;
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.blockFound != null) {
         nbt.putIntArray("blockFound", new int[]{this.blockFound.getX(), this.blockFound.getY(), this.blockFound.getZ()});
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = BcNbt.getIntArray(nbt, "blockFound");
      if (arr.length == 3) {
         this.blockFound = new BlockPos(arr[0], arr[1], arr[2]);
      }
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ / 5L;
   }
}
