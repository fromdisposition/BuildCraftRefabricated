/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.tile.BcBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class ContainerBCTileRecipeBook<T extends BcBlockEntity> extends BcMenuRecipeBook {
   public final T tile;

   public ContainerBCTileRecipeBook(MenuType<?> menuType, int containerId, Player player, T tile) {
      super(menuType, containerId, player);
      this.tile = tile;
      if (tile != null && tile.getLevel() != null && !tile.getLevel().isClientSide()) {
         tile.onPlayerOpen(player);
      }
   }

   public void removed(Player player) {
      super.removed(player);
      if (this.tile != null) {
         this.tile.onPlayerClose(player);
      }
   }

   @Override
   public final boolean stillValid(Player player) {
      return this.tile != null && this.tile.canInteractWith(player);
   }
}
