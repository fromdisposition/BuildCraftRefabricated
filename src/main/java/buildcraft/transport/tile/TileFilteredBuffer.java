/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerFiltered;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;

public class TileFilteredBuffer extends BcBlockEntity implements MenuProvider, BlockEntityExtendedMenu {
   public final ItemHandlerSimple invFilter = this.itemManager.addInvHandler("filter", 9, ItemHandlerManager.EnumAccess.PHANTOM);
   public final ItemHandlerFiltered invMain;

   public TileFilteredBuffer(BlockPos pos, BlockState state) {
      super(BCTransportBlockEntities.FILTERED_BUFFER, pos, state);
      this.invFilter.setLimitedInsertor(1);
      this.invMain = new ItemHandlerFiltered(this.invFilter, false);
      this.itemManager.addInvHandler("main", this.invMain, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcrafttransport.filtered_buffer");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerFilteredBuffer_BC8(containerId, playerInv, this);
   }

   @Override
   protected void writeData(BcValueOut output) {
      super.writeData(output);
      output.store("items", CompoundTag.CODEC, this.itemManager.serializeNBT());
   }

   @Override
   public void readData(BcValueIn input) {
      super.readData(input);
      input.read("items", CompoundTag.CODEC).ifPresent(this.itemManager::deserializeNBT);
   }
}
