/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import buildcraft.core.BCCoreItems;
import buildcraft.api.mj.MjAPI;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.energy.tile.TileEngineRF;
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

public class GuiEngineRF extends BcScreen<ContainerEngineRF> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftenergy:textures/gui/rf_engine_gui.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 177.0);
   private static final GuiIcon ICON_RF = new GuiIcon(TEXTURE, 176.0, 0.0, 16.0, 60.0);

   public GuiEngineRF(ContainerEngineRF menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 177));
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerEngineRF)this.menu).engine != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui, () -> ((ContainerEngineRF)this.menu).engine != null ? ((ContainerEngineRF)this.menu).engine.getOwner() : null, true
               )
            );
         this.mainGui
            .shownElements
            .add(
               new LedgerEngine(
                  this.mainGui,
                  ((ContainerEngineRF)this.menu)::getSyncedCurrentOutput,
                  ((ContainerEngineRF)this.menu)::getSyncedPower,
                  ((ContainerEngineRF)this.menu)::getSyncedHeat,
                  ((ContainerEngineRF)this.menu)::getSyncedPowerStage,
                  ((ContainerEngineRF)this.menu)::isSyncedBurningEngine,
                  true,
                  false
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(62.0, 44.0, 70.0, 16.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.rf_engine.upgrades.title", -10053121, "buildcraft.help.rf_engine.upgrades")
               )
            );
         this.mainGui.shownElements.add(new GuiElementSimple(this.mainGui, new GuiRectangle(60.0, 20.0, 74.0, 20.0).offset(this.mainGui.rootElement)) {
            @Override
            public void addToolTips(List<ToolTip> tooltips) {
               if (this.contains(GuiEngineRF.this.mainGui.mouse)) {
                  List<String> lines = new ArrayList<>();
                  lines.add(LocaleUtil.localize("buildcraft.gui.rf_engine.upgrade_types"));
                  TileEngineRF.initUpgrades();
                  String unitLabel = LocaleUtil.localize("buildcraft.gui.rf_engine.upgrade_rate_unit_mj");

                  for (Entry<Item, Long> entry : TileEngineRF.UPGRADE_VALUES.entrySet()) {
                     String itemName = new ItemStack((ItemLike)entry.getKey()).getHoverName().getString();
                     long mjPerSecond = entry.getValue() * 20L / MjAPI.MJ;
                     lines.add(itemName + " = +" + mjPerSecond + " " + unitLabel);
                  }

                  tooltips.add(new ToolTip(lines));
               }
            }
         });
         this.mainGui.shownElements.add(new GuiElementSimple(this.mainGui, new GuiRectangle(30.0, 17.0, 8.0, 62.0).offset(this.mainGui.rootElement)) {
            @Override
            public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
               int rfPerTick = ((ContainerEngineRF)GuiEngineRF.this.menu).engine.getFeConsumptionRate();
               long mjPerTick = ((ContainerEngineRF)GuiEngineRF.this.menu).engine.getMjPerTick();
               String rf = LocaleUtil.localizeRfFlow(rfPerTick);
               String mj = LocaleUtil.localizeMjFlow(mjPerTick);
               String conversion = LocaleUtil.localize(BCEnergyConfig.rfFeKey("buildcraft.help.rf_engine.battery"), rf, mj);
               String titleKey = "buildcraft.help.rf_engine.battery.title_mj";
               ElementHelpInfo help = ElementHelpInfo.preTranslated(titleKey, -13391309, conversion);
               elements.add(help.target(this));
            }

            @Override
            public void addToolTips(List<ToolTip> tooltips) {
               if (this.contains(GuiEngineRF.this.mainGui.mouse)) {
                  int current = ((ContainerEngineRF)GuiEngineRF.this.menu).getSyncedFeStored();
                  int max = 10000;
                  tooltips.add(new ToolTip(LocaleUtil.localizeExternalBuffer(current, max)));
               }
            }
         });
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      int x = (int)this.mainGui.rootElement.getX();
      int y = (int)this.mainGui.rootElement.getY();
      ItemStack gearIron = new ItemStack(BCCoreItems.GEAR_IRON);
      ItemStack gearGold = new ItemStack(BCCoreItems.GEAR_GOLD);
      graphics.item(gearIron, x + 78, y + 21);
      graphics.item(gearGold, x + 101, y + 21);
      graphics.blit(TEXTURE, x + 57, y + 18, 57.0F, 18.0F, 80, 23, 80, 23, 256, 256, -1509949441);
      double rfHeight = 60.0 * ((ContainerEngineRF)this.menu).getSyncedFeStored() / 10000.0;
      double scale = Minecraft.getInstance().getWindow().getGuiScale();
      rfHeight = Math.round(rfHeight * scale) / scale;
      ICON_RF.drawCutInside(new GuiRectangle(31.0, 78.0 - rfHeight, 6.0, rfHeight).offset(this.mainGui.rootElement));
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
