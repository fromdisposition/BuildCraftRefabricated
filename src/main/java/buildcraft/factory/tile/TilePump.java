/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.tile.ITileOilSpring;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.factory.BCFactoryAttachments;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.fabric.transfer.SidedFluidStorages;
import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.mj.MjRedstoneBatteryReceiver;
import buildcraft.lib.transfer.fabric.TransferConvert;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TilePump extends TileMiner implements IDebuggable {
   private static final Identifier ADVANCEMENT_DRAIN_ANY = Identifier.parse("buildcraftfactory:draining_the_world");
   private static final Identifier ADVANCEMENT_DRAIN_OIL = Identifier.parse("buildcraftfactory:oil_platform");
   private static final Identifier ADVANCEMENT_REFINE_AND_REDEFINE = Identifier.parse("buildcraftenergy:refine_and_redefine");
   private static final Direction[] SEARCH_NORMAL = new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   private static final Direction[] SEARCH_GASEOUS = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   private static final AtomicLong CONFIG_REVISION = new AtomicLong();
   public final SingleFluidTank fluidTank = new SingleFluidTank(16000);
   private boolean queueBuilt = false;
   private long builtAtRevision = -1L;
   private final Map<BlockPos, TilePump.FluidPath> paths = new HashMap<>();
   private final Deque<BlockPos> queue = new ArrayDeque<>();
   private boolean isInfiniteWaterSource;
   private int rebuildDelay = 0;
   private BlockPos targetPos;
   @Nullable
   private BlockPos oilSpringPos;
   @Nullable
   private IMjReceiver mjReceiver;

   public static void onConfigReloaded() {
      CONFIG_REVISION.incrementAndGet();
   }

   public TilePump(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.PUMP, pos, state);
   }

   @Override
   protected IMjReceiver createMjReceiver() {
      if (this.mjReceiver == null) {
         this.mjReceiver = new MjRedstoneBatteryReceiver(this.battery);
      }

      return this.mjReceiver;
   }

   public Storage<FluidVariant> getExtractFluidStorage() {
      return SidedFluidStorages.extractOnly(this.fluidTank);
   }

   private void buildQueue() {
      this.builtAtRevision = CONFIG_REVISION.get();
      this.queue.clear();
      this.paths.clear();
      this.isInfiniteWaterSource = false;
      this.oilSpringPos = null;
      this.targetPos = null;
      int maxDepth = BCCoreConfig.miningMaxDepth.get();
      TilePump.ColumnProbe probeDown = probeDown(this.level, this.worldPosition, maxDepth);
      TilePump.ColumnProbe probeUp = probeUp(this.level, this.worldPosition, maxDepth);
      BlockPos oilPos = probeDown.firstOil();
      BlockPos springPos = probeDown.spring();
      BlockPos seed;
      if (oilPos != null) {
         seed = oilPos;
         this.oilSpringPos = springPos;
      } else {
         if (springPos != null) {
            this.oilSpringPos = springPos;
            return;
         }

         BlockPos downFluidPos = probeDown.firstFluid();
         BlockPos upFluidPos = probeUp.firstFluid();
         Fluid downFluid = downFluidPos != null ? BlockUtil.getFluidWithFlowing(this.level, downFluidPos) : null;
         Fluid upFluid = upFluidPos != null ? BlockUtil.getFluidWithFlowing(this.level, upFluidPos) : null;
         if (upFluid != null && FluidUtilBC.isGaseous(upFluid)) {
            seed = upFluidPos;
         } else if (downFluid != null) {
            seed = downFluidPos;
         } else {
            if (upFluid == null) {
               return;
            }

            seed = upFluidPos;
         }
      }

      Fluid queueFluid = BlockUtil.getFluidWithFlowing(this.level, seed);
      if (queueFluid != null) {
         this.targetPos = seed;
         LongSet checked = new LongOpenHashSet();
         List<BlockPos> nextPosesToCheck = new ArrayList<>();
         nextPosesToCheck.add(seed);
         this.paths.put(seed, new TilePump.FluidPath(seed, null));
         checked.add(seed.asLong());
         if (BlockUtil.getFluid(this.level, seed) != null) {
            this.queue.add(seed);
         }

         this.buildQueue0(queueFluid, nextPosesToCheck, checked);
      }
   }

   private void creditRefineAndRedefineFromPumpedOil(FluidStack drain) {
      if (this.getOwner() != null && this.level != null && !this.level.isClientSide()) {
         MinecraftServer server = this.level.getServer();
         if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(this.getOwner().id());
            if (player != null) {
               String baseName = BCEnergyFluidsFabric.getBaseName(drain.getFluid());
               if (baseName != null) {
                  BCFactoryAttachments.OilAndFuelProduction tracker = BCFactoryAttachments.get(player);
                  String justSaturated = tracker.recordProduction(baseName, drain.getAmount());
                  if (justSaturated != null) {
                     AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_REFINE_AND_REDEFINE, justSaturated);
                  }
               }
            }
         }
      }
   }

   private static boolean isOil(Fluid fluid) {
      Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
      return id.getNamespace().equals("buildcraftenergy") && (id.getPath().equals("oil") || id.getPath().startsWith("oil_heat_"));
   }

   public static TilePump.ColumnProbe probeDown(Level level, BlockPos pumpPos, int maxDepth) {
      BlockPos firstFluid = null;
      BlockPos firstOil = null;

      for (BlockPos pos = pumpPos.below(); !level.isOutsideBuildHeight(pos) && pumpPos.getY() - pos.getY() <= maxDepth; pos = pos.below()) {
         Fluid fluid = BlockUtil.getFluidWithFlowing(level, pos);
         if (fluid != null) {
            if (firstFluid == null) {
               firstFluid = pos;
            }

            if (firstOil == null && isOil(fluid)) {
               firstOil = pos;
            }
         } else {
            BlockState state = level.getBlockState(pos);
            if (state.is(BCCoreBlocks.SPRING_OIL)) {
               return new TilePump.ColumnProbe(firstFluid, firstOil, pos);
            }

            if (!level.isEmptyBlock(pos) && !state.is(BCFactoryBlocks.TUBE)) {
               break;
            }
         }
      }

      return new TilePump.ColumnProbe(firstFluid, firstOil, null);
   }

   public static TilePump.ColumnProbe probeUp(Level level, BlockPos pumpPos, int maxDepth) {
      BlockPos firstFluid = null;
      BlockPos firstOil = null;

      for (BlockPos pos = pumpPos.above(); !level.isOutsideBuildHeight(pos) && pos.getY() - pumpPos.getY() <= maxDepth; pos = pos.above()) {
         Fluid fluid = BlockUtil.getFluidWithFlowing(level, pos);
         if (fluid != null) {
            if (firstFluid == null) {
               firstFluid = pos;
            }

            if (firstOil == null && isOil(fluid)) {
               firstOil = pos;
            }
         } else {
            BlockState state = level.getBlockState(pos);
            if (!level.isEmptyBlock(pos) && !state.is(BCFactoryBlocks.TUBE)) {
               break;
            }
         }
      }

      return new TilePump.ColumnProbe(firstFluid, firstOil, null);
   }

   private void buildQueue0(Fluid queueFluid, List<BlockPos> nextPosesToCheck, LongSet checked) {
      Direction[] directions = FluidUtilBC.isGaseous(queueFluid) ? SEARCH_GASEOUS : SEARCH_NORMAL;
      boolean isWater = !BCCoreConfig.pumpsConsumeWater.get() && FluidUtilBC.areFluidsEqual(queueFluid, Fluids.WATER);
      boolean targetPosIsInfinite = isWater && isInfiniteSourceAt(this.level, this.targetPos);
      this.isInfiniteWaterSource = targetPosIsInfinite;
      int maxLengthSquared = BCCoreConfig.pumpMaxDistance.get() * BCCoreConfig.pumpMaxDistance.get();

      boolean stopSearching = false;
      while (!nextPosesToCheck.isEmpty() && !stopSearching) {
         List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
         nextPosesToCheck.clear();

         for (BlockPos posToCheck : nextPosesToCheckCopy) {
            for (Direction side : directions) {
               BlockPos offsetPos = posToCheck.relative(side);
               if (!(offsetPos.distSqr(this.targetPos) > maxLengthSquared)) {
                  boolean isNew = checked.add(offsetPos.asLong());
                  if (isNew) {
                     Fluid fluidAt = BlockUtil.getFluidWithFlowing(this.level, offsetPos);
                     boolean eq = FluidUtilBC.areFluidsEqual(fluidAt, queueFluid);
                     if (eq) {
                        TilePump.FluidPath oldPath = this.paths.get(posToCheck);
                        TilePump.FluidPath path = new TilePump.FluidPath(offsetPos, oldPath);
                        this.paths.put(offsetPos, path);
                        if (BlockUtil.getFluid(this.level, offsetPos) != null) {
                           this.queue.add(offsetPos);
                        }

                        nextPosesToCheck.add(offsetPos);
                     }
                  }
               }
            }

            if (targetPosIsInfinite) {
               stopSearching = true;
               break;
            }
         }
      }

      if (isOil(queueFluid) && this.oilSpringPos == null) {
         List<BlockPos> springPositions = new ArrayList<>();
         BlockPos center = VecUtil.replaceValue(this.worldPosition, Axis.Y, this.level.getMinY());

         for (BlockPos spring : BlockPos.betweenClosed(center.offset(-10, 0, -10), center.offset(10, 0, 10))) {
            if (this.level.getBlockState(spring).is(BCCoreBlocks.SPRING_OIL)) {
               BlockEntity tile = this.level.getBlockEntity(spring);
               if (tile instanceof ITileOilSpring) {
                  springPositions.add(spring.immutable());
               }
            }
         }

         switch (springPositions.size()) {
            case 0:
               break;
            case 1:
               this.oilSpringPos = springPositions.get(0);
               break;
            default:
               springPositions.sort(Comparator.comparingDouble(this.worldPosition::distSqr));
               this.oilSpringPos = springPositions.get(0);
         }
      }
   }

   private boolean canDrain(BlockPos blockPos) {
      Fluid fluid = BlockUtil.getFluid(this.level, blockPos);
      return this.fluidTank.isEmpty() ? fluid != null : FluidUtilBC.areFluidsEqual(fluid, this.fluidTank.getFluidStack().getFluid());
   }

   public static boolean isInfiniteSourceAt(@Nullable Level level, @Nullable BlockPos pos) {
      if (level != null && pos != null) {
         BlockState below = level.getBlockState(pos.below());
         Fluid fluidBelow = BlockUtil.getFluidWithFlowing(level, pos.below());
         if (!FluidUtilBC.areFluidsEqual(fluidBelow, Fluids.WATER) && !BlockUtil.isSolid(level, pos.below(), below)) {
            return false;
         }

         int sources = 0;

         for (Direction dir : Plane.HORIZONTAL) {
            Fluid neighbour = BlockUtil.getFluid(level, pos.relative(dir));
            if (FluidUtilBC.areFluidsEqual(neighbour, Fluids.WATER)) {
               if (++sources >= 2) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void nextPos() {
      while (!this.queue.isEmpty()) {
         this.currentPos = this.queue.removeLast();
         if (this.canDrain(this.currentPos)) {
            this.updateLength();
            return;
         }
      }

      this.currentPos = null;
      this.updateLength();
   }

   @Nullable
   @Override
   protected BlockPos getTargetPos() {
      return this.targetPos;
   }

   @Override
   public void serverTick() {
      if (!this.queueBuilt || this.builtAtRevision != CONFIG_REVISION.get()) {
         this.buildQueue();
         this.queueBuilt = true;
         this.updateLength();
      }

      super.serverTick();
      FluidUtilBC.pushFluidToNeighbors(this.level, this.worldPosition, this.fluidTank);
   }

   @Override
   protected void mine() {
      if (this.fluidTank.getAmountMb() <= this.fluidTank.getCapacityMb() / 2) {
         if (this.rebuildDelay > 0) {
            this.rebuildDelay--;
         }

         long target = 10L * MjAPI.MJ;
         if (this.currentPos != null && this.paths.containsKey(this.currentPos)) {
            this.progress = this.progress + (int)this.battery.extractPower(0L, target - this.progress);
            if (this.progress < target) {
               return;
            }

            FluidStack drain = BlockUtil.drainBlock(this.level, this.currentPos, false, this.getOwner());
            if (drain != null) {
               BlockPos invalid = this.getFirstInvalidPointOnPath(this.currentPos);
               if (invalid == null && this.canDrain(this.currentPos)) {
                  FluidStack drainedResource = FluidUtilBC.canonicalFluidStack(drain.copyWithAmount(1));
                  FluidVariant variant = TransferConvert.toVariant(drainedResource);

                  int inserted;
                  try (Transaction drainTransaction = Transaction.openOuter()) {
                     long insertedDroplets = this.fluidTank.insert(variant, TransferConvert.mbToDroplets(drain.getAmount()), drainTransaction);
                     inserted = (int)TransferConvert.dropletsToMb(insertedDroplets);
                     if (inserted > 0) {
                        drainTransaction.commit();
                     }
                  }

                  if (inserted > 0) {
                     this.progress = 0;
                     if (this.getOwner() != null) {
                        AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.level, ADVANCEMENT_DRAIN_ANY);
                     }

                     this.isInfiniteWaterSource = !BCCoreConfig.pumpsConsumeWater.get()
                        && FluidUtilBC.areFluidsEqual(drain.getFluid(), Fluids.WATER)
                        && isInfiniteSourceAt(this.level, this.targetPos);
                     if (!this.isInfiniteWaterSource) {
                        BlockUtil.drainBlock(this.level, this.currentPos, true, this.getOwner());
                        if (isOil(drain.getFluid())) {
                           if (this.getOwner() != null) {
                              AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.level, ADVANCEMENT_DRAIN_OIL);
                           }

                           this.creditRefineAndRedefineFromPumpedOil(drain);
                           if (this.oilSpringPos != null && this.level.getBlockEntity(this.oilSpringPos) instanceof ITileOilSpring oilSpring) {
                              oilSpring.onPumpOil(this.getOwner(), this.currentPos);
                           }
                        }

                        this.paths.remove(this.currentPos);
                        this.nextPos();
                     }

                     return;
                  }
               }
            }

            if (this.rebuildDelay > 0) {
               return;
            }

            this.rebuildDelay = 30;
         } else {
            if (this.currentPos == null && this.rebuildDelay > 0) {
               return;
            }

            this.rebuildDelay = 30;
         }

         this.buildQueue();
         this.nextPos();
      }
   }

   @Nullable
   private BlockPos getFirstInvalidPointOnPath(BlockPos from) {
      TilePump.FluidPath path = this.paths.get(from);
      if (path == null) {
         return from;
      }

      while (BlockUtil.getFluidWithFlowing(this.level, path.thisPos) != null) {
         if ((path = path.parent) == null) {
            return null;
         }
      }

      return path.thisPos;
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.fluidTank.serialize(output);
      if (this.oilSpringPos != null) {
         output.putBoolean("hasOilSpring", true);
         output.putInt("oilSpringX", this.oilSpringPos.getX());
         output.putInt("oilSpringY", this.oilSpringPos.getY());
         output.putInt("oilSpringZ", this.oilSpringPos.getZ());
      }
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.fluidTank.deserialize(input);
      if (input.getBooleanOr("hasOilSpring", false)) {
         this.oilSpringPos = new BlockPos(input.getIntOr("oilSpringX", 0), input.getIntOr("oilSpringY", 0), input.getIntOr("oilSpringZ", 0));
      } else {
         this.oilSpringPos = null;
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("battery = " + this.battery.getDebugString());
      left.add("current = " + this.currentPos);
      left.add("wantedLength = " + this.wantedLength);
      left.add("currentLength = " + this.currentLength);
      left.add("lastLength = " + this.lastLength);
      left.add("isComplete = " + this.isComplete());
      left.add("progress = " + MjAPI.formatMj(this.progress));
      left.add("fluid = " + FluidUtilBC.getDebugString(this.fluidTank.getFluidStack()));
      left.add("queue size = " + this.queue.size());
      left.add("infinite = " + this.isInfiniteWaterSource);
   }

   @Override
   public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("battery = " + this.battery.getDebugString());
      left.add("current = " + this.currentPos);
      left.add("wantedLength = " + this.wantedLength);
      left.add("currentLength = " + this.currentLength);
      left.add("isComplete = " + this.isComplete());
      left.add("progress = " + MjAPI.formatMj(this.progress));
      left.add("fluid = " + FluidUtilBC.getDebugString(this.fluidTank.getFluidStack()));
      left.add("queue size = " + this.queue.size());
      left.add("infinite = " + this.isInfiniteWaterSource);
   }

   @Override
   protected long getBatteryCapacity() {
      return 50L * MjAPI.MJ;
   }

   public record ColumnProbe(BlockPos firstFluid, BlockPos firstOil, BlockPos spring) {
   }

   static final class FluidPath {
      public final BlockPos thisPos;
      @Nullable
      public final TilePump.FluidPath parent;

      public FluidPath(BlockPos thisPos, TilePump.FluidPath parent) {
         this.thisPos = thisPos;
         this.parent = parent;
      }

      public TilePump.FluidPath and(BlockPos pos) {
         return new TilePump.FluidPath(pos, this);
      }
   }
}
