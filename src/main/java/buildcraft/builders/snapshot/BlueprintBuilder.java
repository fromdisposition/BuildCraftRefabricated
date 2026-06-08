/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
   private static final double MAX_ENTITY_DISTANCE = 0.1;
   private static final String FLUID_STACK_KEY = "BuilderFluidStack";
   private List<ItemStack>[] remainingDisplayRequiredBlocks;
   private List<ItemStack> remainingDisplayRequiredBlocksConcat = Collections.emptyList();
   public List<ItemStack> remainingDisplayRequired = new ArrayList<>();
   private final Map<Pair<List<ItemStack>, List<FluidStack>>, Optional<List<ItemStack>>> extractRequiredCache = new HashMap<>();

   public BlueprintBuilder(ITileForBlueprintBuilder tile) {
      super(tile);
   }

   private ISchematicBlock getSchematicBlock(BlockPos blockPos) {
      return this.getBuildingInfo().box.contains(blockPos)
         ? this.getBuildingInfo()
            .rotatedPalette
            .get(this.getBuildingInfo().getSnapshot().data[this.getBuildingInfo().getSnapshot().posToIndex(this.getBuildingInfo().fromWorld(blockPos))])
         : null;
   }

   @Override
   protected boolean isAir(BlockPos blockPos) {
      ISchematicBlock schematic = this.getSchematicBlock(blockPos);
      if (schematic == null) {
         return true;
      } else {
         return schematic.isAir() ? true : schematic instanceof SchematicBlockFluid && this.tile.getFluidMode() == EnumFluidHandlingMode.CLEAR;
      }
   }

   protected Blueprint.BuildingInfo getBuildingInfo() {
      return this.tile.getBlueprintBuildingInfo();
   }

   @Override
   @SuppressWarnings("unchecked")
   public void updateSnapshot() {
      super.updateSnapshot();
      this.remainingDisplayRequiredBlocks = new List[this.getBuildingInfo().getSnapshot().getDataSize()];
      Arrays.fill(this.remainingDisplayRequiredBlocks, Collections.emptyList());
   }

   @Override
   public void resourcesChanged() {
      super.resourcesChanged();
      this.extractRequiredCache.clear();
   }

   public void refreshDisplayForContentsMode() {
      if (this.remainingDisplayRequiredBlocks != null) {
         if (this.checkResults != null) {
            if (this.getBuildingInfo() != null) {
               for (int i = 0; i < this.remainingDisplayRequiredBlocks.length; i++) {
                  if (this.checkResults[i] == 3) {
                     this.remainingDisplayRequiredBlocks[i] = this.getDisplayRequired(
                           this.getBuildingInfo().toPlaceRequiredItems[i], this.getBuildingInfo().toPlaceRequiredFluids[i]
                        )
                        .collect(Collectors.toList());
                  } else {
                     this.remainingDisplayRequiredBlocks[i] = Collections.emptyList();
                  }
               }

               this.afterChecks();
            }
         }
      }
   }

   @Override
   public void cancel() {
      super.cancel();
      this.remainingDisplayRequiredBlocks = null;
   }

   private Stream<ItemStack> getDisplayRequired(List<ItemStack> requiredItems, List<FluidStack> requiredFluids) {
      return Stream.concat(
         requiredItems == null ? Stream.empty() : requiredItems.stream(),
         requiredFluids == null ? Stream.empty() : requiredFluids.stream().map(fluidStack -> FluidUtilBC.getFilledBucket(fluidStack))
      );
   }

   private Optional<List<ItemStack>> tryExtractRequired(List<ItemStack> requiredItems, List<FluidStack> requiredFluids, boolean simulate) {
      Supplier<Optional<List<ItemStack>>> function = () -> StackUtil.mergeSameItems(requiredItems)
               .stream()
               .noneMatch(
                  stack -> this.tile
                     .getInvResources()
                     .extract(extracted -> StackUtil.canMerge(stack, extracted), stack.getCount(), stack.getCount(), true)
                     .isEmpty()
               )
            && FluidUtilBC.mergeSameFluids(requiredFluids)
               .stream()
               .allMatch(stack -> this.tile.getFluidTanks().extractMillibuckets(stack, stack.getAmount(), false) == stack.getAmount())
         ? Optional.of(
            StackUtil.mergeSameItems(
               Stream.concat(
                     requiredItems.stream()
                        .map(
                           stack -> this.tile
                              .getInvResources()
                              .extract(extracted -> StackUtil.canMerge(stack, extracted), stack.getCount(), stack.getCount(), simulate)
                        ),
                     FluidUtilBC.mergeSameFluids(requiredFluids).stream().map(fluidStack -> {
                        int extracted = this.tile.getFluidTanks().extractMillibuckets(fluidStack, fluidStack.getAmount(), !simulate);
                        return new FluidStack(fluidStack.getFluid(), extracted);
                     }).map(fluidStack -> {
                        ItemStack stack = FluidUtilBC.getFilledBucket(fluidStack);
                        CompoundTag fluidTag = new CompoundTag();
                        Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluidStack.getFluid());
                        if (fluidId != null) {
                           fluidTag.putString("fluid", fluidId.toString());
                           fluidTag.putInt("amount", fluidStack.getAmount());
                        }

                        CompoundTag wrapper = new CompoundTag();
                        wrapper.put("BuilderFluidStack", fluidTag);
                        if (!stack.isEmpty()) {
                           stack.set(DataComponents.CUSTOM_DATA, CustomData.of(wrapper));
                        }

                        return stack;
                     })
                  )
                  .collect(Collectors.toList())
            )
         )
         : Optional.empty();
      return !simulate ? function.get() : this.extractRequiredCache.computeIfAbsent(Pair.of(requiredItems, requiredFluids), pair -> function.get());
   }

   @Override
   protected boolean canPlace(BlockPos blockPos) {
      if (this.isAir(blockPos)) {
         return false;
      }

      EnumFluidHandlingMode mode = this.tile.getFluidMode();
      boolean hasFluid = !this.tile.getWorldBC().getFluidState(blockPos).isEmpty();
      if (hasFluid) {
         if (mode == EnumFluidHandlingMode.CLEAR) {
            return false;
         }

         if (mode == EnumFluidHandlingMode.REPLACE) {
            return true;
         }
      }

      return this.getSchematicBlock(blockPos).canBuild(this.tile.getWorldBC(), blockPos);
   }

   @Override
   protected boolean isReadyToPlace(BlockPos blockPos) {
      ISchematicBlock self = this.getSchematicBlock(blockPos);
      boolean selfIsFluid = self instanceof SchematicBlockFluid;
      boolean dependenciesMet = self.getRequiredBlockOffsets().stream().<BlockPos>map(blockPos::offset).allMatch(pos -> {
         ISchematicBlock neighbour = this.getSchematicBlock(pos);
         if (neighbour == null) {
            return true;
         } else {
            return this.checkResults[this.posToIndex(pos)] == 1 ? true : selfIsFluid && neighbour instanceof SchematicBlockFluid;
         }
      }) && self.isReadyToBuild(this.tile.getWorldBC(), blockPos);
      return !dependenciesMet
         ? false
         : !(self instanceof SchematicBlockDefault def && def.blockState != null && !def.blockState.canSurvive(this.tile.getWorldBC(), blockPos));
   }

   private boolean isWaterlogClearOnlyAt(BlockPos blockPos) {
      return this.getSchematicBlock(blockPos) instanceof SchematicBlockDefault def
         ? def.isWaterlogClearOnly(this.tile.getWorldBC(), blockPos, this.tile.getFluidMode())
         : false;
   }

   @Override
   protected boolean isAllowedDuringFluidMop(BlockPos blockPos) {
      return this.isWaterlogClearOnlyAt(blockPos);
   }

   @Override
   protected boolean isFragileSchematicAt(BlockPos blockPos) {
      if (this.getSchematicBlock(blockPos) instanceof SchematicBlockDefault def) {
         return def.blockState == null ? false : def.blockState.canBeReplaced(Fluids.WATER);
      } else {
         return false;
      }
   }

   @Override
   protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
      return this.isWaterlogClearOnlyAt(blockPos)
         ? true
         : this.tryExtractRequired(
               this.getBuildingInfo().toPlaceRequiredItems[this.posToIndex(blockPos)],
               this.getBuildingInfo().toPlaceRequiredFluids[this.posToIndex(blockPos)],
               true
            )
            .isPresent();
   }

   @Override
   protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
      return this.isWaterlogClearOnlyAt(blockPos)
         ? Collections.emptyList()
         : this.tryExtractRequired(
               this.getBuildingInfo().toPlaceRequiredItems[this.posToIndex(blockPos)],
               this.getBuildingInfo().toPlaceRequiredFluids[this.posToIndex(blockPos)],
               false
            )
            .orElse(null);
   }

   @Override
   protected void cancelPlaceTask(SnapshotBuilder<ITileForBlueprintBuilder>.PlaceTask placeTask) {
      super.cancelPlaceTask(placeTask);
      placeTask.items.stream().filter(stack -> {
         CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
         return customData == null || !customData.copyTag().contains("BuilderFluidStack");
      }).forEach(stack -> this.tile.getInvResources().insert(stack, false, false));
      placeTask.items
         .stream()
         .filter(stack -> {
            CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
            return customData != null && customData.copyTag().contains("BuilderFluidStack");
         })
         .map(stack -> {
            CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
            CompoundTag fluidTag = customData.copyTag().getCompoundOrEmpty("BuilderFluidStack");
            if (fluidTag.isEmpty()) {
               return FluidStack.EMPTY;
            }

            String fluidIdStr = fluidTag.getString("fluid").orElse("");
            int amount = fluidTag.getInt("amount").orElse(0);
            if (!fluidIdStr.isEmpty() && amount > 0) {
               Identifier id = Identifier.tryParse(fluidIdStr);
               if (id == null) {
                  return FluidStack.EMPTY;
               }

               Fluid fluid = (Fluid)BuiltInRegistries.FLUID.getValue(id);
               return fluid != null && fluid != Fluids.EMPTY ? new FluidStack(fluid, amount) : FluidStack.EMPTY;
            } else {
               return FluidStack.EMPTY;
            }
         })
         .filter(fluidStack -> !fluidStack.isEmpty() && fluidStack.getAmount() > 0)
         .forEach(fluidStack -> this.tile.getFluidTanks().insertMillibuckets(fluidStack, fluidStack.getAmount(), true));
   }

   @Override
   protected boolean isBlockCorrect(BlockPos blockPos) {
      if (this.getBuildingInfo() == null) {
         return false;
      } else {
         ISchematicBlock schematic = this.getSchematicBlock(blockPos);
         if (schematic == null) {
            return false;
         } else {
            return schematic instanceof SchematicBlockDefault def
               ? def.isBuilt(this.tile.getWorldBC(), blockPos, this.tile.getFluidMode())
               : schematic.isBuilt(this.tile.getWorldBC(), blockPos);
         }
      }
   }

   @Override
   protected boolean doPlaceTask(SnapshotBuilder<ITileForBlueprintBuilder>.PlaceTask placeTask) {
      if (this.getBuildingInfo() == null) {
         return false;
      } else {
         ISchematicBlock schematic = this.getSchematicBlock(placeTask.pos);
         if (schematic == null) {
            return false;
         } else {
            ServerLevel level = (ServerLevel)this.tile.getWorldBC();
            GameProfile owner = this.tile.getOwner();
            BlockPos machineOrigin = this.tile.getBuilderPos();
            if (!BlockUtil.canMachinePlace(level, placeTask.pos, owner, machineOrigin)) {
               return false;
            } else if (schematic instanceof SchematicBlockDefault def) {
               boolean includeContents = this.tile.getContainerContentsMode() != EnumContainerContentsMode.IGNORE;
               return def.build(level, placeTask.pos, this.tile.getFluidMode(), includeContents, owner, machineOrigin);
            } else {
               return schematic.build(this.tile.getWorldBC(), placeTask.pos);
            }
         }
      }
   }

   @Override
   public boolean tick() {
      if (this.tile.getWorldBC().isClientSide()) {
         return super.tick();
      }

      List<Entity> entitiesWithinBox = this.tile.getWorldBC().getEntities((Entity)null, this.getBuildingInfo().box.getBoundingBox(), Objects::nonNull);
      List<ISchematicEntity> toSpawn = new ArrayList<>();
      double maxDistSq = 0.010000000000000002;

      for (ISchematicEntity schematicEntity : this.getBuildingInfo().entities) {
         boolean found = false;
         Vec3 targetPos = schematicEntity.getPos().add(Vec3.atLowerCornerOf(this.getBuildingInfo().offsetPos));

         for (Entity entity : entitiesWithinBox) {
            if (entity.position().distanceToSqr(targetPos) < maxDistSq) {
               found = true;
               break;
            }
         }

         if (!found) {
            toSpawn.add(schematicEntity);
         }
      }

      this.remainingDisplayRequired.clear();
      List<ItemStack> displayRequiredConcat = new ArrayList<>(this.remainingDisplayRequiredBlocksConcat);

      for (ISchematicEntity schematicEntity : toSpawn) {
         this.getDisplayRequired(
               this.getBuildingInfo().entitiesRequiredItems.get(schematicEntity), this.getBuildingInfo().entitiesRequiredFluids.get(schematicEntity)
            )
            .forEach(displayRequiredConcat::add);
      }

      this.remainingDisplayRequired.addAll(StackUtil.mergeSameItems(displayRequiredConcat));
      List<Entity> toKill = new ArrayList<>();

      for (Entity entity : entitiesWithinBox) {
         if (entity != null) {
            boolean foundClose = false;

            for (ISchematicEntity schematicEntity : this.getBuildingInfo().entities) {
               Vec3 pos = schematicEntity.getPos().add(Vec3.atLowerCornerOf(this.getBuildingInfo().offsetPos));
               if (entity.position().distanceToSqr(pos) < maxDistSq) {
                  foundClose = true;
                  break;
               }
            }

            if (!foundClose && SchematicEntityManager.getSchematicEntity(new SchematicEntityContext(this.tile.getWorldBC(), BlockPos.ZERO, entity)) != null) {
               toKill.add(entity);
            }
         }
      }

      if (!toKill.isEmpty()) {
         if (!this.tile.getBattery().isFull()) {
            return false;
         }

         toKill.forEach(Entity::discard);
      }

      if (!super.tick()) {
         return false;
      }

      if (!toSpawn.isEmpty()) {
         if (!this.tile.getBattery().isFull()) {
            return false;
         }

         for (ISchematicEntity schematicEntity : toSpawn) {
            if (this.tryExtractRequired(
                     this.getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                     this.getBuildingInfo().entitiesRequiredFluids.get(schematicEntity),
                     true
                  )
                  .isPresent()
               && schematicEntity.build(this.tile.getWorldBC(), this.getBuildingInfo().offsetPos) != null) {
               this.tryExtractRequired(
                  this.getBuildingInfo().entitiesRequiredItems.get(schematicEntity), this.getBuildingInfo().entitiesRequiredFluids.get(schematicEntity), false
               );
            }
         }
      }

      return true;
   }

   @Override
   protected boolean check(BlockPos blockPos) {
      if (super.check(blockPos)) {
         this.remainingDisplayRequiredBlocks[this.posToIndex(blockPos)] = this.checkResults[this.posToIndex(blockPos)] != 1
            ? this.getDisplayRequired(
                  this.getBuildingInfo().toPlaceRequiredItems[this.posToIndex(blockPos)],
                  this.getBuildingInfo().toPlaceRequiredFluids[this.posToIndex(blockPos)]
               )
               .collect(Collectors.toList())
            : Collections.emptyList();
         return true;
      } else {
         return false;
      }
   }

   @Override
   protected void afterChecks() {
      this.remainingDisplayRequiredBlocksConcat = StackUtil.mergeSameItems(
         Arrays.stream(this.remainingDisplayRequiredBlocks).flatMap(Collection::stream).collect(Collectors.toList())
      );
   }

   @Override
   public void writeToByteBuf(FriendlyByteBuf buffer) {
      super.writeToByteBuf(buffer);
      buffer.writeInt(this.remainingDisplayRequired.size());
      this.remainingDisplayRequired.forEach(stack -> {
         CompoundTag tag = new CompoundTag();
         if (!stack.isEmpty()) {
            ItemStack.CODEC.encodeStart(NBTUtilBC.registryAwareOps(), stack.copyWithCount(1)).resultOrPartial().ifPresent(payload -> tag.put("stack", payload));
         }

         buffer.writeNbt(tag);
         buffer.writeInt(stack.getCount());
      });
   }

   @Override
   public void readFromByteBuf(FriendlyByteBuf buffer) {
      super.readFromByteBuf(buffer);
      this.remainingDisplayRequired.clear();
      IntStream.range(0, buffer.readInt())
         .mapToObj(
            i -> {
               CompoundTag tag = buffer.readNbt();
               Tag payload = tag == null ? null : tag.get("stack");
               ItemStack stack = payload == null
                  ? ItemStack.EMPTY
                  : ItemStack.CODEC.parse(NBTUtilBC.registryAwareOps(), payload).resultOrPartial().orElse(ItemStack.EMPTY);
               int count = buffer.readInt();
               return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(count);
            }
         )
         .forEach(this.remainingDisplayRequired::add);
   }
}
