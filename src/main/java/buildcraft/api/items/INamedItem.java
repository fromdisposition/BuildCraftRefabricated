package buildcraft.api.items;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

public interface INamedItem {
    String getLocationName(@Nonnull ItemStack stack);

    boolean setLocationName(@Nonnull ItemStack stack, String name);
}
