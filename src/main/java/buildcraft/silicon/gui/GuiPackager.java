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
import buildcraft.silicon.container.ContainerPackager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiPackager extends BcScreen<ContainerPackager> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftsilicon:textures/gui/bcr/packager.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 222.0);
   private static final GuiIcon ICON_PATTERN_SET = new GuiIcon(TEXTURE_BASE, 176.0, 0.0, 18.0, 18.0);

   public GuiPackager(ContainerPackager container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 176, heightForSlots(container, 222));
   }

   @Override
   protected void initGuiElements() {
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(124.0, 18.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.packager.input.title", -13176, "buildcraft.help.packager.input.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(30.0, 28.0, 52.0, 52.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.packager.pattern.title", -7811960, "buildcraft.help.packager.pattern.desc1", "buildcraft.help.packager.pattern.desc2")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 108.0, 160.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.packager.storage.title", -7811960, "buildcraft.help.packager.storage.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(124.0, 72.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.packager.output.title", -10665929, "buildcraft.help.packager.output.desc")
            )
         );
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      if (this.menu.tile != null) {
         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
               if (this.menu.tile.isPatternSlotSet(y * 3 + x)) {
                  ICON_PATTERN_SET.drawAt(new GuiRectangle(29.0 + x * 18, 27.0 + y * 18, 18.0, 18.0).offset(this.mainGui.rootElement));
               }
            }
         }
      }
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      // Block name, centred on imageWidth at y=6.
      String title = I18n.get("block.buildcraftsilicon.packager", new Object[0]);
      graphics.text(this.font, title, (this.imageWidth - this.font.width(title)) / 2, 6, -12566464, false);
      // "Inventory" label: X = 8 matches the player inventory's left edge; Y = playerInventoryLabelY() derives from
      // the real slot rows (firstPlayerRowY() - 12), so it follows the inventory automatically.
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }
}
