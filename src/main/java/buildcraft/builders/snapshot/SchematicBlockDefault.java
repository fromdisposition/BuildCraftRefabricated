/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;

import buildcraft.lib.misc.NBTUtilBC;

@SuppressWarnings("deprecation")
public class SchematicBlockDefault implements ISchematicBlock {

    private static final Direction[] FRAGILE_FLUID_NEIGHBOUR_DIRS = {
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP
    };

    @SuppressWarnings("WeakerAccess")
    protected final Set<BlockPos> requiredBlockOffsets = new HashSet<>();
    @SuppressWarnings("WeakerAccess")
    protected BlockState blockState;
    @SuppressWarnings("WeakerAccess")
    protected final List<Property<?>> ignoredProperties = new ArrayList<>();
    @SuppressWarnings("WeakerAccess")
    protected CompoundTag tileNbt;
    @SuppressWarnings("WeakerAccess")
    protected Rotation tileRotation = Rotation.NONE;
    @SuppressWarnings("WeakerAccess")
    protected Block placeBlock;
    @SuppressWarnings("WeakerAccess")
    protected final Set<BlockPos> updateBlockOffsets = new HashSet<>();
    @SuppressWarnings("WeakerAccess")
    protected final Set<Block> canBeReplacedWithBlocks = new HashSet<>();

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        if (context.blockState.isAir()) {
            return false;
        }
        Identifier registryName = BuiltInRegistries.BLOCK.getKey(context.block);
        if (registryName == null) return false;
        if (!RulesLoader.READ_DOMAINS.contains(registryName.getNamespace())) return false;
        BlockEntity be = context.world.getBlockEntity(context.pos);
        CompoundTag beNbt = be != null
            ? be.saveWithoutMetadata(context.world.registryAccess())
            : null;
        return RulesLoader.getRules(context.blockState, beNbt)
            .stream()
            .noneMatch(rule -> rule.ignore);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setRequiredBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
        requiredBlockOffsets.clear();
        rules.stream()
            .map(rule -> rule.requiredBlockOffsets)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .forEach(requiredBlockOffsets::add);
        addClassBasedRequiredBlockOffsets(context.block, context.blockState);
    }

    @SuppressWarnings("WeakerAccess")
    protected void addClassBasedRequiredBlockOffsets(Block block, BlockState state) {
        if (block instanceof FallingBlock) {
            requiredBlockOffsets.add(new BlockPos(0, -1, 0));
        }
        if (block instanceof BedBlock && state != null
                && state.hasProperty(BedBlock.PART) && state.hasProperty(BedBlock.FACING)) {
            BedPart part = state.getValue(BedBlock.PART);
            Direction facing = state.getValue(BedBlock.FACING);
            if (part == BedPart.HEAD) {

                requiredBlockOffsets.add(BlockPos.ZERO.relative(facing.getOpposite()));
            } else if (part == BedPart.FOOT) {

                requiredBlockOffsets.add(new BlockPos(facing.getStepX(), -1, facing.getStepZ()));
            }
        }
        if (block instanceof DoorBlock && state != null && state.hasProperty(DoorBlock.HALF)) {
            DoubleBlockHalf half = state.getValue(DoorBlock.HALF);
            if (half == DoubleBlockHalf.UPPER) {

                requiredBlockOffsets.add(new BlockPos(0, -1, 0));
            } else if (half == DoubleBlockHalf.LOWER) {

                requiredBlockOffsets.add(new BlockPos(0, -1, 0));
            }
        }

        if (block instanceof DoublePlantBlock && state != null
                && state.hasProperty(DoublePlantBlock.HALF)) {
            DoubleBlockHalf half = state.getValue(DoublePlantBlock.HALF);
            if (half == DoubleBlockHalf.UPPER) {

                requiredBlockOffsets.add(new BlockPos(0, -1, 0));
            } else if (half == DoubleBlockHalf.LOWER) {

                requiredBlockOffsets.add(new BlockPos(0, -1, 0));
            }
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setBlockState(SchematicBlockContext context, Set<JsonRule> rules) {
        blockState = context.blockState;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setIgnoredProperties(SchematicBlockContext context, Set<JsonRule> rules) {
        ignoredProperties.clear();
        rules.stream()
            .map(rule -> rule.ignoredProperties)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .flatMap(propertyName ->
                context.blockState.getProperties().stream()
                    .filter(property -> property.getName().equals(propertyName))
            )
            .forEach(ignoredProperties::add);
        addClassBasedIgnoredProperties();
    }

    @SuppressWarnings("WeakerAccess")
    protected void addClassBasedIgnoredProperties() {
        if (placeBlock instanceof StairBlock) {
            addIgnoredPropertyByName("shape");
        }
    }

    private void addIgnoredPropertyByName(String name) {
        if (blockState == null) return;
        blockState.getProperties().stream()
            .filter(p -> p.getName().equals(name))
            .filter(p -> !ignoredProperties.contains(p))
            .findFirst()
            .ifPresent(ignoredProperties::add);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setTileNbt(SchematicBlockContext context, Set<JsonRule> rules) {
        tileNbt = null;
        BlockEntity tileEntity = context.world.getBlockEntity(context.pos);
        if (tileEntity != null) {
            tileNbt = tileEntity.saveWithoutMetadata(context.world.registryAccess());
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setPlaceBlock(SchematicBlockContext context, Set<JsonRule> rules) {
        placeBlock = rules.stream()
            .map(rule -> rule.placeBlock)
            .filter(Objects::nonNull)
            .findFirst()
            .map(Identifier::parse)
            .map(BuiltInRegistries.BLOCK::getValue)
            .orElse(context.block);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setUpdateBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
        updateBlockOffsets.clear();
        if (rules.stream().map(rule -> rule.updateBlockOffsets).anyMatch(Objects::nonNull)) {
            rules.stream()
                .map(rule -> rule.updateBlockOffsets)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(updateBlockOffsets::add);
        } else {
            Stream.of(Direction.values())
                .map(Direction::getUnitVec3i)
                .map(BlockPos::new)
                .forEach(updateBlockOffsets::add);
            updateBlockOffsets.add(BlockPos.ZERO);
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setCanBeReplacedWithBlocks(SchematicBlockContext context, Set<JsonRule> rules) {
        canBeReplacedWithBlocks.clear();
        rules.stream()
            .map(rule -> rule.canBeReplacedWithBlocks)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .map(Identifier::parse)
            .map(BuiltInRegistries.BLOCK::getValue)
            .forEach(canBeReplacedWithBlocks::add);
        canBeReplacedWithBlocks.add(context.block);
        canBeReplacedWithBlocks.add(placeBlock);
    }

    @Override
    public void init(SchematicBlockContext context) {
        BlockEntity be = context.world.getBlockEntity(context.pos);
        CompoundTag beNbt = be != null
            ? be.saveWithoutMetadata(context.world.registryAccess())
            : null;
        Set<JsonRule> rules = RulesLoader.getRules(context.blockState, beNbt);
        setRequiredBlockOffsets(context, rules);
        setBlockState(context, rules);
        setIgnoredProperties(context, rules);
        setTileNbt(context, rules);
        setPlaceBlock(context, rules);
        setUpdateBlockOffsets(context, rules);
        setCanBeReplacedWithBlocks(context, rules);
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return requiredBlockOffsets;
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
        return computeRequiredItems(true);
    }

    @Nonnull
    public List<ItemStack> computeRequiredItems(boolean includeContainerContents) {

        if (placeBlock instanceof BedBlock && blockState != null
                && blockState.hasProperty(BedBlock.PART)
                && blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
            return java.util.Collections.emptyList();
        }

        if (placeBlock instanceof DoorBlock && blockState != null
                && blockState.hasProperty(DoorBlock.HALF)
                && blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            return java.util.Collections.emptyList();
        }

        if (placeBlock instanceof DoublePlantBlock && blockState != null
                && blockState.hasProperty(DoublePlantBlock.HALF)
                && blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            return java.util.Collections.emptyList();
        }
        Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
        List<List<RequiredExtractor>> extractorLists = rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return (
            extractorLists.isEmpty()
                ? Stream.of(new RequiredExtractorItemFromBlock())
                : extractorLists.stream().flatMap(Collection::stream)
        )
            .filter(extractor -> includeContainerContents || !(extractor instanceof RequiredExtractorItemsList))
            .flatMap(extractor -> extractor.extractItemsFromBlock(blockState, tileNbt).stream())
            .filter(stack -> !stack.isEmpty())
            .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids() {
        Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
        return rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(extractor -> extractor.extractFluidsFromBlock(blockState, tileNbt).stream())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public BlockState getBlockStateForRender() {
        return blockState;
    }

    @Override
    public CompoundTag getTileNbtForRender() {
        return tileNbt;
    }

    @Override
    public SchematicBlockDefault getRotated(Rotation rotation) {
        SchematicBlockDefault schematicBlock = SchematicBlockManager.createCleanCopy(this);
        requiredBlockOffsets.stream()
            .map(blockPos -> blockPos.rotate(rotation))
            .forEach(schematicBlock.requiredBlockOffsets::add);
        schematicBlock.blockState = blockState.rotate(rotation);
        schematicBlock.ignoredProperties.addAll(ignoredProperties);
        schematicBlock.tileNbt = tileNbt;
        schematicBlock.tileRotation = tileRotation.getRotated(rotation);
        schematicBlock.placeBlock = placeBlock;
        updateBlockOffsets.stream()
            .map(blockPos -> blockPos.rotate(rotation))
            .forEach(schematicBlock.updateBlockOffsets::add);
        schematicBlock.canBeReplacedWithBlocks.addAll(canBeReplacedWithBlocks);
        return schematicBlock;
    }

    @Override
    public boolean canBuild(Level level, BlockPos blockPos) {
        return level.isEmptyBlock(blockPos);
    }

    @Override
    public boolean build(Level level, BlockPos blockPos) {
        return build(level, blockPos, EnumFluidHandlingMode.NO_REPLACE, true);
    }

    public boolean build(Level level, BlockPos blockPos, EnumFluidHandlingMode fluidMode) {
        return build(level, blockPos, fluidMode, true);
    }

    @SuppressWarnings("Duplicates")
    public boolean build(Level level, BlockPos blockPos, EnumFluidHandlingMode fluidMode,
                         boolean includeContainerContents) {
        if (placeBlock == Blocks.AIR) {
            return true;
        }
        BlockState newBlockState = blockState;
        if (placeBlock != blockState.getBlock()) {
            newBlockState = placeBlock.defaultBlockState();
            for (Property<?> property : blockState.getProperties()) {
                if (newBlockState.getProperties().contains(property)) {
                    newBlockState = copyProperty(property, newBlockState, blockState);
                }
            }
        }
        for (Property<?> property : ignoredProperties) {
            newBlockState = copyProperty(property, newBlockState, placeBlock.defaultBlockState());
        }

        boolean willDestroyFluidAtPos = false;
        if (fluidMode == EnumFluidHandlingMode.REPLACE || fluidMode == EnumFluidHandlingMode.CLEAR) {
            FluidState existing = level.getFluidState(blockPos);
            if (!existing.isEmpty() && existing.isSource()) {
                boolean waterloggable =
                    existing.getType() == Fluids.WATER
                        && newBlockState.hasProperty(BlockStateProperties.WATERLOGGED);
                if (waterloggable) {

                    boolean schematicWantsWater = blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                        && blockState.getValue(BlockStateProperties.WATERLOGGED);
                    if (fluidMode == EnumFluidHandlingMode.REPLACE || schematicWantsWater) {
                        newBlockState = newBlockState.setValue(BlockStateProperties.WATERLOGGED, true);
                    } else {

                        newBlockState = newBlockState.setValue(BlockStateProperties.WATERLOGGED, false);
                    }
                } else {
                    willDestroyFluidAtPos = true;
                }
            }
        }

        if (fluidMode == EnumFluidHandlingMode.REPLACE || fluidMode == EnumFluidHandlingMode.CLEAR) {
            boolean placedAsWaterlogged = newBlockState.hasProperty(BlockStateProperties.WATERLOGGED)
                    && newBlockState.getValue(BlockStateProperties.WATERLOGGED);
            if (!placedAsWaterlogged) {
                for (Direction dir : FRAGILE_FLUID_NEIGHBOUR_DIRS) {
                    FluidState neighbour = level.getFluidState(blockPos.relative(dir));
                    if (!neighbour.isEmpty() && newBlockState.canBeReplaced(neighbour.getType())) {
                        return false;
                    }
                }
            }
        }

        if (newBlockState.getBlock() instanceof BedBlock
                && newBlockState.hasProperty(BedBlock.PART)
                && newBlockState.getValue(BedBlock.PART) == BedPart.FOOT) {
            Direction facing = newBlockState.getValue(BedBlock.FACING);
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

        if (willDestroyFluidAtPos) {
            level.destroyBlock(blockPos, false);
        }

        if (newBlockState.getBlock() instanceof LeavesBlock
                && newBlockState.hasProperty(LeavesBlock.PERSISTENT)) {
            newBlockState = newBlockState.setValue(LeavesBlock.PERSISTENT, true);
        }
        boolean placed = level.setBlock(blockPos, newBlockState, 11);
        if (placed) {

            BlockPos secondHalfPos = null;
            if (newBlockState.getBlock() instanceof BedBlock
                    && newBlockState.hasProperty(BedBlock.PART)
                    && newBlockState.getValue(BedBlock.PART) == BedPart.FOOT) {
                Direction facing = newBlockState.getValue(BedBlock.FACING);
                secondHalfPos = blockPos.relative(facing);
                BlockState headState = newBlockState.setValue(BedBlock.PART, BedPart.HEAD);
                level.setBlock(secondHalfPos, headState, 3);
            } else if (newBlockState.getBlock() instanceof DoorBlock
                    && newBlockState.hasProperty(DoorBlock.HALF)
                    && newBlockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                secondHalfPos = blockPos.above();
                BlockState upperState = newBlockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
                level.setBlock(secondHalfPos, upperState, 3);
            } else if (newBlockState.getBlock() instanceof DoublePlantBlock
                    && newBlockState.hasProperty(DoublePlantBlock.HALF)
                    && newBlockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {

                secondHalfPos = blockPos.above();
                BlockState upperState = newBlockState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                level.setBlock(secondHalfPos, upperState, 3);
            }

            BlockState afterShape = newBlockState;
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = blockPos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);
                BlockState updated = afterShape.updateShape(
                    level, level, blockPos, dir, neighborPos, neighborState, level.getRandom()
                );
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
            updateBlockOffsets.stream()
                .map(blockPos::offset)
                .forEach(updatePos -> level.neighborChanged(updatePos, placeBlock, null));
            if (tileNbt != null) {
                BlockEntity tileEntity = level.getBlockEntity(blockPos);
                if (tileEntity != null) {
                    CompoundTag newTileNbt = tileNbt.copy();
                    if (!includeContainerContents) {
                        stripContainerContentsFromNbt(newTileNbt);
                    }
                    newTileNbt.putInt("x", blockPos.getX());
                    newTileNbt.putInt("y", blockPos.getY());
                    newTileNbt.putInt("z", blockPos.getZ());
                    tileEntity.loadWithComponents(
                        TagValueInput.create(
                            ProblemReporter.DISCARDING,
                            level.registryAccess(),
                            newTileNbt
                        )
                    );
                }
            }
            return true;
        }
        return false;
    }

    private void stripContainerContentsFromNbt(CompoundTag tileNbt) {
        Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
        for (JsonRule rule : rules) {
            if (rule.requiredExtractors == null) continue;
            for (RequiredExtractor extractor : rule.requiredExtractors) {
                extractor.clearItemsFromBlock(tileNbt);
            }
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean buildWithoutChecks(Level level, BlockPos blockPos) {
        if (level.setBlock(blockPos, blockState, 0)) {
            if (tileNbt != null) {
                BlockEntity tileEntity = level.getBlockEntity(blockPos);
                if (tileEntity != null) {
                    CompoundTag newTileNbt = tileNbt.copy();
                    newTileNbt.putInt("x", blockPos.getX());
                    newTileNbt.putInt("y", blockPos.getY());
                    newTileNbt.putInt("z", blockPos.getZ());
                    tileEntity.loadWithComponents(
                        TagValueInput.create(
                            ProblemReporter.DISCARDING,
                            level.registryAccess(),
                            newTileNbt
                        )
                    );
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isBuilt(Level level, BlockPos blockPos) {
        return isBuilt(level, blockPos, EnumFluidHandlingMode.NO_REPLACE);
    }

    public boolean isWaterlogClearOnly(Level level, BlockPos blockPos, EnumFluidHandlingMode fluidMode) {
        if (fluidMode != EnumFluidHandlingMode.CLEAR) return false;
        if (blockState == null) return false;
        BlockState worldState = level.getBlockState(blockPos);
        if (worldState.getBlock() != blockState.getBlock()) return false;
        if (!worldState.hasProperty(BlockStateProperties.WATERLOGGED)
                || !blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return false;
        }
        return worldState.getValue(BlockStateProperties.WATERLOGGED)
                && !blockState.getValue(BlockStateProperties.WATERLOGGED);
    }

    public boolean isBuilt(Level level, BlockPos blockPos, EnumFluidHandlingMode fluidMode) {
        if (blockState == null) return false;
        BlockState worldState = level.getBlockState(blockPos);
        if (!canBeReplacedWithBlocks.contains(worldState.getBlock())) return false;
        if (fluidMode != EnumFluidHandlingMode.CLEAR
                && worldState.hasProperty(BlockStateProperties.WATERLOGGED)
                && blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                && worldState.getValue(BlockStateProperties.WATERLOGGED)
                && !blockState.getValue(BlockStateProperties.WATERLOGGED)) {

            worldState = worldState.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return blockStatesWithoutBlockEqual(blockState, worldState, ignoredProperties);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put(
            "requiredBlockOffsets",
            NBTUtilBC.writeCompoundList(
                requiredBlockOffsets.stream()
                    .map(NBTUtilBC::writeBlockPos)
            )
        );
        nbt.put("blockState", NbtUtils.writeBlockState(blockState));
        nbt.put(
            "ignoredProperties",
            NBTUtilBC.writeStringList(
                ignoredProperties.stream()
                    .map(Property::getName)
            )
        );
        if (tileNbt != null) {
            nbt.put("tileNbt", tileNbt);
        }
        nbt.put("tileRotation", NBTUtilBC.writeEnum(tileRotation));
        nbt.putString("placeBlock", BuiltInRegistries.BLOCK.getKey(placeBlock).toString());
        nbt.put(
            "updateBlockOffsets",
            NBTUtilBC.writeCompoundList(
                updateBlockOffsets.stream()
                    .map(NBTUtilBC::writeBlockPos)
            )
        );
        nbt.put(
            "canBeReplacedWithBlocks",
            NBTUtilBC.writeStringList(
                canBeReplacedWithBlocks.stream()
                    .map(BuiltInRegistries.BLOCK::getKey)
                    .map(Object::toString)
            )
        );
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        NBTUtilBC.readCompoundList(nbt.get("requiredBlockOffsets"))
            .map(NBTUtilBC::readBlockPos)
            .forEach(requiredBlockOffsets::add);
        blockState = NbtUtils.readBlockState(
            BuiltInRegistries.BLOCK,
            nbt.getCompoundOrEmpty("blockState")
        );
        NBTUtilBC.readStringList(nbt.get("ignoredProperties"))
            .map(propertyName ->
                blockState.getProperties().stream()
                    .filter(property -> property.getName().equals(propertyName))
                    .findFirst()
                    .orElse(null)
            )
            .filter(java.util.Objects::nonNull)
            .forEach(ignoredProperties::add);
        if (nbt.contains("tileNbt")) {
            tileNbt = nbt.getCompoundOrEmpty("tileNbt");
        }
        tileRotation = NBTUtilBC.readEnum(nbt.get("tileRotation"), Rotation.class);
        if (tileRotation == null) tileRotation = Rotation.NONE;
        placeBlock = BuiltInRegistries.BLOCK.getValue(Identifier.parse(nbt.getStringOr("placeBlock", "")));
        NBTUtilBC.readCompoundList(nbt.get("updateBlockOffsets"))
            .map(NBTUtilBC::readBlockPos)
            .forEach(updateBlockOffsets::add);
        NBTUtilBC.readStringList(nbt.get("canBeReplacedWithBlocks"))
            .map(Identifier::parse)
            .map(BuiltInRegistries.BLOCK::getValue)
            .forEach(canBeReplacedWithBlocks::add);

        java.util.Set<JsonRule> currentRules = RulesLoader.getRules(blockState, tileNbt);
        java.util.Set<String> migratedIgnoredNames = new java.util.HashSet<>();
        for (Property<?> existing : ignoredProperties) {
            migratedIgnoredNames.add(existing.getName());
        }
        currentRules.stream()
            .map(rule -> rule.ignoredProperties)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(migratedIgnoredNames::add)
            .flatMap(propertyName ->
                blockState.getProperties().stream()
                    .filter(property -> property.getName().equals(propertyName))
            )
            .forEach(ignoredProperties::add);

        addClassBasedIgnoredProperties();

        if (placeBlock != null) {
            addClassBasedRequiredBlockOffsets(placeBlock, blockState);
        }

        if (requiredBlockOffsets.isEmpty()) {
            currentRules.stream()
                .map(rule -> rule.requiredBlockOffsets)
                .filter(Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .forEach(requiredBlockOffsets::add);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(
        Property<T> property, BlockState dest, BlockState source
    ) {
        return dest.setValue(property, source.getValue(property));
    }

    private static boolean blockStatesWithoutBlockEqual(
        BlockState a, BlockState b, List<Property<?>> ignored
    ) {
        for (Property<?> property : a.getProperties()) {
            if (ignored.contains(property)) continue;
            if (!b.getProperties().contains(property)) return false;
            if (!propertyValuesEqual(property, a, b)) return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> boolean propertyValuesEqual(
        Property<T> property, BlockState a, BlockState b
    ) {
        return a.getValue(property).equals(b.getValue(property));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchematicBlockDefault that = (SchematicBlockDefault) o;
        return requiredBlockOffsets.equals(that.requiredBlockOffsets) &&
            blockState.equals(that.blockState) &&
            ignoredProperties.equals(that.ignoredProperties) &&
            (tileNbt != null ? tileNbt.equals(that.tileNbt) : that.tileNbt == null) &&
            tileRotation == that.tileRotation &&
            placeBlock.equals(that.placeBlock) &&
            updateBlockOffsets.equals(that.updateBlockOffsets) &&
            canBeReplacedWithBlocks.equals(that.canBeReplacedWithBlocks);
    }

    @Override
    public int hashCode() {
        int result = requiredBlockOffsets.hashCode();
        result = 31 * result + blockState.hashCode();
        result = 31 * result + ignoredProperties.hashCode();
        result = 31 * result + (tileNbt != null ? tileNbt.hashCode() : 0);
        result = 31 * result + tileRotation.hashCode();
        result = 31 * result + placeBlock.hashCode();
        result = 31 * result + updateBlockOffsets.hashCode();
        result = 31 * result + canBeReplacedWithBlocks.hashCode();
        return result;
    }
}
