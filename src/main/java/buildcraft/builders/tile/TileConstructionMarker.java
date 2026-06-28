/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.ConstructionMarkerRegistry;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.tile.BcBlockEntity;
import java.util.Arrays;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;

public class TileConstructionMarker extends BcBlockEntity {
   private ItemStack blueprintStack = ItemStack.EMPTY;
   @Nullable
   private Snapshot snapshot;
   @Nullable
   private Blueprint.BuildingInfo blueprintBuildingInfo;
   @Nullable
   private Rotation rotation;

   public TileConstructionMarker(BlockPos pos, BlockState state) {
      super(BCBuildersBlockEntities.CONSTRUCTION_MARKER, pos, state);
   }

   public ItemStack getBlueprintStack() {
      return this.blueprintStack;
   }

   public boolean hasBlueprint() {
      return this.blueprintBuildingInfo != null;
   }

   @Nullable
   public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
      return this.blueprintBuildingInfo;
   }

   public void setBlueprint(ItemStack stack) {
      this.blueprintStack = stack == null ? ItemStack.EMPTY : stack;
      this.recomputeBuildingInfo();
      this.setChanged();
   }

   public ItemStack removeBlueprint() {
      ItemStack removed = this.blueprintStack;
      this.blueprintStack = ItemStack.EMPTY;
      this.snapshot = null;
      this.blueprintBuildingInfo = null;
      this.rotation = null;
      if (this.level != null) {
         ConstructionMarkerRegistry.unregister(this.level, this.worldPosition);
      }

      this.setChanged();
      return removed;
   }

   
   public void markBuilt() {
      this.blueprintBuildingInfo = null;
      if (this.level != null) {
         ConstructionMarkerRegistry.unregister(this.level, this.worldPosition);
      }

      this.setChanged();
   }

   private void recomputeBuildingInfo() {
      this.snapshot = null;
      this.blueprintBuildingInfo = null;
      this.rotation = null;
      if (this.level != null && !this.level.isClientSide() && this.blueprintStack.getItem() instanceof ItemSnapshot) {
         Snapshot.Header header = ItemSnapshot.getHeader(this.blueprintStack);
         if (header != null) {
            Snapshot resolved = GlobalSavedDataSnapshots.get(this.level).getSnapshot(header.key);
            if (resolved instanceof Blueprint blueprint) {
               this.snapshot = blueprint;
               this.rotation = Arrays.stream(Rotation.values())
                  .filter(r -> r.rotate(blueprint.facing) == this.getBlockState().getValue(HorizontalDirectionalBlock.FACING))
                  .findFirst()
                  .orElse(Rotation.NONE);
               this.blueprintBuildingInfo = blueprint.new BuildingInfo(this.worldPosition, this.rotation);
            }
         }
      }

      if (this.level != null) {
         if (this.blueprintBuildingInfo != null) {
            ConstructionMarkerRegistry.register(this.level, this.worldPosition);
         } else {
            ConstructionMarkerRegistry.unregister(this.level, this.worldPosition);
         }
      }
   }

   @Override
   public void clearRemoved() {
      super.clearRemoved();
      if (!this.blueprintStack.isEmpty() && this.blueprintBuildingInfo == null) {
         this.recomputeBuildingInfo();
      }
   }

   @Override
   public void setRemoved() {
      super.setRemoved();
      if (this.level != null) {
         ConstructionMarkerRegistry.unregister(this.level, this.worldPosition);
      }
   }

   @Override
   protected void writeData(BcValueOut output) {
      super.writeData(output);
      if (!this.blueprintStack.isEmpty()) {
         output.store("blueprint", ItemStack.CODEC, this.blueprintStack);
      }
   }

   @Override
   public void readData(BcValueIn input) {
      super.readData(input);
      this.blueprintStack = input.read("blueprint", ItemStack.CODEC).orElse(ItemStack.EMPTY);
      this.recomputeBuildingInfo();
   }
}
