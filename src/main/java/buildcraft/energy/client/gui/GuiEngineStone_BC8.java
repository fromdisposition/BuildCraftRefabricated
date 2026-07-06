/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiEngineStone_BC8 extends BcScreen<ContainerEngineStone_BC8> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftenergy:textures/gui/steam_engine_gui.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 166.0);

   public GuiEngineStone_BC8(ContainerEngineStone_BC8 menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 166));
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerEngineStone_BC8)this.menu).engine != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui,
                  () -> ((ContainerEngineStone_BC8)this.menu).engine != null ? ((ContainerEngineStone_BC8)this.menu).engine.getOwner() : null,
                  true
               )
            );
         this.mainGui
            .shownElements
            .add(
               new LedgerEngine(
                  this.mainGui,
                  ((ContainerEngineStone_BC8)this.menu)::getSyncedCurrentOutput,
                  ((ContainerEngineStone_BC8)this.menu)::getSyncedPower,
                  ((ContainerEngineStone_BC8)this.menu)::getSyncedHeat,
                  ((ContainerEngineStone_BC8)this.menu)::getSyncedPowerStage,
                  ((ContainerEngineStone_BC8)this.menu)::isSyncedBurningEngine,
                  true
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(81.0, 25.0, 14.0, 14.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.stone_engine.flame.title", -225, "buildcraft.help.stone_engine.flame")
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(80.0, 41.0, 16.0, 16.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.stone_engine.fuel.title", -5622989, "buildcraft.help.stone_engine.fuel")
               )
            );
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      if (((ContainerEngineStone_BC8)this.menu).isBurning()) {
         float progress = ((ContainerEngineStone_BC8)this.menu).getBurnProgress();
         int flameHeight = (int)Math.ceil(progress * 14.0F);
         graphics.blit(
            TEXTURE,
            this.leftPos + 81,
            this.topPos + 25 + 14 - flameHeight,
            176.0F,
            14 - flameHeight,
            14,
            flameHeight + 2,
            256,
            256
         );
      }
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String str = LocaleUtil.localize("tile.engineStone.name");
      int strWidth = this.font.width(str);
      int titleX = (this.imageWidth - strWidth) / 2;
      graphics.text(this.font, str, titleX, 6, -12566464, false);
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }
}
