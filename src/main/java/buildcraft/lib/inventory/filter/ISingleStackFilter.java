package buildcraft.lib.inventory.filter;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ISingleStackFilter {
   boolean matches(ItemStack var1, ItemStack var2);
}
