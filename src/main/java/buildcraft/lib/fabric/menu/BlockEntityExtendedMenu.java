package buildcraft.lib.fabric.menu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityExtendedMenu extends ExtendedMenuProvider<BlockPos> {
    default BlockEntity asBlockEntity() {
        return (BlockEntity) this;
    }

    @Override
    default BlockPos getScreenOpeningData(ServerPlayer player) {
        return asBlockEntity().getBlockPos();
    }
}
