/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.items.IList;
import buildcraft.core.list.ContainerList;
import buildcraft.core.list.ListOpenContext;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.AdvancementUtil;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
//? if >= 1.21.10 {
import net.minecraft.world.item.component.TooltipDisplay;
//?}
import net.minecraft.world.level.Level;

public class ItemList_BC8 extends Item implements IList {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:list");

   public ItemList_BC8(Properties properties) {
      super(properties);
   }

   private static CompoundTag getCustomTag(@Nonnull ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return customData == null ? new CompoundTag() : customData.copyTag();
   }

   private static void setCustomTag(@Nonnull ItemStack stack, CompoundTag tag) {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public static void updateModelData(@Nonnull ItemStack stack) {
      boolean hasItems = ListHandler.hasItems(stack);
      if (hasItems) {
         stack.set(DataComponents.CUSTOM_MODEL_DATA, buildcraft.lib.compat.BcModelData.index(1.0F));
      } else {
         stack.remove(DataComponents.CUSTOM_MODEL_DATA);
      }
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
      if (level.isClientSide()) {
         // Hand off which hand opened the list to the client-side menu factory (BCCoreMenuTypes.LIST) through
         // ListOpenContext — a plain MenuProvider can't carry extra open data. Only the client consumes it; the
         // server builds the menu straight from openHand (ServerPlayer.openMenu -> MenuProvider.createMenu, never
         // MenuType's factory), so remembering it server-side would just leak one never-consumed entry per player.
         ListOpenContext.remember(player, hand);
      } else {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
         if (player instanceof ServerPlayer serverPlayer) {
            final InteractionHand openHand = hand;
            serverPlayer.openMenu(new MenuProvider() {
               public Component getDisplayName() {
                  return Component.translatable("gui.buildcraft.list");
               }

               public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
                  return new ContainerList(containerId, playerInv, openHand);
               }
            });
         }
      }

      return InteractionResult.SUCCESS;
   }

   public static void appendTooltipLines(ItemList_BC8 item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      String label = item.getLocationName(stack);
      if (label != null && !label.isEmpty()) {
         tooltip.add(Component.literal(label).withStyle(ChatFormatting.ITALIC));
      }
   }

   @Override
   public boolean matches(@Nonnull ItemStack stackList, @Nonnull ItemStack item) {
      return ListHandler.matches(stackList, item);
   }

   @Override
   public String getLocationName(@Nonnull ItemStack stack) {
      CompoundTag tag = getCustomTag(stack);
      return BcNbt.getString(tag, "label", "");
   }

   @Override
   public boolean setLocationName(@Nonnull ItemStack stack, String name) {
      CompoundTag tag = getCustomTag(stack);
      tag.putString("label", name);
      setCustomTag(stack, tag);
      return true;
   }
}
