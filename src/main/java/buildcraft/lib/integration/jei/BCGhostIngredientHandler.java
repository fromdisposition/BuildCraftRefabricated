/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.integration.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.slot.IPhantomSlot;

public class BCGhostIngredientHandler<T extends GuiBC8<?>> implements IGhostIngredientHandler<T> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(T gui, ITypedIngredient<I> ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();

        if (!(ingredient.getIngredient() instanceof ItemStack)) {
            return targets;
        }

        ContainerBC_Neptune container = gui.getMenu();
        for (int i = 0; i < container.slots.size(); i++) {
            Slot slot = container.slots.get(i);
            if (slot instanceof IPhantomSlot) {
                final int slotIndex = i;

                int x = gui.getGuiLeftPos() + slot.x;
                int y = gui.getGuiTopPos() + slot.y;
                targets.add(new PhantomSlotTarget<>(container, slotIndex, x, y));
            }
        }

        return targets;
    }

    @Override
    public void onComplete() {

    }

    private static class PhantomSlotTarget<I> implements Target<I> {
        private final ContainerBC_Neptune container;
        private final int slotIndex;
        private final Rect2i area;

        PhantomSlotTarget(ContainerBC_Neptune container, int slotIndex, int x, int y) {
            this.container = container;
            this.slotIndex = slotIndex;

            this.area = new Rect2i(x, y, 16, 16);
        }

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(I ingredient) {
            if (ingredient instanceof ItemStack stack) {

                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(stack.getItem()).toString();
                container.sendMessage(ContainerBC_Neptune.NET_GHOST_SLOT_SET, buf -> {
                    buf.writeShort(slotIndex);
                    buf.writeUtf(itemId);
                });
            }
        }
    }
}

