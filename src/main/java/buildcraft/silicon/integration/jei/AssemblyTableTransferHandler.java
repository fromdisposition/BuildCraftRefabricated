/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.integration.jei.JeiTransferUtil;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;

import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.container.ContainerAssemblyTable;

public class AssemblyTableTransferHandler implements IRecipeTransferHandler<ContainerAssemblyTable, AssemblyRecipeJei> {
    private final IRecipeTransferHandlerHelper helper;

    public AssemblyTableTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<? extends ContainerAssemblyTable> getContainerClass() {
        return ContainerAssemblyTable.class;
    }

    @Override
    public Optional<MenuType<ContainerAssemblyTable>> getMenuType() {
        return Optional.of(BCSiliconMenuTypes.ASSEMBLY_TABLE);
    }

    @Override
    public IRecipeType<AssemblyRecipeJei> getRecipeType() {
        return AssemblyRecipeJeiTypes.ASSEMBLY;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(
            ContainerAssemblyTable container,
            AssemblyRecipeJei recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {

        Inventory inv = player.getInventory();

        List<ItemStack> need = new ArrayList<>();
        for (List<ItemStack> alternatives : recipe.inputSlots()) {
            if (alternatives.isEmpty()) continue;
            ItemStack pick = null;
            for (ItemStack alt : alternatives) {
                if (alt.isEmpty()) continue;
                if (JeiTransferUtil.countMatching(inv, alt) >= alt.getCount()) {
                    pick = alt;
                    break;
                }
            }
            if (pick == null) {
                pick = alternatives.get(0);
            }
            mergeInto(need, pick);
        }

        if (need.isEmpty()) {
            return helper.createInternalError();
        }

        for (ItemStack n : need) {
            if (JeiTransferUtil.countMatching(inv, n) < n.getCount()) {
                return helper.createUserErrorWithTooltip(
                        Component.translatable("gui.jei.transfer.buildcraft.missing"));
            }
        }

        if (doTransfer) {
            container.sendMessage(ContainerBC_Neptune.NET_JEI_TRANSFER_ITEMS, buf -> {
                buf.writeBoolean(maxTransfer);
                buf.writeVarInt(need.size());
                for (ItemStack n : need) {
                    buf.writeNbt(NBTUtilBC.itemStackToNBT(n));
                }
            });
        }
        return null;
    }

    private static void mergeInto(List<ItemStack> list, ItemStack stack) {
        for (ItemStack existing : list) {
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                existing.grow(stack.getCount());
                return;
            }
        }
        list.add(stack.copy());
    }
}

