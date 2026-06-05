/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import net.minecraft.client.Minecraft;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.lib.misc.FluidUtilBC;

@SuppressWarnings("deprecation")
public class GuiFluid implements ISimpleDrawable {
    private final FluidStack stack;

    private static BCGraphics currentGraphics;

    public static void setGuiGraphics(BCGraphics graphics) {
        currentGraphics = graphics;
    }

    public GuiFluid(FluidStack stack) {
        this.stack = stack;
    }

    public FluidStack getStack() {
        return stack;
    }

    @Override
    public void drawAt(double x, double y) {
        if (currentGraphics == null || stack == null || stack.isEmpty()) {
            return;
        }
        Identifier stillTexture = FluidUtilBC.getFluidTexture(stack);
        if (stillTexture == null) return;
        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
            .getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(stillTexture);
        if (sprite == null) return;
        int color = FluidUtilBC.getFluidColor(stack);
        buildcraft.lib.client.fluid.BcFluidGuiDrawer.drawTiled(
                currentGraphics, (int) x, (int) y, 16, 16, sprite, color);
    }
}
