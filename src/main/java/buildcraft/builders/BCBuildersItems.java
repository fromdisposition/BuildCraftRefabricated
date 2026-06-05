/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.fabric.registry.DeferredItem;
import buildcraft.fabric.registry.DeferredRegister;

import buildcraft.api.enums.EnumSnapshotType;
import net.minecraft.world.item.BlockItem;
import buildcraft.fabric.registry.DeferredBlock;

import buildcraft.builders.item.ItemFillerPlanner;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;

public class BCBuildersItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BCBuilders.MODID);

    public static final DeferredItem<BlockItem> FRAME = ITEMS
            .registerSimpleBlockItem("frame", BCBuildersBlocks.FRAME);

    public static final DeferredItem<BlockItem> FILLER = ITEMS
            .registerSimpleBlockItem("filler", BCBuildersBlocks.FILLER);

    public static final DeferredItem<BlockItem> BUILDER = ITEMS
            .registerSimpleBlockItem("builder", BCBuildersBlocks.BUILDER);

    public static final DeferredItem<BlockItem> ARCHITECT = ITEMS
            .registerSimpleBlockItem("architect", BCBuildersBlocks.ARCHITECT);

    public static final DeferredItem<BlockItem> LIBRARY = ITEMS
            .registerSimpleBlockItem("library", BCBuildersBlocks.LIBRARY);

    public static final DeferredItem<BlockItem> REPLACER = ITEMS
            .registerSimpleBlockItem("replacer", BCBuildersBlocks.REPLACER);

    public static final DeferredItem<BlockItem> QUARRY = ITEMS
            .registerSimpleBlockItem("quarry", BCBuildersBlocks.QUARRY);

    public static final DeferredItem<ItemSnapshot> BLUEPRINT_CLEAN = ITEMS.registerItem(
            "blueprint_clean",
            props -> new ItemSnapshot(props, EnumSnapshotType.BLUEPRINT, false),
            props -> props.stacksTo(16));

    public static final DeferredItem<ItemSnapshot> BLUEPRINT_USED = ITEMS.registerItem(
            "blueprint_used",
            props -> new ItemSnapshot(props, EnumSnapshotType.BLUEPRINT, true),
            props -> props.stacksTo(1));

    public static final DeferredItem<ItemSnapshot> TEMPLATE_CLEAN = ITEMS.registerItem(
            "template_clean",
            props -> new ItemSnapshot(props, EnumSnapshotType.TEMPLATE, false),
            props -> props.stacksTo(16));

    public static final DeferredItem<ItemSnapshot> TEMPLATE_USED = ITEMS.registerItem(
            "template_used",
            props -> new ItemSnapshot(props, EnumSnapshotType.TEMPLATE, true),
            props -> props.stacksTo(1));

    public static final DeferredItem<ItemSchematicSingle> SCHEMATIC_SINGLE_CLEAN = ITEMS.registerItem(
            "schematic_single_clean",
            props -> new ItemSchematicSingle(props, false),
            props -> props.stacksTo(16));

    public static final DeferredItem<ItemSchematicSingle> SCHEMATIC_SINGLE_USED = ITEMS.registerItem(
            "schematic_single_used",
            props -> new ItemSchematicSingle(props, true),
            props -> props.stacksTo(1));

    public static final DeferredItem<ItemFillerPlanner> FILLER_PLANNER = ITEMS.registerItem(
            "filler_planner",
            ItemFillerPlanner::new,
            props -> props.stacksTo(1));

    public static void register() {
        ITEMS.register();
    }
}

