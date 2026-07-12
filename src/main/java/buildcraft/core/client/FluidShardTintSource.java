/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client;

import buildcraft.core.BCCore;
import buildcraft.lib.client.fluid.BcFluidRenderLookup;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.fluid.stack.SimpleFluidContent;
//? if >= 1.21.10 {
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
//?}
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

//? if >= 1.21.10 {
public final class FluidShardTintSource implements ItemTintSource {
//?} else {
/*public final class FluidShardTintSource {
*///?}
   public static final FluidShardTintSource INSTANCE = new FluidShardTintSource();
   //? if >= 1.21.10 {
   public static final MapCodec<FluidShardTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);
   //?}

   private FluidShardTintSource() {
   }

   public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
      SimpleFluidContent content = (SimpleFluidContent)stack.getOrDefault(BCCore.FLUID_CONTENT, SimpleFluidContent.EMPTY);
      FluidStack fluid = content.copy();
      if (fluid.isEmpty()) {
         return -1;
      } else {
         return BcFluidRenderLookup.itemMaskTint(fluid, level);
      }
   }

   //? if >= 1.21.10 {
   public MapCodec<? extends ItemTintSource> type() {
      return MAP_CODEC;
   }
   //?}
}
