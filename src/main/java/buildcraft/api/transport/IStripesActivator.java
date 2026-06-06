package buildcraft.api.transport;

import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public interface IStripesActivator {
   boolean sendItem(@Nonnull ItemStack var1, Direction var2);

   void dropItem(@Nonnull ItemStack var1, Direction var2);
}
