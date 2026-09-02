/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiDynamoMJ extends GuiElectricEngine<TileDynamoMJ, ContainerDynamoMJ> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftenergy:textures/gui/bcr/mj_dynamo_gui.png");

   public GuiDynamoMJ(ContainerDynamoMJ menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, TEXTURE, 44, 139, "dynamo");
   }

   @Override
   protected String batteryConversion(TileDynamoMJ dynamo) {
      long mjPerTick = dynamo.getMjPerTick();
      int rfPerTick = dynamo.getFeProductionRate(mjPerTick);
      return LocaleUtil.localize("buildcraft.help.dynamo.battery", LocaleUtil.localizeMjFlow(mjPerTick), LocaleUtil.localizeRfFlow(rfPerTick));
   }
}
