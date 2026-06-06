package buildcraft.lib.tile;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StackInsertionChecker {
   boolean canSet(int var1, @Nonnull ItemStack var2);
}
