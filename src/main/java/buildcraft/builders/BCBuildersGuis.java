/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.builders.container.ContainerFillerPlanner;
import buildcraft.lib.fabric.menu.FillerPlannerMenuKey;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class BCBuildersGuis {
   private BCBuildersGuis() {
   }

   public static void openFillerPlannerGUI(Player player, final AddonFillerPlanner addon) {
      if (player instanceof ServerPlayer sp) {
         if (addon != null && addon.volumeBox != null) {
            final FillerPlannerMenuKey key = new FillerPlannerMenuKey(addon.volumeBox.id, addon.getSlot());
            sp.openMenu(new ExtendedMenuProvider<FillerPlannerMenuKey>() {
               public FillerPlannerMenuKey getScreenOpeningData(ServerPlayer serverPlayer) {
                  return key;
               }

               public Component getDisplayName() {
                  return Component.translatable("item.buildcraftbuilders.filler_planner");
               }

               public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
                  return new ContainerFillerPlanner(containerId, playerInv, addon);
               }
            });
         }
      }
   }
}
