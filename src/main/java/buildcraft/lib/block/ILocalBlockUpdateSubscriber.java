package buildcraft.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ILocalBlockUpdateSubscriber {
   BlockPos getSubscriberPos();

   int getUpdateRange();

   void setLevelUpdated(Level var1, BlockPos var2, BlockState var3, BlockState var4, int var5);
}
