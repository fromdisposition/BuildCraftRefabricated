/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.plug.PluggableGate;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomModelData;
//? if >= 1.21.10 {
import net.minecraft.world.item.component.TooltipDisplay;
//?}
import net.minecraft.world.level.block.state.BlockState;

public class ItemPluggableGate extends Item implements IItemPluggable {
   public ItemPluggableGate(Properties properties) {
      super(properties);
   }

   public static GateVariant getVariant(ItemStack stack) {
      return new GateVariant(buildcraft.lib.nbt.BcNbt.getCompound(NBTUtilBC.getItemData(stack), "gate"));
   }

   public ItemStack getStack(GateVariant variant) {
      ItemStack stack = new ItemStack(this);
      CompoundTag data = NBTUtilBC.getItemData(stack);
      data.put("gate", variant.writeToNBT());
      NBTUtilBC.setItemData(stack, data);
      stack.set(DataComponents.CUSTOM_MODEL_DATA, buildcraft.lib.compat.BcModelData.variant(variant.getVariantName()));
      return stack;
   }

   @Override
   public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
      GateVariant variant = getVariant(stack);
      BlockState renderState = variant.material.block.defaultBlockState();
      SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), renderState);
      PluggableDefinition def = BCSiliconPlugs.gate;
      return new PluggableGate(def, holder, side, variant);
   }

   public Component getName(ItemStack stack) {
      return getVariant(stack).getLocalizedName();
   }

   public static void appendTooltipLines(ItemPluggableGate item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      GateVariant variant = getVariant(stack);
      tooltip.add(Component.translatable("gate.slots", new Object[]{variant.numSlots}));
      if (variant.numTriggerArgs == variant.numActionArgs) {
         if (variant.numTriggerArgs > 0) {
            tooltip.add(Component.translatable("gate.params", new Object[]{variant.numTriggerArgs}));
         }
      } else {
         if (variant.numTriggerArgs > 0) {
            tooltip.add(Component.translatable("gate.params.trigger", new Object[]{variant.numTriggerArgs}));
         }

         if (variant.numActionArgs > 0) {
            tooltip.add(Component.translatable("gate.params.action", new Object[]{variant.numActionArgs}));
         }
      }
   }
}
