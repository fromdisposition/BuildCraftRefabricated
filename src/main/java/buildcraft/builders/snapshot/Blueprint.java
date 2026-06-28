/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.NBTUtilBC;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;

public class Blueprint extends Snapshot {
   public final List<ISchematicBlock> palette = new ArrayList<>();
   public int[] data;
   public final List<ISchematicEntity> entities = new ArrayList<>();

   public Blueprint copy() {
      Blueprint blueprint = new Blueprint();
      blueprint.size = this.size;
      blueprint.facing = this.facing;
      blueprint.offset = this.offset;
      blueprint.palette.addAll(this.palette);
      blueprint.data = (int[])this.data.clone();
      blueprint.entities.addAll(this.entities);
      blueprint.computeKey();
      return blueprint;
   }

   public void replace(ISchematicBlock from, ISchematicBlock to) {
      if (from != null && to != null) {
         for (int i = 0; i < this.palette.size(); i++) {
            if (schematicsMatch(this.palette.get(i), from)) {
               this.palette.set(i, to);
            }
         }
      }
   }

   public int countMatchingPaletteEntries(ISchematicBlock from) {
      if (from == null) {
         return 0;
      }

      int n = 0;

      for (ISchematicBlock entry : this.palette) {
         if (schematicsMatch(entry, from)) {
            n++;
         }
      }

      return n;
   }

   public int countMatchingCells(ISchematicBlock from) {
      if (from != null && this.data != null) {
         BitSet matchingIndices = new BitSet(this.palette.size());

         for (int i = 0; i < this.palette.size(); i++) {
            if (schematicsMatch(this.palette.get(i), from)) {
               matchingIndices.set(i);
            }
         }

         if (matchingIndices.isEmpty()) {
            return 0;
         }

         int n = 0;

         for (int cell : this.data) {
            if (cell >= 0 && cell < this.palette.size() && matchingIndices.get(cell)) {
               n++;
            }
         }

         return n;
      } else {
         return 0;
      }
   }

   private static boolean schematicsMatch(ISchematicBlock a, ISchematicBlock b) {
      if (a == b) {
         return true;
      } else if (a != null && b != null) {
         return a instanceof SchematicBlockDefault ad && b instanceof SchematicBlockDefault bd
            ? Objects.equals(ad.blockState, bd.blockState) && Objects.equals(ad.placeBlock, bd.placeBlock)
            : a.equals(b);
      } else {
         return false;
      }
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = super.serializeNBT();
      nbt.put("palette", NBTUtilBC.writeCompoundList(this.palette.stream().map(SchematicBlockManager::writeToNBT)));
      ListTag list = new ListTag();

      for (int z = 0; z < this.size.getZ(); z++) {
         for (int y = 0; y < this.size.getY(); y++) {
            for (int x = 0; x < this.size.getX(); x++) {
               list.add(IntTag.valueOf(this.data[this.posToIndex(x, y, z)]));
            }
         }
      }

      nbt.put("data", list);
      nbt.put("entities", NBTUtilBC.writeCompoundList(this.entities.stream().map(SchematicEntityManager::writeToNBT)));
      return nbt;
   }

   @Override
   public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
      super.deserializeNBT(nbt);
      this.palette.clear();

      for (CompoundTag schematicBlockTag : NBTUtilBC.readCompoundList(nbt.get("palette")).collect(Collectors.toList())) {
         this.palette.add(SchematicBlockManager.readFromNBT(schematicBlockTag));
      }

      this.data = new int[Snapshot.getDataSize(this.size)];
      ListTag serializedDataList = nbt.get("data") instanceof ListTag lt ? lt : null;
      int[] serializedDataIntArray = (int[])BcNbt.getIntArray(nbt, "data");
      if (serializedDataIntArray == null && serializedDataList == null) {
         throw new InvalidInputDataException("Can't read a blueprint with no data!");
      }

      int serializedDataLength = serializedDataList == null ? serializedDataIntArray.length : serializedDataList.size();
      if (serializedDataLength != this.getDataSize()) {
         throw new InvalidInputDataException(
            "Serialized data has length of " + serializedDataLength + ", but we expected " + this.getDataSize() + " (" + this.size.toString() + ")"
         );
      }

      for (int z = 0; z < this.size.getZ(); z++) {
         for (int y = 0; y < this.size.getY(); y++) {
            for (int x = 0; x < this.size.getX(); x++) {
               int idx = this.posToIndex(x, y, z);
               if (serializedDataList != null) {
                  //? if >= 1.21.10 {
                  this.data[idx] = serializedDataList.get(idx) instanceof IntTag it ? it.value() : 0;
                  //?} else {
                  /*this.data[idx] = serializedDataList.get(idx) instanceof IntTag it ? it.getAsInt() : 0;
                  *///?}
               } else {
                  this.data[idx] = serializedDataIntArray[idx];
               }
            }
         }
      }

