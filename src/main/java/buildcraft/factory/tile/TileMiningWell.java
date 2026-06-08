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
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.fluids.FluidTypes;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.InventoryUtil;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class TileMiningWell extends TileMiner {
   private boolean shouldCheck = true;
   private int recheckCooldown = 0;
   private IMjReceiver mjReceiver;

   public TileMiningWell(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.MINING_WELL, pos, state);
   }

   @Override
   protected void mine() {
      if (this.currentPos != null && this.canBreak()) {
         this.shouldCheck = true;
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

            this.nextPos();
         } else if (!this.level.isEmptyBlock(this.currentPos)) {
            this.level.destroyBlockProgress(this.currentPos.hashCode(), this.currentPos, (int)(this.progress * 9 / target));
         }
      } else if (this.currentPos != null && !this.canBreak()) {
         this.progress = 0;
         this.nextPos();
      } else if (!this.shouldCheck && this.recheckCooldown > 0) {
         this.recheckCooldown--;
      } else {
         this.nextPos();
         if (this.currentPos == null) {
            this.shouldCheck = false;
         }

         this.recheckCooldown = 256;
      }
   }

   private boolean canBreak() {
      if (!this.level.isEmptyBlock(this.currentPos) && !BlockUtil.isUnbreakableBlock(this.level, this.currentPos, this.getOwner())) {
         BlockState state = this.level.getBlockState(this.currentPos);
         if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
         }

         Fluid fluid = BlockUtil.getFluidWithFlowing(this.level, this.currentPos);
         return fluid == null ? true : FluidTypes.of(fluid).getViscosity() <= 1000;
      } else {
         return false;
      }
   }

   private void nextPos() {
      this.currentPos = this.worldPosition;

      while (true) {
         this.currentPos = this.currentPos.below();
         if (!this.level.isOutsideBuildHeight(this.currentPos) && this.worldPosition.getY() - this.currentPos.getY() <= BCCoreConfig.miningMaxDepth.get()) {
            if (this.canBreak()) {
               if (this.level instanceof ServerLevel serverLevel && !BlockUtil.canMachineBreak(serverLevel, this.currentPos, this.getOwner())) {
                  continue;
               }

               this.updateLength();
               return;
            }

            FluidState fluidState = this.level.getFluidState(this.currentPos);
            boolean isPassable = !fluidState.isEmpty() && FluidTypes.of(fluidState.getType()).getViscosity() <= 1000;
            if (this.level.isEmptyBlock(this.currentPos) || this.level.getBlockState(this.currentPos).is(BCFactoryBlocks.TUBE) || isPassable) {
               continue;
            }
         }

         this.currentPos = null;
         this.updateLength();
         return;
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
