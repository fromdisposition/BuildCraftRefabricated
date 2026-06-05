package buildcraft.api.transport.pipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.IInjectable;

public interface IFlowItems extends IInjectable {

    @Deprecated
    default int tryExtractItems(int count, Direction from, @Nullable DyeColor colour, IStackFilter filter) {
        return tryExtractItems(count, from, colour, filter, false);
    }

    int tryExtractItems(int count, Direction from, @Nullable DyeColor colour, IStackFilter filter, boolean simulate);

    void insertItemsForce(@Nonnull ItemStack stack, Direction from, @Nullable DyeColor colour, double speed);

    void sendPhantomItem(@Nonnull ItemStack stack, @Nullable Direction from, @Nullable Direction to, @Nullable DyeColor colour);
}
