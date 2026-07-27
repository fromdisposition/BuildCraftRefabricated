package buildcraft.transport.container;

import buildcraft.api.transport.pipe.IPipeHolder;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class PipeFilterMenus {
   private PipeFilterMenus() {
   }

   @FunctionalInterface
   public interface Factory {
      AbstractContainerMenu create(int containerId, Inventory playerInv, BlockPos pos);
   }

   public static void open(ServerPlayer player, IPipeHolder holder, Component title, Factory factory) {
      BlockPos pos = holder.getPipePos();
      player.openMenu(new ExtendedMenuProvider<BlockPos>() {
         @Override
         public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) {
            return pos;
         }

         @Override
         public Component getDisplayName() {
            return title;
         }

         @Override
         public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player p) {
            return factory.create(containerId, inv, pos);
         }
      });
   }
}
