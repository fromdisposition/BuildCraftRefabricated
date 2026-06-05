package buildcraft.api.facades;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

public interface IFacadeState {
    boolean isTransparent();

    BlockState getBlockState();

    ItemStack getRequiredStack();
}
