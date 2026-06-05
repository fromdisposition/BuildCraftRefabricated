package buildcraft.api.core;

import java.util.List;

import net.minecraft.core.BlockPos;

import buildcraft.api.items.IMapLocation.MapLocationType;

public interface IPathProvider {

    List<BlockPos> getPath();

    void removeFromWorld();
}
