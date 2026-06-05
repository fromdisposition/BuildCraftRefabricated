package buildcraft.core;

import net.minecraft.world.item.Item;

import buildcraft.api.items.FluidItemDrops;
import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.core.item.ItemGoggles;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemMarkerConnector;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.core.item.ItemVolumeBox;
import buildcraft.core.item.ItemWrench_Neptune;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;

public class BCCoreItems {
    public static ItemWrench_Neptune WRENCH;
    public static ItemFragileFluidContainer FRAGILE_FLUID_CONTAINER;
    public static ItemMarkerConnector MARKER_CONNECTOR;
    public static ItemVolumeBox VOLUME_BOX;
    public static ItemPaintbrush_BC8 PAINTBRUSH;
    public static ItemList_BC8 LIST;

    public static Item GEAR_WOOD;
    public static Item GEAR_STONE;
    public static Item GEAR_IRON;
    public static Item GEAR_GOLD;
    public static Item GEAR_DIAMOND;
    public static Item DIAMOND_SHARD;

    public static net.minecraft.world.item.BlockItem ENGINE_REDSTONE;
    public static net.minecraft.world.item.BlockItem ENGINE_CREATIVE;
    public static net.minecraft.world.item.BlockItem MARKER_VOLUME;
    public static net.minecraft.world.item.BlockItem MARKER_PATH;

    public static net.minecraft.world.item.BlockItem SPRING_WATER;
    public static net.minecraft.world.item.BlockItem SPRING_OIL;
    public static net.minecraft.world.item.BlockItem DECORATED_LASER;

    public static net.minecraft.world.item.BlockItem DECORATED_DESTROY;
    public static net.minecraft.world.item.BlockItem DECORATED_BLUEPRINT;
    public static net.minecraft.world.item.BlockItem DECORATED_TEMPLATE;
    public static net.minecraft.world.item.BlockItem DECORATED_PAPER;
    public static net.minecraft.world.item.BlockItem DECORATED_LEATHER;

    public static ItemGoggles GOGGLES;
    public static net.minecraft.world.item.BlockItem POWER_TESTER;
    public static ItemMapLocation MAP_LOCATION;

    private BCCoreItems() {}

    public static void register() {
        WRENCH = BCRegistries.registerItem(BCCore.MODID, "wrench", ItemWrench_Neptune::new, p -> p.stacksTo(1));
        FRAGILE_FLUID_CONTAINER = BCRegistries.registerItem(BCCore.MODID, "fragile_fluid_container", ItemFragileFluidContainer::new, p -> p);
        FluidItemDrops.item = FRAGILE_FLUID_CONTAINER;
        MARKER_CONNECTOR = BCRegistries.registerItem(BCCore.MODID, "marker_connector", ItemMarkerConnector::new, p -> p.stacksTo(1));
        VOLUME_BOX = BCRegistries.registerItem(BCCore.MODID, "volume_box", ItemVolumeBox::new, p -> p.stacksTo(16));
        PAINTBRUSH = BCRegistries.registerItem(BCCore.MODID, "paintbrush", ItemPaintbrush_BC8::new, p -> p.stacksTo(1));
        LIST = BCRegistries.registerItem(BCCore.MODID, "list", ItemList_BC8::new, p -> p.stacksTo(1));

        GEAR_WOOD = BCRegistries.registerItem(BCCore.MODID, "gear_wood", Item::new);
        GEAR_STONE = BCRegistries.registerItem(BCCore.MODID, "gear_stone", Item::new);
        GEAR_IRON = BCRegistries.registerItem(BCCore.MODID, "gear_iron", Item::new);
        GEAR_GOLD = BCRegistries.registerItem(BCCore.MODID, "gear_gold", Item::new);
        GEAR_DIAMOND = BCRegistries.registerItem(BCCore.MODID, "gear_diamond", Item::new);
        DIAMOND_SHARD = BCRegistries.registerItem(BCCore.MODID, "diamond_shard", Item::new);

        ENGINE_REDSTONE = BCRegistries.registerBlockItem(BCCore.MODID, "engine_redstone", BCCoreBlocks.ENGINE_REDSTONE);
        ENGINE_CREATIVE = BCRegistries.registerBlockItem(BCCore.MODID, "engine_creative", BCCoreBlocks.ENGINE_CREATIVE);
        MARKER_VOLUME = BCRegistries.registerBlockItem(BCCore.MODID, "marker_volume", BCCoreBlocks.MARKER_VOLUME);
        MARKER_PATH = BCRegistries.registerBlockItem(BCCore.MODID, "marker_path", BCCoreBlocks.MARKER_PATH);

        SPRING_WATER = BCRegistries.registerBlockItem(BCCore.MODID, "spring_water", BCCoreBlocks.SPRING_WATER);
        SPRING_OIL = BCRegistries.registerBlockItem(BCCore.MODID, "spring_oil", BCCoreBlocks.SPRING_OIL);
        DECORATED_LASER = BCRegistries.registerBlockItem(BCCore.MODID, "decorated_laser", BCCoreBlocks.DECORATED_LASER);

        if (BCLib.DEV) {
            GOGGLES = BCRegistries.registerItem(BCCore.MODID, "goggles", ItemGoggles::new, p -> p.stacksTo(1).equippable(net.minecraft.world.entity.EquipmentSlot.HEAD));
            if (BCCoreBlocks.POWER_TESTER != null) {
                POWER_TESTER = BCRegistries.registerBlockItem(BCCore.MODID, "power_tester", BCCoreBlocks.POWER_TESTER);
            }
            MAP_LOCATION = BCRegistries.registerItem(BCCore.MODID, "map_location", ItemMapLocation::new, p -> p.stacksTo(1));
            if (BCCoreBlocks.DECORATED_DESTROY != null) {
                DECORATED_DESTROY = BCRegistries.registerBlockItem(BCCore.MODID, "decorated_destroy", BCCoreBlocks.DECORATED_DESTROY);
            }
            if (BCCoreBlocks.DECORATED_BLUEPRINT != null) {
                DECORATED_BLUEPRINT = BCRegistries.registerBlockItem(BCCore.MODID, "decorated_blueprint", BCCoreBlocks.DECORATED_BLUEPRINT);
            }
            if (BCCoreBlocks.DECORATED_TEMPLATE != null) {
                DECORATED_TEMPLATE = BCRegistries.registerBlockItem(BCCore.MODID, "decorated_template", BCCoreBlocks.DECORATED_TEMPLATE);
            }
            if (BCCoreBlocks.DECORATED_PAPER != null) {
                DECORATED_PAPER = BCRegistries.registerBlockItem(BCCore.MODID, "decorated_paper", BCCoreBlocks.DECORATED_PAPER);
            }
            if (BCCoreBlocks.DECORATED_LEATHER != null) {
                DECORATED_LEATHER = BCRegistries.registerBlockItem(BCCore.MODID, "decorated_leather", BCCoreBlocks.DECORATED_LEATHER);
            }
        }
    }
}
