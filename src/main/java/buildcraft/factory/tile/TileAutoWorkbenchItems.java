package buildcraft.factory.tile;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileAutoWorkbenchItems extends TileAutoWorkbenchBase implements MenuProvider, BlockEntityExtendedMenu {
   public TileAutoWorkbenchItems(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS, pos, state, 3, 3);
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   public Component getDisplayName() {
      return Component.translatable("item.buildcraftfactory.autoworkbench_item");
   }

   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerAutoCraftItems(containerId, playerInv, this);
   }
}
