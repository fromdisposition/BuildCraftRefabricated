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
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;
import net.minecraft.world.phys.Vec3;

/** Mines straight down: first iron-pick solid from the top, stop on fluid or higher-tier blocks. */
public class TileMiningWell extends TileMiner {
   @Nullable
   private BlockPos shaftTipPos;
   private IMjReceiver mjReceiver;

   public TileMiningWell(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.MINING_WELL, pos, state);
   }

   @Nullable
   @Override
   protected BlockPos getTargetPos() {
      return this.currentPos != null ? this.currentPos : this.shaftTipPos;
   }

   @Override
   protected BlockPos resolveShaftEnd(BlockPos target) {
      return target.above();
   }

   @Override
   protected int getShaftCollisionLengthBlocks() {
      BlockPos tip = this.currentPos != null ? this.currentPos : this.shaftTipPos;
      return tip == null ? 0 : Math.max(0, this.worldPosition.getY() - tip.getY());
   }

   @Override
   protected void mine() {
      BlockPos previousTarget = this.currentPos;
      this.syncColumnState();

      if (previousTarget != null && !Objects.equals(previousTarget, this.currentPos)) {
         this.progress = 0;
         this.clearBreakProgress(previousTarget);
      }

      if (this.currentPos == null) {
         return;
      }

      if (!this.isMineableSolid(this.currentPos)) {
         this.progress = 0;
         return;
      }

      long target = BlockUtil.computeBlockBreakPower(this.level, this.currentPos);
      this.progress = this.progress + (int)this.battery.extractPower(0L, target - this.progress);
      if (this.progress >= target) {
         this.progress = 0;
         this.clearBreakProgress(this.currentPos);
         if (this.level instanceof ServerLevel serverLevel) {
            Optional<BlockUtil.BreakResult> result = BlockUtil.breakBlockAndGetDropsWithXp(
               serverLevel, this.currentPos, new ItemStack(Items.IRON_PICKAXE), this.getOwner()
            );
            if (result.isPresent()) {
               result.get().drops().forEach(stack -> InventoryUtil.addToBestAcceptor(this.level, this.worldPosition, null, stack));
               int xp = result.get().xp();
               if (xp > 0) {
                  ExperienceOrb.award(serverLevel, Vec3.atCenterOf(this.worldPosition), xp);
               }
            }
         }

         this.syncColumnState();
      } else {
         this.showBreakProgress(this.currentPos, (int)(this.progress * 9L / target));
      }
   }

   /** Top-down: first solid — dig if iron-pick mineable, else stop with shaft at obstruction. */
   private void syncColumnState() {
      int minY = Math.max(this.level.getMinY(), this.worldPosition.getY() - BCCoreConfig.miningMaxDepth.get());
      BlockPos firstMineable = null;

      for (int y = this.worldPosition.getY() - 1; y >= minY; y--) {
         BlockPos pos = this.columnPos(y);
         if (this.level.isEmptyBlock(pos)) {
            continue;
         }

         if (this.isMineableSolid(pos)) {
            firstMineable = pos;
            break;
         }

         this.applyColumnState(null, pos);
         return;
      }

      if (firstMineable == null) {
         this.applyColumnState(null, null);
         return;
      }

      this.applyColumnState(firstMineable, null);
   }

   private boolean isMineableSolid(BlockPos pos) {
      if (this.level.isEmptyBlock(pos)) {
         return false;
      }

      BlockState state = this.level.getBlockState(pos);
      if (!BlockUtil.isSolid(this.level, pos, state) || !state.getFluidState().isEmpty()) {
         return false;
      }

      if (BlockUtil.isUnbreakableBlock(this.level, pos, this.getOwner()) || state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
         return false;
      }

      return !(this.level instanceof ServerLevel serverLevel) || BlockUtil.canMachineBreak(serverLevel, pos, this.getOwner());
   }

   private void applyColumnState(@Nullable BlockPos target, @Nullable BlockPos shaftTip) {
      boolean changed = !Objects.equals(this.currentPos, target) || !Objects.equals(this.shaftTipPos, shaftTip);
      if (!changed) {
         return;
      }

      this.currentPos = target;
      this.shaftTipPos = shaftTip;
      this.applyShaftChange();
   }

   private void applyShaftChange() {
      this.registeredCollisionLength = -1;
      this.updateLength();
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

      this.level.destroyBlockProgress(pos.hashCode(), pos, stage);
   }

   private BlockPos columnPos(int y) {
      return new BlockPos(this.worldPosition.getX(), y, this.worldPosition.getZ());
   }

   @Override
   protected void writeData(BcValueOut output) {
      super.writeData(output);
      if (this.shaftTipPos != null) {
         output.putBoolean("hasShaftTipPos", true);
         output.putInt("shaftTipY", this.shaftTipPos.getY());
      } else {
         output.putBoolean("hasShaftTipPos", false);
      }
   }

   @Override
   public void readData(BcValueIn input) {
      super.readData(input);
      if (input.getBooleanOr("hasShaftTipPos", false)) {
         this.shaftTipPos = this.columnPos(input.getIntOr("shaftTipY", this.worldPosition.getY() - 1));
      } else if (input.getBooleanOr("hasColumnState", false)) {
         int faceY = input.getBooleanOr("hasFace", false) ? input.getIntOr("faceY", this.worldPosition.getY() - 1) : this.worldPosition.getY() - 1;
         int ordinal = input.getByteOr("faceState", (byte)0) & 0xFF;
         if (ordinal == 2) {
            this.shaftTipPos = this.columnPos(faceY);
            this.currentPos = null;
         } else if (ordinal == 1) {
            this.currentPos = this.columnPos(faceY);
            this.shaftTipPos = null;
         } else {
            this.currentPos = null;
            this.shaftTipPos = null;
         }
      } else {
         this.shaftTipPos = null;
      }

      this.wantedLength = this.getShaftLengthBlocks();
      this.deferredShaftCollision = true;
   }

   @Override
   public void setRemoved() {
      if (this.level != null && !this.level.isClientSide() && this.currentPos != null) {
         this.clearBreakProgress(this.currentPos);
      }

      super.setRemoved();
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
