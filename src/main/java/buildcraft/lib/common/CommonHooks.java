package buildcraft.lib.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CommonHooks {
    private CommonHooks() {}

    public static boolean canPlayerDestroy(Player player, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        return !player.isSpectator() && player.mayBuild();
    }

    public static boolean onPlayerTossEvent(Player player, ItemStack stack, boolean dropAround, boolean includeThrowerName) {
        return true;
    }
}
