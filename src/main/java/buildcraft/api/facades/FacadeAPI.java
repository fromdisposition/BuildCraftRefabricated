/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.facades;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class FacadeAPI {
   public static final String IMC_MOD_TARGET = "buildcraftrefabricated";
   public static final String IMC_FACADE_DISABLE = "facade_disable_block";
   public static final String IMC_FACADE_CUSTOM = "facade_custom_map_block_item";
   public static final String NBT_CUSTOM_BLOCK_REG_KEY = "block_registry_name";
   public static final String NBT_CUSTOM_BLOCK_META = "block_meta";
   public static final String NBT_CUSTOM_ITEM_STACK = "item_stack";
   @Nullable
   public static IFacadeItem facadeItem;
   @Nullable
   public static IFacadeRegistry registry;

   private FacadeAPI() {
   }

   public static void disableBlock(Block block) {
      if (registry != null) {
         registry.disableBlock(block, IMC_MOD_TARGET);
      }
   }

   public static void mapStateToStack(BlockState state, ItemStack stack) {
      if (registry != null) {
         registry.mapStateToStack(state, stack);
      }
   }

   public static boolean isFacadeMessageId(String id) {
      return "facade_custom_map_block_item".equals(id) || "facade_disable_block".equals(id);
   }
}
