/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client;

import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.item.ItemGuideNote;
import buildcraft.robotics.ItemRedstoneBoard;
import buildcraft.robotics.item.ItemRobot;
import buildcraft.silicon.item.ItemGateCopier;
import buildcraft.silicon.item.ItemPackage;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.transport.item.ItemPipeHolder;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class BcItemTooltipCallback {
   private BcItemTooltipCallback() {
   }

   public static void register() {
      ItemTooltipCallback.EVENT.register((ItemTooltipCallback)(stack, ctx, flag, lines) -> append(stack, flag, lines));
   }

   private static void append(ItemStack stack, TooltipFlag flag, List<Component> lines) {
      if (stack.getItem() instanceof ItemPipeHolder item) {
         ItemPipeHolder.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemSnapshot item) {
         ItemSnapshot.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemSchematicSingle item) {
         ItemSchematicSingle.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemPaintbrush_BC8 item) {
         ItemPaintbrush_BC8.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemMapLocation item) {
         ItemMapLocation.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemList_BC8 item) {
         ItemList_BC8.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemFragileFluidContainer item) {
         ItemFragileFluidContainer.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemGuideNote item) {
         ItemGuideNote.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemPluggableGate item) {
         ItemPluggableGate.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemPluggableFacade item) {
         ItemPluggableFacade.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemGateCopier item) {
         ItemGateCopier.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemRedstoneBoard item) {
         ItemRedstoneBoard.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemRobot item) {
         ItemRobot.appendTooltipLines(item, stack, flag, lines);
      } else if (stack.getItem() instanceof ItemPackage item) {
         ItemPackage.appendTooltipLines(item, stack, flag, lines);
      }
   }
}
