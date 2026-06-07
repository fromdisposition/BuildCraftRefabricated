package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.tile.TileStampingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerStampingTable extends ContainerBCTile<TileStampingTable> {
   public ContainerStampingTable(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv.player, getTile(playerInv, pos));
   }

   public ContainerStampingTable(int containerId, Player player, TileStampingTable tile) {
      super(BCSiliconMenuTypes.STAMPING_TABLE, containerId, player, tile);
      if (tile != null) {
         this.addSlot(new SlotBase(tile.invInput, 0, 15, 18));
         this.addSlot(new SlotOutput(tile.invOutput, 0, 143, 18));
         this.addSlot(new SlotOutput(tile.invOutput, 1, 111, 45));
         this.addSlot(new SlotOutput(tile.invOutput, 2, 129, 45));
         this.addSlot(new SlotOutput(tile.invOutput, 3, 147, 45));
      }

      this.addFullPlayerInventory(8, 69);
   }

   private static TileStampingTable getTile(Inventory inv, BlockPos pos) {
      return inv.player.level().getBlockEntity(pos) instanceof TileStampingTable table ? table : null;
   }
}
