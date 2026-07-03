/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import buildcraft.lib.nbt.BcAuth;
import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersConfig;
import buildcraft.builders.BCBuildersEntities;
import buildcraft.builders.BCBuildersEventDist;
import buildcraft.builders.entity.EntityQuarryRig;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.marker.VolumeCache;
import buildcraft.core.marker.VolumeConnection;
import buildcraft.core.marker.VolumeSubCache;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.chunkload.ChunkLoaderManager;
import buildcraft.lib.chunkload.IChunkLoadingTile;
import buildcraft.lib.debug.IAdvDebugTarget;
import buildcraft.lib.fabric.transfer.MjEnergyStorage;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AxisOrder;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.BcBlockEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TileQuarry extends BcBlockEntity implements IDebuggable, IHasWork, IChunkLoadingTile, IAdvDebugTarget {
   public static final boolean DEBUG_QUARRY = BCDebugging.shouldDebugLog("builders.quarry");
   private static final long MAX_POWER_PER_TICK = 512L * MjAPI.MJ;
   private static final Identifier DIGGY_DIGGY_HOLE = Identifier.parse("buildcraftbuilders:diggy_diggy_hole");
   private final MjBattery battery = new MjBattery(24000L * MjAPI.MJ);
   private final MjBatteryReceiver mjReceiver = new MjBatteryReceiver(this.battery);
   public final Box frameBox = new Box();
   final Box miningBox = new Box();
   private BoxIterator boxIterator;
   public final List<BlockPos> framePoses = new ArrayList<>();
   private int frameBoxPosesCount = 0;
   private final LinkedList<BlockPos> toCheck = new LinkedList<>();
   private final Set<BlockPos> firstCheckedPoses = new HashSet<>();
   private boolean firstChecked = false;
   private final Set<BlockPos> frameBreakBlockPoses = new TreeSet<>(
      BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(p -> this.getBlockPos().distSqr(p)))
   );
   private final Set<BlockPos> framePlaceFramePoses = new HashSet<>();
   public TileQuarry.Task currentTask = null;
   public Vec3 drillPos;
   public Vec3 clientDrillPos;
   public Vec3 prevClientDrillPos;
   private long debugPowerRate = 0L;
   private double blockPercentSoFar;
   private double moveDistanceSoFar;
   private boolean advancementGranted = false;
   long lastFullSpeedTick = Long.MIN_VALUE;
   private boolean deferredUpdatePoses = false;
   private boolean deferredChunkLoad = false;
   private List<AABB> collisionBoxes = ImmutableList.of();
   private Vec3 collisionDrillPos;
   private final List<EntityQuarryRig> rigs = new ArrayList<>();
   // The getCollisionBoxes() list reference + phasing the rig segments were last built from, so updateRigs can
   // skip the per-tick section-split + rig re-set while the drill sits on one block (the common case).
   private List<AABB> rigsBuiltFromBoxes;
   private boolean rigsBuiltPhasing;
   // Minecraft stores each entity in the single 16-block section of its CENTRE, and a player's collision query
   // only scans the sections around the player. A rig box that CROSSES a section boundary is therefore not found
   // when the player stands on the part that lies in a different section than the box centre (the hitbox still
   // renders — rendering iterates every entity — but there is no collision). So a rig box is cut at the 16-block
   // section boundaries: each piece lies within one section, where the player standing on it always queries it.
   // A box that already fits inside one section (the common case for a normal quarry) stays a single entity.

   public TileQuarry(BlockPos pos, BlockState state) {
      super(BCBuildersBlockEntities.QUARRY, pos, state);
   }

   public MjBatteryReceiver getMjReceiver() {
      return this.mjReceiver;
   }

   public long getLastFullSpeedTick() {
      return this.lastFullSpeedTick;
   }

   public MjBattery getBattery() {
      return this.battery;
   }

   @Nullable
   public MjEnergyStorage getSidedEnergyStorage() {
      return MjEnergyStorage.createIfRfEnabled(this.getBattery());
   }

   @Nonnull
   private BoxIterator createBoxIterator() {
      long x = this.getBlockPos().getX();
      long y = this.getBlockPos().getY();
      long z = this.getBlockPos().getZ();
      long seed = (x & 65535L) << 0 | (y & 65535L) << 16 | (z & 65535L) << 32;
      Random rand = new Random(seed);
      EnumAxisOrder axisOrder = rand.nextBoolean() ? EnumAxisOrder.XZY : EnumAxisOrder.ZXY;
      AxisOrder.Inversion inv = AxisOrder.Inversion.getFor(rand.nextBoolean(), rand.nextBoolean(), false);
      return new BoxIterator(this.miningBox, AxisOrder.getFor(axisOrder, inv), true);
   }

   private List<BlockPos> getFramePositions() {
      Set<BlockPos> visitedSet = new HashSet<>();
      List<BlockPos> framePositions = new ArrayList<>();
      List<BlockPos> openSet = new ArrayList<>();
      List<BlockPos> nextOpenSet = new ArrayList<>();
      openSet.add(this.getBlockPos());
      Direction[] order = Direction.values();
      List<Direction> orderAsList = Arrays.asList(order);
      int maxIterationCount = this.frameBox.getBlocksOnEdgeCount();
      int iterationCount = 0;

      do {
         for (BlockPos p : openSet) {
            Collections.shuffle(orderAsList);

            for (Direction face : order) {
               BlockPos next = p.relative(face);
               if (this.frameBox.isOnEdge(next) && visitedSet.add(next)) {
                  nextOpenSet.add(next);
                  framePositions.add(next);
               }
            }
         }

         openSet.clear();
         List<BlockPos> t = openSet;
         openSet = nextOpenSet;
         nextOpenSet = t;
         Collections.shuffle(openSet);
         if (openSet.size() > 24) {
            String msg = "OpenSet got too big!";
            msg = msg + "\n  Position = " + this.worldPosition;
            msg = msg + "\n  Frame Box = " + this.frameBox;
            msg = msg + "\n  Iteration Count = " + iterationCount;
            msg = msg + "\n  OpenSet = " + openSet.stream().map(Object::toString).collect(Collectors.joining("\n  ", "[", "]"));
            throw new IllegalStateException(msg);
         }

         if (++iterationCount >= maxIterationCount) {
            String msg = "Failed to generate a correct list of frame positions! Was the frame box wrong?";
            msg = msg + "\n  Position = " + this.worldPosition;
            msg = msg + "\n  Frame Box = " + this.frameBox;
            msg = msg + "\n  Iteration Count = " + iterationCount;
            msg = msg + "\n  OpenSet = " + openSet.stream().map(Object::toString).collect(Collectors.joining("\n  ", "[", "]"));
            throw new IllegalStateException(msg);
         }
      } while (!openSet.isEmpty());

      if (framePositions.isEmpty()) {
         String msg = "Failed to generate a correct list of frame positions! Was the frame box wrong?";
         msg = msg + "\n  Position = " + this.worldPosition;
         msg = msg + "\n  Frame Box = " + this.frameBox;
         throw new IllegalStateException(msg);
      } else {
         return framePositions;
      }
   }

   private boolean shouldBeFrame(BlockPos p) {
      return this.frameBox.isOnEdge(p);
   }

   @Override
   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
      if (this.level != null && !this.level.isClientSide()) {
         super.onPlacedBy(placer, stack);
         Direction facing = (Direction)this.level.getBlockState(this.worldPosition).getValue(HorizontalDirectionalBlock.FACING);
         BlockPos areaPos = this.worldPosition.relative(facing.getOpposite());
         BlockEntity tile = this.level.getBlockEntity(areaPos);
         BlockPos min = null;
         BlockPos max = null;
         if (tile instanceof IAreaProvider provider) {
            min = provider.min();
            max = provider.max();
            int dx = max.getX() - min.getX();
            int dz = max.getZ() - min.getZ();
            if (dx >= 3 && dz >= 3) {
               provider.removeFromWorld();
            } else {
               min = null;
               max = null;
            }
         }

         if (min == null || max == null) {
            min = null;
            max = null;
            VolumeSubCache cache = VolumeCache.INSTANCE.getSubCache(this.getLevel());
            UnmodifiableIterator<BlockPos> markerIterator = cache.getAllMarkers().iterator();

            while (markerIterator.hasNext()) {
               BlockPos markerPos = markerIterator.next();
               TileMarkerVolume marker = (TileMarkerVolume)cache.getMarker(markerPos);
               if (marker != null) {
                  VolumeConnection connection = marker.getCurrentConnection();
                  if (connection != null) {
                     Box volBox = connection.getBox();
                     Box box2 = new Box();
                     box2.initialize(volBox);
                     if (box2.isInitialized()
                        && this.worldPosition.getY() == box2.min().getY()
                        && !box2.contains(this.worldPosition)
                        && box2.contains(areaPos)
                        && box2.size().getX() >= 3
                        && box2.size().getZ() >= 3) {
                        box2.expand(1);
                        box2.setMin(box2.min().above());
                        if (box2.isOnEdge(this.worldPosition)) {
                           min = volBox.min();
                           max = volBox.max();
                           marker.removeFromWorld();
                           break;
                        }
                     }
                  }
               }
            }
         }

         if (min == null || max == null) {
            this.miningBox.reset();
            this.frameBox.reset();
            switch (facing.getOpposite()) {
               case DOWN:
               case UP:
               case EAST:
               default:
                  min = this.worldPosition.offset(1, 0, -5);
                  max = this.worldPosition.offset(11, 4, 5);
                  break;
               case WEST:
                  min = this.worldPosition.offset(-11, 0, -5);
                  max = this.worldPosition.offset(-1, 4, 5);
                  break;
               case SOUTH:
                  min = this.worldPosition.offset(-5, 0, 1);
                  max = this.worldPosition.offset(5, 4, 11);
                  break;
               case NORTH:
                  min = this.worldPosition.offset(-5, 0, -11);
                  max = this.worldPosition.offset(5, 4, -1);
            }
         }

         if (max.getY() - min.getY() < BCBuildersConfig.quarryFrameMinHeight.get()) {
            max = new BlockPos(max.getX(), min.getY() + BCBuildersConfig.quarryFrameMinHeight.get(), max.getZ());
         }

         if (this.level.isOutsideBuildHeight(max)) {
            int dist = max.getY() - min.getY();
            min = min.below(dist);
            max = max.below(dist);
         }

         this.frameBox.reset();
         this.frameBox.setMin(min);
         this.frameBox.setMax(max);
         this.miningBox.reset();
         int minY = this.computeMiningMinY();
         this.miningBox.setMin(new BlockPos(min.getX() + 1, minY, min.getZ() + 1));
         this.miningBox.setMax(new BlockPos(max.getX() - 1, max.getY() - 1, max.getZ() - 1));
         this.deferredUpdatePoses = true;
         this.schedulePipeNeighborNotify();
         this.setChanged();
         if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         }
      }
   }

   private int computeMiningMinY() {
      int minY = this.worldPosition.getY() - BCCoreConfig.miningMaxDepth.get();
      if (this.level != null && this.level.isOutsideBuildHeight(minY)) {
         minY = this.level.getMinY();
      }

      return minY;
   }

   private boolean canMine(BlockPos blockPos) {
      if (this.level.getBlockState(blockPos).getDestroySpeed(this.level, blockPos) < 0.0F) {
         return false;
      }

      Fluid fluid = BlockUtil.getFluidWithFlowing(this.level, blockPos);
      return fluid != null ? false : !(this.level instanceof ServerLevel serverLevel && !BlockUtil.canMachineBreak(serverLevel, blockPos, this.getOwner()));
   }

   private boolean canMoveThrough(BlockPos blockPos) {
      if (this.level.getBlockState(blockPos).isAir()) {
         return true;
      }

      Fluid fluid = BlockUtil.getFluidWithFlowing(this.level, blockPos);
      return fluid != null;
   }

   private boolean canMoveDownTo(BlockPos blockPos) {
      for (int y = this.miningBox.max().getY(); y > blockPos.getY(); y--) {
         if (!this.canMoveThrough(VecUtil.replaceValue(blockPos, Axis.Y, y))) {
            return false;
         }
      }

      return true;
   }

   private boolean canIgnoreInFrameBox(BlockPos blockPos) {
      return !this.level.getBlockState(blockPos).isAir() && BlockUtil.getFluidWithFlowing(this.level, blockPos) == null;
   }

   private void check(BlockPos blockPos) {
      this.frameBreakBlockPoses.remove(blockPos);
      this.framePlaceFramePoses.remove(blockPos);
      if (this.shouldBeFrame(blockPos)) {
         if (!this.level.getBlockState(blockPos).is(BCBuildersBlocks.FRAME)) {
            if (this.canIgnoreInFrameBox(blockPos)) {
               if (this.canMine(blockPos)) {
                  this.frameBreakBlockPoses.add(blockPos);
               }
            } else {
               this.framePlaceFramePoses.add(blockPos);
            }
         }
      } else if (this.canIgnoreInFrameBox(blockPos) && this.canMine(blockPos)) {
         this.frameBreakBlockPoses.add(blockPos);
      }

      if (!this.firstChecked) {
         this.firstCheckedPoses.add(blockPos);
         if (this.firstCheckedPoses.size() >= this.frameBoxPosesCount) {
            this.firstChecked = true;
         }
      }
   }

   public void onLoad() {
      if (this.level != null && !this.level.isClientSide()) {
         this.deferredUpdatePoses = true;
         this.schedulePipeNeighborNotify();
      }
   }

   public void setRemoved() {
      super.setRemoved();
      BCBuildersEventDist.INSTANCE.invalidateQuarry(this);
      if (this.level != null && !this.level.isClientSide()) {
         ChunkLoaderManager.releaseChunksFor(this);
         this.discardRigs();
      }
   }

   public void clearRemoved() {
      super.clearRemoved();
      BCBuildersEventDist.INSTANCE.validateQuarry(this);
      if (this.level != null && !this.level.isClientSide()) {
         this.schedulePipeNeighborNotify();
      }
   }

   @Override
   public IChunkLoadingTile.@Nullable LoadType getLoadType() {
      return IChunkLoadingTile.LoadType.HARD;
   }

   @Nullable
   @Override
   public Set<ChunkPos> getChunksToLoad() {
      if (!this.miningBox.isInitialized()) {
         return null;
      }

      Set<ChunkPos> chunkPoses = new HashSet<>();
      ChunkPos minChunkPos = PositionUtil.chunkContaining(this.frameBox.min());
      ChunkPos maxChunkPos = PositionUtil.chunkContaining(this.frameBox.max());
      int minX = PositionUtil.chunkX(minChunkPos);
      int maxX = PositionUtil.chunkX(maxChunkPos);
      int minZ = PositionUtil.chunkZ(minChunkPos);
      int maxZ = PositionUtil.chunkZ(maxChunkPos);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            chunkPoses.add(new ChunkPos(x, z));
         }
      }

      return chunkPoses;
   }

   @Override
   public Component getAdvDebugMessage() {
      return Component.translatable("chat.debugger.quarry");
   }

   private void updatePoses(boolean preserveFirstChecked) {
      boolean wasFirstChecked = preserveFirstChecked && this.firstChecked;
      this.framePoses.clear();
      this.frameBreakBlockPoses.clear();
      this.framePlaceFramePoses.clear();
      if (!wasFirstChecked) {
         this.frameBoxPosesCount = 0;
         this.toCheck.clear();
         this.firstCheckedPoses.clear();
         if (!preserveFirstChecked) {
            this.firstChecked = false;
         }
      }

      BlockState state = this.level.getBlockState(this.worldPosition);
      if (state.is(BCBuildersBlocks.QUARRY) && this.frameBox.isInitialized()) {
         if (wasFirstChecked) {
            this.framePoses.addAll(this.getFramePositions());

            for (BlockPos edgePos : this.framePoses) {
               this.check(edgePos);
            }

            if (this.toCheck.isEmpty()) {
               List<BlockPos> blocksInArea = this.frameBox.getBlocksInArea();
               blocksInArea.sort(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(this.worldPosition::distSqr)));
               this.toCheck.addAll(blocksInArea);
            }

            this.deferredChunkLoad = true;
         } else {
            List<BlockPos> blocksInArea = this.frameBox.getBlocksInArea();
            blocksInArea.sort(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(this.worldPosition::distSqr)));
            this.frameBoxPosesCount = blocksInArea.size();
            this.toCheck.addAll(blocksInArea);
            this.framePoses.addAll(this.getFramePositions());
            ChunkLoaderManager.loadChunksForTile(this);
         }
      }

      if (preserveFirstChecked) {
         this.firstChecked = wasFirstChecked;
      }
   }

   private void advancePastNonWorkableBlocks() {
      if (this.boxIterator == null) {
         return;
      }

      while (this.boxIterator.hasNext()) {
         BlockPos current = this.boxIterator.getCurrent();
         if (!this.canMoveThrough(current) && this.canMine(current) && this.canMoveDownTo(current)) {
            break;
         }

         this.boxIterator.advance();
      }
   }

   private void advanceMiningIteratorPast(BlockPos pos) {
      if (this.boxIterator != null && pos != null && this.boxIterator.hasNext()) {
         BlockPos current = this.boxIterator.getCurrent();
         if (current != null && current.equals(pos)) {
            this.boxIterator.advance();
         }
      }
   }

   public boolean hasPower() {
      return this.battery.getStored() > 0L;
   }

   public boolean isMining() {
      return this.currentTask != null;
   }

   @Override
   public boolean hasWork() {
      if (!this.miningBox.isInitialized() || !this.firstChecked || !this.hasPower()) {
         return false;
      }

      return this.isMining()
         || this.boxIterator != null && this.boxIterator.hasNext()
         || !this.framePlaceFramePoses.isEmpty()
         || !this.frameBreakBlockPoses.isEmpty();
   }

   public void tick() {
      if (this.level != null) {
         if (this.drillPos == null) {
            this.collisionBoxes = ImmutableList.of();
            this.collisionDrillPos = null;
         }

         if (this.level.isClientSide()) {
            this.prevClientDrillPos = this.clientDrillPos;
            this.clientDrillPos = this.drillPos;
            if (this.currentTask != null) {
               this.currentTask.clientTick();
            }
         } else {
            if (this.deferredUpdatePoses) {
               this.deferredUpdatePoses = false;
               this.updatePoses(true);
            }

            if (this.deferredChunkLoad) {
               this.deferredChunkLoad = false;
               ChunkLoaderManager.loadChunksForTile(this);
            }

            this.flushPipeNeighborNotify();
            this.battery.tick(this.level, this.worldPosition);

            if (this.frameBox.isInitialized() && this.miningBox.isInitialized()) {
               int desiredMinY = this.computeMiningMinY();
               if (this.miningBox.min().getY() != desiredMinY) {
                  BlockPos oldMin = this.miningBox.min();
                  this.miningBox.setMin(new BlockPos(oldMin.getX(), desiredMinY, oldMin.getZ()));
                  this.boxIterator = null;
               }

               if (!this.toCheck.isEmpty() || !this.frameBreakBlockPoses.isEmpty() || !this.framePlaceFramePoses.isEmpty()) {
                  QuarryFrameChecker.runChecks(
                     this.firstChecked, this.toCheck, this.frameBreakBlockPoses, this.framePlaceFramePoses, this::check, this.firstChecked ? 10 : 500
                  );
               }

               if (this.firstChecked) {
                  long stored = this.battery.getStored();
                  boolean atFullSpeedThisTick = stored > this.battery.getCapacity() / 2L;
                  long max;
                  if (stored <= 0L) {
                     max = 0L;
                  } else if (atFullSpeedThisTick) {
                     max = MAX_POWER_PER_TICK;
                  } else {
                     if (stored > Long.MAX_VALUE / MAX_POWER_PER_TICK) {
                        max = BigInteger.valueOf(stored)
                           .multiply(BigInteger.valueOf(MAX_POWER_PER_TICK))
                           .divide(BigInteger.valueOf(this.battery.getCapacity() / 2L))
                           .longValue();
                     } else {
                        max = MAX_POWER_PER_TICK * stored / (this.battery.getCapacity() / 2L);
                     }

                     max = MathUtil.clamp(max, 0L, MAX_POWER_PER_TICK);
                  }

                  this.debugPowerRate = max;
                  this.blockPercentSoFar = 0.0;
                  this.moveDistanceSoFar = 0.0;
                  int maxTasks = max <= 0L ? 0 : Math.max(1, (int)(max * BCBuildersConfig.quarryMaxTasksPerTick.get() / MAX_POWER_PER_TICK));
                  boolean sendUpdate = false;

                  for (int i = 0; i < maxTasks; i++) {
                     if (this.currentTask != null) {
                        long needed = this.currentTask.getRequiredPowerThisTick();
                        int mult = BCBuildersConfig.quarryTaskPowerDivisor.get();
                        long added;
                        if (mult > 0) {
                           long scaledNeeded = needed * (mult + i) / mult;
                           long leftover = needed * (mult + i) % mult;
                           long power = this.battery.extractPower(0L, Math.min(max, scaledNeeded));
                           max -= power;
                           added = power * mult / (mult + i);
                           if (leftover > 0L && power > 0L) {
                              added++;
                           }
                        } else {
                           added = this.battery.extractPower(0L, Math.min(max, needed));
                           max -= added;
                        }

                        if (!this.currentTask.addPower(added)) {
                           sendUpdate = true;
                           break;
                        }

                        this.currentTask = null;
                     }

                     if (!this.frameBreakBlockPoses.isEmpty()) {
                        BlockPos blockPos = this.frameBreakBlockPoses.iterator().next();
                        if (this.canMine(blockPos)) {
                           this.drillPos = null;
                           this.currentTask = new TileQuarry.TaskBreakBlock(blockPos);
                           sendUpdate = true;
                        } else {
                           this.frameBreakBlockPoses.remove(blockPos);
                        }

                        this.check(blockPos);
                     } else {
                        if (!this.framePlaceFramePoses.isEmpty()) {
                           boolean queuedFrameTask = false;
                           for (BlockPos blockPos : this.framePoses) {
                              if (this.framePlaceFramePoses.contains(blockPos)) {
                                 this.check(blockPos);
                                 if (this.framePlaceFramePoses.contains(blockPos)) {
                                    this.drillPos = null;
                                    this.currentTask = new TileQuarry.TaskAddFrame(blockPos);
                                    sendUpdate = true;
                                    queuedFrameTask = true;
                                    break;
                                 }
                              }
                           }

                           if (queuedFrameTask) {
                              continue;
                           }
                        }

                        if (this.boxIterator == null || this.drillPos == null) {
                           this.boxIterator = this.createBoxIterator();
                           this.advancePastNonWorkableBlocks();
                           this.drillPos = Vec3.atLowerCornerOf(this.miningBox.closestInsideTo(this.worldPosition));
                        }

                        if (this.boxIterator != null && this.boxIterator.hasNext()) {
                           this.advancePastNonWorkableBlocks();

                           if (this.boxIterator.hasNext()) {
                              boolean found = false;
                              Vec3 targetVec = Vec3.atLowerCornerOf(this.boxIterator.getCurrent());
                              if (this.drillPos.distanceToSqr(targetVec) >= 1.0) {
                                 this.currentTask = new TileQuarry.TaskMoveDrill(this.drillPos, targetVec);
                                 found = true;
                              } else if (this.canMine(this.boxIterator.getCurrent())) {
                                 this.currentTask = new TileQuarry.TaskBreakBlock(this.boxIterator.getCurrent());
                                 found = true;
                              }

                              if (found) {
                                 sendUpdate = true;
                              }
                           }
                        }
                     }
                  }

                  this.debugPowerRate -= max;
                  if (atFullSpeedThisTick && this.currentTask != null && !(this.currentTask instanceof TileQuarry.TaskAddFrame)) {
                     this.lastFullSpeedTick = this.level.getGameTime();
                  }

                  if (!this.advancementGranted
                     && this.boxIterator != null
                     && !this.boxIterator.hasNext()
                     && this.frameBreakBlockPoses.isEmpty()
                     && this.framePlaceFramePoses.isEmpty()
                     && this.frameBox.isInitialized()) {
                     int sizeX = this.frameBox.max().getX() - this.frameBox.min().getX() + 1;
                     int sizeZ = this.frameBox.max().getZ() - this.frameBox.min().getZ() + 1;
                     if (sizeX >= 64
                        && sizeZ >= 64
                        && this.getOwner() != null
                        && AdvancementUtil.unlockAdvancement(BcAuth.id(this.getOwner()), this.level, DIGGY_DIGGY_HOLE)) {
                        this.advancementGranted = true;
                        this.setChanged();
                     }
                  }

                  if (sendUpdate) {
                     this.setChanged();
                     if (this.level != null && !this.level.isClientSide()) {
                        MessageUtil.sendUpdateToTrackingPlayers(this);
                     }
                  }

                  this.updateRigs();
               }
            }
         }
      }
   }

   private void updateRigs() {
      if (this.level != null && !this.level.isClientSide()) {
         if (this.drillPos != null && this.frameBox.isInitialized()) {
            List<AABB> boxes = this.getCollisionBoxes();
            if (boxes.size() != 3) {
               this.discardRigs();
            } else {
               boolean isDrillMoving = this.currentTask instanceof TileQuarry.TaskMoveDrill;

               // The drill sits on one block for many ticks while breaking it; getCollisionBoxes() returns the
               // same cached list by reference until the drill actually moves. When neither the geometry nor the
               // phasing changed, every rig already holds the right box/position/phasing, so skip the rebuild —
               // otherwise each idle tick churns the segments ArrayList + splitBoxAtSections AABBs and re-runs
               // Entity.setPos -> makeBoundingBox on every rig, per quarry. (HARD chunk-loading keeps the rig
               // entities alive, so nothing needs re-validating while idle.)
               if (boxes == this.rigsBuiltFromBoxes && isDrillMoving == this.rigsBuiltPhasing) {
                  return;
               }

               // Cut each beam/drill box at section boundaries so every piece is found by the player's
               // section-local collision query (see the rigs field). A box inside one section stays one entity.
               // Index >= drillSegmentStart are the vertical-drill pieces, which phase (no collision) while the
               // drill is travelling.
               List<AABB> segments = new ArrayList<>();
               splitBoxAtSections(boxes.get(0), segments);
               splitBoxAtSections(boxes.get(1), segments);
               int drillSegmentStart = segments.size();
               splitBoxAtSections(boxes.get(2), segments);

               while (this.rigs.size() > segments.size()) {
                  EntityQuarryRig extra = this.rigs.remove(this.rigs.size() - 1);
                  if (extra != null && !extra.isRemoved()) {
                     extra.discard();
                  }
               }

               for (int i = 0; i < segments.size(); i++) {
                  EntityQuarryRig rig = i < this.rigs.size() ? this.rigs.get(i) : null;
                  if (rig == null || rig.isRemoved()) {
                     rig = new EntityQuarryRig(BCBuildersEntities.QUARRY_RIG, this.level);
                     this.level.addFreshEntity(rig);
                     if (i < this.rigs.size()) {
                        this.rigs.set(i, rig);
                     } else {
                        this.rigs.add(rig);
                     }
                  }

                  rig.setRiggingBox(segments.get(i));
                  rig.setPhasing(i >= drillSegmentStart && isDrillMoving);
               }

               this.rigsBuiltFromBoxes = boxes;
               this.rigsBuiltPhasing = isDrillMoving;
            }
         } else {
            this.discardRigs();
         }
      }
   }

   /**
    * Cuts {@code box} along its longest axis at 16-block section boundaries so every piece lies within a single
    * entity section, appending the pieces to {@code out}. A box that already fits one section is added unchanged.
    */
   private static void splitBoxAtSections(AABB box, List<AABB> out) {
      double dx = box.maxX - box.minX;
      double dy = box.maxY - box.minY;
      double dz = box.maxZ - box.minZ;
      double longest = Math.max(dx, Math.max(dy, dz));

      double min;
      double max;
      Axis axis;
      if (longest == dx) {
         axis = Axis.X;
         min = box.minX;
         max = box.maxX;
      } else if (longest == dy) {
         axis = Axis.Y;
         min = box.minY;
         max = box.maxY;
      } else {
         axis = Axis.Z;
         min = box.minZ;
         max = box.maxZ;
      }

      for (double from = min; from < max; ) {
         double to = Math.min(Math.floor(from / 16.0) * 16.0 + 16.0, max);
         if (axis == Axis.X) {
            out.add(new AABB(from, box.minY, box.minZ, to, box.maxY, box.maxZ));
         } else if (axis == Axis.Y) {
            out.add(new AABB(box.minX, from, box.minZ, box.maxX, to, box.maxZ));
         } else {
            out.add(new AABB(box.minX, box.minY, from, box.maxX, box.maxY, to));
         }

         from = to;
      }
   }

   private void discardRigs() {
      for (EntityQuarryRig rig : this.rigs) {
         if (rig != null && !rig.isRemoved()) {
            rig.discard();
         }
      }

      this.rigs.clear();
      // Force the next updateRigs to rebuild rather than short-circuit against a stale reference.
      this.rigsBuiltFromBoxes = null;
   }

   public List<AABB> getCollisionBoxes() {
      if (this.drillPos != null && this.drillPos != this.collisionDrillPos && this.frameBox.isInitialized()) {
         Vec3 fMax = VecUtil.convertCenter(this.frameBox.max());
         Vec3 fMin = VecUtil.replaceValue(VecUtil.convertCenter(this.frameBox.min()), Axis.Y, fMax.y);
         this.collisionBoxes = ImmutableList.of(
            BoundingBoxUtil.makeFrom(VecUtil.replaceValue(fMin, Axis.X, this.drillPos.x + 0.5), VecUtil.replaceValue(fMax, Axis.X, this.drillPos.x + 0.5), 0.25),
            BoundingBoxUtil.makeFrom(VecUtil.replaceValue(fMin, Axis.Z, this.drillPos.z + 0.5), VecUtil.replaceValue(fMax, Axis.Z, this.drillPos.z + 0.5), 0.25),
            BoundingBoxUtil.makeFrom(this.drillPos.add(0.5, 0.0, 0.5), VecUtil.replaceValue(this.drillPos, Axis.Y, fMax.y).add(0.5, 0.0, 0.5), 0.25)
         );
         this.collisionDrillPos = this.drillPos;
      }

      return this.collisionBoxes;
   }

   @Override
   protected void writeData(BcValueOut output) {
      super.writeData(output);
      output.putBoolean("box_init", this.miningBox.isInitialized());
      if (this.miningBox.isInitialized()) {
         output.putInt("box_minX", this.miningBox.min().getX());
         output.putInt("box_minY", this.miningBox.min().getY());
         output.putInt("box_minZ", this.miningBox.min().getZ());
         output.putInt("box_maxX", this.miningBox.max().getX());
         output.putInt("box_maxY", this.miningBox.max().getY());
         output.putInt("box_maxZ", this.miningBox.max().getZ());
      }

      output.putBoolean("frame_init", this.frameBox.isInitialized());
      if (this.frameBox.isInitialized()) {
         output.putInt("frame_minX", this.frameBox.min().getX());
         output.putInt("frame_minY", this.frameBox.min().getY());
         output.putInt("frame_minZ", this.frameBox.min().getZ());
         output.putInt("frame_maxX", this.frameBox.max().getX());
         output.putInt("frame_maxY", this.frameBox.max().getY());
         output.putInt("frame_maxZ", this.frameBox.max().getZ());
      }

      if (this.boxIterator != null) {
         output.putBoolean("hasBoxIterator", true);
         output.store("boxIterator", CompoundTag.CODEC, this.boxIterator.writeToNbt());
      } else {
         output.putBoolean("hasBoxIterator", false);
      }

      output.putLong("battery_mj", this.battery.getStored());
      if (this.currentTask != null) {
         int taskId = -1;

         for (TileQuarry.EnumTaskType type : TileQuarry.EnumTaskType.values()) {
            if (type.clazz == this.currentTask.getClass()) {
               taskId = type.ordinal();
               break;
            }
         }

         output.putByte("currentTaskId", (byte)taskId);
         CompoundTag taskTag = this.currentTask.serializeNBT();
         output.putLong("task_power", buildcraft.lib.nbt.BcNbt.getLong(taskTag, "power", 0L));
         if (this.currentTask instanceof TileQuarry.TaskBreakBlock tb) {
            output.putInt("task_breakX", tb.breakPos.getX());
            output.putInt("task_breakY", tb.breakPos.getY());
            output.putInt("task_breakZ", tb.breakPos.getZ());
         } else if (this.currentTask instanceof TileQuarry.TaskAddFrame tf) {
            output.putInt("task_frameX", tf.framePos.getX());
            output.putInt("task_frameY", tf.framePos.getY());
            output.putInt("task_frameZ", tf.framePos.getZ());
         } else if (this.currentTask instanceof TileQuarry.TaskMoveDrill tm) {
            output.putDouble("task_fromX", tm.from.x);
            output.putDouble("task_fromY", tm.from.y);
            output.putDouble("task_fromZ", tm.from.z);
            output.putDouble("task_toX", tm.to.x);
            output.putDouble("task_toY", tm.to.y);
            output.putDouble("task_toZ", tm.to.z);
         }
      } else {
         output.putByte("currentTaskId", (byte)-1);
      }

      if (this.drillPos != null) {
         output.putDouble("drillX", this.drillPos.x);
         output.putDouble("drillY", this.drillPos.y);
         output.putDouble("drillZ", this.drillPos.z);
         output.putBoolean("hasDrill", true);
      } else {
         output.putBoolean("hasDrill", false);
      }

      output.putBoolean("firstChecked", this.firstChecked);
      output.putBoolean("advancementGranted", this.advancementGranted);
   }

   @Override
   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public void readData(BcValueIn input) {
      super.readData(input);
      if (input.getBooleanOr("box_init", false)) {
         this.miningBox.reset();
         this.miningBox.setMin(new BlockPos(input.getIntOr("box_minX", 0), input.getIntOr("box_minY", 0), input.getIntOr("box_minZ", 0)));
         this.miningBox.setMax(new BlockPos(input.getIntOr("box_maxX", 0), input.getIntOr("box_maxY", 0), input.getIntOr("box_maxZ", 0)));
      }

      if (input.getBooleanOr("frame_init", false)) {
         this.frameBox.reset();
         this.frameBox.setMin(new BlockPos(input.getIntOr("frame_minX", 0), input.getIntOr("frame_minY", 0), input.getIntOr("frame_minZ", 0)));
         this.frameBox.setMax(new BlockPos(input.getIntOr("frame_maxX", 0), input.getIntOr("frame_maxY", 0), input.getIntOr("frame_maxZ", 0)));
      }

      this.boxIterator = null;
      if (input.getBooleanOr("hasBoxIterator", false)) {
         this.boxIterator = input.read("boxIterator", CompoundTag.CODEC).map(BoxIterator::readFromNbt).orElse(null);
      }

      long stored = input.getLongOr("battery_mj", 0L);
      CompoundTag mjTag = new CompoundTag();
      mjTag.putLong("stored", stored);
      this.battery.deserializeNBT(mjTag);
      int taskId = input.getByteOr("currentTaskId", (byte)-1);
      if (taskId >= 0 && taskId < TileQuarry.EnumTaskType.values().length) {
         this.currentTask = TileQuarry.EnumTaskType.values()[taskId].supplier.apply(this);
         this.currentTask.power = input.getLongOr("task_power", 0L);
         this.currentTask.clientPower = this.currentTask.power;
         this.currentTask.prevClientPower = this.currentTask.power;
         if (this.currentTask instanceof TileQuarry.TaskBreakBlock tb) {
            tb.breakPos = new BlockPos(input.getIntOr("task_breakX", 0), input.getIntOr("task_breakY", 0), input.getIntOr("task_breakZ", 0));
         } else if (this.currentTask instanceof TileQuarry.TaskAddFrame tf) {
            tf.framePos = new BlockPos(input.getIntOr("task_frameX", 0), input.getIntOr("task_frameY", 0), input.getIntOr("task_frameZ", 0));
         } else if (this.currentTask instanceof TileQuarry.TaskMoveDrill tm) {
            tm.from = new Vec3(input.getDoubleOr("task_fromX", 0.0), input.getDoubleOr("task_fromY", 0.0), input.getDoubleOr("task_fromZ", 0.0));
            tm.to = new Vec3(input.getDoubleOr("task_toX", 0.0), input.getDoubleOr("task_toY", 0.0), input.getDoubleOr("task_toZ", 0.0));
         }
      } else {
         this.currentTask = null;
      }

      if (input.getBooleanOr("hasDrill", false)) {
         this.drillPos = new Vec3(input.getDoubleOr("drillX", 0.0), input.getDoubleOr("drillY", 0.0), input.getDoubleOr("drillZ", 0.0));
      } else {
         this.drillPos = null;
      }

      this.firstChecked = input.getBooleanOr("firstChecked", false);
      this.advancementGranted = input.getBooleanOr("advancementGranted", false);
      if (this.drillPos != null && this.drillPos.distanceToSqr(Vec3.atLowerCornerOf(this.getBlockPos())) > 1048576.0) {
         this.drillPos = null;
      }

      boolean isValid = false;
      if (this.frameBox.isInitialized() && this.miningBox.isInitialized()) {
         isValid = true;
         Direction validFace = null;

         for (Direction face : Direction.values()) {
            if (face.getAxis() != Axis.Y && this.frameBox.isOnEdge(this.getBlockPos().relative(face))) {
               validFace = face;
               break;
            }
         }

         if (validFace == null) {
            isValid = false;
         } else {
            int fx0 = this.frameBox.min().getX();
            int fz0 = this.frameBox.min().getZ();
            int fx1 = this.frameBox.max().getX();
            int fy1 = this.frameBox.max().getY();
            int fz1 = this.frameBox.max().getZ();
            int mx0 = this.miningBox.min().getX();
            int mz0 = this.miningBox.min().getZ();
            int mx1 = this.miningBox.max().getX();
            int my1 = this.miningBox.max().getY();
            int mz1 = this.miningBox.max().getZ();
            isValid = fx0 + 1 == mx0 && fx1 - 1 == mx1 && fz0 + 1 == mz0 && fz1 - 1 == mz1 && fy1 - 1 == my1;
         }
      }

      if (!isValid) {
         this.frameBox.reset();
         this.miningBox.reset();
         this.drillPos = null;
      } else {
         this.deferredUpdatePoses = true;
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("battery = " + this.battery.getDebugString());
      left.add("rate = " + LocaleUtil.localizeMjFlow(this.debugPowerRate));
      left.add("frameBox");
      left.add(" - min = " + this.frameBox.min());
      left.add(" - max = " + this.frameBox.max());
      left.add("miningBox:");
      left.add(" - min = " + this.miningBox.min());
      left.add(" - max = " + this.miningBox.max());
      left.add("firstCheckedPoses = " + this.firstCheckedPoses.size());
      left.add("frameBoxPosesCount = " + this.frameBoxPosesCount);
      left.add("firstChecked = " + this.firstChecked);
      BoxIterator iter = this.boxIterator;
      left.add("current = " + (iter == null ? "null" : iter.getCurrent()));
      TileQuarry.Task task = this.currentTask;
      if (task != null) {
         left.add("task:");
         left.add(" - class = " + task.getClass().getName());
         left.add(" - power = " + LocaleUtil.localizeMj(task.power));
         left.add(" - target = " + LocaleUtil.localizeMj(task.getTarget()));
      } else {
         left.add("task = null");
      }

      left.add("drill = " + this.drillPos);
   }

   private enum EnumTaskType {
      BREAK_BLOCK(TileQuarry.TaskBreakBlock.class, quarry -> quarry.new TaskBreakBlock()),
      ADD_FRAME(TileQuarry.TaskAddFrame.class, quarry -> quarry.new TaskAddFrame()),
      MOVE_DRILL(TileQuarry.TaskMoveDrill.class, quarry -> quarry.new TaskMoveDrill());

      public final Class<? extends TileQuarry.Task> clazz;
      public final Function<TileQuarry, TileQuarry.Task> supplier;

      EnumTaskType(Class<? extends TileQuarry.Task> clazz, Function<TileQuarry, TileQuarry.Task> supplier) {
         this.clazz = clazz;
         this.supplier = supplier;
      }
   }

   public abstract class Task {
      public long power;
      public long clientPower;
      public long prevClientPower;

      CompoundTag serializeNBT() {
         CompoundTag nbt = new CompoundTag();
         nbt.putLong("power", this.power);
         return nbt;
      }

      void clientTick() {
         this.prevClientPower = this.clientPower;
         this.clientPower = this.power;
      }

      public abstract long getTarget();

      public long getRequiredPowerThisTick() {
         return Math.max(0L, this.getTarget() - this.power);
      }

      protected abstract boolean onReceivePower(long microJoules, long target);

      protected abstract boolean finish(long microJoules, long target);

      final boolean addPower(long microJoules) {
         this.power += microJoules;
         long target = this.getTarget();
         if (this.power >= target) {
            if (!this.finish(microJoules, target)) {
               TileQuarry.this.battery.addPower(Math.min(this.power, TileQuarry.this.battery.getCapacity() - TileQuarry.this.battery.getStored()), false);
            }

            return true;
         } else {
            return this.onReceivePower(microJoules, target);
         }
      }
   }

   public class TaskAddFrame extends TileQuarry.Task {
      public BlockPos framePos = BlockPos.ZERO;

      TaskAddFrame() {
      }

      TaskAddFrame(BlockPos framePos) {
         this.framePos = framePos;
      }

      @Override
      public long getTarget() {
         return 24L * MjAPI.MJ;
      }

      @Override
      protected boolean onReceivePower(long added, long target) {
         return TileQuarry.this.canIgnoreInFrameBox(this.framePos);
      }

      @Override
      protected boolean finish(long added, long target) {
         if (TileQuarry.this.canIgnoreInFrameBox(this.framePos)) {
            return false;
         } else if (TileQuarry.this.level instanceof ServerLevel serverLevel
            && !BlockUtil.canMachinePlace(serverLevel, this.framePos, TileQuarry.this.getOwner(), TileQuarry.this.worldPosition)) {
            return false;
         } else {
            TileQuarry.this.level.setBlockAndUpdate(this.framePos, BCBuildersBlocks.FRAME.defaultBlockState());
            return true;
         }
      }
   }

   public class TaskBreakBlock extends TileQuarry.Task {
      public BlockPos breakPos = BlockPos.ZERO;

      TaskBreakBlock() {
      }

      TaskBreakBlock(BlockPos pos) {
         this.breakPos = pos;
      }

      @Override
      public long getTarget() {
         return BlockUtil.computeBlockBreakPower(TileQuarry.this.level, this.breakPos);
      }

      @Override
      public long getRequiredPowerThisTick() {
         long target = this.getTarget();
         long req = Math.max(0L, target - this.power);
         double rate = BCBuildersConfig.quarryMaxBlockMineRate.get();
         if (rate < 0.1) {
            return req;
         }

         rate /= 20.0;
         rate -= TileQuarry.this.blockPercentSoFar;
         return rate <= 0.0 ? 0L : Math.min(req, (long)(target * rate));
      }

      @Override
      protected boolean onReceivePower(long added, long target) {
         TileQuarry.this.blockPercentSoFar += (double)added / target;
         if (!TileQuarry.this.level.getBlockState(this.breakPos).isAir()) {
            TileQuarry.this.level.destroyBlockProgress(this.breakPos.hashCode(), this.breakPos, (int)(this.power * 9L / this.getTarget()));
            return false;
         }

         TileQuarry.this.level.destroyBlockProgress(this.breakPos.hashCode(), this.breakPos, -1);
         TileQuarry.this.check(this.breakPos);
         TileQuarry.this.advanceMiningIteratorPast(this.breakPos);
         return true;
      }

      @Override
      protected boolean finish(long added, long target) {
         TileQuarry.this.blockPercentSoFar += (double)added / target;
         if (!TileQuarry.this.canMine(this.breakPos)) {
            TileQuarry.this.level.destroyBlockProgress(this.breakPos.hashCode(), this.breakPos, -1);
            TileQuarry.this.check(this.breakPos);
            TileQuarry.this.advanceMiningIteratorPast(this.breakPos);
            return true;
         }

         TileQuarry.this.level.destroyBlockProgress(this.breakPos.hashCode(), this.breakPos, -1);
         if (TileQuarry.this.level instanceof ServerLevel serverLevel) {
            Optional<BlockUtil.BreakResult> result = BlockUtil.breakBlockAndGetDropsWithXp(
               serverLevel, this.breakPos, new ItemStack(Items.DIAMOND_PICKAXE), TileQuarry.this.getOwner()
            );
            if (result.isPresent()) {
               result.get().drops().forEach(stack -> InventoryUtil.addToBestAcceptor(TileQuarry.this.level, TileQuarry.this.worldPosition, null, stack));
               int xp = result.get().xp();
               if (xp > 0) {
                  ExperienceOrb.award(serverLevel, Vec3.atCenterOf(TileQuarry.this.worldPosition), xp);
               }
            }

            TileQuarry.this.check(this.breakPos);
            TileQuarry.this.advanceMiningIteratorPast(this.breakPos);
            return result.isPresent();
         } else {
            TileQuarry.this.advanceMiningIteratorPast(this.breakPos);
            return false;
         }
      }
   }

   public class TaskMoveDrill extends TileQuarry.Task {
      public Vec3 from = Vec3.ZERO;
      public Vec3 to = Vec3.ZERO;

      TaskMoveDrill() {
      }

      TaskMoveDrill(Vec3 from, Vec3 to) {
         this.from = from;
         this.to = to;
      }

      @Override
      public long getTarget() {
         return (long)(this.from.distanceTo(this.to) * 20.0 * MjAPI.MJ);
      }

      @Override
      public long getRequiredPowerThisTick() {
         long req = Math.max(0L, this.getTarget() - this.power);
         double max = BCBuildersConfig.quarryMaxFrameMoveSpeed.get();
         if (max < 0.1) {
            return req;
         }

         max /= 20.0;
         max -= TileQuarry.this.moveDistanceSoFar;
         return max <= 0.0 ? 0L : Math.min(req, (long)(max * 20.0 * MjAPI.MJ));
      }

      @Override
      protected boolean onReceivePower(long added, long target) {
         TileQuarry.this.moveDistanceSoFar = TileQuarry.this.moveDistanceSoFar + (double)added / MjAPI.MJ;
         TileQuarry.this.drillPos = this.from.scale(1.0 - (double)this.power / target).add(this.to.scale((double)this.power / target));
         return false;
      }

      @Override
      protected boolean finish(long added, long target) {
         TileQuarry.this.moveDistanceSoFar = TileQuarry.this.moveDistanceSoFar + (double)added / MjAPI.MJ;
         TileQuarry.this.drillPos = this.to;
         return true;
      }
   }
}
