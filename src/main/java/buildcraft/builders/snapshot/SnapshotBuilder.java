/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.nbt.BcNbt;
import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.net.PacketBufferBC;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public abstract class SnapshotBuilder<T extends ITileForSnapshotBuilder> {
   private static final int MAX_QUEUE_SIZE = 16;
   protected static final byte CHECK_RESULT_UNKNOWN = 0;
   protected static final byte CHECK_RESULT_CORRECT = 1;
   protected static final byte CHECK_RESULT_TO_BREAK = 2;
   protected static final byte CHECK_RESULT_TO_PLACE = 3;
   private static final byte REQUIRED_UNKNOWN = 0;
   private static final byte REQUIRED_TRUE = 1;
   private static final byte REQUIRED_FALSE = 2;
   private static final int CHECKS_PER_TICK = 10;
   private static final long MAX_POWER_PER_TICK = 256L * MjAPI.MJ;
   protected final T tile;
   public final Queue<SnapshotBuilder<T>.BreakTask> breakTasks = new ArrayDeque<>();
   public final Queue<SnapshotBuilder<T>.BreakTask> clientBreakTasks = new ArrayDeque<>();
   public final Set<SnapshotBuilder<T>.BreakTask> clientBreakTasksCache = new HashSet<>();
   public final Set<SnapshotBuilder<T>.PlaceTask> clientPlaceTasksCache = new HashSet<>();
   public final Queue<SnapshotBuilder<T>.BreakTask> prevClientBreakTasks = new ArrayDeque<>();
   public final Queue<SnapshotBuilder<T>.PlaceTask> placeTasks = new ArrayDeque<>();
   public final Queue<SnapshotBuilder<T>.PlaceTask> clientPlaceTasks = new ArrayDeque<>();
   public final Queue<SnapshotBuilder<T>.PlaceTask> prevClientPlaceTasks = new ArrayDeque<>();
   protected byte[] checkResults;
   private byte[] requiredCache;
   private int[] breakOrder;
   private int[] placeOrder;
   private int[] checkOrder;
   private int currentCheckIndex;
   public Vec3 robotPos;
   public Vec3 prevRobotPos;
   public Vec3 visualRobotPos;
   public Vec3 visualPrevRobotPos;
   public int leftToBreak = 0;
   public int leftToPlace = 0;
   @Nullable
   private Boolean areaHasFluidCache;

   protected SnapshotBuilder(T tile) {
      this.tile = tile;
   }

   protected abstract Snapshot.BuildingInfo getBuildingInfo();

   protected EnumFluidHandlingMode getFluidMode() {
      return this.tile.getFluidMode();
   }

   private int breakPriorityTier(BlockPos pos) {
      FluidState fs = this.tile.getWorldBC().getFluidState(pos);
      if (fs.isEmpty()) {
         return 2;
      } else {
         return fs.isSource() ? 0 : 1;
      }
   }

   protected boolean isAllowedDuringFluidMop(BlockPos blockPos) {
      return false;
   }

   protected boolean isFragileSchematicAt(BlockPos blockPos) {
      return false;
   }

   protected void invalidateFluidCache() {
      this.areaHasFluidCache = null;
   }

   protected boolean buildAreaHasAnyFluid() {
      if (this.areaHasFluidCache != null) {
         return this.areaHasFluidCache;
      }

      Snapshot.BuildingInfo info = this.getBuildingInfo();
      if (info == null) {
         this.areaHasFluidCache = false;
         return false;
      }

      BlockPos min = info.box.min();
      BlockPos max = info.box.max();

      for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
         if (!this.tile.getWorldBC().getFluidState(pos).isEmpty()) {
            this.areaHasFluidCache = true;
            return true;
         }
      }

      this.areaHasFluidCache = false;
      return false;
   }

   public void validate() {
   }

   public void invalidate() {
   }

   protected abstract boolean isAir(BlockPos var1);

   protected abstract boolean canPlace(BlockPos var1);

   protected abstract boolean isReadyToPlace(BlockPos var1);

   protected abstract boolean hasEnoughToPlaceItems(BlockPos var1);

   protected abstract List<ItemStack> getToPlaceItems(BlockPos var1);

   protected abstract boolean doPlaceTask(SnapshotBuilder<T>.PlaceTask var1);

   private void cancelBreakTask(SnapshotBuilder<T>.BreakTask breakTask) {
      if (this.tile.getWorldBC() != null && !this.tile.getWorldBC().isClientSide()) {
         this.tile.getBattery().addPower(Math.min(breakTask.power, this.tile.getBattery().getCapacity() - this.tile.getBattery().getStored()), false);
      }
   }

   protected void cancelPlaceTask(SnapshotBuilder<T>.PlaceTask placeTask) {
      if (this.tile.getWorldBC() != null && !this.tile.getWorldBC().isClientSide()) {
         this.tile.getBattery().addPower(Math.min(placeTask.power, this.tile.getBattery().getCapacity() - this.tile.getBattery().getStored()), false);
      }
   }

   protected abstract boolean isBlockCorrect(BlockPos var1);

   public Vec3 getPlaceTaskItemPos(SnapshotBuilder<T>.PlaceTask placeTask) {
      Vec3 height = Vec3.atLowerCornerOf(placeTask.pos.subtract(this.tile.getBuilderPos()));
      double progress = placeTask.power * 1.0 / placeTask.getTarget();
      return Vec3.atLowerCornerOf(this.tile.getBuilderPos())
         .add(height.scale(progress))
         .add(new Vec3(0.0, Math.sin(progress * Math.PI) * (Math.abs(height.y) + 1.0), 0.0))
         .add(new Vec3(0.5, 1.0, 0.5));
   }

   public void updateSnapshot() {
      Snapshot.BuildingInfo info = this.getBuildingInfo();
      int volume = info.box.size().getX() * info.box.size().getY() * info.box.size().getZ();
      this.checkResults = new byte[volume];
      Arrays.fill(this.checkResults, (byte)0);
      this.requiredCache = new byte[volume];
      Arrays.fill(this.requiredCache, (byte)0);
      this.invalidateFluidCache();
      List<BlockPos> blocks = info.box.getBlocksInArea();
      BlockPos center = info.box.center();
      BlockPos builderPos = this.tile.getBuilderPos();
      int centerX = center.getX();
      int centerY = center.getY();
      int centerZ = center.getZ();
      int builderX = builderPos.getX();
      int builderY = builderPos.getY();
      int builderZ = builderPos.getZ();
      this.breakOrder = blocks.stream().sorted(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos -> {
         int dx = blockPos.getX() - centerX;
         int dz = blockPos.getZ() - centerZ;
         return (double)dx * dx + (double)dz * dz + 100000.0 - Math.abs(blockPos.getY() - builderY) * 100000;
      }))).mapToInt(this::posToIndex).toArray();
      this.placeOrder = blocks.stream().sorted(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos -> {
         int dx = blockPos.getX() - builderX;
         int dz = blockPos.getZ() - builderZ;
         return 100000.0 - ((double)dx * dx + (double)dz * dz) + Math.abs(blockPos.getY() - builderY) * 100000;
      }))).mapToInt(this::posToIndex).toArray();
      this.checkOrder = blocks.stream().sorted(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos -> {
         int dx = blockPos.getX() - centerX;
         int dy = blockPos.getY() - centerY;
         int dz = blockPos.getZ() - centerZ;
         return (double)dx * dx + (double)dy * dy + (double)dz * dz;
      }))).mapToInt(this::posToIndex).toArray();
   }

   public void resourcesChanged() {
      if (this.requiredCache != null) {
         Arrays.fill(this.requiredCache, (byte)0);
      }
   }

   public void cancel() {
      this.breakTasks.forEach(this::cancelBreakTask);
      this.placeTasks.forEach(this::cancelPlaceTask);
      this.breakTasks.clear();
      this.clientBreakTasks.clear();
      this.prevClientBreakTasks.clear();
      this.placeTasks.clear();
      this.clientPlaceTasks.clear();
      this.prevClientPlaceTasks.clear();
      this.checkResults = null;
      this.requiredCache = null;
      this.areaHasFluidCache = null;
      this.breakOrder = null;
      this.placeOrder = null;
      this.checkOrder = null;
      this.currentCheckIndex = 0;
      this.robotPos = null;
      this.prevRobotPos = null;
      this.leftToBreak = 0;
      this.leftToPlace = 0;
   }

   public boolean tick() {
      boolean checkResultsChanged = false;

      for (int i = 0; i < 10; i++) {
         if (this.check(this.indexToPos(this.checkOrder[this.currentCheckIndex]))) {
            checkResultsChanged = true;
         }

         this.currentCheckIndex = (this.currentCheckIndex + 1) % this.checkOrder.length;
      }

      Iterator<SnapshotBuilder<T>.BreakTask> iterator = this.breakTasks.iterator();

      while (iterator.hasNext()) {
         SnapshotBuilder<T>.BreakTask breakTask = iterator.next();
         if (this.checkResults[this.posToIndex(breakTask.pos)] == 1) {
            iterator.remove();
            this.cancelBreakTask(breakTask);
         }
      }

      Iterator<SnapshotBuilder<T>.PlaceTask> placeIterator = this.placeTasks.iterator();

      while (placeIterator.hasNext()) {
         SnapshotBuilder<T>.PlaceTask placeTask = placeIterator.next();
         if (this.checkResults[this.posToIndex(placeTask.pos)] == 1) {
            placeIterator.remove();
            this.cancelPlaceTask(placeTask);
         }
      }

      boolean isDone = true;

      for (byte result : this.checkResults) {
         if (result == 0) {
            isDone = false;
            break;
         }
      }

      if (isDone && (!this.breakTasks.isEmpty() || !this.placeTasks.isEmpty())) {
         isDone = false;
      }

      long stored = this.tile.getBattery().getStored();
      long max = stored <= 0L
         ? 0L
         : Math.min((long)((double)MAX_POWER_PER_TICK * stored / (this.tile.getBattery().getCapacity() * 2L)), MAX_POWER_PER_TICK);
      if (this.tile.canExcavate()) {
         IntOpenHashSet breakTasksIndexes = new IntOpenHashSet(this.breakTasks.size());

         for (SnapshotBuilder<T>.BreakTask breakTask : this.breakTasks) {
            breakTasksIndexes.add(this.posToIndex(breakTask.pos));
         }

         // Fused single pass over breakOrder. The old stream materialized every candidate into an int[], then
         // re-probed the world's FluidState per element in the accept filter AND on every comparison of the
         // boxed priority sort — O(k log k) block lookups per tick just to pick <= 16 tasks. The stable sort
         // by tier (0 = source, 1 = flowing, 2 = dry) + limit is equivalent to three order-preserving buckets
         // capped at the task budget, fed by ONE FluidState probe per candidate; once the source bucket is
         // full no later candidate can enter the selection, so the rest of the pass only counts — and the
         // count never needed world access in the first place.
         int needed = max > 0L ? 16 - this.breakTasks.size() : 0;
         boolean clearFluidMode = this.getFluidMode() == EnumFluidHandlingMode.CLEAR;
         IntArrayList sourceBucket = new IntArrayList();
         IntArrayList flowingBucket = new IntArrayList();
         IntArrayList dryBucket = new IntArrayList();
         int breakCandidates = 0;

         for (int index : this.breakOrder) {
            if (this.checkResults[index] == 2 && !breakTasksIndexes.contains(index)) {
               breakCandidates++;
               if (needed > 0 && sourceBucket.size() < needed) {
                  FluidState fluidAt = this.tile.getWorldBC().getFluidState(this.indexToPos(index));
                  if (fluidAt.isEmpty()) {
                     if (dryBucket.size() < needed) {
                        dryBucket.add(index);
                     }
                  } else if (clearFluidMode) {
                     if (fluidAt.isSource()) {
                        sourceBucket.add(index);
                     } else if (flowingBucket.size() < needed) {
                        flowingBucket.add(index);
                     }
                  }
               }
            }
         }

         this.leftToBreak = breakCandidates;
         if (breakCandidates != 0) {
            isDone = false;
         }

         int added = 0;
         for (IntArrayList bucket : new IntArrayList[]{sourceBucket, flowingBucket, dryBucket}) {
            for (int i = 0; i < bucket.size() && added < needed; i++) {
               this.breakTasks.add(new BreakTask(this.indexToPos(bucket.getInt(i)), 0L));
               added++;
            }
         }
      } else {
         this.leftToBreak = 0;
      }

      IntOpenHashSet placeTasksIndexes = new IntOpenHashSet(this.placeTasks.size());

      for (SnapshotBuilder<T>.PlaceTask placeTaskx : this.placeTasks) {
         placeTasksIndexes.add(this.posToIndex(placeTaskx.pos));
      }

      boolean areaHasFluid = (this.getFluidMode() == EnumFluidHandlingMode.CLEAR || this.getFluidMode() == EnumFluidHandlingMode.REPLACE)
         && this.buildAreaHasAnyFluid();
      boolean clearStillMopping = areaHasFluid && this.getFluidMode() == EnumFluidHandlingMode.CLEAR;
      boolean replaceFragileGated = areaHasFluid && this.getFluidMode() == EnumFluidHandlingMode.REPLACE;
      // Fused single pass, mirroring the break path: the old code materialized every candidate into an int[]
      // just to count them, then re-scanned it with a second stream. The selection chain below runs exactly as
      // the stream did — limit() counted the candidates that passed isReadyToPlace, with canPlace and the item
      // lookup applied only to those — and stops evaluating once the task budget is met, while the cheap count
      // continues to the end of the order.
      boolean selecting = !this.tile.canExcavate() || this.breakTasks.isEmpty();
      int neededPlace = selecting && max > 0L ? 16 - this.placeTasks.size() : 0;
      int placeCandidates = 0;
      int passedReady = 0;

      for (int index : this.placeOrder) {
         if (this.checkResults[index] == 3 && !placeTasksIndexes.contains(index)) {
            placeCandidates++;
            if (passedReady < neededPlace) {
               boolean has;
               if (this.requiredCache[index] != 0) {
                  has = this.requiredCache[index] == 1;
               } else {
                  has = this.hasEnoughToPlaceItems(this.indexToPos(index));
                  this.requiredCache[index] = (byte)(has ? 1 : 2);
               }

               if (has) {
                  BlockPos pos = this.indexToPos(index);
                  boolean gateOk = clearStillMopping ? this.isAllowedDuringFluidMop(pos) : !replaceFragileGated || !this.isFragileSchematicAt(pos);
                  if (gateOk && this.isReadyToPlace(pos)) {
                     passedReady++;
                     if (this.canPlace(pos)) {
                        List<ItemStack> items = this.getToPlaceItems(pos);
                        if (items != null) {
                           this.placeTasks.add(new PlaceTask(pos, items, 0L));
                        }
                     }
                  }
               }
            }
         }
      }

      this.leftToPlace = placeCandidates;
      if (selecting && placeCandidates != 0) {
         isDone = false;
      }

      if (!this.breakTasks.isEmpty()) {
         Iterator<SnapshotBuilder<T>.BreakTask> iteratorx = this.breakTasks.iterator();

         while (iteratorx.hasNext()) {
            SnapshotBuilder<T>.BreakTask breakTask = iteratorx.next();
            if (breakTask.isImpossible()) {
               this.cancelBreakTask(breakTask);
               iteratorx.remove();
            } else {
               long target = breakTask.getTarget();
               breakTask.power = breakTask.power + this.tile.getBattery().extractPower(0L, Math.min(target - breakTask.power, max / this.breakTasks.size()));
               if (breakTask.power >= target) {
                  this.clientBreakTasksCache.add(breakTask);
                  this.tile.getWorldBC().destroyBlockProgress(breakTask.pos.hashCode(), breakTask.pos, -1);
                  Optional<BlockUtil.BreakResult> result = BlockUtil.breakBlockAndGetDropsWithXp(
                     (ServerLevel)this.tile.getWorldBC(), breakTask.pos, this.tile.getBreakingTool(), this.tile.getOwner()
                  );
                  if (result.isEmpty()) {
                     this.cancelBreakTask(breakTask);
                  } else {
                     BlockUtil.BreakResult br = result.get();
                     this.tile.onBlockBroken(breakTask.pos, br.drops(), br.xp(), br.capturedFluid());
                  }

                  if (this.check(breakTask.pos)) {
                     checkResultsChanged = true;
                  }

                  this.invalidateFluidCache();
                  iteratorx.remove();
               } else {
                  this.clientBreakTasksCache.add(breakTask);
                  this.tile.getWorldBC().destroyBlockProgress(breakTask.pos.hashCode(), breakTask.pos, (int)(breakTask.power * 9L / target));
               }
            }
         }
      }

      if (!this.placeTasks.isEmpty()) {
         Iterator<SnapshotBuilder<T>.PlaceTask> iteratorx = this.placeTasks.iterator();

         while (iteratorx.hasNext()) {
            SnapshotBuilder<T>.PlaceTask placeTask = iteratorx.next();
            long target = placeTask.getTarget();
            placeTask.power = placeTask.power + this.tile.getBattery().extractPower(0L, Math.min(target - placeTask.power, max / this.placeTasks.size()));
            if (placeTask.power >= target) {
               this.clientPlaceTasksCache.add(placeTask);
               if (!this.doPlaceTask(placeTask)) {
                  this.cancelPlaceTask(placeTask);
               }

               if (this.check(placeTask.pos)) {
                  checkResultsChanged = true;
               }

               this.invalidateFluidCache();
               iteratorx.remove();
            } else {
               this.clientPlaceTasksCache.add(placeTask);
            }
         }
      }

      if (checkResultsChanged) {
         this.afterChecks();
      }

      return isDone;
   }

   public void clientTick() {
      long stored = this.tile.getBattery().getStored();
      long max = stored <= 0L
         ? 0L
         : Math.min((long)((double)MAX_POWER_PER_TICK * stored / (this.tile.getBattery().getCapacity() * 2L)), MAX_POWER_PER_TICK);
      this.prevClientBreakTasks.clear();

      for (SnapshotBuilder<T>.BreakTask task : this.clientBreakTasks) {
         this.prevClientBreakTasks.add(new BreakTask(task.pos, task.power));
         long target = task.getTarget();
         if (stored > 0L && task.power < target) {
            long increment = Math.min(target - task.power, max / Math.max(1, this.clientBreakTasks.size()));
            task.power = task.power + Math.min(increment, stored);
         }
      }

      this.prevClientPlaceTasks.clear();

      for (SnapshotBuilder<T>.PlaceTask task : this.clientPlaceTasks) {
         this.prevClientPlaceTasks.add(new PlaceTask(task.pos, task.items, task.power));
         long target = task.getTarget();
         if (stored > 0L && this.clientBreakTasks.isEmpty() && task.power < target) {
            long increment = Math.min(target - task.power, max / Math.max(1, this.clientPlaceTasks.size()));
            task.power = task.power + Math.min(increment, stored);
         }
      }

      this.prevRobotPos = this.robotPos;
      if (!this.clientBreakTasks.isEmpty()) {
         Vec3 newRobotPos = this.clientBreakTasks
            .stream()
            .map(breakTask -> breakTask.pos)
            .map(Vec3::atLowerCornerOf)
            .<Vec3>map(VecUtil.VEC_HALF::add)
            .reduce(Vec3.ZERO, Vec3::add)
            .scale(1.0 / this.clientBreakTasks.size());
         newRobotPos = new Vec3(
            newRobotPos.x, this.clientBreakTasks.stream().map(breakTask -> breakTask.pos).mapToDouble(Vec3i::getY).max().orElse(newRobotPos.y), newRobotPos.z
         );
         newRobotPos = newRobotPos.add(new Vec3(0.0, 3.0, 0.0));
         Vec3 oldRobotPos = this.robotPos;
         this.robotPos = newRobotPos;
         if (oldRobotPos != null) {
            this.robotPos = oldRobotPos.add(newRobotPos.subtract(oldRobotPos).scale(0.25));
         }
      } else if (!this.clientPlaceTasks.isEmpty()) {
         Vec3 newRobotPos = this.clientPlaceTasks
            .stream()
            .map(placeTask -> placeTask.pos)
            .map(Vec3::atLowerCornerOf)
            .<Vec3>map(VecUtil.VEC_HALF::add)
            .reduce(Vec3.ZERO, Vec3::add)
            .scale(1.0 / this.clientPlaceTasks.size());
         newRobotPos = newRobotPos.add(new Vec3(0.0, 3.0, 0.0));
         Vec3 oldRobotPos = this.robotPos;
         this.robotPos = newRobotPos;
         if (oldRobotPos != null) {
            this.robotPos = oldRobotPos.add(newRobotPos.subtract(oldRobotPos).scale(0.25));
         }
      } else {
         this.robotPos = null;
      }

      this.visualPrevRobotPos = this.visualRobotPos;
      if (this.robotPos != null) {
         if (this.visualRobotPos == null) {
            this.visualRobotPos = this.robotPos;
            this.visualPrevRobotPos = this.robotPos;
         } else {
            this.visualRobotPos = this.visualRobotPos.add(this.robotPos.subtract(this.visualRobotPos).scale(0.25));
         }
      } else {
         this.visualRobotPos = null;
         this.visualPrevRobotPos = null;
      }
   }

   public void receiveServerTaskData(Queue<SnapshotBuilder<T>.BreakTask> serverBreakTasks, Queue<SnapshotBuilder<T>.PlaceTask> serverPlaceTasks) {
      this.receiveServerTaskData(serverBreakTasks, serverPlaceTasks, this.clientBreakTasks, this.clientPlaceTasks);
   }

   public void receiveServerTaskData(
      Queue<SnapshotBuilder<T>.BreakTask> serverBreakTasks,
      Queue<SnapshotBuilder<T>.PlaceTask> serverPlaceTasks,
      Iterable<SnapshotBuilder<T>.BreakTask> savedClientBreak,
      Iterable<SnapshotBuilder<T>.PlaceTask> savedClientPlace
   ) {
      Queue<SnapshotBuilder<T>.BreakTask> mergedBreak = new ArrayDeque<>();

      for (SnapshotBuilder<T>.BreakTask serverTask : serverBreakTasks) {
         long mergedPower = serverTask.power;

         for (SnapshotBuilder<T>.BreakTask clientTask : savedClientBreak) {
            if (clientTask.pos.equals(serverTask.pos)) {
               mergedPower = Math.max(mergedPower, clientTask.power);
               break;
            }
         }

         mergedBreak.add(new BreakTask(serverTask.pos, mergedPower));
      }

      this.clientBreakTasks.clear();
      this.clientBreakTasks.addAll(mergedBreak);
      Queue<SnapshotBuilder<T>.PlaceTask> mergedPlace = new ArrayDeque<>();

      for (SnapshotBuilder<T>.PlaceTask serverTask : serverPlaceTasks) {
         long mergedPower = serverTask.power;

         for (SnapshotBuilder<T>.PlaceTask clientTask : savedClientPlace) {
            if (clientTask.pos.equals(serverTask.pos)) {
               mergedPower = Math.max(mergedPower, clientTask.power);
               break;
            }
         }

         mergedPlace.add(new PlaceTask(serverTask.pos, serverTask.items, mergedPower));
      }

      this.clientPlaceTasks.clear();
      this.clientPlaceTasks.addAll(mergedPlace);
   }

   private boolean shouldBreakQueueAcceptFluid(BlockPos blockPos) {
      FluidState fs = this.tile.getWorldBC().getFluidState(blockPos);
      return fs.isEmpty() ? true : this.getFluidMode() == EnumFluidHandlingMode.CLEAR;
   }

   public void invalidateChecksForFluidPositions() {
      if (this.checkResults != null && this.getBuildingInfo() != null) {
         for (int i = 0; i < this.checkResults.length; i++) {
            BlockPos pos = this.indexToPos(i);
            if (!this.tile.getWorldBC().getFluidState(pos).isEmpty()) {
               this.checkResults[i] = 0;
            }
         }
      }
   }

   protected int posToIndex(BlockPos blockPos) {
      return this.getBuildingInfo().getSnapshot().posToIndex(this.getBuildingInfo().fromWorld(blockPos));
   }

   protected BlockPos indexToPos(int i) {
      return this.getBuildingInfo().toWorld(this.getBuildingInfo().getSnapshot().indexToPos(i));
   }

   protected boolean check(BlockPos blockPos) {
      int i = this.posToIndex(blockPos);
      byte prev = this.checkResults[i];
      if (this.isAir(blockPos)) {
         if (this.tile.getWorldBC().isEmptyBlock(blockPos)) {
            this.checkResults[i] = 1;
         } else {
            this.checkResults[i] = 2;
         }
      } else if (this.isBlockCorrect(blockPos)) {
         this.checkResults[i] = 1;
      } else if (this.canPlace(blockPos)) {
         this.checkResults[i] = 3;
      } else {
         this.checkResults[i] = 2;
      }

      return prev != this.checkResults[i];
   }

   protected void afterChecks() {
   }

   public void writeToByteBuf(FriendlyByteBuf buffer) {
      buffer.writeInt(this.breakTasks.size());
      this.breakTasks.forEach(breakTask -> breakTask.writePayload(buffer));
      buffer.writeInt(this.placeTasks.size());
      this.placeTasks.forEach(placeTask -> placeTask.writePayload(buffer));
      buffer.writeInt(this.leftToBreak);
      buffer.writeInt(this.leftToPlace);
   }

   public void readFromByteBuf(FriendlyByteBuf buffer) {
      this.breakTasks.clear();
      IntStream.range(0, buffer.readInt()).mapToObj(i -> new BreakTask(buffer)).forEach(this.breakTasks::add);
      this.placeTasks.clear();
      IntStream.range(0, buffer.readInt()).mapToObj(i -> new PlaceTask(buffer)).forEach(this.placeTasks::add);
      this.leftToBreak = buffer.readInt();
      this.leftToPlace = buffer.readInt();
   }

   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      if (this.checkResults != null) {
         nbt.putByteArray("checkResults", this.checkResults);
      } else {
         nbt.putByteArray("checkResults", new byte[0]);
      }

      nbt.put("breakTasks", NBTUtilBC.writeCompoundList(this.breakTasks.stream().map(SnapshotBuilder.BreakTask::writeToNBT)));
      nbt.put("placeTasks", NBTUtilBC.writeCompoundList(this.placeTasks.stream().map(SnapshotBuilder.PlaceTask::writeToNBT)));
      nbt.putInt("currentCheckIndex", this.currentCheckIndex);
      return nbt;
   }

   public CompoundTag serializeClientNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.put("breakTasks", NBTUtilBC.writeCompoundList(this.clientBreakTasksCache.stream().map(SnapshotBuilder.BreakTask::writeToNBT)));
      nbt.put("placeTasks", NBTUtilBC.writeCompoundList(this.clientPlaceTasksCache.stream().map(SnapshotBuilder.PlaceTask::writeToNBT)));
      return nbt;
   }

   public void onNetworkSync() {
      this.clientBreakTasksCache.clear();
      this.clientPlaceTasksCache.clear();
   }

   public void deserializeNBT(CompoundTag nbt) {
      if (this.getBuildingInfo() != null) {
         this.updateSnapshot();
         //? if >= 1.21.10 {
         byte[] loadedCheckResults = nbt.getByteArray("checkResults").orElse(new byte[0]);
         //?} else {
         /*byte[] loadedCheckResults = nbt.getByteArray("checkResults");
         *///?}
         if (loadedCheckResults.length == this.checkResults.length) {
            System.arraycopy(loadedCheckResults, 0, this.checkResults, 0, this.checkResults.length);
         }

         this.breakTasks.clear();
         NBTUtilBC.readCompoundList(nbt.get("breakTasks")).map(x$0 -> new BreakTask(x$0)).forEach(this.breakTasks::add);
         this.placeTasks.clear();
         NBTUtilBC.readCompoundList(nbt.get("placeTasks")).map(x$0 -> new PlaceTask(x$0)).forEach(this.placeTasks::add);
         this.currentCheckIndex = BcNbt.getInt(nbt, "currentCheckIndex", 0);
      }
   }

   public void loadClientNBT(CompoundTag tag, Iterable<SnapshotBuilder<T>.BreakTask> savedClientBreak, Iterable<SnapshotBuilder<T>.PlaceTask> savedClientPlace) {
      Queue<SnapshotBuilder<T>.BreakTask> serverBreak = new ArrayDeque<>();
      Queue<SnapshotBuilder<T>.PlaceTask> serverPlace = new ArrayDeque<>();
      NBTUtilBC.readCompoundList(tag.get("breakTasks")).map(x$0 -> new BreakTask(x$0)).forEach(serverBreak::add);
      NBTUtilBC.readCompoundList(tag.get("placeTasks")).map(x$0 -> new PlaceTask(x$0)).forEach(serverPlace::add);
      this.receiveServerTaskData(serverBreak, serverPlace, savedClientBreak, savedClientPlace);
   }

   public class BreakTask {
      public final BlockPos pos;
      public long power;

      public BreakTask(BlockPos pos, long power) {
         this.pos = pos;
         this.power = power;
      }

      public BreakTask(FriendlyByteBuf buffer) {
         this.pos = buffer.readBlockPos();
         this.power = buffer.readLong();
      }

      public BreakTask(CompoundTag nbt) {
         this.pos = new BlockPos((int)BcNbt.getLong(nbt, "pos_x", 0L), (int)BcNbt.getLong(nbt, "pos_y", 0L), (int)BcNbt.getLong(nbt, "pos_z", 0L));
         this.power = BcNbt.getLong(nbt, "power", 0L);
      }

      public boolean isImpossible() {
         return BlockUtil.isUnbreakableBlock(SnapshotBuilder.this.tile.getWorldBC(), this.pos, SnapshotBuilder.this.tile.getOwner())
            ? true
            : SnapshotBuilder.this.tile.getWorldBC() instanceof ServerLevel serverLevel
               && !BlockUtil.canMachineBreak(serverLevel, this.pos, SnapshotBuilder.this.tile.getOwner());
      }

      public long getTarget() {
         return BlockUtil.computeBlockBreakPower(SnapshotBuilder.this.tile.getWorldBC(), this.pos);
      }

      public void writePayload(FriendlyByteBuf buffer) {
         buffer.writeBlockPos(this.pos);
         buffer.writeLong(this.power);
      }

      public CompoundTag writeToNBT() {
         CompoundTag nbt = new CompoundTag();
         nbt.putLong("pos_x", this.pos.getX());
         nbt.putLong("pos_y", this.pos.getY());
         nbt.putLong("pos_z", this.pos.getZ());
         nbt.putLong("power", this.power);
         return nbt;
      }
   }

   public class PlaceTask {
      public final BlockPos pos;
      public final List<ItemStack> items;
      public long power;

      public PlaceTask(BlockPos pos, List<ItemStack> items, long power) {
         this.pos = pos;
         this.items = Optional.ofNullable(items).<List<ItemStack>>map(ImmutableList::copyOf).orElse(null);
         this.power = power;
      }

      public PlaceTask(FriendlyByteBuf buffer) {
         this.pos = buffer.readBlockPos();
         this.items = IntStream.range(0, buffer.readInt())
            .mapToObj(
               j -> {
                  CompoundTag itemTag = buffer.readNbt();
                  Tag payload = itemTag == null ? null : itemTag.get("stack");
                  ItemStack stack = payload == null
                     ? ItemStack.EMPTY
                     : ItemStack.CODEC.parse(NBTUtilBC.registryAwareOps(), payload).resultOrPartial().orElse(ItemStack.EMPTY);
                  int count = buffer.readInt();
                  return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(count);
               }
            )
            .collect(Collectors.toList());
         this.power = buffer.readLong();
      }

      public PlaceTask(CompoundTag nbt) {
         this.pos = new BlockPos((int)BcNbt.getLong(nbt, "pos_x", 0L), (int)BcNbt.getLong(nbt, "pos_y", 0L), (int)BcNbt.getLong(nbt, "pos_z", 0L));
         this.items = ImmutableList.copyOf(
            NBTUtilBC.readCompoundList(nbt.get("items"))
               .map(
                  tag -> ItemStack.CODEC.parse(NBTUtilBC.registryAwareOps(), tag).resultOrPartial().orElse(ItemStack.EMPTY)
               )
               .filter(stack -> !stack.isEmpty())
               .collect(Collectors.toList())
         );
         this.power = BcNbt.getLong(nbt, "power", 0L);
      }

      public long getTarget() {
         return (long)(Math.sqrt(this.pos.distSqr(SnapshotBuilder.this.tile.getBuilderPos())) * 10.0 * MjAPI.MJ);
      }

      public void writePayload(FriendlyByteBuf buffer) {
         buffer.writeBlockPos(this.pos);
         buffer.writeInt(this.items.size());
         this.items
            .forEach(
               item -> {
                  CompoundTag tag = new CompoundTag();
                  if (!item.isEmpty()) {
                     ItemStack.CODEC
                        .encodeStart(NBTUtilBC.registryAwareOps(), item.copyWithCount(1))
                        .resultOrPartial()
                        .ifPresent(payload -> tag.put("stack", payload));
                  }

                  buffer.writeNbt(tag);
                  buffer.writeInt(item.getCount());
               }
            );
         buffer.writeLong(this.power);
      }

      public CompoundTag writeToNBT() {
         CompoundTag nbt = new CompoundTag();
         nbt.putLong("pos_x", this.pos.getX());
         nbt.putLong("pos_y", this.pos.getY());
         nbt.putLong("pos_z", this.pos.getZ());
         ListTag list = new ListTag();

         for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
               ItemStack.CODEC.encodeStart(NBTUtilBC.registryAwareOps(), stack).resultOrPartial().ifPresent(list::add);
            }
         }

         nbt.put("items", list);
         nbt.putLong("power", this.power);
         return nbt;
      }
   }
}
