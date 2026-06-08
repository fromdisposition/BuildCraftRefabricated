/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.button;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class BCButton extends AbstractButton {
   protected BCButton(int x, int y, int width, int height, Component message) {
      super(x, y, width, height, message);
   }

   protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      this.drawButtonContent(new BCGraphics(graphics), mouseX, mouseY, partialTick);
   }

   protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.drawDefaultButtonSprite(graphics);
   }

   protected void updateWidgetNarration(NarrationElementOutput output) {
      this.defaultButtonNarrationText(output);
   }

   protected void drawDefaultButtonSprite(BCGraphics graphics) {
      this.extractDefaultSprite(graphics.raw);
   }

   protected void drawDefaultButtonLabel(BCGraphics graphics) {
      this.extractDefaultLabel(graphics.raw.textRendererForWidget(this, HoveredTextEffects.NONE));
   }
}
