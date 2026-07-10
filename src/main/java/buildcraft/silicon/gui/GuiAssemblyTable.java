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
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.silicon.EnumAssemblyRecipeState;
import buildcraft.silicon.container.ContainerAssemblyTable;
import java.util.ArrayList;
//? if >= 1.21.10 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiAssemblyTable extends BcScreen<ContainerAssemblyTable> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftsilicon:textures/gui/assembly_table.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 186.0);
   private static final GuiIcon ICON_SAVED = new GuiIcon(TEXTURE_BASE, 176.0, 0.0, 16.0, 16.0);
   private static final GuiIcon ICON_SAVED_ENOUGH = new GuiIcon(TEXTURE_BASE, 176.0, 16.0, 16.0, 16.0);
   private static final GuiIcon ICON_SAVED_ENOUGH_ACTIVE = new GuiIcon(TEXTURE_BASE, 176.0, 32.0, 16.0, 16.0);
   private static final GuiIcon ICON_PAUSED = new GuiIcon(TEXTURE_BASE, 192.0, 0.0, 16.0, 16.0);
   private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 176.0, 48.0, 4.0, 70.0);

   public GuiAssemblyTable(ContainerAssemblyTable container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 176, heightForSlots(container, 186));
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerAssemblyTable)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui, () -> ((ContainerAssemblyTable)this.menu).tile != null ? ((ContainerAssemblyTable)this.menu).tile.getOwner() : null, true
               )
            );
      }

      this.mainGui.shownElements.add(new LedgerTablePower(this.mainGui, ((ContainerAssemblyTable)this.menu).tile, true));
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 18.0, 52.0, 70.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.assembly_table.input.title", -13176, "buildcraft.help.assembly_table.input.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(116.0, 18.0, 52.0, 70.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.assembly_table.recipes.title",
                  -7811960,
                  "buildcraft.help.assembly_table.recipes.desc1",
                  "buildcraft.help.assembly_table.recipes.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(86.0, 18.0, 4.0, 70.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.assembly_table.power.title", -2249985, "buildcraft.help.assembly_table.power.desc")
            )
         );
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      long target = ((ContainerAssemblyTable)this.menu).tile.getTarget();
      if (target != 0L) {
         double v = (double)((ContainerAssemblyTable)this.menu).tile.power / target;
         ICON_PROGRESS.drawCutInside(
            new GuiRectangle(
                  86.0,
                  (int)(18.0 + 70.0 * Math.max(1.0 - v, 0.0)),
                  4.0,
                  (int)Math.ceil(70.0 * Math.min(v, 1.0))
               )
               .offset(this.mainGui.rootElement)
         );
      }

      for (int i = 0; i < ((ContainerAssemblyTable)this.menu).tile.recipesStates.size(); i++) {
         EnumAssemblyRecipeState state = new ArrayList<>(((ContainerAssemblyTable)this.menu).tile.recipesStates.values()).get(i);
         IGuiArea area = this.getRecipeArea(i);
         if (state == EnumAssemblyRecipeState.SAVED) {
            ICON_SAVED.drawAt(area);
         }

         if (state == EnumAssemblyRecipeState.PAUSED) {
            ICON_PAUSED.drawAt(area);
         }

         if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
            ICON_SAVED_ENOUGH.drawAt(area);
         }

         if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
            ICON_SAVED_ENOUGH_ACTIVE.drawAt(area);
         }
      }
   }

   private IGuiArea getRecipeArea(int index) {
      int posX = index % 3;
      int posY = index / 3;
      return new GuiRectangle(16.0, 16.0).offset(this.mainGui.rootElement).offset(116 + posX * 18, 18 + posY * 18);
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String title = I18n.get("block.buildcraftsilicon.assembly_table", new Object[0]);
      graphics.text(this.font, title, (this.imageWidth - this.font.width(title)) / 2, 6, -12566464, false);
      // "Inventory" label: X = 8 matches the player inventory's own left edge (addFullPlayerInventory(8, ...)),
      // Y = playerInventoryLabelY() is derived from the real slot rows (firstPlayerRowY() - 12), not hardcoded.
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }

   @Override
   //? if >= 1.21.10 {
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      return this.bcMouseClicked((int)event.x(), (int)event.y(), event.button()) || super.mouseClicked(event, doubleClick);
   }
   //?} else {
   /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.bcMouseClicked((int)mouseX, (int)mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
   }
   *///?}

   private boolean bcMouseClicked(int mouseX, int mouseY, int button) {
      if (button == 0) {
         for (int i = 0; i < ((ContainerAssemblyTable)this.menu).tile.recipesStates.size(); i++) {
            IGuiArea area = this.getRecipeArea(i);
            if (area.contains(mouseX, mouseY)) {
               if (this.minecraft != null && this.minecraft.gameMode != null) {
                  this.minecraft.gameMode.handleInventoryButtonClick(((ContainerAssemblyTable)this.menu).containerId, i);
               }

               return true;
            }
         }
      }

      return false;
   }
}
