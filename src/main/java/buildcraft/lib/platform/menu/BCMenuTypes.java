package buildcraft.lib.platform.menu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class BCMenuTypes {
   private BCMenuTypes() {
   }

   public static <T extends AbstractContainerMenu> MenuType<T> create(BCMenuTypes.MenuFactory<T> factory) {
      return new ExtendedMenuType<>((int syncId, Inventory inv, BlockPos pos) -> factory.create(syncId, inv, pos), BlockPos.STREAM_CODEC);
   }

   @FunctionalInterface
   public interface MenuFactory<T extends AbstractContainerMenu> {
      T create(int var1, Inventory var2, BlockPos var3);
   }
}
