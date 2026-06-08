/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.plug.PluggableLens;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class ItemPluggableLens extends Item implements IItemPluggable {
   public ItemPluggableLens(Properties properties) {
      super(properties);
   }

   public Component getName(ItemStack stack) {
      DyeColor colour = getColour(stack);
      boolean filter = isFilter(stack);
      String colourName = colour == null ? "Clear" : ColourUtil.getTextFullTooltip(colour);
      String typeName = filter ? "Filter" : "Lens";
      return Component.literal(colourName + " " + typeName);
   }

   @Nonnull
   public ItemStack getStack(@Nullable DyeColor colour, boolean isFilter) {
      ItemStack stack = new ItemStack(this);
      CompoundTag nbt = NBTUtilBC.getItemData(stack);
      if (colour != null) {
         nbt.putString("colour", colour.getName());
      }

      nbt.putBoolean("isFilter", isFilter);
      NBTUtilBC.setItemData(stack, nbt);
      return stack;
   }

   @Nullable
   public static DyeColor getColour(@Nonnull ItemStack stack) {
      CompoundTag nbt = NBTUtilBC.getItemData(stack);
      return nbt.contains("colour") ? DyeColor.byName(nbt.getStringOr("colour", ""), null) : null;
   }

   public static boolean isFilter(@Nonnull ItemStack stack) {
      return NBTUtilBC.getItemData(stack).getBooleanOr("isFilter", false);
   }

   @Nullable
   @Override
   public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
      IPipe pipe = holder.getPipe();
      if (pipe != null && pipe.getFlow() instanceof IFlowItems) {
         DyeColor colour = getColour(stack);
         boolean filter = isFilter(stack);
         SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), Blocks.GLASS.defaultBlockState());
         return new PluggableLens(BCSiliconPlugs.lens, holder, side, colour, filter);
      } else {
         return null;
      }
   }

   @Nonnull
   @Override
   public AABB getPlacementBoundingBox(@Nonnull ItemStack stack, Direction side) {
      return PluggableLens.boundingBoxFor(side);
   }
}
