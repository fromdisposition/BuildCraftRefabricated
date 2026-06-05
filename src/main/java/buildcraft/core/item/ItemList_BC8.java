/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import buildcraft.api.items.IList;

import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.AdvancementUtil;

@SuppressWarnings("deprecation")
public class ItemList_BC8 extends Item implements IList {

    private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:list");

    public ItemList_BC8(Item.Properties properties) {
        super(properties);
    }

    private static CompoundTag getCustomTag(@Nonnull ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return new CompoundTag();
        }
        return customData.copyTag();
    }

    private static void setCustomTag(@Nonnull ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void updateModelData(@Nonnull ItemStack stack) {
        boolean hasItems = ListHandler.hasItems(stack);
        if (hasItems) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                    java.util.List.of(1.0f),
                    java.util.List.of(),
                    java.util.List.of(),
                    java.util.List.of()
            ));
        } else {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        buildcraft.core.list.ListOpenContext.remember(player, hand);
        if (!level.isClientSide()) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                final InteractionHand openHand = hand;
                serverPlayer.openMenu(new net.minecraft.world.MenuProvider() {
                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return Component.translatable("gui.buildcraft.list");
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                            int containerId, net.minecraft.world.entity.player.Inventory playerInv, Player p) {
                        return new buildcraft.core.list.ContainerList(containerId, playerInv, openHand);
                    }
                });
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        String label = getLocationName(stack);
        if (label != null && !label.isEmpty()) {
            tooltip.accept(Component.literal(label).withStyle(net.minecraft.ChatFormatting.ITALIC));
        }
    }

    @Override
    public boolean matches(@Nonnull ItemStack stackList, @Nonnull ItemStack item) {
        return ListHandler.matches(stackList, item);
    }

    @Override
    public String getLocationName(@Nonnull ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        return tag.getString("label").orElse("");
    }

    @Override
    public boolean setLocationName(@Nonnull ItemStack stack, String name) {
        CompoundTag tag = getCustomTag(stack);
        tag.putString("label", name);
        setCustomTag(stack, tag);
        return true;
    }
}
