package buildcraft.api.items;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public interface INamedItem {
   String getLocationName(@Nonnull ItemStack var1);

   boolean setLocationName(@Nonnull ItemStack var1, String var2);
}
