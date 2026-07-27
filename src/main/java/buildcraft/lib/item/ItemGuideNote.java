/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.fabric.BCLibClientBridge;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class ItemGuideNote extends Item {
   public static final String TAG_PAGE = "page";
   private static final String DEFAULT_BOOK = "buildcraftcore:main";

   public ItemGuideNote(Properties properties) {
      super(properties);
   }

   public static ItemStack withPage(Identifier page) {
      ItemStack stack = new ItemStack(BCLibItems.GUIDE_NOTE);
      CompoundTag tag = new CompoundTag();
      tag.putString("page", page.toString());
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      return stack;
   }

   @Nullable
   public static Identifier getLinkedPage(ItemStack stack) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (data == null) {
         return null;
      }

      String page = BcNbt.getString(data.copyTag(), "page", "");
      return page.isEmpty() ? null : Identifier.tryParse(page);
   }

   //? if >= 1.21.10 {
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      return this.bcUse(level, player, hand);
   }
   //?} else {
   /*public net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack> use(Level level, Player player, InteractionHand hand) {
      return buildcraft.lib.compat.BcInteract.toUse(this.bcUse(level, player, hand), player, hand);
   }
   *///?}

   private InteractionResult bcUse(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      Identifier page = getLinkedPage(stack);
      if (level.isClientSide()) {
         if (page != null) {
            BCLibClientBridge.openGuidePage("buildcraftcore:main", page);
         } else {
            BCLibClientBridge.openGuideScreen("buildcraftcore:main");
         }
      }

      return InteractionResult.SUCCESS;
   }

   public static void appendTooltipLines(ItemGuideNote item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      Identifier page = getLinkedPage(stack);
      if (page != null) {
         tooltip.add(Component.translatable("buildcraft.guide.note.linked", new Object[]{page.toString()}));
      }
   }
}
