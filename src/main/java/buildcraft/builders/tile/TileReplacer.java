/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.container.ContainerReplacer;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.Date;
import org.jspecify.annotations.Nullable;
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

public class TileReplacer extends BcBlockEntity implements MenuProvider, BlockEntityExtendedMenu {
   public final ItemHandlerSimple invSnapshot = this.itemManager
      .addInvHandler(
         "snapshot",
         1,
         (slot, stack) -> stack.getItem() instanceof ItemSnapshot snap && snap.isUsed() && snap.getSnapshotType() == EnumSnapshotType.BLUEPRINT,
         ItemHandlerManager.EnumAccess.NONE
      );
   public final ItemHandlerSimple invSchematicFrom = this.itemManager
      .addInvHandler("schematicFrom", 1, (slot, stack) -> stack.getItem() instanceof ItemSchematicSingle s && s.isUsed(), ItemHandlerManager.EnumAccess.NONE);
   public final ItemHandlerSimple invSchematicTo = this.itemManager
      .addInvHandler("schematicTo", 1, (slot, stack) -> stack.getItem() instanceof ItemSchematicSingle s && s.isUsed(), ItemHandlerManager.EnumAccess.NONE);

   public TileReplacer(BlockPos pos, BlockState state) {
      super(BCBuildersBlockEntities.REPLACER, pos, state);
   }

   public void doReplace(@Nullable String newName) {
      if (this.level != null && !this.level.isClientSide()) {
         if (!this.invSnapshot.getStackInSlot(0).isEmpty()
            && !this.invSchematicFrom.getStackInSlot(0).isEmpty()
            && !this.invSchematicTo.getStackInSlot(0).isEmpty()) {
            Snapshot.Header header = ItemSnapshot.getHeader(this.invSnapshot.getStackInSlot(0));
            if (header != null) {
               if (GlobalSavedDataSnapshots.get(this.level).getSnapshot(header.key) instanceof Blueprint blueprint) {
                  try {
                     ISchematicBlock from = ItemSchematicSingle.getSchematic(this.invSchematicFrom.getStackInSlot(0));
                     ISchematicBlock to = ItemSchematicSingle.getSchematic(this.invSchematicTo.getStackInSlot(0));
                     if (from == null || to == null) {
                        return;
                     }

                     Blueprint newBlueprint = blueprint.copy();
                     newBlueprint.replace(from, to);
                     newBlueprint.computeKey();
                     GlobalSavedDataSnapshots.get(this.level).addSnapshot(newBlueprint);
                     String resolvedName = newName != null && !newName.isBlank() ? newName.trim() : header.name;
                     ItemSnapshot usedItem = BCBuildersItems.BLUEPRINT_USED;
                     Snapshot.Header newHeader = new Snapshot.Header(newBlueprint.key, header.owner, new Date(), resolvedName);
                     this.invSnapshot.setStackInSlot(0, usedItem.createUsedStack(newHeader));
                     this.setChanged();
                  } catch (InvalidInputDataException e) {
                     BCLog.logger.warn("[builders.replacer] Invalid replacer blueprint data", e);
                  }
               }
            }
         }
      }
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

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftbuilders.replacer");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerReplacer(containerId, playerInv, this);
   }
}
