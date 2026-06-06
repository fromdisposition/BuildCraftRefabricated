package buildcraft.api.tiles;

import buildcraft.api.core.IAreaProvider;
import net.minecraft.core.BlockPos;

public interface ITileAreaProvider extends IAreaProvider {
   boolean isValidFromLocation(BlockPos var1);
}
