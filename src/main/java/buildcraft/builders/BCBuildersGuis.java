/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.builders.container.ContainerFillerPlanner;
import buildcraft.lib.fabric.menu.FillerPlannerMenuKey;

public enum BCBuildersGuis {
    ARCHITECT,
    BUILDER,
    FILLER,
    LIBRARY,
    REPLACER,
    FILLER_PLANNER;

    public void openGUI(Player player) {
    }

    public void openGUI(Player player, BlockPos pos) {
    }

    public static void openFillerPlannerGUI(Player player, AddonFillerPlanner addon) {
        if (!(player instanceof ServerPlayer sp)) return;
        if (addon == null || addon.volumeBox == null) return;
        FillerPlannerMenuKey key = new FillerPlannerMenuKey(addon.volumeBox.id, addon.getSlot());
        sp.openMenu(new ExtendedMenuProvider<FillerPlannerMenuKey>() {
            @Override
            public FillerPlannerMenuKey getScreenOpeningData(ServerPlayer serverPlayer) {
                return key;
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("item.buildcraftbuilders.filler_planner");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
                return new ContainerFillerPlanner(containerId, playerInv, addon);
            }
        });
    }
}
