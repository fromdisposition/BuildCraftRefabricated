/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

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

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;

import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;

@SuppressWarnings("unchecked")
public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
    private static final double MAX_ENTITY_DISTANCE = 0.1D;
    private static final String FLUID_STACK_KEY = "BuilderFluidStack";

    private List<ItemStack>[] remainingDisplayRequiredBlocks;
    private List<ItemStack> remainingDisplayRequiredBlocksConcat = Collections.emptyList();
    public List<ItemStack> remainingDisplayRequired = new ArrayList<>();
    private final Map<Pair<List<ItemStack>, List<FluidStack>>, Optional<List<ItemStack>>> extractRequiredCache =
        new HashMap<>();

    public BlueprintBuilder(ITileForBlueprintBuilder tile) {
        super(tile);
    }

    private ISchematicBlock getSchematicBlock(BlockPos blockPos) {
        return getBuildingInfo().box.contains(blockPos)
            ?
            getBuildingInfo().rotatedPalette.get(
                getBuildingInfo().getSnapshot().data[getBuildingInfo().getSnapshot().posToIndex(
                    getBuildingInfo().fromWorld(blockPos)
                )]
            )
            : null;
    }

    @Override
    protected boolean isAir(BlockPos blockPos) {
        ISchematicBlock schematic = getSchematicBlock(blockPos);
        if (schematic == null) return true;
        if (schematic.isAir()) return true;

        if (schematic instanceof SchematicBlockFluid && tile.getFluidMode() == EnumFluidHandlingMode.CLEAR) {
            return true;
        }
        return false;
    }

    @Override
    protected Blueprint.BuildingInfo getBuildingInfo() {
        return tile.getBlueprintBuildingInfo();
    }

    @Override
    public void updateSnapshot() {
        super.updateSnapshot();

        remainingDisplayRequiredBlocks = (List<ItemStack>[]) new List<?>[getBuildingInfo().getSnapshot().getDataSize()];
        Arrays.fill(remainingDisplayRequiredBlocks, Collections.emptyList());
    }

    @Override
    public void resourcesChanged() {
        super.resourcesChanged();
        extractRequiredCache.clear();
    }

    public void refreshDisplayForContentsMode() {
        if (remainingDisplayRequiredBlocks == null) return;

        if (checkResults == null) return;
        if (getBuildingInfo() == null) return;
        for (int i = 0; i < remainingDisplayRequiredBlocks.length; i++) {
            if (checkResults[i] == CHECK_RESULT_TO_PLACE) {
                remainingDisplayRequiredBlocks[i] = getDisplayRequired(
                        getBuildingInfo().toPlaceRequiredItems[i],
                        getBuildingInfo().toPlaceRequiredFluids[i]
                ).collect(Collectors.toList());
            } else {
                remainingDisplayRequiredBlocks[i] = Collections.emptyList();
            }
        }
        afterChecks();
    }

    @Override
    public void cancel() {
        super.cancel();
        remainingDisplayRequiredBlocks = null;
    }

    private Stream<ItemStack> getDisplayRequired(List<ItemStack> requiredItems, List<FluidStack> requiredFluids) {
        return Stream.concat(
            requiredItems == null ? Stream.empty() : requiredItems.stream(),
            requiredFluids == null ? Stream.empty() : requiredFluids.stream()
                .map(fluidStack -> {

                    ItemStack bucket = buildcraft.lib.misc.FluidUtilBC.getFilledBucket(fluidStack);
                    return bucket;
                })
        );
    }

    private Optional<List<ItemStack>> tryExtractRequired(List<ItemStack> requiredItems,
                                                         List<FluidStack> requiredFluids,
                                                         boolean simulate) {
        Supplier<Optional<List<ItemStack>>> function = () ->
            (
                StackUtil.mergeSameItems(requiredItems).stream()
                    .noneMatch(stack ->
                        tile.getInvResources().extract(
                            extracted -> StackUtil.canMerge(stack, extracted),
                            stack.getCount(),
                            stack.getCount(),
                            true
                        ).isEmpty()
                    ) &&
                    FluidUtilBC.mergeSameFluids(requiredFluids).stream()
                        .allMatch(stack -> {
                            try (buildcraft.lib.transfer.transaction.Transaction tx = buildcraft.lib.transfer.transaction.Transaction.openRoot()) {
                                int extracted = tile.getTankManager().extract(buildcraft.lib.transfer.fluid.FluidResource.of(stack), stack.getAmount(), tx);
                                return extracted == stack.getAmount();
                            }
                        })
            )
                ?
                Optional.of(
                    StackUtil.mergeSameItems(
                        Stream.concat(
                            requiredItems.stream()
                                .map(stack ->
                                    tile.getInvResources().extract(
                                        extracted -> StackUtil.canMerge(stack, extracted),
                                        stack.getCount(),
                                        stack.getCount(),
                                        simulate
                                    )
                                ),
                            FluidUtilBC.mergeSameFluids(requiredFluids).stream()
                                .map(fluidStack -> {
                                    try (buildcraft.lib.transfer.transaction.Transaction tx = buildcraft.lib.transfer.transaction.Transaction.openRoot()) {
                                        int extracted = tile.getTankManager().extract(buildcraft.lib.transfer.fluid.FluidResource.of(fluidStack), fluidStack.getAmount(), tx);
                                        if (!simulate) tx.commit();
                                        return new FluidStack(fluidStack.getFluid(), extracted);
                                    }
                                })
                                .map(fluidStack -> {
                                    ItemStack stack = buildcraft.lib.misc.FluidUtilBC.getFilledBucket(fluidStack);

                                    CompoundTag fluidTag = new CompoundTag();
                                    net.minecraft.resources.Identifier fluidId =
                                        net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluidStack.getFluid());
                                    if (fluidId != null) {
                                        fluidTag.putString("fluid", fluidId.toString());
                                        fluidTag.putInt("amount", fluidStack.getAmount());
                                    }
                                    CompoundTag wrapper = new CompoundTag();
                                    wrapper.put(FLUID_STACK_KEY, fluidTag);
                                    if (!stack.isEmpty()) {
                                        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                                            net.minecraft.world.item.component.CustomData.of(wrapper));
                                    }
                                    return stack;
                                })
                        ).collect(Collectors.toList())
                    )
                )
                : Optional.empty();
        if (!simulate) {
            return function.get();
        }
        return extractRequiredCache.computeIfAbsent(
            Pair.of(requiredItems, requiredFluids),
            pair -> function.get()
        );
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {

        if (isAir(blockPos)) return false;
        EnumFluidHandlingMode mode = tile.getFluidMode();
        boolean hasFluid = !tile.getWorldBC().getFluidState(blockPos).isEmpty();
        if (hasFluid) {

            if (mode == EnumFluidHandlingMode.CLEAR) return false;

            if (mode == EnumFluidHandlingMode.REPLACE) return true;
        }
        return getSchematicBlock(blockPos).canBuild(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean isReadyToPlace(BlockPos blockPos) {

        ISchematicBlock self = getSchematicBlock(blockPos);
        boolean selfIsFluid = self instanceof SchematicBlockFluid;
        boolean dependenciesMet = self.getRequiredBlockOffsets().stream()
            .map(blockPos::offset)
            .allMatch(pos -> {
                ISchematicBlock neighbour = getSchematicBlock(pos);
                if (neighbour == null) return true;
                if (checkResults[posToIndex(pos)] == CHECK_RESULT_CORRECT) return true;

                if (selfIsFluid && neighbour instanceof SchematicBlockFluid) return true;
                return false;
            }) && self.isReadyToBuild(tile.getWorldBC(), blockPos);
        if (!dependenciesMet) return false;

        if (self instanceof SchematicBlockDefault def && def.blockState != null
                && !def.blockState.canSurvive(tile.getWorldBC(), blockPos)) {
            return false;
        }
        return true;
    }

    private boolean isWaterlogClearOnlyAt(BlockPos blockPos) {
        ISchematicBlock schematic = getSchematicBlock(blockPos);
        if (!(schematic instanceof SchematicBlockDefault def)) return false;
        return def.isWaterlogClearOnly(tile.getWorldBC(), blockPos, tile.getFluidMode());
    }

    @Override
    protected boolean isAllowedDuringFluidMop(BlockPos blockPos) {

        return isWaterlogClearOnlyAt(blockPos);
    }

    @Override
    protected boolean isFragileSchematicAt(BlockPos blockPos) {
        ISchematicBlock schematic = getSchematicBlock(blockPos);
        if (!(schematic instanceof SchematicBlockDefault def)) return false;
        if (def.blockState == null) return false;

        return def.blockState.canBeReplaced(net.minecraft.world.level.material.Fluids.WATER);
    }

    @Override
    protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
        if (isWaterlogClearOnlyAt(blockPos)) return true;
        return tryExtractRequired(
            getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
            getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)],
            true
        ).isPresent();
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        if (isWaterlogClearOnlyAt(blockPos)) return Collections.emptyList();
        return tryExtractRequired(
            getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
            getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)],
            false
        ).orElse(null);
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
        super.cancelPlaceTask(placeTask);

        placeTask.items.stream()
            .filter(stack -> {
                net.minecraft.world.item.component.CustomData customData =
                    stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                return customData == null || !customData.copyTag().contains(FLUID_STACK_KEY);
            })
            .forEach(stack -> tile.getInvResources().insert(stack, false, false));

        placeTask.items.stream()
            .filter(stack -> {
                net.minecraft.world.item.component.CustomData customData =
                    stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                return customData != null && customData.copyTag().contains(FLUID_STACK_KEY);
            })
            .map(stack -> {
                net.minecraft.world.item.component.CustomData customData =
                    stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                CompoundTag fluidTag = customData.copyTag().getCompoundOrEmpty(FLUID_STACK_KEY);
                if (fluidTag.isEmpty()) return FluidStack.EMPTY;
                String fluidIdStr = fluidTag.getString("fluid").orElse("");
                int amount = fluidTag.getInt("amount").orElse(0);
                if (fluidIdStr.isEmpty() || amount <= 0) return FluidStack.EMPTY;
                net.minecraft.resources.Identifier id =
                    net.minecraft.resources.Identifier.tryParse(fluidIdStr);
                if (id == null) return FluidStack.EMPTY;
                net.minecraft.world.level.material.Fluid fluid =
                    net.minecraft.core.registries.BuiltInRegistries.FLUID.getValue(id);
                if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) return FluidStack.EMPTY;
                return new FluidStack(fluid, amount);
            })

            .filter(fluidStack -> !fluidStack.isEmpty() && fluidStack.getAmount() > 0)
            .forEach(fluidStack -> {
                try (buildcraft.lib.transfer.transaction.Transaction tx = buildcraft.lib.transfer.transaction.Transaction.openRoot()) {
                    tile.getTankManager().insert(buildcraft.lib.transfer.fluid.FluidResource.of(fluidStack), fluidStack.getAmount(), tx);
                    tx.commit();
                }
            });
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {

        if (getBuildingInfo() == null) return false;
        ISchematicBlock schematic = getSchematicBlock(blockPos);
        if (schematic == null) return false;

        if (schematic instanceof SchematicBlockDefault def) {
            return def.isBuilt(tile.getWorldBC(), blockPos, tile.getFluidMode());
        }
        return schematic.isBuilt(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {

        if (getBuildingInfo() == null) return false;
        ISchematicBlock schematic = getSchematicBlock(placeTask.pos);
        if (schematic == null) return false;

        if (schematic instanceof SchematicBlockDefault def) {
            boolean includeContents = tile.getContainerContentsMode() != EnumContainerContentsMode.IGNORE;
            return def.build(tile.getWorldBC(), placeTask.pos, tile.getFluidMode(), includeContents);
        }
        return schematic.build(tile.getWorldBC(), placeTask.pos);
    }

    @Override
    public boolean tick() {
        if (tile.getWorldBC().isClientSide()) {
            return super.tick();
        }

        List<Entity> entitiesWithinBox = tile.getWorldBC().getEntities(
            (Entity) null,
            getBuildingInfo().box.getBoundingBox(),
            Objects::nonNull
        );

        List<ISchematicEntity> toSpawn = new ArrayList<>();
        double maxDistSq = MAX_ENTITY_DISTANCE * MAX_ENTITY_DISTANCE;
        for (ISchematicEntity schematicEntity : getBuildingInfo().entities) {
            boolean found = false;
            Vec3 targetPos = schematicEntity.getPos().add(Vec3.atLowerCornerOf(getBuildingInfo().offsetPos));
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

        remainingDisplayRequired.clear();
        List<ItemStack> displayRequiredConcat = new ArrayList<>(remainingDisplayRequiredBlocksConcat);
        for (ISchematicEntity schematicEntity : toSpawn) {
            getDisplayRequired(
                getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                getBuildingInfo().entitiesRequiredFluids.get(schematicEntity)
            ).forEach(displayRequiredConcat::add);
        }
        remainingDisplayRequired.addAll(StackUtil.mergeSameItems(displayRequiredConcat));

        List<Entity> toKill = new ArrayList<>();
        for (Entity entity : entitiesWithinBox) {
            if (entity == null) continue;

            boolean foundClose = false;
            for (ISchematicEntity schematicEntity : getBuildingInfo().entities) {
                Vec3 pos = schematicEntity.getPos().add(Vec3.atLowerCornerOf(getBuildingInfo().offsetPos));
                if (entity.position().distanceToSqr(pos) < maxDistSq) {
                    foundClose = true;
                    break;
                }
            }

            if (!foundClose) {
                if (SchematicEntityManager.getSchematicEntity(new SchematicEntityContext(
                        tile.getWorldBC(),
                        BlockPos.ZERO,
                        entity
                    )) != null) {
                    toKill.add(entity);
                }
            }
        }
        if (!toKill.isEmpty()) {
            if (!tile.getBattery().isFull()) {
                return false;
            } else {
                toKill.forEach(Entity::discard);
            }
        }

        if (super.tick()) {

            if (!toSpawn.isEmpty()) {
                if (!tile.getBattery().isFull()) {
                    return false;
                } else {

                    for (ISchematicEntity schematicEntity : toSpawn) {
                        if (tryExtractRequired(
                                getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                                getBuildingInfo().entitiesRequiredFluids.get(schematicEntity),
                                true
                            ).isPresent()) {
                            if (schematicEntity.build(tile.getWorldBC(), getBuildingInfo().offsetPos) != null) {
                                tryExtractRequired(
                                    getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                                    getBuildingInfo().entitiesRequiredFluids.get(schematicEntity),
                                    false
                                );
                            }
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean check(BlockPos blockPos) {
        if (super.check(blockPos)) {
            remainingDisplayRequiredBlocks[posToIndex(blockPos)] =
                checkResults[posToIndex(blockPos)] != CHECK_RESULT_CORRECT
                    ?
                    getDisplayRequired(
                        getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
                        getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)]
                    ).collect(Collectors.toList())
                    : Collections.emptyList();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void afterChecks() {
        remainingDisplayRequiredBlocksConcat = StackUtil.mergeSameItems(
            Arrays.stream(remainingDisplayRequiredBlocks)
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
        );
    }

    @Override
    public void writeToByteBuf(PacketBufferBC buffer) {
        super.writeToByteBuf(buffer);
        buffer.writeInt(remainingDisplayRequired.size());
        remainingDisplayRequired.forEach(stack -> {
            CompoundTag tag = new CompoundTag();
            if (!stack.isEmpty()) {

                ItemStack.CODEC.encodeStart(NBTUtilBC.registryAwareOps(), stack.copyWithCount(1))
                        .resultOrPartial()
                        .ifPresent(payload -> tag.put("stack", payload));
            }
            buffer.writeNbt(tag);
            buffer.writeInt(stack.getCount());
        });
    }

    @Override
    public void readFromByteBuf(PacketBufferBC buffer) {
        super.readFromByteBuf(buffer);
        remainingDisplayRequired.clear();
        IntStream.range(0, buffer.readInt()).mapToObj(i -> {
            CompoundTag tag = buffer.readNbt();
            Tag payload = tag == null ? null : tag.get("stack");
            ItemStack stack = payload == null
                    ? ItemStack.EMPTY
                    : ItemStack.CODEC.parse(NBTUtilBC.registryAwareOps(), payload)
                            .resultOrPartial()
                            .orElse(ItemStack.EMPTY);
            int count = buffer.readInt();
            return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(count);
        }).forEach(remainingDisplayRequired::add);
    }
}
