/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.silicon.container.ContainerIntegrationTable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiIntegrationTable extends BcScreen<ContainerIntegrationTable> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftsilicon:textures/gui/integration_table.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 186.0);
   private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 176.0, 0.0, 4.0, 70.0);

   public GuiIntegrationTable(ContainerIntegrationTable container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 176, heightForSlots(container, 186));
   }

   @Override
   protected void initGuiElements() {
      this.mainGui.shownElements.add(new LedgerTablePower(this.mainGui, ((ContainerIntegrationTable)this.menu).tile, true));
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(9.0, 20.0, 66.0, 66.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.integration_table.input.title", -13176, "buildcraft.help.integration_table.input.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(133.0, 45.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.integration_table.output.title", -7798632, "buildcraft.help.integration_table.output.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(164.0, 18.0, 4.0, 70.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.integration_table.power.title", -2249985, "buildcraft.help.integration_table.power.desc")
            )
         );
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      long target = ((ContainerIntegrationTable)this.menu).tile.getTarget();
      if (target != 0L) {
         double v = (double)((ContainerIntegrationTable)this.menu).tile.power / target;
         ICON_PROGRESS.drawCutInside(
            new GuiRectangle(
                  164.0,
                  (int)(18.0 + 70.0 * Math.max(1.0 - v, 0.0)),
                  4.0,
                  (int)Math.ceil(70.0 * Math.min(v, 1.0))
               )
               .offset(this.mainGui.rootElement)
         );
      }
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String title = I18n.get("block.buildcraftsilicon.integration_table", new Object[0]);
      graphics.text(this.font, title, 8, 6, -12566464, false);
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }
}
