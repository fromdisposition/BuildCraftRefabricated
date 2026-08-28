/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreItems;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.FeEnergyStorage;
import buildcraft.lib.misc.BlockDropsUtil;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileElectricEngineBase extends TileEngineBase_BC8 implements MenuProvider, BlockEntityExtendedMenu {
   public static final int MAX_FE = 10000;
   public static final float HEAT_RATE = 0.06F;
   public static final float COOLDOWN_RATE = 0.01F;
   public static final Map<Item, Long> UPGRADE_VALUES = new LinkedHashMap<>();
   public final ItemHandlerSimple upgrades = new ItemHandlerSimple(4, (handler, slot, bef, aft) -> this.setChanged());
   public final FeEnergyStorage energyStorage;

   public static void initUpgrades() {
      if (UPGRADE_VALUES.isEmpty()) {
         UPGRADE_VALUES.put(BCCoreItems.GEAR_IRON, MjAPI.MJ * 2L);
         UPGRADE_VALUES.put(BCCoreItems.GEAR_GOLD, MjAPI.MJ * 3L);
      }
   }

   protected TileElectricEngineBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxFeInsert, int maxFeExtract) {
      super(type, pos, state);
      this.upgrades.setChecker((slot, stack) -> {
         initUpgrades();
         return UPGRADE_VALUES.containsKey(stack.getItem());
      });
      this.upgrades.setLimitedInsertor(1);
      this.energyStorage = new FeEnergyStorage(MAX_FE, maxFeInsert, maxFeExtract) {
         @Override
         protected void onEnergyChanged(int previousAmount) {
            TileElectricEngineBase.this.setChanged();
         }
      };
   }

   public int getCurrentFe() {
      return (int)this.energyStorage.getAmount();
   }

   public void setCurrentFe(int fe) {
      this.energyStorage.set(Math.max(0, Math.min(MAX_FE, fe)));
   }

   public long getMjPerTick() {
      initUpgrades();
      long value = MjAPI.MJ * 4L;

      for (int slot = 0; slot < this.upgrades.getSlots(); slot++) {
         ItemStack stack = this.upgrades.getStackInSlot(slot);
         if (!stack.isEmpty()) {
            Long add = UPGRADE_VALUES.get(stack.getItem());
            if (add != null) {
               value += add;
            }
         }
      }

      return value;
   }

   @Override
   public void updateHeatLevel() {
      if (this.heat > 20.0F) {
         this.heat -= 0.01F;
      }

      if (this.heat <= 20.0F) {
         this.heat = 20.0F;
      }

      this.getPowerStage();
   }

   @Override
   protected EnumPowerStage computePowerStage() {
      // Electric converter with no coolant: never overheat (cap at RED), so it can't latch into the permanent,
      // manual-clear-only OVERHEAT death state.
      EnumPowerStage stage = super.computePowerStage();
      return stage == EnumPowerStage.OVERHEAT ? EnumPowerStage.RED : stage;
   }

   @Override
   public long minPowerReceived() {
      return 0L;
   }

   @Override
   public float explosionRange() {
      return 4.0F;
   }

   @Override
   protected void writeData(BcValueOut output) {
      super.writeData(output);
      output.putInt("currentFe", this.getCurrentFe());
      output.store("upgrades", CompoundTag.CODEC, this.upgrades.serializeNBT());
   }

   @Override
   public void readData(BcValueIn input) {
      super.readData(input);
      this.setCurrentFe(input.getIntOr("currentFe", 0));
      this.upgrades.deserializeNBT(input.read("upgrades", CompoundTag.CODEC).orElseGet(CompoundTag::new));
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   @Override
   protected void dropEngineContents(BlockPos pos) {
      BlockDropsUtil.dropItems(this.level, pos, this.upgrades);
   }
}
