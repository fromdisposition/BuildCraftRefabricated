/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.fabric.BCRegistries;
import buildcraft.transport.item.ItemPipeHolder;
import buildcraft.transport.item.ItemPluggableBlocker;
import buildcraft.transport.item.ItemPluggablePowerAdaptor;
import buildcraft.transport.item.ItemWire;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public final class BCTransportItems {
   public static DataComponentType<DyeColor> PIPE_COLOUR;
   public static BlockItem FILTERED_BUFFER;
   public static Item WATERPROOF;
   public static ItemPluggableBlocker PLUG_BLOCKER;
   public static ItemPluggablePowerAdaptor PLUG_POWER_ADAPTOR;
   public static Map<DyeColor, ItemWire> WIRE_ITEMS;
   public static ItemPipeHolder PIPE_STRUCTURE;
   public static ItemPipeHolder PIPE_WOOD_ITEM;
   public static ItemPipeHolder PIPE_COBBLE_ITEM;
   public static ItemPipeHolder PIPE_STONE_ITEM;
   public static ItemPipeHolder PIPE_QUARTZ_ITEM;
   public static ItemPipeHolder PIPE_IRON_ITEM;
   public static ItemPipeHolder PIPE_GOLD_ITEM;
   public static ItemPipeHolder PIPE_CLAY_ITEM;
   public static ItemPipeHolder PIPE_SANDSTONE_ITEM;
   public static ItemPipeHolder PIPE_VOID_ITEM;
   public static ItemPipeHolder PIPE_OBSIDIAN_ITEM;
   public static ItemPipeHolder PIPE_DIAMOND_ITEM;
   public static ItemPipeHolder PIPE_DIAMOND_WOOD_ITEM;
   public static ItemPipeHolder PIPE_LAPIS_ITEM;
   public static ItemPipeHolder PIPE_DAIZULI_ITEM;
   public static ItemPipeHolder PIPE_EMZULI_ITEM;
   public static ItemPipeHolder PIPE_STRIPES_ITEM;
   public static ItemPipeHolder PIPE_WOOD_FLUID;
   public static ItemPipeHolder PIPE_COBBLE_FLUID;
   public static ItemPipeHolder PIPE_STONE_FLUID;
   public static ItemPipeHolder PIPE_QUARTZ_FLUID;
   public static ItemPipeHolder PIPE_GOLD_FLUID;
   public static ItemPipeHolder PIPE_IRON_FLUID;
   public static ItemPipeHolder PIPE_CLAY_FLUID;
   public static ItemPipeHolder PIPE_SANDSTONE_FLUID;
   public static ItemPipeHolder PIPE_VOID_FLUID;
   public static ItemPipeHolder PIPE_DIAMOND_FLUID;
   public static ItemPipeHolder PIPE_DIAMOND_WOOD_FLUID;
   public static ItemPipeHolder PIPE_WOOD_POWER;
   public static ItemPipeHolder PIPE_COBBLE_POWER;
   public static ItemPipeHolder PIPE_STONE_POWER;
   public static ItemPipeHolder PIPE_QUARTZ_POWER;
   public static ItemPipeHolder PIPE_IRON_POWER;
   public static ItemPipeHolder PIPE_GOLD_POWER;
   public static ItemPipeHolder PIPE_SANDSTONE_POWER;
   public static ItemPipeHolder PIPE_DIAMOND_POWER;
   public static ItemPipeHolder PIPE_DIAMOND_WOOD_POWER;
   public static ItemPipeHolder PIPE_WOOD_RF;
   public static ItemPipeHolder PIPE_COBBLE_RF;
   public static ItemPipeHolder PIPE_STONE_RF;
   public static ItemPipeHolder PIPE_QUARTZ_RF;
   public static ItemPipeHolder PIPE_IRON_RF;
   public static ItemPipeHolder PIPE_GOLD_RF;
   public static ItemPipeHolder PIPE_SANDSTONE_RF;
   public static ItemPipeHolder PIPE_DIAMOND_RF;
   public static ItemPipeHolder PIPE_DIAMOND_WOOD_RF;

   private BCTransportItems() {
   }

   public static void register() {
      Objects.requireNonNull(BCTransportPipes.woodItem, "BCTransportPipes.preInit() must run before BCTransportItems.register()");
      Objects.requireNonNull(BCTransportPlugs.blocker, "BCTransportPlugs.preInit() must run before BCTransportItems.register()");
      PIPE_COLOUR = BCRegistries.registerDataComponent(
         "buildcrafttransport", "pipe_colour", builder -> builder.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
      );
      FILTERED_BUFFER = BCRegistries.registerBlockItem("buildcrafttransport", "filtered_buffer", BCTransportBlocks.FILTERED_BUFFER);
      WATERPROOF = BCRegistries.registerItem("buildcrafttransport", "waterproof", Item::new);
      PLUG_BLOCKER = BCRegistries.registerItem("buildcrafttransport", "plug_blocker", ItemPluggableBlocker::new);
      PLUG_POWER_ADAPTOR = BCRegistries.registerItem("buildcrafttransport", "plug_power_adaptor", ItemPluggablePowerAdaptor::new);
      Map<DyeColor, ItemWire> wires = new EnumMap<>(DyeColor.class);

      for (DyeColor color : DyeColor.values()) {
         DyeColor c = color;
         wires.put(color, BCRegistries.registerItemDynamicName("buildcrafttransport", "wire_" + color.getName(), props -> new ItemWire(props, c)));
      }

      WIRE_ITEMS = Collections.unmodifiableMap(wires);
      PIPE_STRUCTURE = registerPipeItem("pipe_structure", () -> BCTransportPipes.structure);
      PIPE_WOOD_ITEM = registerPipeItem("pipe_wood_item", () -> BCTransportPipes.woodItem);
      PIPE_COBBLE_ITEM = registerPipeItem("pipe_cobble_item", () -> BCTransportPipes.cobbleItem);
      PIPE_STONE_ITEM = registerPipeItem("pipe_stone_item", () -> BCTransportPipes.stoneItem);
      PIPE_QUARTZ_ITEM = registerPipeItem("pipe_quartz_item", () -> BCTransportPipes.quartzItem);
      PIPE_IRON_ITEM = registerPipeItem("pipe_iron_item", () -> BCTransportPipes.ironItem);
      PIPE_GOLD_ITEM = registerPipeItem("pipe_gold_item", () -> BCTransportPipes.goldItem);
      PIPE_CLAY_ITEM = registerPipeItem("pipe_clay_item", () -> BCTransportPipes.clayItem);
      PIPE_SANDSTONE_ITEM = registerPipeItem("pipe_sandstone_item", () -> BCTransportPipes.sandstoneItem);
      PIPE_VOID_ITEM = registerPipeItem("pipe_void_item", () -> BCTransportPipes.voidItem);
      PIPE_OBSIDIAN_ITEM = registerPipeItem("pipe_obsidian_item", () -> BCTransportPipes.obsidianItem);
      PIPE_DIAMOND_ITEM = registerPipeItem("pipe_diamond_item", () -> BCTransportPipes.diamondItem);
      PIPE_DIAMOND_WOOD_ITEM = registerPipeItem("pipe_diamond_wood_item", () -> BCTransportPipes.diaWoodItem);
      PIPE_LAPIS_ITEM = registerPipeItem("pipe_lapis_item", () -> BCTransportPipes.lapisItem);
      PIPE_DAIZULI_ITEM = registerPipeItem("pipe_daizuli_item", () -> BCTransportPipes.daizuliItem);
      PIPE_EMZULI_ITEM = registerPipeItem("pipe_emzuli_item", () -> BCTransportPipes.emzuliItem);
      PIPE_STRIPES_ITEM = registerPipeItem("pipe_stripes_item", () -> BCTransportPipes.stripesItem);
      PIPE_WOOD_FLUID = registerPipeItem("pipe_wood_fluid", () -> BCTransportPipes.woodFluid);
      PIPE_COBBLE_FLUID = registerPipeItem("pipe_cobble_fluid", () -> BCTransportPipes.cobbleFluid);
      PIPE_STONE_FLUID = registerPipeItem("pipe_stone_fluid", () -> BCTransportPipes.stoneFluid);
      PIPE_QUARTZ_FLUID = registerPipeItem("pipe_quartz_fluid", () -> BCTransportPipes.quartzFluid);
      PIPE_GOLD_FLUID = registerPipeItem("pipe_gold_fluid", () -> BCTransportPipes.goldFluid);
      PIPE_IRON_FLUID = registerPipeItem("pipe_iron_fluid", () -> BCTransportPipes.ironFluid);
      PIPE_CLAY_FLUID = registerPipeItem("pipe_clay_fluid", () -> BCTransportPipes.clayFluid);
      PIPE_SANDSTONE_FLUID = registerPipeItem("pipe_sandstone_fluid", () -> BCTransportPipes.sandstoneFluid);
      PIPE_VOID_FLUID = registerPipeItem("pipe_void_fluid", () -> BCTransportPipes.voidFluid);
      PIPE_DIAMOND_FLUID = registerPipeItem("pipe_diamond_fluid", () -> BCTransportPipes.diamondFluid);
      PIPE_DIAMOND_WOOD_FLUID = registerPipeItem("pipe_diamond_wood_fluid", () -> BCTransportPipes.diaWoodFluid);
      PIPE_WOOD_POWER = registerPipeItem("pipe_wood_power", () -> BCTransportPipes.woodPower);
      PIPE_COBBLE_POWER = registerPipeItem("pipe_cobble_power", () -> BCTransportPipes.cobblePower);
      PIPE_STONE_POWER = registerPipeItem("pipe_stone_power", () -> BCTransportPipes.stonePower);
      PIPE_QUARTZ_POWER = registerPipeItem("pipe_quartz_power", () -> BCTransportPipes.quartzPower);
      PIPE_IRON_POWER = registerPipeItem("pipe_iron_power", () -> BCTransportPipes.ironPower);
      PIPE_GOLD_POWER = registerPipeItem("pipe_gold_power", () -> BCTransportPipes.goldPower);
      PIPE_SANDSTONE_POWER = registerPipeItem("pipe_sandstone_power", () -> BCTransportPipes.sandstonePower);
      PIPE_DIAMOND_POWER = registerPipeItem("pipe_diamond_power", () -> BCTransportPipes.diamondPower);
      PIPE_DIAMOND_WOOD_POWER = registerPipeItem("pipe_diamond_wood_power", () -> BCTransportPipes.diaWoodPower);
      // RF pipe items registered in BCTransportRfPipes.preInit() — only when TRE is loaded
   }

   static ItemPipeHolder registerPipeItem(String path, Supplier<PipeDefinition> definition) {
      return BCRegistries.registerItem(
         "buildcrafttransport", path, props -> new ItemPipeHolder(BCTransportBlocks.PIPE_HOLDER, definition, props).registerWithPipeApi()
      );
   }
}
