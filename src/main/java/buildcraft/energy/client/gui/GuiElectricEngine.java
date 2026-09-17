/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreItems;
import buildcraft.energy.container.ContainerElectricEngine;
import buildcraft.energy.tile.TileElectricEngineBase;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public abstract class GuiElectricEngine<T extends TileElectricEngineBase, M extends ContainerElectricEngine<T>> extends BcScreen<M> {
   private final Identifier texture;
   private final GuiIcon iconGui;
   private final GuiIcon iconRf;
   private final int upgradesX;
   private final int barX;
   private final String helpPrefix;

   protected GuiElectricEngine(M menu, Inventory playerInv, Component title, Identifier texture, int upgradesX, int barX, String helpPrefix) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 176));
      this.texture = texture;
      this.iconGui = new GuiIcon(texture, 0.0, 0.0, 176.0, 176.0);
      this.iconRf = new GuiIcon(texture, 176.0, 0.0, 16.0, 60.0);
      this.upgradesX = upgradesX;
      this.barX = barX;
      this.helpPrefix = helpPrefix;
   }

   protected abstract String batteryConversion(T engine);

   @Override
   protected void initGuiElements() {
      if (this.menu.engine != null) {
         this.mainGui
            .shownElements
            .add(new LedgerOwnership(this.mainGui, () -> this.menu.engine != null ? this.menu.engine.getOwner() : null, true));
         this.mainGui
            .shownElements
            .add(
               new LedgerEngine(
                  this.mainGui,
                  this.menu::getSyncedCurrentOutput,
                  this.menu::getSyncedPower,
                  this.menu::getSyncedHeat,
                  this.menu::getSyncedPowerStage,
                  this.menu::isSyncedBurningEngine,
                  true,
                  false
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(this.upgradesX, 44.0, 70.0, 16.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help." + this.helpPrefix + ".upgrades.title", -10053121, "buildcraft.help." + this.helpPrefix + ".upgrades")
               )
            );
         this.mainGui.shownElements.add(new GuiElementSimple(this.mainGui, new GuiRectangle(this.upgradesX - 2, 20.0, 74.0, 20.0).offset(this.mainGui.rootElement)) {
            @Override
            public void addToolTips(List<ToolTip> tooltips) {
               if (this.contains(GuiElectricEngine.this.mainGui.mouse)) {
                  List<String> lines = new ArrayList<>();
                  lines.add(LocaleUtil.localize("buildcraft.gui.rf_engine.upgrade_types"));
                  TileElectricEngineBase.initUpgrades();
                  String unitLabel = LocaleUtil.localize("buildcraft.gui.rf_engine.upgrade_rate_unit_mj");

                  for (Entry<Item, Long> entry : TileElectricEngineBase.UPGRADE_VALUES.entrySet()) {
                     String itemName = new ItemStack((ItemLike) entry.getKey()).getHoverName().getString();
                     long mjPerSecond = entry.getValue() * 20L / MjAPI.MJ;
                     lines.add(itemName + " = +" + mjPerSecond + " " + unitLabel);
                  }

                  tooltips.add(new ToolTip(lines));
               }
            }
         });
         this.mainGui.shownElements.add(new GuiElementSimple(this.mainGui, new GuiRectangle(this.barX - 1, 17.0, 8.0, 62.0).offset(this.mainGui.rootElement)) {
            @Override
            public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
               String conversion = GuiElectricEngine.this.batteryConversion(GuiElectricEngine.this.menu.engine);
               String titleKey = "buildcraft.help." + GuiElectricEngine.this.helpPrefix + ".battery.title_mj";
               ElementHelpInfo help = ElementHelpInfo.preTranslated(titleKey, -13391309, conversion);
               elements.add(help.target(this));
            }

            @Override
            public void addToolTips(List<ToolTip> tooltips) {
               if (this.contains(GuiElectricEngine.this.mainGui.mouse)) {
                  int current = GuiElectricEngine.this.menu.getSyncedFeStored();
                  int max = 10000;
                  tooltips.add(new ToolTip(LocaleUtil.localizeExternalBuffer(current, max)));
               }
            }
         });
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      this.iconGui.drawAt(this.mainGui.rootElement);
      int x = (int) this.mainGui.rootElement.getX();
      int y = (int) this.mainGui.rootElement.getY();
      int panelX = this.upgradesX - 5;
      graphics.item(new ItemStack(BCCoreItems.GEAR_IRON), x + this.upgradesX + 16, y + 21);
      graphics.item(new ItemStack(BCCoreItems.GEAR_GOLD), x + this.upgradesX + 39, y + 21);
      graphics.blit(this.texture, x + panelX, y + 18, (float) panelX, 18.0F, 80, 23, 80, 23, 256, 256, -1509949441);
      double rfHeight = 60.0 * this.menu.getSyncedFeStored() / 10000.0;
      double scale = Minecraft.getInstance().getWindow().getGuiScale();
      rfHeight = Math.round(rfHeight * scale) / scale;
      this.iconRf.drawCutInside(new GuiRectangle(this.barX, 78.0 - rfHeight, 6.0, rfHeight).offset(this.mainGui.rootElement));
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String str = this.title.getString();
      int strWidth = this.font.width(str);
      int titleX = (this.imageWidth - strWidth) / 2;
      graphics.text(this.font, str, titleX, 6, -12566464, false);
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }
}
