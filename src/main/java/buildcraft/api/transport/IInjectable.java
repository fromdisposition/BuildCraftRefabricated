package buildcraft.api.transport;

import javax.annotation.Nonnull;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;

public interface IInjectable {

    boolean canInjectItems(Direction from);

    @Nonnull
    ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, Direction from, DyeColor color, double speed);
}
