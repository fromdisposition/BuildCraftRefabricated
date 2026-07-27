/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import buildcraft.factory.container.ContainerHeatExchange;
import buildcraft.factory.tile.TileHeatExchange;
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

public class GuiHeatExchange extends BcScreen<ContainerHeatExchange> {
   private int lastSectionSyncHash = Integer.MIN_VALUE;
   private static final Identifier TEXTURE = Identifier.parse("buildcraftfactory:textures/gui/bcr/heat_exchanger.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 185.0);
   // The atlas rows just below these sprites (y=223 / y=202) are solid black separators, not part of the overlays:
   // start both at y=185 (the first fully transparent row under the panel) so they stay out of the drawn region.
   private static final GuiIcon OVERLAY_VERTICAL = new GuiIcon(TEXTURE, 0.0, 185.0, 16.0, 38.0);
   private static final GuiIcon OVERLAY_HORIZONTAL = new GuiIcon(TEXTURE, 17.0, 185.0, 34.0, 17.0);

   public GuiHeatExchange(ContainerHeatExchange menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 185));
   }

   @Override
   protected void initGuiElements() {
      TileHeatExchange.ExchangeSectionStart start = ((ContainerHeatExchange)this.menu).startSection();
      TileHeatExchange.ExchangeSectionEnd end = ((ContainerHeatExchange)this.menu).endSection();
      if (start != null) {
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(44.0, 70.0, 34.0, 17.0).offset(this.mainGui.rootElement),
                  ((ContainerHeatExchange)this.menu).widgetTankStartInput.getTankStorage(),
                  ((ContainerHeatExchange)this.menu).widgetTankStartInput,
                  OVERLAY_HORIZONTAL
               )
            );
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(116.0, 49.0, 16.0, 38.0).offset(this.mainGui.rootElement),
                  ((ContainerHeatExchange)this.menu).widgetTankStartOutput.getTankStorage(),
                  ((ContainerHeatExchange)this.menu).widgetTankStartOutput,
                  OVERLAY_VERTICAL
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(44.0, 70.0, 34.0, 17.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.heat_exchange.cold_in.title", -11162881, "buildcraft.help.heat_exchange.cold_in.desc")
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(116.0, 49.0, 16.0, 38.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.heat_exchange.cooled_out.title", -5583617, "buildcraft.help.heat_exchange.cooled_out.desc")
               )
            );
      }

      if (end != null) {
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(44.0, 18.0, 16.0, 38.0).offset(this.mainGui.rootElement),
                  ((ContainerHeatExchange)this.menu).widgetTankEndInput.getTankStorage(),
                  ((ContainerHeatExchange)this.menu).widgetTankEndInput,
                  OVERLAY_VERTICAL
               )
            );
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(98.0, 18.0, 34.0, 17.0).offset(this.mainGui.rootElement),
                  ((ContainerHeatExchange)this.menu).widgetTankEndOutput.getTankStorage(),
                  ((ContainerHeatExchange)this.menu).widgetTankEndOutput,
                  OVERLAY_HORIZONTAL
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(44.0, 18.0, 16.0, 38.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.heat_exchange.hot_in.title", -43691, "buildcraft.help.heat_exchange.hot_in.desc")
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(98.0, 18.0, 34.0, 17.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.heat_exchange.heated_out.title", -21931, "buildcraft.help.heat_exchange.heated_out.desc")
               )
            );
      }

      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(73.0, 42.0, 30.0, 21.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.heat_exchange.progress.title",
                  -7811960,
                  "buildcraft.help.heat_exchange.progress.desc1",
                  "buildcraft.help.heat_exchange.progress.desc2"
               )
            )
         );
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      int hash = ((ContainerHeatExchange)this.menu).getSectionSyncHash();
      if (hash != this.lastSectionSyncHash) {
         this.lastSectionSyncHash = hash;
         this.init();
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void drawForegroundLayer() {
      // Block name, left-anchored at x=8 (canonical vanilla title anchor), y=6.
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String titleStr = this.title.getString();
      graphics.text(this.font, titleStr, 8, 6, -12566464, false);
      // "Inventory" label: X = 8 matches the player inventory's left edge; Y = playerInventoryLabelY() derives from
      // the real slot rows (firstPlayerRowY() - 12), so it follows the +7px inventory shift automatically.
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }

   @Override
   protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
      this.drawCenterWipeOverlay(partialTick);
   }

   private void drawCenterWipeOverlay(float partialTicks) {
      TileHeatExchange.ExchangeSectionStart start = ((ContainerHeatExchange)this.menu).startSection();
      if (start != null) {
         TileHeatExchange.EnumProgressState state = start.getProgressState();
         if (state != TileHeatExchange.EnumProgressState.OFF) {
            double progress = Math.max(0.0, Math.min(1.0, start.getProgress(partialTicks)));
            int leftOffset;
            int visibleW;
            if (state == TileHeatExchange.EnumProgressState.PREPARING) {
               leftOffset = 0;
               visibleW = (int)Math.round(progress * 54.0);
            } else if (state == TileHeatExchange.EnumProgressState.STOPPING) {
               leftOffset = (int)Math.round((1.0 - progress) * 54.0);
               visibleW = 54 - leftOffset;
            } else {
               leftOffset = 0;
               visibleW = 54;
            }

            if (visibleW > 0) {
               int absX = this.leftPos + 61 + leftOffset;
               // the sprite SOURCE stays at atlas y=71 (right column, x>=176, which did not move); only the draw
               // position follows the machine content down.
               int absY = this.topPos + 17;
               new GuiIcon(TEXTURE, 176 + leftOffset, 71.0, visibleW, 71.0).drawAt(absX, absY);
            }
         }
      }
   }
}
