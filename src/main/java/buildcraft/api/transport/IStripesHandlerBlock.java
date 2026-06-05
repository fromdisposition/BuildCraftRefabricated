package buildcraft.api.transport;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IStripesHandlerBlock {

    boolean handle(Level world, BlockPos pos, Direction direction, Player player, IStripesActivator activator);
}
