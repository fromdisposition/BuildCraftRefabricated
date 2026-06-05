/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.world.item.ItemStack;

public class GuiStack implements ISimpleDrawable {
    private final ItemStack stack;

    private static BCGraphics currentGraphics;

    public static void setGuiGraphics(BCGraphics graphics) {
        currentGraphics = graphics;
    }

    public GuiStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void drawAt(double x, double y) {
        if (currentGraphics == null || stack == null || stack.isEmpty()) {
            return;
        }

        currentGraphics.fakeItem(stack, (int) x, (int) y);
    }
}
