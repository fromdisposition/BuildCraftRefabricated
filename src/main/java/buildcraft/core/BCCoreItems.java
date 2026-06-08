/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

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
   public static BlockItem ENGINE_REDSTONE;
   public static BlockItem ENGINE_CREATIVE;
   public static BlockItem MARKER_VOLUME;
   public static BlockItem MARKER_PATH;
   public static BlockItem SPRING_WATER;
   public static BlockItem SPRING_OIL;
   public static BlockItem DECORATED_LASER;
   public static BlockItem DECORATED_DESTROY;
   public static BlockItem DECORATED_BLUEPRINT;
   public static BlockItem DECORATED_TEMPLATE;
   public static BlockItem DECORATED_PAPER;
   public static BlockItem DECORATED_LEATHER;
   public static ItemGoggles GOGGLES;
   public static BlockItem POWER_TESTER;
   public static ItemMapLocation MAP_LOCATION;

   private BCCoreItems() {
   }

   public static void register() {
      WRENCH = BCRegistries.registerItem("buildcraftcore", "wrench", ItemWrench_Neptune::new, p -> p.stacksTo(1));
      FRAGILE_FLUID_CONTAINER = BCRegistries.registerItem("buildcraftcore", "fragile_fluid_container", ItemFragileFluidContainer::new, p -> p);
      FluidItemDrops.item = FRAGILE_FLUID_CONTAINER;
      MARKER_CONNECTOR = BCRegistries.registerItem("buildcraftcore", "marker_connector", ItemMarkerConnector::new, p -> p.stacksTo(1));
      VOLUME_BOX = BCRegistries.registerItem("buildcraftcore", "volume_box", ItemVolumeBox::new, p -> p.stacksTo(16));
      PAINTBRUSH = BCRegistries.registerItem("buildcraftcore", "paintbrush", ItemPaintbrush_BC8::new, p -> p.stacksTo(1));
      LIST = BCRegistries.registerItem("buildcraftcore", "list", ItemList_BC8::new, p -> p.stacksTo(1));
      GEAR_WOOD = BCRegistries.registerItem("buildcraftcore", "gear_wood", Item::new);
      GEAR_STONE = BCRegistries.registerItem("buildcraftcore", "gear_stone", Item::new);
      GEAR_IRON = BCRegistries.registerItem("buildcraftcore", "gear_iron", Item::new);
      GEAR_GOLD = BCRegistries.registerItem("buildcraftcore", "gear_gold", Item::new);
      GEAR_DIAMOND = BCRegistries.registerItem("buildcraftcore", "gear_diamond", Item::new);
      DIAMOND_SHARD = BCRegistries.registerItem("buildcraftcore", "diamond_shard", Item::new);
      ENGINE_REDSTONE = BCRegistries.registerBlockItem("buildcraftcore", "engine_redstone", BCCoreBlocks.ENGINE_REDSTONE);
      ENGINE_CREATIVE = BCRegistries.registerBlockItem("buildcraftcore", "engine_creative", BCCoreBlocks.ENGINE_CREATIVE);
      MARKER_VOLUME = BCRegistries.registerBlockItem("buildcraftcore", "marker_volume", BCCoreBlocks.MARKER_VOLUME);
      MARKER_PATH = BCRegistries.registerBlockItem("buildcraftcore", "marker_path", BCCoreBlocks.MARKER_PATH);
      SPRING_WATER = BCRegistries.registerBlockItem("buildcraftcore", "spring_water", BCCoreBlocks.SPRING_WATER);
      SPRING_OIL = BCRegistries.registerBlockItem("buildcraftcore", "spring_oil", BCCoreBlocks.SPRING_OIL);
      DECORATED_LASER = BCRegistries.registerBlockItem("buildcraftcore", "decorated_laser", BCCoreBlocks.DECORATED_LASER);
      MAP_LOCATION = BCRegistries.registerItem("buildcraftcore", "map_location", ItemMapLocation::new, p -> p.stacksTo(1));
      if (BCLib.DEV) {
         GOGGLES = BCRegistries.registerItem("buildcraftcore", "goggles", ItemGoggles::new, p -> p.stacksTo(1).equippable(EquipmentSlot.HEAD));
         if (BCCoreBlocks.POWER_TESTER != null) {
            POWER_TESTER = BCRegistries.registerBlockItem("buildcraftcore", "power_tester", BCCoreBlocks.POWER_TESTER);
         }

         if (BCCoreBlocks.DECORATED_DESTROY != null) {
            DECORATED_DESTROY = BCRegistries.registerBlockItem("buildcraftcore", "decorated_destroy", BCCoreBlocks.DECORATED_DESTROY);
         }

         if (BCCoreBlocks.DECORATED_BLUEPRINT != null) {
            DECORATED_BLUEPRINT = BCRegistries.registerBlockItem("buildcraftcore", "decorated_blueprint", BCCoreBlocks.DECORATED_BLUEPRINT);
         }

         if (BCCoreBlocks.DECORATED_TEMPLATE != null) {
            DECORATED_TEMPLATE = BCRegistries.registerBlockItem("buildcraftcore", "decorated_template", BCCoreBlocks.DECORATED_TEMPLATE);
         }

         if (BCCoreBlocks.DECORATED_PAPER != null) {
            DECORATED_PAPER = BCRegistries.registerBlockItem("buildcraftcore", "decorated_paper", BCCoreBlocks.DECORATED_PAPER);
         }

         if (BCCoreBlocks.DECORATED_LEATHER != null) {
            DECORATED_LEATHER = BCRegistries.registerBlockItem("buildcraftcore", "decorated_leather", BCCoreBlocks.DECORATED_LEATHER);
         }
      }
   }
}
