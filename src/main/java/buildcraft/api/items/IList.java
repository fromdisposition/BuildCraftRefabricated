package buildcraft.api.items;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public interface IList extends INamedItem {
   boolean matches(@Nonnull ItemStack var1, @Nonnull ItemStack var2);
}
