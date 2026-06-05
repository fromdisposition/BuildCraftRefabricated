package buildcraft.lib.fabric.menu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class ExtendedMenuTypes {
    private ExtendedMenuTypes() {}

    @FunctionalInterface
    public interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory inv, BlockPos pos);
    }

    public static <T extends AbstractContainerMenu> MenuType<T> create(MenuFactory<T> factory) {
        return new ExtendedMenuType<>(
                (syncId, inv, pos) -> factory.create(syncId, inv, pos),
                BlockPos.STREAM_CODEC);
    }
}
