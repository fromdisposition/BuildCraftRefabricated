/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiFilteredBuffer extends BcScreen<ContainerFilteredBuffer_BC8> {
   private static final Identifier TEXTURE = Identifier.parse("buildcrafttransport:textures/gui/bcr/filtered_buffer.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 166.0);
   private static final GuiIcon ICON_EMPTY_SLOT = new GuiIcon(
      Identifier.parse("buildcrafttransport:textures/gui/bcr/empty_filtered_buffer_slot.png"), 0.0, 0.0, 16.0, 16.0, 16
   );
   private static final GuiIcon ICON_NOTHING_SLOT = new GuiIcon(
      Identifier.parse("buildcrafttransport:textures/gui/bcr/nothing_filtered_buffer_slot.png"), 0.0, 0.0, 16.0, 16.0, 16
   );

   public GuiFilteredBuffer(ContainerFilteredBuffer_BC8 menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 166));
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      int rootX = (int)this.mainGui.rootElement.getX();
      int rootY = (int)this.mainGui.rootElement.getY();

      for (int i = 0; i < 9; i++) {
         ItemStack stackFilter = ((ContainerFilteredBuffer_BC8)this.menu).tile.invFilter.getStackInSlot(i);
         ItemStack stackMain = ((ContainerFilteredBuffer_BC8)this.menu).tile.invMain.getStackInSlot(i);
         int x = rootX + 8 + i * 18;
         int filterY = rootY + 18;
         int mainY = rootY + 52;
         if (stackFilter.isEmpty()) {
            ICON_EMPTY_SLOT.drawAt(x, filterY);
         }

         if (stackMain.isEmpty()) {
            if (!stackFilter.isEmpty()) {
               graphics.fakeItem(stackFilter, x, mainY);
               graphics.fill(x, mainY, x + 16, mainY + 16, -1299477621);
            } else {
               ICON_NOTHING_SLOT.drawAt(x, mainY);
            }
         }
      }
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerFilteredBuffer_BC8)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui,
                  () -> ((ContainerFilteredBuffer_BC8)this.menu).tile != null ? ((ContainerFilteredBuffer_BC8)this.menu).tile.getOwner() : null,
                  true
               )
            );
      }

      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 18.0, 160.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.filtered_buffer.filter.title", -11184641, "buildcraft.help.filtered_buffer.filter")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 52.0, 160.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.filtered_buffer.main.title", -11141291, "buildcraft.help.filtered_buffer.main")
            )
         );
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      // Block name on the canonical left title anchor.
      graphics.text(this.font, this.title.getString(), 8, 6, -12566464, false);
      // X = 8 matches the player inventory's left edge; Y = playerInventoryLabelY() derives from the real slot rows
      // (firstPlayerRowY() - 12), so it follows the inventory automatically.
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }
}
