/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.gui.button;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;

import buildcraft.lib.gui.BCGraphics;

public abstract class BCButton extends AbstractButton {
    protected BCButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    protected void extractContents(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        drawButtonContent(new BCGraphics(graphics), mouseX, mouseY, partialTick);
    }

    protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawDefaultButtonSprite(graphics);
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    protected void drawDefaultButtonSprite(BCGraphics graphics) {

        extractDefaultSprite(graphics.raw);

    }

    protected void drawDefaultButtonLabel(BCGraphics graphics) {

        extractDefaultLabel(graphics.raw.textRendererForWidget(this,
            net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects.NONE));

    }
}
