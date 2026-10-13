/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiEngineRF extends GuiElectricEngine<TileEngineRF, ContainerEngineRF> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftenergy:textures/gui/bcr/rf_engine_gui.png");

   public GuiEngineRF(ContainerEngineRF menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, TEXTURE, 62, 31, "rf_engine");
   }

   @Override
   protected String batteryConversion(TileEngineRF engine) {
      int rfPerTick = engine.getFeConsumptionRate();
      long mjPerTick = engine.getMjPerTick();
      return LocaleUtil.localize("buildcraft.help.rf_engine.battery", LocaleUtil.localizeRfFlow(rfPerTick), LocaleUtil.localizeMjFlow(mjPerTick));
   }
}
