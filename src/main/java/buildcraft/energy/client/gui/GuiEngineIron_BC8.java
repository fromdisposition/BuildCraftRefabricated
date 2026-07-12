/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import buildcraft.energy.container.ContainerEngineIron_BC8;
import buildcraft.lib.fabric.transfer.fluid.FluidStorageSnapshot;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.GuiElementFluidTank;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiEngineIron_BC8 extends BcScreen<ContainerEngineIron_BC8> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftenergy:textures/gui/bcr/combustion_engine_gui.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 176.0);
   private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE, 176.0, 0.0, 16.0, 60.0);

   public GuiEngineIron_BC8(ContainerEngineIron_BC8 menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 176));
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerEngineIron_BC8)this.menu).engine != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui, () -> ((ContainerEngineIron_BC8)this.menu).engine != null ? ((ContainerEngineIron_BC8)this.menu).engine.getOwner() : null, true
               )
            );
         this.mainGui
            .shownElements
            .add(
               new LedgerEngine(
                  this.mainGui,
                  ((ContainerEngineIron_BC8)this.menu)::getSyncedCurrentOutput,
                  ((ContainerEngineIron_BC8)this.menu)::getSyncedPower,
                  ((ContainerEngineIron_BC8)this.menu)::getSyncedHeat,
                  ((ContainerEngineIron_BC8)this.menu)::getSyncedPowerStage,
                  ((ContainerEngineIron_BC8)this.menu)::isSyncedBurning,
                  true,
                  true
               )
            );
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(26.0, 18.0, 16.0, 60.0).offset(this.mainGui.rootElement),
                  () -> FluidStorageSnapshot.fromLevels(
                     ((ContainerEngineIron_BC8)this.menu).getSyncedFuelFluid(), ((ContainerEngineIron_BC8)this.menu).getSyncedFuelAmount(), 10000
                  ),
                  ((ContainerEngineIron_BC8)this.menu).widgetFuel,
                  ICON_TANK_OVERLAY
               )
            );
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(80.0, 18.0, 16.0, 60.0).offset(this.mainGui.rootElement),
                  () -> FluidStorageSnapshot.fromLevels(
                     ((ContainerEngineIron_BC8)this.menu).getSyncedCoolantFluid(), ((ContainerEngineIron_BC8)this.menu).getSyncedCoolantAmount(), 10000
                  ),
                  ((ContainerEngineIron_BC8)this.menu).widgetCoolant,
                  ICON_TANK_OVERLAY
               )
            );
         this.mainGui
            .shownElements
            .add(
               new GuiElementFluidTank(
                  this.mainGui,
                  new GuiRectangle(134.0, 18.0, 16.0, 60.0).offset(this.mainGui.rootElement),
                  () -> FluidStorageSnapshot.fromLevels(
                     ((ContainerEngineIron_BC8)this.menu).getSyncedResidueFluid(), ((ContainerEngineIron_BC8)this.menu).getSyncedResidueAmount(), 10000
                  ),
                  ((ContainerEngineIron_BC8)this.menu).widgetResidue,
                  ICON_TANK_OVERLAY
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(26.0, 18.0, 16.0, 60.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.tank.title.tankFuel", -52429, "buildcraft.help.tank.generic", "buildcraft.help.tank.fuel")
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(80.0, 18.0, 16.0, 60.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.tank.title.tankCoolant", -11184641, "buildcraft.help.tank.generic", "buildcraft.help.tank.coolant")
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(134.0, 18.0, 16.0, 60.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.tank.title.tankResidue", -5622870, "buildcraft.help.tank.generic", "buildcraft.help.tank.residue")
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
      String str = LocaleUtil.localize("tile.engineIron.name");
      int strWidth = this.font.width(str);
      int titleX = (this.imageWidth - strWidth) / 2;
      graphics.text(this.font, str, titleX, 6, -12566464, false);
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }
}
