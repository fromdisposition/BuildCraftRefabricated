package buildcraft.api.transport.pipe;

import buildcraft.api.transport.IStripesActivator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IPipeExtensionManager {
   boolean requestPipeExtension(Level var1, BlockPos var2, Direction var3, IStripesActivator var4, ItemStack var5);

   void registerRetractionPipe(PipeDefinition var1);
}
