/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.item;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.tooltip.BlueprintPreviewTooltipComponent;
import buildcraft.lib.misc.HashUtil;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.CustomData;

public class ItemSnapshot extends Item {
   private final EnumSnapshotType snapshotType;
   private final boolean used;

   public ItemSnapshot(Properties properties, EnumSnapshotType snapshotType, boolean used) {
      super(properties);
      this.snapshotType = snapshotType;
      this.used = used;
   }

   public EnumSnapshotType getSnapshotType() {
      return this.snapshotType;
   }

   public boolean isUsed() {
      return this.used;
   }

   public ItemStack createUsedStack(Snapshot.Header header) {
      ItemStack stack = new ItemStack(this);
      CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
      tag.put("header", header.serializeNBT());
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      return stack;
   }

   public static Snapshot.Header getHeader(ItemStack stack) {
      if (stack.getItem() instanceof ItemSnapshot snapshotItem && snapshotItem.used) {
         CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
         if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            if (nbt.contains("header")) {
               return new Snapshot.Header(BcNbt.getCompound(nbt, "header"));
            }
         }
      }

      return null;
   }

   public static void appendTooltipLines(ItemSnapshot item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      Snapshot.Header header = getHeader(stack);
      if (header == null) {
         tooltip.add(Component.translatable("item.blueprint.blank").withStyle(ChatFormatting.GRAY));
      } else {
         if (!tooltip.isEmpty()) {
            tooltip.set(0, Component.literal(header.name));
         }

         if (flag.isAdvanced()) {
            tooltip.add(Component.literal("Hash: " + HashUtil.convertHashToString(header.key.hash)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Date: " + header.created).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Owner UUID: " + header.owner).withStyle(ChatFormatting.GRAY));
         }
      }
   }

   @Override
   public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
      Snapshot.Header header = getHeader(itemStack);
      return header != null ? Optional.of(new BlueprintPreviewTooltipComponent(header)) : Optional.empty();
   }
}
