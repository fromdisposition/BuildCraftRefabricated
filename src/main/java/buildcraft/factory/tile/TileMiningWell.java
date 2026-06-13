/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.core.BCCoreConfig;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.InventoryUtil;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Single-column quarry: one block down, dig or stop on fluid/solid. */
public class TileMiningWell extends TileMiner {
   private static final int NO_FACE = Integer.MIN_VALUE;
   private static final int RECHECK_INTERVAL = 10;

   private enum FaceState {
      IDLE,
      DIGGING,
      BLOCKED
   }

   private FaceState faceState = FaceState.IDLE;
   private int faceY = NO_FACE;
   @Nullable
   private BlockPos breakingPos;
   @Nullable
   private TaskBreakBlock currentTask;
   private boolean hadPower;
   private IMjReceiver mjReceiver;

   public TileMiningWell(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.MINING_WELL, pos, state);
   }

   @Nullable
   @Override
   protected BlockPos getTargetPos() {
      return this.hasFace() ? this.columnPos(this.faceY) : null;
   }

   @Override
   protected BlockPos resolveShaftEnd(BlockPos target) {
      return target.above();
   }

   @Override
   protected int getShaftCollisionLengthBlocks() {
      return this.hasFace() ? Math.max(0, this.worldPosition.getY() - this.faceY) : 0;
   }

   @Override
   public boolean isComplete() {
      return this.faceState == FaceState.IDLE;
   }

   @Override
   public boolean hasWork() {
      return this.faceState == FaceState.DIGGING && this.battery.getStored() > 0L;
   }

   @Override
   protected void mine() {
      boolean hasPower = this.battery.getStored() > 0L;
      if (!hasPower) {
         this.hadPower = false;
         this.cancelBreakTask();
         return;
      }

      if (!this.hadPower) {
         this.hadPower = true;
         if (this.faceState == FaceState.IDLE) {
            this.advanceToWork();
         }
      }

      if (this.level.getGameTime() % RECHECK_INTERVAL == this.offset) {
         this.advanceToWork();
      }

      if (this.currentTask != null) {
         long needed = this.currentTask.getTarget() - this.currentTask.power;
         long added = this.battery.extractPower(0L, Math.max(0L, needed));
         if (this.currentTask.addPower(added)) {
            this.currentTask = null;
         }

         return;
      }

      if (this.faceState == FaceState.DIGGING) {
         BlockPos digPos = this.getDigPos();
         if (digPos != null && this.canMine(digPos)) {
            this.currentTask = new TaskBreakBlock(digPos);
         } else {
            this.advanceToWork();
         }
      }
   }

   /** Top-down: first solid — dig if mineable, else stop. */
   private void advanceToWork() {
      int minY = this.computeMinY();

      for (int y = this.worldPosition.getY() - 1; y >= minY; y--) {
         BlockPos pos = this.columnPos(y);
         if (this.canPassThrough(pos)) {
            continue;
         }

         if (!this.canMine(pos)) {
            this.setBlocked(y);
            return;
         }

         this.setDigging(y);
         return;
      }

      this.setIdle();
   }

   private int computeMinY() {
      int minY = this.worldPosition.getY() - BCCoreConfig.miningMaxDepth.get();
      return Math.max(this.level.getMinY(), minY);
   }

   private boolean canPassThrough(BlockPos pos) {
      return this.level.isEmptyBlock(pos);
   }

   /** Same rules as {@link buildcraft.builders.tile.TileQuarry#canMine}. */
   private boolean canMine(BlockPos pos) {
      if (this.level.getBlockState(pos).getDestroySpeed(this.level, pos) < 0.0F) {
         return false;
      }

      Fluid fluid = BlockUtil.getFluidWithFlowing(this.level, pos);
      if (fluid != null) {
         return false;
      }

      return !(this.level instanceof ServerLevel serverLevel) || BlockUtil.canMachineBreak(serverLevel, pos, this.getOwner());
   }

   private void setIdle() {
      this.clearBreakingOverlay();
      this.faceState = FaceState.IDLE;
      this.faceY = NO_FACE;
      this.currentPos = null;
      this.cancelBreakTask();
      this.applyShaftChange();
   }

   private void setBlocked(int y) {
      if (this.faceState != FaceState.BLOCKED || this.faceY != y) {
         this.clearBreakingOverlay();
      }

      this.faceState = FaceState.BLOCKED;
      this.faceY = y;
      this.currentPos = null;
      this.cancelBreakTask();
      this.applyShaftChange();
   }

   private void setDigging(int y) {
      if (this.faceState != FaceState.DIGGING || this.faceY != y) {
         this.clearBreakingOverlay();
         this.cancelBreakTask();
      }

      this.faceState = FaceState.DIGGING;
      this.faceY = y;
      this.currentPos = this.columnPos(y);
      this.applyShaftChange();
   }

   private void cancelBreakTask() {
      this.currentTask = null;
      this.progress = 0;
   }

   private void clearBreakingOverlay() {
      if (this.breakingPos != null) {
         this.clearBreakProgress(this.breakingPos);
         this.breakingPos = null;
      }
   }

   private void clearBreakProgress(@Nullable BlockPos pos) {
      if (this.level != null && !this.level.isClientSide() && pos != null) {
         this.level.destroyBlockProgress(pos.hashCode(), pos, -1);
      }
   }

   private void showBreakProgress(BlockPos pos, int stage) {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      if (this.breakingPos != null && !this.breakingPos.equals(pos)) {
         this.clearBreakProgress(this.breakingPos);
      }

      this.breakingPos = pos;
      this.level.destroyBlockProgress(pos.hashCode(), pos, stage);
   }

   private void applyShaftChange() {
      this.updateLength();
      this.updateShaftCollision();
   }

   private boolean hasFace() {
      return this.faceY != NO_FACE;
   }

   @Nullable
   private BlockPos getDigPos() {
      return this.faceState == FaceState.DIGGING && this.hasFace() ? this.columnPos(this.faceY) : null;
   }

   private BlockPos columnPos(int y) {
      return new BlockPos(this.worldPosition.getX(), y, this.worldPosition.getZ());
   }

   private final class TaskBreakBlock {
      private final BlockPos breakPos;
      private long power;

      private TaskBreakBlock(BlockPos breakPos) {
         this.breakPos = breakPos;
      }

      private long getTarget() {
         return BlockUtil.computeBlockBreakPower(TileMiningWell.this.level, this.breakPos);
      }

      private boolean addPower(long microJoules) {
         this.power += microJoules;
         long target = this.getTarget();
         if (this.power < target) {
            if (!TileMiningWell.this.level.getBlockState(this.breakPos).isAir()) {
               TileMiningWell.this.showBreakProgress(this.breakPos, (int)(this.power * 9L / target));
            }

            TileMiningWell.this.progress = (int)this.power;
            return false;
         }

         TileMiningWell.this.clearBreakProgress(this.breakPos);
         TileMiningWell.this.breakingPos = null;
         TileMiningWell.this.progress = 0;
         if (!TileMiningWell.this.canMine(this.breakPos)) {
            TileMiningWell.this.advanceToWork();
            return true;
         }

         if (TileMiningWell.this.level instanceof ServerLevel serverLevel) {
            BlockUtil.breakBlockAndGetDropsWithXp(serverLevel, this.breakPos, new ItemStack(Items.IRON_PICKAXE), TileMiningWell.this.getOwner())
               .ifPresent(
                  result -> {
                     result.drops().forEach(stack -> InventoryUtil.addToBestAcceptor(TileMiningWell.this.level, TileMiningWell.this.worldPosition, null, stack));
                     if (result.xp() > 0) {
                        ExperienceOrb.award(serverLevel, Vec3.atCenterOf(TileMiningWell.this.worldPosition), result.xp());
                     }
                  }
               );
         }

         if (TileMiningWell.this.faceState == FaceState.DIGGING && Objects.equals(TileMiningWell.this.getDigPos(), this.breakPos)) {
            TileMiningWell.this.faceY--;
            if (TileMiningWell.this.faceY < TileMiningWell.this.computeMinY()) {
               TileMiningWell.this.setIdle();
            } else {
               TileMiningWell.this.currentPos = TileMiningWell.this.getDigPos();
               TileMiningWell.this.applyShaftChange();
            }
         }

         TileMiningWell.this.advanceToWork();
         return true;
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putByte("faceState", (byte)this.faceState.ordinal());
      output.putBoolean("hasColumnState", true);
      output.putBoolean("hasFace", this.hasFace());
      if (this.hasFace()) {
         output.putInt("faceY", this.faceY);
      }
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      if (input.getBooleanOr("hasColumnState", false)) {
         int ordinal = input.getByteOr("faceState", input.getByteOr("columnState", (byte)0)) & 0xFF;
         this.faceState = ordinal < FaceState.values().length ? FaceState.values()[ordinal] : FaceState.IDLE;
         this.faceY = input.getBooleanOr("hasFace", false) ? input.getIntOr("faceY", NO_FACE) : NO_FACE;
         if (!input.getBooleanOr("hasFace", false) && input.getBooleanOr("hasAnchor", false)) {
            this.faceY = input.getIntOr("anchorY", NO_FACE);
         }
      } else if (input.getBooleanOr("hasShaftTipPos", false)) {
         this.faceState = FaceState.BLOCKED;
         this.faceY = input.getIntOr("shaftTipY", NO_FACE);
      } else if (this.currentPos != null) {
         this.faceState = FaceState.DIGGING;
         this.faceY = this.currentPos.getY();
      } else {
         this.faceState = FaceState.IDLE;
         this.faceY = NO_FACE;
      }

      this.currentTask = null;
      this.breakingPos = null;
      if (this.faceState == FaceState.DIGGING && this.hasFace()) {
         this.currentPos = this.columnPos(this.faceY);
      } else {
         this.currentPos = null;
      }

      this.wantedLength = this.getShaftLengthBlocks();
      this.updateShaftCollision();
   }

   @Override
   protected IMjReceiver createMjReceiver() {
      if (this.mjReceiver == null) {
         this.mjReceiver = new IMjReceiver() {
            @Override
            public long getPowerRequested() {
               return TileMiningWell.this.battery.getCapacity() - TileMiningWell.this.battery.getStored();
            }

            @Override
            public long receivePower(long microJoules, boolean simulate) {
               return TileMiningWell.this.battery.addPowerChecking(microJoules, simulate);
            }

            @Override
            public boolean canConnect(@Nonnull IMjConnector other) {
               return true;
            }
         };
      }

      return this.mjReceiver;
   }
}
