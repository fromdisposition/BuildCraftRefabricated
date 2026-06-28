/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.container.PipeFilterMenus;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class PipeBehaviourWoodDiamond extends PipeBehaviourWood {
   public final ItemHandlerSimple filters = new ItemHandlerSimple(9);
   public PipeBehaviourWoodDiamond.FilterMode filterMode = PipeBehaviourWoodDiamond.FilterMode.WHITE_LIST;
   public int currentFilter = 0;
   public boolean filterValid = false;

   public PipeBehaviourWoodDiamond(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourWoodDiamond(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      CompoundTag filtersTag = BcNbt.getCompound(nbt, "filters");
      if (!filtersTag.isEmpty()) {
         this.filters.deserializeNBT(filtersTag);
      }

      this.filterMode = PipeBehaviourWoodDiamond.FilterMode.get(BcNbt.getByte(nbt, "mode", (byte)0));
      this.currentFilter = BcNbt.getByte(nbt, "currentFilter", (byte)0) % this.filters.getSlots();
      this.filterValid = this.hasAnyFilter();
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.put("filters", this.filters.serializeNBT());
      nbt.putByte("mode", (byte)this.filterMode.ordinal());
      nbt.putByte("currentFilter", (byte)this.currentFilter);
      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      super.readFromNbt(nbt);
      this.filters.deserializeNBT(BcNbt.getCompound(nbt, "filters"));
      this.filterMode = PipeBehaviourWoodDiamond.FilterMode.get(BcNbt.getByte(nbt, "mode", (byte)0));
      this.currentFilter = BcNbt.getByte(nbt, "currentFilter", (byte)0) % this.filters.getSlots();
      this.filterValid = this.hasAnyFilter();
   }

   @Override
   public void writePayload(FriendlyByteBuf buffer) {
      super.writePayload(buffer);
      buffer.writeByte(this.filterMode.ordinal());
      buffer.writeByte(this.currentFilter);
      buffer.writeBoolean(this.filterValid);
   }

   @Override
   public void readPayload(FriendlyByteBuf buffer, boolean isClientSide) {
      super.readPayload(buffer, isClientSide);
      this.filterMode = PipeBehaviourWoodDiamond.FilterMode.get(buffer.readUnsignedByte());
      this.currentFilter = buffer.readUnsignedByte() % this.filters.getSlots();
      this.filterValid = buffer.readBoolean();
   }

   @Override
   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      if (isHoldingWrench(player)) {
         return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
      }

      ItemStack held = player.getMainHandItem();
      if (!held.isEmpty() && held.getItem() instanceof IItemPluggable) {
         return false;
      }

      if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
         PipeFilterMenus.open(
            serverPlayer,
            this.pipe.getHolder(),
            Component.translatable("gui.buildcraft.pipe_diamond_wood.title"),
            ContainerDiamondWoodPipe::new
         );
      }

      return true;
   }

   private IStackFilter getStackFilter() {
      switch (this.filterMode) {
         case WHITE_LIST:
         default:
            if (!this.hasAnyFilter()) {
               return stack -> true;
            }

            return stack -> {
               for (int i = 0; i < this.filters.getSlots(); i++) {
                  ItemStack filter = this.filters.getStackInSlot(i);
                  if (!filter.isEmpty() && StackUtil.isMatchingItemOrList(filter, stack)) {
                     return true;
                  }
               }

               return false;
            };
         case BLACK_LIST:
            return stack -> {
               for (int i = 0; i < this.filters.getSlots(); i++) {
                  ItemStack filter = this.filters.getStackInSlot(i);
                  if (!filter.isEmpty() && StackUtil.isMatchingItemOrList(filter, stack)) {
                     return false;
                  }
               }

               return true;
            };
         case ROUND_ROBIN:
            return comparison -> {
               ItemStack filter = this.filters.getStackInSlot(this.currentFilter);
               return StackUtil.isMatchingItemOrList(filter, comparison);
            };
      }
   }

   @Override
   protected int extractItems(IFlowItems flow, Direction dir, int count, boolean simulate) {
      if (this.filters.getStackInSlot(this.currentFilter).isEmpty()) {
         this.advanceFilter();
      }

      int extracted = flow.tryExtractItems(1, this.getCurrentDir(), null, this.getStackFilter(), simulate);
      if (extracted > 0 && this.filterMode == PipeBehaviourWoodDiamond.FilterMode.ROUND_ROBIN && !simulate) {
         this.advanceFilter();
      }

      return extracted;
   }

   private void advanceFilter() {
      int lastFilter = this.currentFilter;
      this.filterValid = false;

      do {
         this.currentFilter++;
         if (this.currentFilter >= this.filters.getSlots()) {
            this.currentFilter = 0;
         }

         if (!this.filters.getStackInSlot(this.currentFilter).isEmpty()) {
            this.filterValid = true;
            break;
         }
      } while (this.currentFilter != lastFilter);

      if (lastFilter != this.currentFilter) {
         this.pipe.getHolder().scheduleNetworkGuiUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
      }
   }

   private boolean hasAnyFilter() {
      for (int i = 0; i < this.filters.getSlots(); i++) {
         if (!this.filters.getStackInSlot(i).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public enum FilterMode {
      WHITE_LIST,
      BLACK_LIST,
      ROUND_ROBIN;

      public static PipeBehaviourWoodDiamond.FilterMode get(int index) {
         return index >= 0 && index < values().length ? values()[index] : WHITE_LIST;
      }
   }
}
