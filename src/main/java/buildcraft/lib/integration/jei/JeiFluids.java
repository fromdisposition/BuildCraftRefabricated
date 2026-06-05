/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.integration.jei;

import java.util.Optional;

import net.minecraft.client.renderer.Rect2i;

import buildcraft.lib.fabric.transfer.TransferConvert;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fluid.FluidResource;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.JeiFluidIngredient;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.runtime.IClickableIngredient;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

public final class JeiFluids {

    private JeiFluids() {
    }

    public static void addFluidStack(IRecipeSlotBuilder slot, FluidStack stack) {
        addFluidStack(slot, stack, stack.getAmount());
    }

    public static void addFluidStack(IRecipeSlotBuilder slot, FluidStack stack, long amount) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        FluidVariant variant = TransferConvert.toVariant(FluidResource.of(stack));
        slot.add(FabricTypes.FLUID_STACK, new JeiFluidIngredient(variant, amount));
    }

    public static Optional<? extends IClickableIngredient<?>> clickableFluidIngredient(
            IClickableIngredientFactory factory,
            FluidResource fluid,
            long amount,
            int x,
            int y,
            int width,
            int height
    ) {
        if (fluid == null || fluid.isEmpty() || amount <= 0) {
            return Optional.empty();
        }
        FluidVariant variant = TransferConvert.toVariant(fluid);
        return factory.createBuilder(FabricTypes.FLUID_STACK, new JeiFluidIngredient(variant, amount))
                .buildWithArea(new Rect2i(x, y, width, height));
    }
}
