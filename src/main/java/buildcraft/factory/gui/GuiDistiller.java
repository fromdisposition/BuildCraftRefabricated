/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import buildcraft.factory.container.ContainerDistiller;
import buildcraft.factory.tile.TileDistiller;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.GuiElementFluidTank;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiDistiller extends BcScreen<ContainerDistiller> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftfactory:textures/gui/distiller.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 177.0);
   // The atlas rows just below these sprites (y=215 / y=194) are solid black separators, not part of the overlays.
   private static final GuiIcon OVERLAY_VERTICAL = new GuiIcon(TEXTURE, 0.0, 177.0, 16.0, 38.0);
   private static final GuiIcon OVERLAY_HORIZONTAL = new GuiIcon(TEXTURE, 17.0, 177.0, 34.0, 17.0);
   private static final GuiIcon OVERLAY_STUCK = new GuiIcon(TEXTURE, 176.0, 0.0, 36.0, 57.0);
   private static final GuiIcon OVERLAY_RUNNING = new GuiIcon(TEXTURE, 212.0, 0.0, 36.0, 57.0);

   public GuiDistiller(ContainerDistiller menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 177));
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerDistiller)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui, () -> ((ContainerDistiller)this.menu).tile != null ? ((ContainerDistiller)this.menu).tile.getOwner() : null, true
               )
            );
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(44.0, 31.0, 16.0, 38.0).offset(this.mainGui.rootElement),
                  ((ContainerDistiller)this.menu).widgetTankIn.getTankStorage(),
                  ((ContainerDistiller)this.menu).widgetTankIn,
                  OVERLAY_VERTICAL
               )
            );
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(98.0, 18.0, 34.0, 17.0).offset(this.mainGui.rootElement),
                  ((ContainerDistiller)this.menu).widgetTankGasOut.getTankStorage(),
                  ((ContainerDistiller)this.menu).widgetTankGasOut,
                  OVERLAY_HORIZONTAL
               )
            );
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(98.0, 62.0, 34.0, 17.0).offset(this.mainGui.rootElement),
                  ((ContainerDistiller)this.menu).widgetTankLiquidOut.getTankStorage(),
                  ((ContainerDistiller)this.menu).widgetTankLiquidOut,
                  OVERLAY_HORIZONTAL
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(44.0, 31.0, 16.0, 38.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.distiller.input.title", -13176, "buildcraft.help.distiller.input.desc")
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(98.0, 18.0, 34.0, 17.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.distiller.gas_out.title", -5579265, "buildcraft.help.distiller.gas_out.desc")
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(98.0, 62.0, 34.0, 17.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.distiller.liquid_out.title", -5622870, "buildcraft.help.distiller.liquid_out.desc")
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(61.0, 20.0, 36.0, 57.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.distiller.process.title", -7811960, "buildcraft.help.distiller.process.desc1", "buildcraft.help.distiller.process.desc2"
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
      graphics.text(this.font, titleStr, 8, 6, -12566464, false);
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }

   @Override
   protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
      this.drawCenterStateOverlay();
   }

   private void drawCenterStateOverlay() {
      TileDistiller tile = ((ContainerDistiller)this.menu).tile;
      if (tile != null) {
         if (tile.isActive()) {
            double frac = Math.min(1.0, tile.getPowerAvgVisual() / TileDistiller.MAX_MJ_PER_TICK);
            OVERLAY_RUNNING.drawCutInside(this.leftPos + 61, this.topPos + 20, 36.0, 57.0 * Math.max(0.35, frac));
         } else if (tile.isStuck()) {
            OVERLAY_STUCK.drawAt(this.leftPos + 61, this.topPos + 20);
         }
      }
   }
}
