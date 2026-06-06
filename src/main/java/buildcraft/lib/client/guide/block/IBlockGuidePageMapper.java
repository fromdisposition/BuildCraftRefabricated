package buildcraft.lib.client.guide.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockGuidePageMapper {
   String getFor(Level var1, BlockPos var2, BlockState var3);

   List<String> getAllPossiblePages();
}
