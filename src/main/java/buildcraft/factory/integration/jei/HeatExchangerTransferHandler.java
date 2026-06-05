/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.integration.jei;

import java.util.Optional;

import javax.annotation.Nullable;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.container.ContainerHeatExchange;
import buildcraft.lib.integration.jei.JeiTransferUtil;
import buildcraft.lib.gui.ContainerBC_Neptune;

public class HeatExchangerTransferHandler implements IRecipeTransferHandler<ContainerHeatExchange, HeatExchangerRecipePair> {
    private final IRecipeTransferHandlerHelper helper;

    public HeatExchangerTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<? extends ContainerHeatExchange> getContainerClass() {
        return ContainerHeatExchange.class;
    }

    @Override
    public Optional<MenuType<ContainerHeatExchange>> getMenuType() {
        return Optional.of(BCFactoryMenuTypes.HEAT_EXCHANGE);
    }

    @Override
    public IRecipeType<HeatExchangerRecipePair> getRecipeType() {
        return HeatExchangerRecipeTypes.PAIR;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(
            ContainerHeatExchange container,
            HeatExchangerRecipePair pair,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {

        Item slot0Bucket = bucketOf(pair.coolable().in());
        Item slot1Bucket = bucketOf(pair.heatable().in());
        if (slot0Bucket == Items.AIR || slot1Bucket == Items.AIR) {
            return missing();
        }

        Inventory inv = player.getInventory();

        if (slot0Bucket == slot1Bucket) {
            if (JeiTransferUtil.countMatching(inv, new ItemStack(slot0Bucket)) < 2) return missing();
        } else if (JeiTransferUtil.countMatching(inv, new ItemStack(slot0Bucket)) < 1
                || JeiTransferUtil.countMatching(inv, new ItemStack(slot1Bucket)) < 1) {
            return missing();
        }

        if (doTransfer) {
            String slot0Id = BuiltInRegistries.ITEM.getKey(slot0Bucket).toString();
            String slot1Id = BuiltInRegistries.ITEM.getKey(slot1Bucket).toString();
            container.sendMessage(ContainerBC_Neptune.NET_JEI_TRANSFER_BUCKETS, buf -> {
                buf.writeVarInt(2);
                buf.writeVarInt(0);
                buf.writeUtf(slot0Id);
                buf.writeVarInt(1);
                buf.writeUtf(slot1Id);
            });
        }
        return null;
    }

    private static Item bucketOf(FluidStack fluid) {
        return (fluid == null || fluid.isEmpty()) ? Items.AIR : fluid.getFluid().getBucket();
    }

    private IRecipeTransferError missing() {
        return helper.createUserErrorWithTooltip(
                Component.translatable("gui.jei.transfer.buildcraft.missing"));
    }
}