      this.entities.clear();

      for (CompoundTag schematicEntityTag : NBTUtilBC.readCompoundList(nbt.get("entities")).collect(Collectors.toList())) {
         this.entities.add(SchematicEntityManager.readFromNBT(schematicEntityTag));
      }
   }

   @Override
   public EnumSnapshotType getType() {
      return EnumSnapshotType.BLUEPRINT;
   }

   @Override
   public int countNonAirCells() {
      if (this.data != null && !this.palette.isEmpty()) {
         BitSet airPaletteIndices = new BitSet(this.palette.size());

         for (int i = 0; i < this.palette.size(); i++) {
            ISchematicBlock entry = this.palette.get(i);
            if (entry != null && entry.isAir()) {
               airPaletteIndices.set(i);
            }
         }

         int n = 0;

         for (int cell : this.data) {
            if (cell >= 0 && cell < this.palette.size() && !airPaletteIndices.get(cell)) {
               n++;
            }
         }

         return n;
      } else {
         return 0;
      }
   }

   @SuppressWarnings("unchecked")
   public class BuildingInfo extends Snapshot.BuildingInfo {
      public final List<ItemStack>[] toPlaceRequiredItems = new List[Blueprint.this.getDataSize()];
      public final List<FluidStack>[] toPlaceRequiredFluids = new List[Blueprint.this.getDataSize()];
      public final List<ISchematicBlock> rotatedPalette;
      public final Set<ISchematicEntity> entities;
      public final Map<ISchematicEntity, List<ItemStack>> entitiesRequiredItems;
      public final Map<ISchematicEntity, List<FluidStack>> entitiesRequiredFluids;

      public BuildingInfo(BlockPos basePos, Rotation rotation) {
         super(basePos, rotation);
         this.rotatedPalette = ImmutableList.copyOf(
            Blueprint.this.palette.stream().map(schematicBlockx -> schematicBlockx.getRotated(rotation)).collect(Collectors.toList())
         );

         for (int z = 0; z < this.getSnapshot().size.getZ(); z++) {
            for (int y = 0; y < this.getSnapshot().size.getY(); y++) {
               for (int x = 0; x < this.getSnapshot().size.getX(); x++) {
                  ISchematicBlock schematicBlock = this.rotatedPalette.get(Blueprint.this.data[Blueprint.this.posToIndex(x, y, z)]);
                  if (!schematicBlock.isAir()) {
                     this.toPlaceRequiredItems[Blueprint.this.posToIndex(x, y, z)] = schematicBlock.computeRequiredItems();
                     this.toPlaceRequiredFluids[Blueprint.this.posToIndex(x, y, z)] = schematicBlock.computeRequiredFluids();
                  }
               }
            }
         }

         Builder<ISchematicEntity> entitiesBuilder = ImmutableSet.builder();
         com.google.common.collect.ImmutableMap.Builder<ISchematicEntity, List<ItemStack>> entitiesRequiredItemsBuilder = ImmutableMap.builder();
         com.google.common.collect.ImmutableMap.Builder<ISchematicEntity, List<FluidStack>> entitiesRequiredFluidsBuilder = ImmutableMap.builder();

         for (ISchematicEntity schematicEntity : this.getSnapshot().entities) {
            ISchematicEntity rotatedSchematicEntity = schematicEntity.getRotated(rotation);
            entitiesBuilder.add(rotatedSchematicEntity);
            entitiesRequiredItemsBuilder.put(rotatedSchematicEntity, schematicEntity.computeRequiredItems());
            entitiesRequiredFluidsBuilder.put(rotatedSchematicEntity, schematicEntity.computeRequiredFluids());
         }

         this.entities = entitiesBuilder.build();
         this.entitiesRequiredItems = entitiesRequiredItemsBuilder.build();
         this.entitiesRequiredFluids = entitiesRequiredFluidsBuilder.build();
      }

      public Blueprint getSnapshot() {
         return Blueprint.this;
      }

      public void refreshRequiredItemsForContentsMode(EnumContainerContentsMode mode) {
         boolean include = mode != EnumContainerContentsMode.IGNORE;

         for (int z = 0; z < this.getSnapshot().size.getZ(); z++) {
            for (int y = 0; y < this.getSnapshot().size.getY(); y++) {
               for (int x = 0; x < this.getSnapshot().size.getX(); x++) {
                  int idx = Blueprint.this.posToIndex(x, y, z);
                  ISchematicBlock schematicBlock = this.rotatedPalette.get(Blueprint.this.data[idx]);
                  if (!schematicBlock.isAir()) {
                     if (schematicBlock instanceof SchematicBlockDefault def) {
                        this.toPlaceRequiredItems[idx] = def.computeRequiredItems(include);
                     } else {
                        this.toPlaceRequiredItems[idx] = schematicBlock.computeRequiredItems();
                     }
                  }
               }
            }
         }
      }
   }
}
