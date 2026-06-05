/*
 * Copyright (c) 2026 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.snapshot;

import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum EnumFluidHandlingMode {
    NO_REPLACE(() -> new ItemStack(Items.BARRIER),
               "gui.buildcraft.builder.fluidmode.no_replace"),
    REPLACE   (() -> new ItemStack(Items.BRICKS),
               "gui.buildcraft.builder.fluidmode.replace"),
    CLEAR     (() -> new ItemStack(Items.BUCKET),
               "gui.buildcraft.builder.fluidmode.clear");

    private final Supplier<ItemStack> iconSupplier;
    private final String tooltipKey;

    EnumFluidHandlingMode(Supplier<ItemStack> iconSupplier, String tooltipKey) {
        this.iconSupplier = iconSupplier;
        this.tooltipKey = tooltipKey;
    }

    public EnumFluidHandlingMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public ItemStack icon() {
        return iconSupplier.get();
    }

    public String tooltipKey() {
        return tooltipKey;
    }

    public static EnumFluidHandlingMode fromOrdinal(int ord) {
        if (ord < 0 || ord >= values().length) return NO_REPLACE;
        return values()[ord];
    }
}
