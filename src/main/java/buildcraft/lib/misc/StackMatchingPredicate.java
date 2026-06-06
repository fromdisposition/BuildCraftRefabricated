package buildcraft.lib.misc;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StackMatchingPredicate {
   boolean isMatching(@Nonnull ItemStack var1, @Nonnull ItemStack var2);
}
