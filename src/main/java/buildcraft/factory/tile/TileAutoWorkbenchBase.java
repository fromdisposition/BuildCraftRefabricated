/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.lib.nbt.BcAuth;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IHasWork;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.BcBlockEntity;
import org.jspecify.annotations.Nullable;
import buildcraft.lib.tile.ItemHandlerFiltered;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.lib.tile.craft.IAutoCraft;
import buildcraft.lib.tile.craft.WorkbenchCrafting;
import java.util.Arrays;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;

public abstract class TileAutoWorkbenchBase extends BcBlockEntity implements IHasWork, IAutoCraft {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftfactory:lazy_crafting");
   private static final long POWER_GEN_PASSIVE = MjAPI.MJ / 5L;
   private static final long POWER_REQUIRED = POWER_GEN_PASSIVE * 20L * 10L;
   private static final long POWER_LOST = POWER_GEN_PASSIVE * 10L;
   protected final int width;
   protected final int height;
   public final ItemHandlerSimple invBlueprint;
   public final ItemHandlerSimple invMaterialFilter;
   public final ItemHandlerSimple invMaterials;
   public final ItemHandlerSimple invResult;
   public final WorkbenchCrafting crafting;
   public ItemStack resultClient = ItemStack.EMPTY;
   private long powerStored;
   private long powerStoredLast;
   private final IMjRedstoneReceiver mjReceiver = new IMjRedstoneReceiver() {
      @Override
      public long getPowerRequested() {
         return TileAutoWorkbenchBase.POWER_REQUIRED - TileAutoWorkbenchBase.this.powerStored;
      }

      @Override
      public long receivePower(long microJoules, boolean simulate) {
         long req = this.getPowerRequested();
         long taken = Math.min(req, microJoules);
         if (!simulate) {
            TileAutoWorkbenchBase.this.powerStored += taken;
         }

         return microJoules - taken;
      }

      @Override
      public boolean canConnect(@Nonnull IMjConnector other) {
         return true;
      }
   };

