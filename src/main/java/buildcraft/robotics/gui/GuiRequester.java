/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.robotics.container.ContainerRequester;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiRequester extends BcScreen<ContainerRequester> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftrobotics:textures/gui/requester.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 196.0, 181.0);

   public GuiRequester(ContainerRequester container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 196, 181);
   }

   @Override
   protected void initGuiElements() {
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(9.0, 7.0, 70.0, 88.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.requester.requests.title", -13176, "buildcraft.help.requester.requests.desc1", "buildcraft.help.requester.requests.desc2")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(117.0, 7.0, 70.0, 88.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.requester.items.title", -7811960, "buildcraft.help.requester.items.desc")
            )
         );
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String title = I18n.get("block.buildcraftrobotics.requester", new Object[0]);
      graphics.text(this.font, title, (this.imageWidth - this.font.width(title)) / 2, 6, -12566464, false);
   }
}
