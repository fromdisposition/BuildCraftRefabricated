/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.fabric.transfer.ItemEnergyCharging;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileChargingTable extends TileLaserTableBase {
   public final ItemHandlerSimple inv = this.itemManager.addInvHandler("inv", 1, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);

   public TileChargingTable(BlockPos pos, BlockState state) {
      super(BCSiliconBlockEntities.CHARGING_TABLE, pos, state);
   }

   @Override
   protected int getLaserBufferMultiplier() {
      return 5;
   }

   @Override
   public long getTarget() {
      return ItemEnergyCharging.getRequiredMj(this.inv.getStackInSlot(0));
   }

   @Override
   public void serverTick() {
      super.serverTick();
      if (this.power <= 0L) {
         return;
      }

      ItemStack stack = this.inv.getStackInSlot(0);
      long required = ItemEnergyCharging.getRequiredMj(stack);
      if (required > 0L) {
         long transferred = ItemEnergyCharging.chargeMj(stack, this.power);
         if (transferred > 0L) {
            this.power -= transferred;
            this.inv.setStackInSlot(0, stack);
            this.setChanged();
         }
      } else if (!stack.isEmpty()) {
         long bleed = Math.min(this.power, this.getIdleBleed());
         this.power -= bleed;
         this.setChanged();
      }
   }

   private long getIdleBleed() {
      long mjPerRf = MjAPI.isRfAutoConversionEnabled() ? MjAPI.getRfConversion().mjPerRf : 0L;
      return mjPerRf > 0L ? 10L * mjPerRf : MjAPI.MJ;
   }

   public boolean isValidChargeItem(ItemStack stack) {
      return ItemEnergyCharging.canCharge(stack);
   }
}
