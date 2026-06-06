package buildcraft.api.transport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IStripesHandlerItem {
   boolean handle(Level var1, BlockPos var2, Direction var3, ItemStack var4, Player var5, IStripesActivator var6);
}
