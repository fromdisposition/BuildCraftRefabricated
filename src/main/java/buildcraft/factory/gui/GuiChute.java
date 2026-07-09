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
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 153.0);

   public GuiChute(ContainerChute menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 153));
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void drawForegroundLayer() {
      // "Inventory" label only: X = 8 matches the player inventory's left edge (addFullPlayerInventory(8, 71)),
      // Y = playerInventoryLabelY() derives from the real slot rows (firstPlayerRowY() - 12), not hardcoded.
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }

   @Override
   protected void initGuiElements() {
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(62.0, 18.0, 52.0, 34.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.chute.slots.title", -7811960, "buildcraft.help.chute.slots.desc1", "buildcraft.help.chute.slots.desc2")
            )
         );
   }
}
