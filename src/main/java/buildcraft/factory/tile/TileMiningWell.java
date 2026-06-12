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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

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
   protected void mine() {
      BlockPos previousTarget = this.currentPos;
      this.syncColumnState();

      if (previousTarget != null && !Objects.equals(previousTarget, this.currentPos)) {
         this.progress = 0;
         this.level.destroyBlockProgress(previousTarget.hashCode(), previousTarget, -1);
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
         this.level.destroyBlockProgress(this.currentPos.hashCode(), this.currentPos, -1);
         if (this.level instanceof ServerLevel serverLevel) {
            BlockUtil.breakBlockAndGetDropsWithXp(serverLevel, this.currentPos, new ItemStack(Items.IRON_PICKAXE), this.getOwner()).ifPresent(result -> {
               result.drops().forEach(stack -> InventoryUtil.addToBestAcceptor(this.level, this.worldPosition, null, stack));
               if (result.xp() > 0) {
                  ExperienceOrb.award(serverLevel, Vec3.atCenterOf(this.worldPosition), result.xp());
               }
            });
         }

         this.syncColumnState();
      } else {
         this.level.destroyBlockProgress(this.currentPos.hashCode(), this.currentPos, (int)(this.progress * 9 / target));
      }
   }

   /**
    * Scan the well column each tick.
    * Mine the first iron-pick solid from the top (classic BC behaviour).
    * Stop with the shaft at non-mineable blocks. Retract when a solid appears in the
    * open shaft above the current dig face (e.g. a block placed in the nozzle).
    */
   private void syncColumnState() {
      int x = this.worldPosition.getX();
      int z = this.worldPosition.getZ();
      int minY = Math.max(this.level.getMinY(), this.worldPosition.getY() - BCCoreConfig.miningMaxDepth.get());

      BlockPos firstMineable = null;

      for (int y = this.worldPosition.getY() - 1; y >= minY; y--) {
         BlockPos pos = new BlockPos(x, y, z);
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

      if (this.currentPos != null && firstMineable.getY() > this.currentPos.getY()) {
         this.applyColumnState(null, firstMineable);
         return;
      }

      this.applyColumnState(firstMineable, firstMineable);
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
      this.updateLength();
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      if (this.shaftTipPos != null) {
         output.putBoolean("hasShaftTipPos", true);
         output.putInt("shaftTipX", this.shaftTipPos.getX());
         output.putInt("shaftTipY", this.shaftTipPos.getY());
         output.putInt("shaftTipZ", this.shaftTipPos.getZ());
      } else {
         output.putBoolean("hasShaftTipPos", false);
      }
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      if (input.getBooleanOr("hasShaftTipPos", false)) {
         this.shaftTipPos = new BlockPos(
            input.getIntOr("shaftTipX", 0),
            input.getIntOr("shaftTipY", 0),
            input.getIntOr("shaftTipZ", 0)
         );
      } else {
         this.shaftTipPos = null;
      }
   }

   @Override
   public void setRemoved() {
      if (this.level != null && !this.level.isClientSide() && this.currentPos != null) {
         this.level.destroyBlockProgress(this.currentPos.hashCode(), this.currentPos, -1);
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
