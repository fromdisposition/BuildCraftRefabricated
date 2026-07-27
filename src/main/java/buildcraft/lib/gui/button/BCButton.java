/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.button;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects;
//?}
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class BCButton extends AbstractButton {
   protected BCButton(int x, int y, int width, int height, Component message) {
      super(x, y, width, height, message);
   }

   //? if >= 26.1 {
   protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      this.drawButtonContent(new BCGraphics(graphics), mouseX, mouseY, partialTick);
   }
   //?} else if >= 1.21.11 {
   /*// 1.21.11: AbstractButton.renderWidget draws the button background sprite, then calls renderContents.
   @Override
   protected void renderContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      this.drawButtonContent(new BCGraphics(graphics), mouseX, mouseY, partialTick);
   }
   *///?} else {
   /*// 1.21.10 has no renderContents hook; renderWidget draws the whole button. Let super draw the
   // background + default label, then layer BC's content on top.
   @Override
   protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      super.renderWidget(graphics, mouseX, mouseY, partialTick);
      this.drawButtonContent(new BCGraphics(graphics), mouseX, mouseY, partialTick);
   }
   *///?}

   protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.drawDefaultButtonSprite(graphics);
   }

   protected void updateWidgetNarration(NarrationElementOutput output) {
      this.defaultButtonNarrationText(output);
   }

   protected void drawDefaultButtonSprite(BCGraphics graphics) {
      //? if >= 26.1 {
      this.extractDefaultSprite(graphics.raw);
      //?} else {
      /*// The button sprite is already drawn by super.renderWidget; draw the default label as content.
      this.drawDefaultButtonLabel(graphics);
      *///?}
   }

   protected void drawDefaultButtonLabel(BCGraphics graphics) {
      //? if >= 26.1 {
      this.extractDefaultLabel(graphics.raw.textRendererForWidget(this, HoveredTextEffects.NONE));
      //?} else {
      /*net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
      int color = this.active ? 0xFFFFFFFF : 0xFFA0A0A0;
      graphics.raw.drawCenteredString(mc.font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
      *///?}
   }
}
