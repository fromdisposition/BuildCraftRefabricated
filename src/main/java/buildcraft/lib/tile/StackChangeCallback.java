package buildcraft.lib.tile;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StackChangeCallback {
   void onStackChange(BcItemInventory var1, int var2, @Nonnull ItemStack var3, @Nonnull ItemStack var4);
}
