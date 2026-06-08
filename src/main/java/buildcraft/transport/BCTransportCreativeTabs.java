/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.fabric.BCRegistries;
import buildcraft.fabric.BCSiliconCreativeEntries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.CreativeModeTab.Row;
import net.minecraft.world.level.ItemLike;

public final class BCTransportCreativeTabs {
   public static CreativeModeTab PIPES_TAB;
   public static CreativeModeTab PLUGS_TAB;
   public static final ResourceKey<CreativeModeTab> PIPES_TAB_KEY = BCRegistries.creativeTabKey("buildcrafttransport", "pipes");
   public static final ResourceKey<CreativeModeTab> PLUGS_TAB_KEY = BCRegistries.creativeTabKey("buildcrafttransport", "plugs");

   private BCTransportCreativeTabs() {
   }

   public static void register() {
      PIPES_TAB = BCRegistries.registerCreativeTab(
         "buildcrafttransport",
         "pipes",
         CreativeModeTab.builder(Row.TOP, 2)
            .title(Component.translatable("itemGroup.buildcraft.pipes"))
            .icon(() -> new ItemStack(BCTransportItems.PIPE_DIAMOND_ITEM))
            .displayItems((parameters, output) -> addPipeItems(output))
            .build()
      );
      PLUGS_TAB = BCRegistries.registerCreativeTab(
         "buildcrafttransport",
         "plugs",
         CreativeModeTab.builder(Row.TOP, 3)
            .title(Component.translatable("itemGroup.buildcraft.plugs"))
            .icon(() -> new ItemStack(BCTransportItems.PLUG_BLOCKER))
            .displayItems((parameters, output) -> addPlugItems(output))
            .build()
      );
   }

   private static void addPlugItems(Output output) {
      output.accept(BCTransportItems.PLUG_BLOCKER);
      output.accept(BCTransportItems.PLUG_POWER_ADAPTOR);

      for (DyeColor color : DyeColor.values()) {
         output.accept((ItemLike)BCTransportItems.WIRE_ITEMS.get(color));
      }

      BCSiliconCreativeEntries.addSiliconPlugItems(output);
   }

   private static void addPipeItems(Output output) {
      output.accept(BCTransportItems.FILTERED_BUFFER);
      output.accept(BCTransportItems.WATERPROOF);
      output.accept(BCTransportItems.PIPE_STRUCTURE);
      output.accept(BCTransportItems.PIPE_WOOD_ITEM);
      output.accept(BCTransportItems.PIPE_COBBLE_ITEM);
      output.accept(BCTransportItems.PIPE_STONE_ITEM);
      output.accept(BCTransportItems.PIPE_QUARTZ_ITEM);
      output.accept(BCTransportItems.PIPE_IRON_ITEM);
      output.accept(BCTransportItems.PIPE_GOLD_ITEM);
      output.accept(BCTransportItems.PIPE_CLAY_ITEM);
      output.accept(BCTransportItems.PIPE_SANDSTONE_ITEM);
      output.accept(BCTransportItems.PIPE_VOID_ITEM);
      output.accept(BCTransportItems.PIPE_OBSIDIAN_ITEM);
      output.accept(BCTransportItems.PIPE_DIAMOND_ITEM);
      output.accept(BCTransportItems.PIPE_DIAMOND_WOOD_ITEM);
      output.accept(BCTransportItems.PIPE_LAPIS_ITEM);
      output.accept(BCTransportItems.PIPE_DAIZULI_ITEM);
      output.accept(BCTransportItems.PIPE_EMZULI_ITEM);
      output.accept(BCTransportItems.PIPE_STRIPES_ITEM);
      output.accept(BCTransportItems.PIPE_WOOD_FLUID);
      output.accept(BCTransportItems.PIPE_COBBLE_FLUID);
      output.accept(BCTransportItems.PIPE_STONE_FLUID);
      output.accept(BCTransportItems.PIPE_QUARTZ_FLUID);
      output.accept(BCTransportItems.PIPE_GOLD_FLUID);
      output.accept(BCTransportItems.PIPE_IRON_FLUID);
      output.accept(BCTransportItems.PIPE_CLAY_FLUID);
      output.accept(BCTransportItems.PIPE_SANDSTONE_FLUID);
      output.accept(BCTransportItems.PIPE_VOID_FLUID);
      output.accept(BCTransportItems.PIPE_DIAMOND_FLUID);
      output.accept(BCTransportItems.PIPE_DIAMOND_WOOD_FLUID);
      output.accept(BCTransportItems.PIPE_WOOD_POWER);
      output.accept(BCTransportItems.PIPE_COBBLE_POWER);
      output.accept(BCTransportItems.PIPE_STONE_POWER);
      output.accept(BCTransportItems.PIPE_QUARTZ_POWER);
      output.accept(BCTransportItems.PIPE_IRON_POWER);
      output.accept(BCTransportItems.PIPE_GOLD_POWER);
      output.accept(BCTransportItems.PIPE_SANDSTONE_POWER);
      output.accept(BCTransportItems.PIPE_DIAMOND_POWER);
      output.accept(BCTransportItems.PIPE_DIAMOND_WOOD_POWER);
      output.accept(BCTransportItems.PIPE_WOOD_RF);
      output.accept(BCTransportItems.PIPE_COBBLE_RF);
      output.accept(BCTransportItems.PIPE_STONE_RF);
      output.accept(BCTransportItems.PIPE_QUARTZ_RF);
      output.accept(BCTransportItems.PIPE_IRON_RF);
      output.accept(BCTransportItems.PIPE_GOLD_RF);
      output.accept(BCTransportItems.PIPE_SANDSTONE_RF);
      output.accept(BCTransportItems.PIPE_DIAMOND_RF);
      output.accept(BCTransportItems.PIPE_DIAMOND_WOOD_RF);
   }
}
