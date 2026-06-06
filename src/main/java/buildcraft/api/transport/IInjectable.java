package buildcraft.api.transport;

import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public interface IInjectable {
   boolean canInjectItems(Direction var1);

   @Nonnull
   ItemStack injectItem(@Nonnull ItemStack var1, boolean var2, Direction var3, DyeColor var4, double var5);
}
