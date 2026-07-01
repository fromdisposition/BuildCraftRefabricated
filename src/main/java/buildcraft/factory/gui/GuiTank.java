/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import buildcraft.factory.container.ContainerTank;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.GuiElementFluidTank;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiTank extends BcScreen<ContainerTank> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftfactory:textures/gui/tank.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 181.0);
   private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE, 176.0, 0.0, 16.0, 64.0);

   public GuiTank(ContainerTank menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, 181);
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerTank)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(80.0, 18.0, 16.0, 64.0).offset(this.mainGui.rootElement),
                  ((ContainerTank)this.menu).widgetTank.getTankStorage(),
                  ((ContainerTank)this.menu).widgetTank,
                  ICON_TANK_OVERLAY
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(80.0, 18.0, 16.0, 64.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.tank.title.tankGeneric", -11162881, "buildcraft.help.tank.generic_block.desc", "buildcraft.help.tank.generic"
                  )
               )
            );
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String titleStr = this.title.getString();
      int titleWidth = this.font.width(titleStr);
      int titleX = (this.imageWidth - titleWidth) / 2;
      graphics.text(this.font, titleStr, titleX, 6, -12566464, false);
      graphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, -12566464, false);
   }
}