   public TileAutoWorkbenchBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int width, int height) {
      super(type, pos, state);
      this.width = width;
      this.height = height;
      int gridSize = width * height;
      this.invBlueprint = this.itemManager.addInvHandler("blueprint", gridSize, ItemHandlerManager.EnumAccess.PHANTOM);
      this.invMaterialFilter = this.itemManager.addInvHandler("material_filter", gridSize, ItemHandlerManager.EnumAccess.PHANTOM);
      ItemHandlerFiltered filtered = new ItemHandlerFiltered(this.invMaterialFilter, true);
      filtered.setCallback(this.itemManager.callback);
      this.invMaterials = this.itemManager.addInvHandler("materials", filtered, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
      this.invResult = this.itemManager.addInvHandler("result", 1, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
      this.crafting = new WorkbenchCrafting(width, height, this, this.invBlueprint, this.invMaterials, this.invResult);
      this.invBlueprint.setCallback((handler, slot, before, after) -> {
         this.setChanged();
         this.crafting.onInventoryChange(this.invBlueprint);
      });
      this.invMaterials.setCallback((handler, slot, before, after) -> {
         this.setChanged();
         this.crafting.onInventoryChange(this.invMaterials);
      });
   }

   @Override
   public boolean hasWork() {
      return this.crafting.canCraft();
   }

   @Override
   public ItemStack getCurrentRecipeOutput() {
      return this.crafting.getAssumedResult();
   }

   @Override
   public ItemHandlerSimple getInvBlueprint() {
      return this.invBlueprint;
   }

   public IMjRedstoneReceiver getMjReceiver() {
      return this.mjReceiver;
   }


   public double getProgress(float partialTicks) {
      double interp = (float)this.powerStoredLast + (float)(this.powerStored - this.powerStoredLast) * partialTicks;
      return interp / POWER_REQUIRED;
   }

   public long getPowerStored() {
      return this.powerStored;
   }

   public void setPowerStored(long value) {
      this.powerStoredLast = this.powerStored;
      this.powerStored = value;
      if (this.powerStored < 10L) {
         this.powerStoredLast = this.powerStored;
      }
   }

   public void serverTick() {
      ItemStack prevResult = this.resultClient;
      boolean didChange = this.crafting.tick();
      if (didChange) {
         this.resultClient = this.crafting.getAssumedResult().copy();
         this.createFilters();
      }

      if (this.crafting.canCraft()) {
         if (this.powerStored >= POWER_REQUIRED) {
            if (this.crafting.craft()) {
               this.powerStored = this.crafting.canCraft() ? 1L : 0L;
               if (this.getOwner() != null) {
                  AdvancementUtil.unlockAdvancement(BcAuth.id(this.getOwner()), this.level, ADVANCEMENT);
               }
            }
         } else {
            this.powerStored = this.powerStored + POWER_GEN_PASSIVE;
         }
      } else if (this.powerStored >= POWER_LOST) {
         this.powerStored = this.powerStored - POWER_LOST;
      } else {
         this.powerStored = 0L;
      }

      if (!ItemStack.matches(prevResult, this.resultClient)) {
         this.setChanged();
         if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
         }
      }
   }

   private void createFilters() {
      int slotCount = this.invMaterialFilter.getSlots();
      if (this.crafting.getAssumedResult().isEmpty()) {
         for (int s = 0; s < slotCount; s++) {
            this.invMaterialFilter.setStackInSlot(s, ItemStack.EMPTY);
         }
      } else {
         NonNullList<ItemStack> uniqueStacks = NonNullList.create();
         int[] requirements = new int[slotCount];

         for (int s = 0; s < this.invBlueprint.getSlots(); s++) {
            ItemStack bptStack = this.invBlueprint.getStackInSlot(s);
            if (!bptStack.isEmpty()) {
               boolean foundMatch = false;

               for (int i = 0; i < uniqueStacks.size(); i++) {
                  if (StackUtil.canMerge(bptStack, (ItemStack)uniqueStacks.get(i))) {
                     foundMatch = true;
                     requirements[i]++;
                     break;
                  }
               }

               if (!foundMatch) {
                  requirements[uniqueStacks.size()] = 1;
                  uniqueStacks.add(bptStack);
               }
            }
         }

         int uniqueSlotCount = uniqueStacks.size();
         if (uniqueSlotCount == 0) {
            for (int s = 0; s < slotCount; s++) {
               this.invMaterialFilter.setStackInSlot(s, ItemStack.EMPTY);
            }
         } else {
            int[] slotAllocationCount = new int[uniqueSlotCount];
            Arrays.fill(slotAllocationCount, 1);
            int slotsLeft = slotCount - uniqueSlotCount;

            for (int i = 0; i < slotsLeft; i++) {
               int smallestDifference = Integer.MAX_VALUE;
               int smallestDifferenceIndex = 0;

               for (int s = 0; s < uniqueSlotCount; s++) {
                  ItemStack stack = (ItemStack)uniqueStacks.get(s);
                  int uniqueCountTotal = stack.getMaxStackSize() * slotAllocationCount[s];
                  int difference = uniqueCountTotal / requirements[s];
                  if (difference < smallestDifference) {
                     smallestDifference = difference;
                     smallestDifferenceIndex = s;
                  }
               }

               slotAllocationCount[smallestDifferenceIndex]++;
            }

            int realIndex = 0;

            for (int s = 0; s < uniqueSlotCount; s++) {
               ItemStack stack = ((ItemStack)uniqueStacks.get(s)).copyWithCount(1);

               for (int i = 0; i < slotAllocationCount[s]; i++) {
                  this.invMaterialFilter.setStackInSlot(realIndex, stack);
                  realIndex++;
               }
            }
         }
      }
   }

   @Override
   protected void writeData(BcValueOut output) {
      super.writeData(output);
      output.store("items", CompoundTag.CODEC, this.itemManager.serializeNBT());
      output.putLong("powerStored", this.powerStored);
      if (!this.resultClient.isEmpty()) {
         output.store("resultClient", ItemStack.CODEC, this.resultClient);
      }
   }

   @Override
   public void readData(BcValueIn input) {
      super.readData(input);
      input.read("items", CompoundTag.CODEC).ifPresent(this.itemManager::deserializeNBT);
      this.powerStored = input.getLongOr("powerStored", 0L);
      this.resultClient = input.read("resultClient", ItemStack.CODEC).orElse(ItemStack.EMPTY);
   }

   @Override
   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
