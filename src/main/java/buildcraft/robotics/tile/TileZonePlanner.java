/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.tile;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.core.BCCore;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.robotics.BCRoboticsBlockEntities;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.zone.ZonePlan;

import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class TileZonePlanner extends TileBC_Neptune implements MenuProvider,
        buildcraft.lib.fabric.menu.BlockEntityExtendedMenu {

    public final ItemHandlerSimple invPaintbrushes = new ItemHandlerSimple(16, null);
    public final ItemHandlerSimple invInputPaintbrush = new ItemHandlerSimple(1, null);
    public final ItemHandlerSimple invInputMapLocation = new ItemHandlerSimple(1, null);
    public final ItemHandlerSimple invInputResult = new ItemHandlerSimple(1, null);
    public final ItemHandlerSimple invOutputPaintbrush = new ItemHandlerSimple(1, null);
    public final ItemHandlerSimple invOutputMapLocation = new ItemHandlerSimple(1, null);
    public final ItemHandlerSimple invOutputResult = new ItemHandlerSimple(1, null);

    private static final int PROGRESS_TARGET = 200;
    private static final String TAG_MAP_TYPE = "mapType";
    private static final String MAP_TYPE_CLEAN = "CLEAN";
    private static final String MAP_TYPE_ZONE = "ZONE";

    private int progressInput = -1;
    private int progressOutput = -1;

    public ZonePlan[] layers = new ZonePlan[16];

    public TileZonePlanner(BlockPos pos, BlockState state) {
        super(BCRoboticsBlockEntities.ZONE_PLANNER, pos, state);
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new ZonePlan();
        }
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        tickInput();
        tickOutput();
    }

    private void tickInput() {
        ItemStack brush = invInputPaintbrush.getStackInSlot(0);
        ItemStack map = invInputMapLocation.getStackInSlot(0);
        boolean canRun = isPaintbrush(brush) && isZoneMap(map) && invInputResult.getStackInSlot(0).isEmpty();
        if (!canRun) {
            if (progressInput != -1) {
                progressInput = -1;
                setChanged();
            }
            return;
        }
        if (progressInput < 0) {
            progressInput = 0;
        }
        progressInput++;
        if (progressInput >= PROGRESS_TARGET) {
            progressInput = -1;
            int layer = layerFor(brush);
            if (layer >= 0) {
                ZonePlan plan = new ZonePlan();
                readZoneFromMap(map, plan);
                layers[layer] = plan;
            }
            invInputResult.setStackInSlot(0, map.copy());
            invInputMapLocation.setStackInSlot(0, ItemStack.EMPTY);
        }
        setChanged();
    }

    private void tickOutput() {
        ItemStack brush = invOutputPaintbrush.getStackInSlot(0);
        ItemStack map = invOutputMapLocation.getStackInSlot(0);
        boolean canRun = isPaintbrush(brush) && isCleanMap(map) && invOutputResult.getStackInSlot(0).isEmpty();
        if (!canRun) {
            if (progressOutput != -1) {
                progressOutput = -1;
                setChanged();
            }
            return;
        }
        if (progressOutput < 0) {
            progressOutput = 0;
        }
        progressOutput++;
        if (progressOutput >= PROGRESS_TARGET) {
            progressOutput = -1;
            int layer = layerFor(brush);
            ItemStack result = map.copy();
            result.setCount(1);
            if (layer >= 0) {
                writeZoneToMap(result, layers[layer]);
            }
            invOutputResult.setStackInSlot(0, result);
            map.shrink(1);
            invOutputMapLocation.setStackInSlot(0, map.isEmpty() ? ItemStack.EMPTY : map);
        }
        setChanged();
    }

    public void applyPaint(int layer, int x, int z, boolean set) {
        if (layer < 0 || layer >= layers.length) {
            return;
        }
        if (layers[layer] == null) {
            layers[layer] = new ZonePlan();
        }
        layers[layer].set(x, z, set);
        setChanged();
    }

    private static boolean isPaintbrush(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemPaintbrush_BC8;
    }

    private static int layerFor(ItemStack brush) {
        DyeColor colour = brush.get(BCCore.BRUSH_COLOR);
        if (colour == null) {
            return -1;
        }
        return colour.getId();
    }

    private static String mapTypeOf(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return MAP_TYPE_CLEAN;
        }
        return data.copyTag().getString(TAG_MAP_TYPE).orElse(MAP_TYPE_CLEAN);
    }

    private static boolean isZoneMap(ItemStack stack) {
        return stack.getItem() instanceof ItemMapLocation && MAP_TYPE_ZONE.equals(mapTypeOf(stack));
    }

    private static boolean isCleanMap(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemMapLocation)) {
            return false;
        }
        String type = mapTypeOf(stack);
        return type.isEmpty() || MAP_TYPE_CLEAN.equals(type);
    }

    private static void readZoneFromMap(ItemStack map, ZonePlan plan) {
        CustomData data = map.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return;
        }
        plan.readFromNBT(data.copyTag());
    }

    private static void writeZoneToMap(ItemStack map, ZonePlan plan) {
        CustomData existing = map.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing == null ? new CompoundTag() : existing.copyTag();
        plan.writeToNBT(tag);
        tag.putString(TAG_MAP_TYPE, MAP_TYPE_ZONE);
        map.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        map.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                java.util.List.of(4.0f),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < layers.length; i++) {
            CompoundTag layerTag = new CompoundTag();
            layers[i].writeToNBT(layerTag);
            output.store("layer_" + i, CompoundTag.CODEC, layerTag);
        }
        output.store("invPaintbrushes", CompoundTag.CODEC, invPaintbrushes.serializeNBT());
        output.store("invInputPaintbrush", CompoundTag.CODEC, invInputPaintbrush.serializeNBT());
        output.store("invInputMapLocation", CompoundTag.CODEC, invInputMapLocation.serializeNBT());
        output.store("invInputResult", CompoundTag.CODEC, invInputResult.serializeNBT());
        output.store("invOutputPaintbrush", CompoundTag.CODEC, invOutputPaintbrush.serializeNBT());
        output.store("invOutputMapLocation", CompoundTag.CODEC, invOutputMapLocation.serializeNBT());
        output.store("invOutputResult", CompoundTag.CODEC, invOutputResult.serializeNBT());
        output.putInt("progressInput", progressInput);
        output.putInt("progressOutput", progressOutput);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < layers.length; i++) {
            final int idx = i;
            input.read("layer_" + i, CompoundTag.CODEC).ifPresent(tag -> {
                layers[idx] = new ZonePlan();
                layers[idx].readFromNBT(tag);
            });
        }
        input.read("invPaintbrushes", CompoundTag.CODEC).ifPresent(invPaintbrushes::deserializeNBT);
        input.read("invInputPaintbrush", CompoundTag.CODEC).ifPresent(invInputPaintbrush::deserializeNBT);
        input.read("invInputMapLocation", CompoundTag.CODEC).ifPresent(invInputMapLocation::deserializeNBT);
        input.read("invInputResult", CompoundTag.CODEC).ifPresent(invInputResult::deserializeNBT);
        input.read("invOutputPaintbrush", CompoundTag.CODEC).ifPresent(invOutputPaintbrush::deserializeNBT);
        input.read("invOutputMapLocation", CompoundTag.CODEC).ifPresent(invOutputMapLocation::deserializeNBT);
        input.read("invOutputResult", CompoundTag.CODEC).ifPresent(invOutputResult::deserializeNBT);
        progressInput = input.getIntOr("progressInput", -1);
        progressOutput = input.getIntOr("progressOutput", -1);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftrobotics.zone_planner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new ContainerZonePlanner(containerId, playerInv, this);
    }

    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("progress_input = " + progressInput);
        left.add("progress_output = " + progressOutput);
    }
}
