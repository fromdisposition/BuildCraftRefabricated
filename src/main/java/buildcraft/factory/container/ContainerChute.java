package buildcraft.factory.container;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileChute;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;

public class ContainerChute extends ContainerBCTile<TileChute> {
   public ContainerChute(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileChute.class));
   }

   public ContainerChute(int containerId, Inventory playerInv, TileChute tile) {
      super(BCFactoryMenuTypes.CHUTE, containerId, playerInv.player, tile);
      if (tile != null) {
         this.addSlot(new SlotBase(tile.inv, 0, 62, 18));
         this.addSlot(new SlotBase(tile.inv, 1, 80, 18));
         this.addSlot(new SlotBase(tile.inv, 2, 98, 18));
         this.addSlot(new SlotBase(tile.inv, 3, 80, 36));
      }

      this.addFullPlayerInventory(8, 71);
   }
}
