package buildcraft.lib.gui;

import buildcraft.lib.tile.BcBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class ContainerBCTile<T extends BcBlockEntity> extends BcMenu {
   public final T tile;

   public ContainerBCTile(MenuType<?> menuType, int containerId, Player player, T tile) {
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
