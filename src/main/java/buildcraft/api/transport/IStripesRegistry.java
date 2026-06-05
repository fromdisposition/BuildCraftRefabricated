package buildcraft.api.transport;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraft.api.core.EnumHandlerPriority;

public interface IStripesRegistry {

    default void addHandler(IStripesHandlerItem handler) {
        addHandler(handler, EnumHandlerPriority.NORMAL);
    }

    void addHandler(IStripesHandlerItem handler, EnumHandlerPriority priority);

    default void addHandler(IStripesHandlerBlock handler) {
        addHandler(handler, EnumHandlerPriority.NORMAL);
    }

    void addHandler(IStripesHandlerBlock handler, EnumHandlerPriority priority);

    boolean handleItem(Level world,
                       BlockPos pos,
                       Direction direction,
                       ItemStack stack,
                       Player player,
                       IStripesActivator activator);

    boolean handleBlock(Level world,
                        BlockPos pos,
                        Direction direction,
                        Player player,
                        IStripesActivator activator);
}
