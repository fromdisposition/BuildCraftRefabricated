/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import buildcraft.lib.fabric.BcRegistryUtil;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.transfer.handler.ItemStackResourceHandler;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if >= 26.1 {
import net.minecraft.world.item.ItemStackTemplate;
//?}
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileEngineStone_BC8 extends TileEngineBase_BC8 implements MenuProvider, BlockEntityExtendedMenu {
   private static final Identifier ADVANCEMENT_POWERING_UP = Identifier.parse("buildcraftenergy:powering_up");
   private static final Identifier ADVANCEMENT_LAVA_POWER = Identifier.parse("buildcraftenergy:lava_power");
   private static final long MAX_OUTPUT = MjAPI.MJ;
   private static final long MIN_OUTPUT = MAX_OUTPUT / 3L;
   private static final long eLimit = (MAX_OUTPUT - MIN_OUTPUT) * 20L;
   public int burnTime = 0;
   public int totalBurnTime = 0;
   private long esum = 0L;
   private ItemStack fuelStack = ItemStack.EMPTY;
   public final ItemStackResourceHandler fuelItemHandler = new ItemStackResourceHandler() {
      @Override
      protected ItemStack getStack() {
         return TileEngineStone_BC8.this.fuelStack;
      }

      @Override
      protected void setStack(ItemStack stack) {
         TileEngineStone_BC8.this.fuelStack = stack;
      }

      @Override
      protected boolean isValid(ItemStack stack) {
         return TileEngineStone_BC8.this.isValidFuel(stack);
      }

      protected void onFinalCommit() {
         TileEngineStone_BC8.this.setChanged();
      }
   };

   public TileEngineStone_BC8(BlockPos pos, BlockState state) {
      super(BCEnergyBlockEntities.ENGINE_STONE, pos, state);
   }

   @Nullable
   public Storage<ItemVariant> getSidedFuelStorage(@Nullable Direction direction) {
      return direction != null && direction != this.getOrientation() ? this.fuelItemHandler : null;
   }

   @Nonnull
   public ItemStack getFuelStack() {
      return this.fuelStack;
   }

   public void setFuelStack(@Nonnull ItemStack stack) {
      this.fuelStack = stack;
      this.setChanged();
   }

   public boolean isValidFuel(@Nonnull ItemStack stack) {
      return this.getBurnTime(stack) > 0;
   }

   private int getBurnTime(@Nonnull ItemStack stack) {
      return !stack.isEmpty() && this.level != null ? this.level.fuelValues().burnDuration(stack) : 0;
   }

   @Nonnull
   @Override
   protected IMjConnector createConnector() {
      return new EngineConnector(false);
   }

   @Override
   public boolean isBurning() {
      return this.burnTime > 0;
   }

   @Override
   protected void engineUpdate() {
      if (this.burnTime > 0) {
         this.burnTime--;
         if (this.getPowerStage() != EnumPowerStage.OVERHEAT) {
            long output = this.getCurrentOutput();
            this.addPower(output);
         }
      }

      if (this.burnTime == 0 && this.isRedstonePowered) {
         int newBurn = this.getBurnTime(this.fuelStack);
         if (newBurn > 0) {
            this.burnTime = newBurn;
            this.totalBurnTime = newBurn;
            if (this.getOwner() != null && this.level != null) {
               AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.level, ADVANCEMENT_POWERING_UP);
               if (this.fuelStack.getItem() == Items.LAVA_BUCKET) {
                  AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.level, ADVANCEMENT_LAVA_POWER);
               }
            }

            ItemStack consumed = this.fuelStack.copy();
            consumed.setCount(1);
            this.fuelStack.shrink(1);
            if (this.fuelStack.isEmpty()) {
               this.fuelStack = ItemStack.EMPTY;
            }

            //? if >= 26.1 {
            ItemStackTemplate containerTemplate = consumed.getItem().getCraftingRemainder();
            ItemStack container = containerTemplate != null ? containerTemplate.create() : ItemStack.EMPTY;
            //?} else {
            /*ItemStack container = consumed.getItem().getCraftingRemainder();
            *///?}
            if (!container.isEmpty()) {
               if (this.fuelStack.isEmpty()) {
                  this.fuelStack = container;
               } else if (this.level != null) {
                  ItemEntity entity = new ItemEntity(
                     this.level, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 1.0, this.getBlockPos().getZ() + 0.5, container
                  );
                  this.level.addFreshEntity(entity);
               }
            }

            this.setChanged();
         }
      }
   }

   protected void addPower(long microMj) {
      this.power = Math.min(this.power + microMj, this.getMaxPower());
   }

   @Override
   public long maxPowerReceived() {
      return 200L * MjAPI.MJ;
   }

   @Override
   public long maxPowerExtracted() {
      return 100L * MjAPI.MJ;
   }

   @Override
   public long getMaxPower() {
      return 1000L * MjAPI.MJ;
   }

   @Override
   public float explosionRange() {
      return 2.0F;
   }

   @Override
   public long getCurrentOutput() {
      long e = 3L * this.getMaxPower() / 8L - this.power;
      this.esum = clamp(this.esum + e, -eLimit, eLimit);
      return clamp(e + this.esum / 20L, MIN_OUTPUT, MAX_OUTPUT);
   }

   @Override
   public long minPowerReceived() {
      return MjAPI.MJ / 10L;
   }

   private static long clamp(long val, long min, long max) {
      return Math.max(min, Math.min(max, val));
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("burnTime", this.burnTime);
      output.putInt("totalBurnTime", this.totalBurnTime);
      output.putLong("esum", this.esum);
      if (!this.fuelStack.isEmpty()) {
         Identifier itemId = BuiltInRegistries.ITEM.getKey(this.fuelStack.getItem());
         output.putString("fuelId", itemId.toString());
         output.putInt("fuelCount", this.fuelStack.getCount());
      }
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.burnTime = input.getIntOr("burnTime", 0);
      this.totalBurnTime = input.getIntOr("totalBurnTime", 0);
      this.esum = input.getLongOr("esum", 0L);
      String fuelId = input.getStringOr("fuelId", "");
      if (!fuelId.isEmpty()) {
         Identifier id = Identifier.tryParse(fuelId);
         if (id != null) {
            Item item = BcRegistryUtil.getItem(id);
            int count = input.getIntOr("fuelCount", 1);
            if (item != null && item != Items.AIR) {
               this.fuelStack = new ItemStack(item, count);
            } else {
               this.fuelStack = ItemStack.EMPTY;
            }
         }
      } else {
         this.fuelStack = ItemStack.EMPTY;
      }
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftenergy.engine_stone");
   }

   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerEngineStone_BC8(containerId, playerInv, this);
   }
}
