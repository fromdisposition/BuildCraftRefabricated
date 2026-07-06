/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import buildcraft.lib.client.ColorBlindUtil;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.transport.container.ContainerDiamondPipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiDiamondPipe extends BcScreen<ContainerDiamondPipe> {
   private static final Identifier TEXTURE = Identifier.parse("buildcrafttransport:textures/gui/filter.png");
   private static final Identifier TEXTURE_CB = Identifier.parse("buildcrafttransport:textures/gui/filter_cb.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 175.0, 225.0);
   private static final GuiIcon ICON_GUI_CB = new GuiIcon(TEXTURE_CB, 0.0, 0.0, 175.0, 225.0);

   public GuiDiamondPipe(ContainerDiamondPipe menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 175, heightForSlots(menu, 225));
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      GuiIcon icon = ColorBlindUtil.isActive() ? ICON_GUI_CB : ICON_GUI;
      icon.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void initGuiElements() {
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 18.0, 160.0, 106.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.diamond_pipe.filter.title",
                  -7811841,
                  "buildcraft.help.diamond_pipe.filter.desc1",
                  "buildcraft.help.diamond_pipe.filter.desc2"
               )
            )
         );
   }
}
