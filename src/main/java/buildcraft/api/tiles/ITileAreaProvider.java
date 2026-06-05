package buildcraft.api.tiles;

import net.minecraft.core.BlockPos;

import buildcraft.api.core.IAreaProvider;

public interface ITileAreaProvider extends IAreaProvider {
    boolean isValidFromLocation(BlockPos pos);
}
