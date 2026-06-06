package buildcraft.api.core;

import java.util.List;
import net.minecraft.core.BlockPos;

public interface IPathProvider {
   List<BlockPos> getPath();

   void removeFromWorld();
}
