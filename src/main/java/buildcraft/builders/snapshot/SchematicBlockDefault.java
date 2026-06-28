/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.lib.nbt.BcEntityNbt;
import buildcraft.lib.fabric.BcRegistryUtil;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.NBTUtilBC;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class SchematicBlockDefault implements ISchematicBlock {
   private static final Direction[] FRAGILE_FLUID_NEIGHBOUR_DIRS = new Direction[]{
      Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP
   };
   protected final Set<BlockPos> requiredBlockOffsets = new HashSet<>();
   protected BlockState blockState;
   protected final List<Property<?>> ignoredProperties = new ArrayList<>();
   protected CompoundTag tileNbt;
   protected Rotation tileRotation = Rotation.NONE;
   protected Block placeBlock;
   protected final Set<BlockPos> updateBlockOffsets = new HashSet<>();
   protected final Set<Block> canBeReplacedWithBlocks = new HashSet<>();

   public static boolean predicate(SchematicBlockContext context) {
      if (context.blockState.isAir()) {
         return false;
      }

      Identifier registryName = BuiltInRegistries.BLOCK.getKey(context.block);
      if (registryName == null) {
         return false;
      }

      if (!RulesLoader.READ_DOMAINS.contains(registryName.getNamespace())) {
         return false;
      }

      BlockEntity be = context.world.getBlockEntity(context.pos);
      CompoundTag beNbt = be != null ? be.saveWithoutMetadata(context.world.registryAccess()) : null;
      return RulesLoader.getRules(context.blockState, beNbt).stream().noneMatch(rule -> rule.ignore);
   }

   protected void setRequiredBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
      this.requiredBlockOffsets.clear();
      rules.stream().map(rule -> rule.requiredBlockOffsets).filter(Objects::nonNull).flatMap(Collection::stream).forEach(this.requiredBlockOffsets::add);
      this.addClassBasedRequiredBlockOffsets(context.block, context.blockState);
   }

   protected void addClassBasedRequiredBlockOffsets(Block block, BlockState state) {
      if (block instanceof FallingBlock) {
         this.requiredBlockOffsets.add(new BlockPos(0, -1, 0));
      }

      if (block instanceof BedBlock && state != null && state.hasProperty(BedBlock.PART) && state.hasProperty(BedBlock.FACING)) {
         BedPart part = (BedPart)state.getValue(BedBlock.PART);
         Direction facing = (Direction)state.getValue(BedBlock.FACING);
         if (part == BedPart.HEAD) {
            this.requiredBlockOffsets.add(BlockPos.ZERO.relative(facing.getOpposite()));
         } else if (part == BedPart.FOOT) {
            this.requiredBlockOffsets.add(new BlockPos(facing.getStepX(), -1, facing.getStepZ()));
         }
      }

      if (block instanceof DoorBlock && state != null && state.hasProperty(DoorBlock.HALF)) {
         DoubleBlockHalf half = (DoubleBlockHalf)state.getValue(DoorBlock.HALF);
         if (half == DoubleBlockHalf.UPPER) {
            this.requiredBlockOffsets.add(new BlockPos(0, -1, 0));
         } else if (half == DoubleBlockHalf.LOWER) {
            this.requiredBlockOffsets.add(new BlockPos(0, -1, 0));
         }
      }

      if (block instanceof DoublePlantBlock && state != null && state.hasProperty(DoublePlantBlock.HALF)) {
         DoubleBlockHalf half = (DoubleBlockHalf)state.getValue(DoublePlantBlock.HALF);
         if (half == DoubleBlockHalf.UPPER) {
            this.requiredBlockOffsets.add(new BlockPos(0, -1, 0));
         } else if (half == DoubleBlockHalf.LOWER) {
            this.requiredBlockOffsets.add(new BlockPos(0, -1, 0));
         }
      }
   }

   protected void setBlockState(SchematicBlockContext context, Set<JsonRule> rules) {
      this.blockState = context.blockState;
   }

   protected void setIgnoredProperties(SchematicBlockContext context, Set<JsonRule> rules) {
      this.ignoredProperties.clear();
      rules.stream()
         .map(rule -> rule.ignoredProperties)
         .filter(Objects::nonNull)
         .flatMap(Collection::stream)
         .flatMap(propertyName -> context.blockState.getProperties().stream().filter(property -> property.getName().equals(propertyName)))
         .forEach(this.ignoredProperties::add);
      this.addClassBasedIgnoredProperties();
   }

   protected void addClassBasedIgnoredProperties() {
      if (this.placeBlock instanceof StairBlock) {
         this.addIgnoredPropertyByName("shape");
      }
   }

   private void addIgnoredPropertyByName(String name) {
      if (this.blockState != null) {
         this.blockState
            .getProperties()
            .stream()
            .filter(p -> p.getName().equals(name))
            .filter(p -> !this.ignoredProperties.contains(p))
            .findFirst()
            .ifPresent(this.ignoredProperties::add);
      }
   }

   protected void setTileNbt(SchematicBlockContext context, Set<JsonRule> rules) {
      this.tileNbt = null;
      BlockEntity tileEntity = context.world.getBlockEntity(context.pos);
      if (tileEntity != null) {
         this.tileNbt = tileEntity.saveWithoutMetadata(context.world.registryAccess());
      }
   }

   protected void setPlaceBlock(SchematicBlockContext context, Set<JsonRule> rules) {
      this.placeBlock = rules.stream()
         .map(rule -> rule.placeBlock)
         .filter(Objects::nonNull)
         .findFirst()
         .map(Identifier::parse)
         .<Block>map(BcRegistryUtil::getBlock)
         .orElse(context.block);
   }

   protected void setUpdateBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
      this.updateBlockOffsets.clear();
      if (rules.stream().map(rule -> rule.updateBlockOffsets).anyMatch(Objects::nonNull)) {
         rules.stream().map(rule -> rule.updateBlockOffsets).filter(Objects::nonNull).flatMap(Collection::stream).forEach(this.updateBlockOffsets::add);
      } else {
         Stream.of(Direction.values()).map(Direction::getUnitVec3i).map(BlockPos::new).forEach(this.updateBlockOffsets::add);
         this.updateBlockOffsets.add(BlockPos.ZERO);
      }
   }

   protected void setCanBeReplacedWithBlocks(SchematicBlockContext context, Set<JsonRule> rules) {
      this.canBeReplacedWithBlocks.clear();
      rules.stream()
         .map(rule -> rule.canBeReplacedWithBlocks)
         .filter(Objects::nonNull)
         .flatMap(Collection::stream)
         .map(Identifier::parse)
         .map(BcRegistryUtil::getBlock)
         .forEach(this.canBeReplacedWithBlocks::add);
      this.canBeReplacedWithBlocks.add(context.block);
      this.canBeReplacedWithBlocks.add(this.placeBlock);
   }

   @Override
   public void init(SchematicBlockContext context) {
      BlockEntity be = context.world.getBlockEntity(context.pos);
      CompoundTag beNbt = be != null ? be.saveWithoutMetadata(context.world.registryAccess()) : null;
      Set<JsonRule> rules = RulesLoader.getRules(context.blockState, beNbt);
      this.setRequiredBlockOffsets(context, rules);
      this.setBlockState(context, rules);
      this.setIgnoredProperties(context, rules);
      this.setTileNbt(context, rules);
      this.setPlaceBlock(context, rules);
      this.setUpdateBlockOffsets(context, rules);
      this.setCanBeReplacedWithBlocks(context, rules);
   }

   @Nonnull
   @Override
   public Set<BlockPos> getRequiredBlockOffsets() {
      return this.requiredBlockOffsets;
   }

   @Nonnull
   @Override
   public List<ItemStack> computeRequiredItems() {
      return this.computeRequiredItems(true);
   }

   @Nonnull
   public List<ItemStack> computeRequiredItems(boolean includeContainerContents) {
      if (this.placeBlock instanceof BedBlock
         && this.blockState != null
         && this.blockState.hasProperty(BedBlock.PART)
         && this.blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
         return Collections.emptyList();
      }

      if (this.placeBlock instanceof DoorBlock
         && this.blockState != null
         && this.blockState.hasProperty(DoorBlock.HALF)
         && this.blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
         return Collections.emptyList();
      }

      if (this.placeBlock instanceof DoublePlantBlock
         && this.blockState != null
         && this.blockState.hasProperty(DoublePlantBlock.HALF)
         && this.blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
         return Collections.emptyList();
      }

      Set<JsonRule> rules = RulesLoader.getRules(this.blockState, this.tileNbt);
      List<List<RequiredExtractor>> extractorLists = rules.stream().map(rule -> rule.requiredExtractors).filter(Objects::nonNull).collect(Collectors.toList());
      return (extractorLists.isEmpty() ? Stream.of(new RequiredExtractorItemFromBlock()) : extractorLists.stream().flatMap(Collection::stream))
         .filter(extractor -> includeContainerContents || !(extractor instanceof RequiredExtractorItemsList))
         .flatMap(extractor -> extractor.extractItemsFromBlock(this.blockState, this.tileNbt).stream())
         .filter(stack -> !stack.isEmpty())
         .collect(Collectors.toList());
   }

   @Nonnull
   @Override
   public List<FluidStack> computeRequiredFluids() {
      Set<JsonRule> rules = RulesLoader.getRules(this.blockState, this.tileNbt);
      return rules.stream()
         .map(rule -> rule.requiredExtractors)
         .filter(Objects::nonNull)
         .flatMap(Collection::stream)
         .flatMap(extractor -> extractor.extractFluidsFromBlock(this.blockState, this.tileNbt).stream())
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   @Override
   public BlockState getBlockStateForRender() {
      return this.blockState;
   }

   @Override
   public CompoundTag getTileNbtForRender() {
      return this.tileNbt;
   }

   public SchematicBlockDefault getRotated(Rotation rotation) {
      SchematicBlockDefault schematicBlock = SchematicBlockManager.createCleanCopy(this);
      this.requiredBlockOffsets.stream().map(blockPos -> blockPos.rotate(rotation)).forEach(schematicBlock.requiredBlockOffsets::add);
      schematicBlock.blockState = this.blockState.rotate(rotation);
      schematicBlock.ignoredProperties.addAll(this.ignoredProperties);
      schematicBlock.tileNbt = this.tileNbt;
      schematicBlock.tileRotation = this.tileRotation.getRotated(rotation);
      schematicBlock.placeBlock = this.placeBlock;
      this.updateBlockOffsets.stream().map(blockPos -> blockPos.rotate(rotation)).forEach(schematicBlock.updateBlockOffsets::add);
      schematicBlock.canBeReplacedWithBlocks.addAll(this.canBeReplacedWithBlocks);
      return schematicBlock;
   }

   @Override
   public boolean canBuild(Level level, BlockPos blockPos) {
      return level.isEmptyBlock(blockPos);
   }

   @Override
   public boolean build(Level level, BlockPos blockPos) {
      return this.build(level, blockPos, EnumFluidHandlingMode.NO_REPLACE, true);
   }

   public boolean build(Level level, BlockPos blockPos, EnumFluidHandlingMode fluidMode) {
      return this.build(level, blockPos, fluidMode, true);
   }

   public boolean build(
      Level level, BlockPos blockPos, EnumFluidHandlingMode fluidMode, boolean includeContainerContents, GameProfile owner, BlockPos machineOrigin
   ) {
      return this.build(level, blockPos, fluidMode, includeContainerContents, owner, machineOrigin, true);
   }

   public boolean build(Level level, BlockPos blockPos, EnumFluidHandlingMode fluidMode, boolean includeContainerContents) {
      return this.build(level, blockPos, fluidMode, includeContainerContents, null, null, true);
   }

   private boolean build(
      Level level,
      BlockPos blockPos,
      EnumFluidHandlingMode fluidMode,
      boolean includeContainerContents,
      @Nullable GameProfile owner,
      @Nullable BlockPos machineOrigin,
      boolean checkPrimaryPos
   ) {
      if (this.placeBlock == Blocks.AIR) {
         return true;
      } else if (checkPrimaryPos
         && owner != null
         && level instanceof ServerLevel serverLevel
         && !BlockUtil.canMachinePlace(serverLevel, blockPos, owner, machineOrigin)) {
         return false;
      } else {
         BlockState newBlockState = this.blockState;
         if (this.placeBlock != this.blockState.getBlock()) {
            newBlockState = this.placeBlock.defaultBlockState();

            for (Property<?> property : this.blockState.getProperties()) {
               if (newBlockState.getProperties().contains(property)) {
                  newBlockState = copyProperty(property, newBlockState, this.blockState);
               }
            }
         }

         for (Property<?> property : this.ignoredProperties) {
            newBlockState = copyProperty(property, newBlockState, this.placeBlock.defaultBlockState());
         }

         boolean willDestroyFluidAtPos = false;
         if (fluidMode == EnumFluidHandlingMode.REPLACE || fluidMode == EnumFluidHandlingMode.CLEAR) {
            FluidState existing = level.getFluidState(blockPos);
            if (!existing.isEmpty() && existing.isSource()) {
               boolean waterloggable = existing.getType() == Fluids.WATER && newBlockState.hasProperty(BlockStateProperties.WATERLOGGED);
               if (waterloggable) {
                  boolean schematicWantsWater = this.blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                     && (Boolean)this.blockState.getValue(BlockStateProperties.WATERLOGGED);
                  if (fluidMode != EnumFluidHandlingMode.REPLACE && !schematicWantsWater) {
                     newBlockState = (BlockState)newBlockState.setValue(BlockStateProperties.WATERLOGGED, false);
                  } else {
                     newBlockState = (BlockState)newBlockState.setValue(BlockStateProperties.WATERLOGGED, true);
                  }
               } else {
                  willDestroyFluidAtPos = true;
               }
            }
         }

         if (fluidMode == EnumFluidHandlingMode.REPLACE || fluidMode == EnumFluidHandlingMode.CLEAR) {
            boolean placedAsWaterlogged = newBlockState.hasProperty(BlockStateProperties.WATERLOGGED)
               && (Boolean)newBlockState.getValue(BlockStateProperties.WATERLOGGED);
            if (!placedAsWaterlogged) {
               for (Direction dir : FRAGILE_FLUID_NEIGHBOUR_DIRS) {
                  FluidState neighbour = level.getFluidState(blockPos.relative(dir));
                  if (!neighbour.isEmpty() && newBlockState.canBeReplaced(neighbour.getType())) {
                     return false;
                  }
               }
            }
         }

         if (newBlockState.getBlock() instanceof BedBlock && newBlockState.hasProperty(BedBlock.PART) && newBlockState.getValue(BedBlock.PART) == BedPart.FOOT) {
            Direction facing = (Direction)newBlockState.getValue(BedBlock.FACING);
            BlockPos headPos = blockPos.relative(facing);
            BlockState atHead = level.getBlockState(headPos);
            if (!atHead.isAir() && !atHead.canBeReplaced(Fluids.WATER)) {
               return false;
            }
         }

         if (newBlockState.getBlock() instanceof DoorBlock
            && newBlockState.hasProperty(DoorBlock.HALF)
            && newBlockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
            BlockPos upperPos = blockPos.above();
            BlockState atUpper = level.getBlockState(upperPos);
            if (!atUpper.isAir() && !atUpper.canBeReplaced(Fluids.WATER)) {
               return false;
            }
         }

         if (newBlockState.getBlock() instanceof DoublePlantBlock
            && newBlockState.hasProperty(DoublePlantBlock.HALF)
            && newBlockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            BlockPos upperPos = blockPos.above();
            BlockState atUpper = level.getBlockState(upperPos);
            if (!atUpper.isAir() && !atUpper.canBeReplaced(Fluids.WATER)) {
               return false;
            }
         }

         if (willDestroyFluidAtPos && !machineDestroyBlock(level, blockPos, owner, machineOrigin)) {
            return false;
         }

         if (newBlockState.getBlock() instanceof LeavesBlock && newBlockState.hasProperty(LeavesBlock.PERSISTENT)) {
            newBlockState = (BlockState)newBlockState.setValue(LeavesBlock.PERSISTENT, true);
         }

         boolean placed = machineSetBlock(level, blockPos, newBlockState, 11, owner, machineOrigin);
         if (!placed) {
            return false;
         }

         BlockPos secondHalfPos = null;
         if (newBlockState.getBlock() instanceof BedBlock && newBlockState.hasProperty(BedBlock.PART) && newBlockState.getValue(BedBlock.PART) == BedPart.FOOT) {
            Direction facing = (Direction)newBlockState.getValue(BedBlock.FACING);
            secondHalfPos = blockPos.relative(facing);
            BlockState headState = (BlockState)newBlockState.setValue(BedBlock.PART, BedPart.HEAD);
            if (!machineSetBlock(level, secondHalfPos, headState, 3, owner, machineOrigin)) {
               return false;
            }
         } else if (newBlockState.getBlock() instanceof DoorBlock
            && newBlockState.hasProperty(DoorBlock.HALF)
            && newBlockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
            secondHalfPos = blockPos.above();
            BlockState upperState = (BlockState)newBlockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
            if (!machineSetBlock(level, secondHalfPos, upperState, 3, owner, machineOrigin)) {
               return false;
            }
         } else if (newBlockState.getBlock() instanceof DoublePlantBlock
            && newBlockState.hasProperty(DoublePlantBlock.HALF)
            && newBlockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            secondHalfPos = blockPos.above();
            BlockState upperState = (BlockState)newBlockState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
            if (!machineSetBlock(level, secondHalfPos, upperState, 3, owner, machineOrigin)) {
               return false;
            }
         }

         BlockState afterShape = newBlockState;

         for (Direction dir : Direction.values()) {
            BlockPos neighborPos = blockPos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            //? if >= 1.21.10 {
            BlockState updated = afterShape.updateShape(level, level, blockPos, dir, neighborPos, neighborState, level.getRandom());
            //?} else {
            /*BlockState updated = afterShape.updateShape(dir, neighborState, level, blockPos, neighborPos);
            *///?}
            if (updated.isAir()) {
               level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
               if (secondHalfPos != null) {
                  level.setBlock(secondHalfPos, Blocks.AIR.defaultBlockState(), 3);
               }

               return false;
            }

            if (!updated.equals(afterShape)) {
               level.setBlock(blockPos, updated, 3);
               afterShape = updated;
            }
         }

         //? if >= 1.21.10 {
         this.updateBlockOffsets.stream().<BlockPos>map(blockPos::offset).forEach(updatePos -> level.neighborChanged(updatePos, this.placeBlock, null));
         //?} else {
         /*this.updateBlockOffsets.stream().<BlockPos>map(blockPos::offset).forEach(updatePos -> level.neighborChanged(updatePos, this.placeBlock, blockPos));
         *///?}
         if (this.tileNbt != null) {
            BlockEntity tileEntity = level.getBlockEntity(blockPos);
            if (tileEntity != null) {
               CompoundTag newTileNbt = this.tileNbt.copy();
               if (!includeContainerContents) {
                  this.stripContainerContentsFromNbt(newTileNbt);
               }

               newTileNbt.putInt("x", blockPos.getX());
               newTileNbt.putInt("y", blockPos.getY());
               newTileNbt.putInt("z", blockPos.getZ());
               BcEntityNbt.loadBlockEntity(tileEntity, newTileNbt, level.registryAccess());
            }
         }

         return true;
      }
   }

   private static boolean machineSetBlock(Level level, BlockPos pos, BlockState state, int flags, @Nullable GameProfile owner, @Nullable BlockPos machineOrigin) {
      return level instanceof ServerLevel serverLevel && owner != null
         ? BlockUtil.machineSetBlock(serverLevel, pos, state, flags, owner, machineOrigin)
         : level.setBlock(pos, state, flags);
   }

   private static boolean machineDestroyBlock(Level level, BlockPos pos, @Nullable GameProfile owner, @Nullable BlockPos machineOrigin) {
      return level instanceof ServerLevel serverLevel && owner != null && !BlockUtil.canMachineBreak(serverLevel, pos, owner)
         ? false
         : level.destroyBlock(pos, false);
   }

   private void stripContainerContentsFromNbt(CompoundTag tileNbt) {
      for (JsonRule rule : RulesLoader.getRules(this.blockState, tileNbt)) {
         if (rule.requiredExtractors != null) {
            for (RequiredExtractor extractor : rule.requiredExtractors) {
               extractor.clearItemsFromBlock(tileNbt);
            }
         }
      }
   }

   @Override
   public boolean buildWithoutChecks(Level level, BlockPos blockPos) {
      if (level.setBlock(blockPos, this.blockState, 0)) {
         if (this.tileNbt != null) {
            BlockEntity tileEntity = level.getBlockEntity(blockPos);
            if (tileEntity != null) {
               CompoundTag newTileNbt = this.tileNbt.copy();
               newTileNbt.putInt("x", blockPos.getX());
               newTileNbt.putInt("y", blockPos.getY());
               newTileNbt.putInt("z", blockPos.getZ());
               BcEntityNbt.loadBlockEntity(tileEntity, newTileNbt, level.registryAccess());
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isBuilt(Level level, BlockPos blockPos) {
      return this.isBuilt(level, blockPos, EnumFluidHandlingMode.NO_REPLACE);
   }

   public boolean isWaterlogClearOnly(Level level, BlockPos blockPos, EnumFluidHandlingMode fluidMode) {
      if (fluidMode != EnumFluidHandlingMode.CLEAR) {
         return false;
      } else if (this.blockState == null) {
         return false;
      } else {
         BlockState worldState = level.getBlockState(blockPos);
         if (worldState.getBlock() != this.blockState.getBlock()) {
            return false;
         } else {
            return worldState.hasProperty(BlockStateProperties.WATERLOGGED) && this.blockState.hasProperty(BlockStateProperties.WATERLOGGED)
               ? (Boolean)worldState.getValue(BlockStateProperties.WATERLOGGED) && !(Boolean)this.blockState.getValue(BlockStateProperties.WATERLOGGED)
               : false;
         }
      }
   }

   public boolean isBuilt(Level level, BlockPos blockPos, EnumFluidHandlingMode fluidMode) {
      if (this.blockState == null) {
         return false;
      }

      BlockState worldState = level.getBlockState(blockPos);
      if (!this.canBeReplacedWithBlocks.contains(worldState.getBlock())) {
         return false;
      }

      if (fluidMode != EnumFluidHandlingMode.CLEAR
         && worldState.hasProperty(BlockStateProperties.WATERLOGGED)
         && this.blockState.hasProperty(BlockStateProperties.WATERLOGGED)
         && (Boolean)worldState.getValue(BlockStateProperties.WATERLOGGED)
         && !(Boolean)this.blockState.getValue(BlockStateProperties.WATERLOGGED)) {
         worldState = (BlockState)worldState.setValue(BlockStateProperties.WATERLOGGED, false);
      }

      return blockStatesWithoutBlockEqual(this.blockState, worldState, this.ignoredProperties);
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.put("requiredBlockOffsets", NBTUtilBC.writeCompoundList(this.requiredBlockOffsets.stream().map(NBTUtilBC::writeBlockPos)));
      nbt.put("blockState", NbtUtils.writeBlockState(this.blockState));
      nbt.put("ignoredProperties", NBTUtilBC.writeStringList(this.ignoredProperties.stream().map(Property::getName)));
      if (this.tileNbt != null) {
         nbt.put("tileNbt", this.tileNbt);
      }

      nbt.put("tileRotation", NBTUtilBC.writeEnum(this.tileRotation));
      nbt.putString("placeBlock", BuiltInRegistries.BLOCK.getKey(this.placeBlock).toString());
      nbt.put("updateBlockOffsets", NBTUtilBC.writeCompoundList(this.updateBlockOffsets.stream().map(NBTUtilBC::writeBlockPos)));
      nbt.put(
         "canBeReplacedWithBlocks",
         NBTUtilBC.writeStringList(this.canBeReplacedWithBlocks.stream().<Identifier>map(BuiltInRegistries.BLOCK::getKey).map(Object::toString))
      );
      return nbt;
   }

   @Override
   public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
      NBTUtilBC.readCompoundList(nbt.get("requiredBlockOffsets")).map(NBTUtilBC::readBlockPos).forEach(this.requiredBlockOffsets::add);
      this.blockState = NbtUtils.readBlockState(BcRegistryUtil.blockLookup(), BcNbt.getCompound(nbt, "blockState"));
      NBTUtilBC.readStringList(nbt.get("ignoredProperties"))
         .map(propertyName -> this.blockState.getProperties().stream().filter(property -> property.getName().equals(propertyName)).findFirst().orElse(null))
         .filter(Objects::nonNull)
         .forEach(this.ignoredProperties::add);
      if (nbt.contains("tileNbt")) {
         this.tileNbt = BcNbt.getCompound(nbt, "tileNbt");
      }

      this.tileRotation = NBTUtilBC.readEnum(nbt.get("tileRotation"), Rotation.class);
      if (this.tileRotation == null) {
         this.tileRotation = Rotation.NONE;
      }

      this.placeBlock = BcRegistryUtil.getBlock(Identifier.parse(BcNbt.getString(nbt, "placeBlock", "")));
      NBTUtilBC.readCompoundList(nbt.get("updateBlockOffsets")).map(NBTUtilBC::readBlockPos).forEach(this.updateBlockOffsets::add);
      NBTUtilBC.readStringList(nbt.get("canBeReplacedWithBlocks"))
         .map(Identifier::parse)
         .map(BcRegistryUtil::getBlock)
         .forEach(this.canBeReplacedWithBlocks::add);
      Set<JsonRule> currentRules = RulesLoader.getRules(this.blockState, this.tileNbt);
      Set<String> migratedIgnoredNames = new HashSet<>();

      for (Property<?> existing : this.ignoredProperties) {
         migratedIgnoredNames.add(existing.getName());
      }

      currentRules.stream()
         .map(rule -> rule.ignoredProperties)
         .filter(Objects::nonNull)
         .flatMap(Collection::stream)
         .filter(migratedIgnoredNames::add)
         .flatMap(propertyName -> this.blockState.getProperties().stream().filter(property -> property.getName().equals(propertyName)))
         .forEach(this.ignoredProperties::add);
      this.addClassBasedIgnoredProperties();
      if (this.placeBlock != null) {
         this.addClassBasedRequiredBlockOffsets(this.placeBlock, this.blockState);
      }

      if (this.requiredBlockOffsets.isEmpty()) {
         currentRules.stream()
            .map(rule -> rule.requiredBlockOffsets)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .forEach(this.requiredBlockOffsets::add);
      }
   }

   private static <T extends Comparable<T>> BlockState copyProperty(Property<T> property, BlockState dest, BlockState source) {
      return (BlockState)dest.setValue(property, source.getValue(property));
   }

   private static boolean blockStatesWithoutBlockEqual(BlockState a, BlockState b, List<Property<?>> ignored) {
      for (Property<?> property : a.getProperties()) {
         if (!ignored.contains(property)) {
            if (!b.getProperties().contains(property)) {
               return false;
            }

            if (!propertyValuesEqual(property, a, b)) {
               return false;
            }
         }
      }

      return true;
   }

   private static <T extends Comparable<T>> boolean propertyValuesEqual(Property<T> property, BlockState a, BlockState b) {
      return a.getValue(property).equals(b.getValue(property));
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SchematicBlockDefault that = (SchematicBlockDefault)o;
         return this.requiredBlockOffsets.equals(that.requiredBlockOffsets)
            && this.blockState.equals(that.blockState)
            && this.ignoredProperties.equals(that.ignoredProperties)
            && (this.tileNbt != null ? this.tileNbt.equals(that.tileNbt) : that.tileNbt == null)
            && this.tileRotation == that.tileRotation
            && this.placeBlock.equals(that.placeBlock)
            && this.updateBlockOffsets.equals(that.updateBlockOffsets)
            && this.canBeReplacedWithBlocks.equals(that.canBeReplacedWithBlocks);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.requiredBlockOffsets.hashCode();
      result = 31 * result + this.blockState.hashCode();
      result = 31 * result + this.ignoredProperties.hashCode();
      result = 31 * result + (this.tileNbt != null ? this.tileNbt.hashCode() : 0);
      result = 31 * result + this.tileRotation.hashCode();
      result = 31 * result + this.placeBlock.hashCode();
      result = 31 * result + this.updateBlockOffsets.hashCode();
      return 31 * result + this.canBeReplacedWithBlocks.hashCode();
   }
}
