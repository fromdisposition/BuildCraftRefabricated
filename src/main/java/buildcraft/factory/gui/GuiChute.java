/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import buildcraft.factory.container.ContainerChute;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiChute extends BcScreen<ContainerChute> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftfactory:textures/gui/chute.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 156.0);

   public GuiChute(ContainerChute menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 156));
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      // Block name, centred: the chute's slots are a small centred cluster, so a centred title balances over them
      // (vanilla centres the title for compact machines like the furnace/dispenser). Centred on imageWidth.
      String titleStr = this.title.getString();
      graphics.text(this.font, titleStr, (this.imageWidth - this.font.width(titleStr)) / 2, 6, -12566464, false);
      // "Inventory" label: X = 8 matches the player inventory's left edge; Y = playerInventoryLabelY() derives from
      // the real slot rows (firstPlayerRowY() - 12), so it follows the +3px shift automatically.
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }

   @Override
   protected void initGuiElements() {
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(62.0, 21.0, 52.0, 34.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.chute.slots.title", -7811960, "buildcraft.help.chute.slots.desc1", "buildcraft.help.chute.slots.desc2")
            )
         );
   }
}
