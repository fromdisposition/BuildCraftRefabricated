package buildcraft.api.robots;

import net.minecraft.world.item.ItemStack;

public interface IRequestProvider {
   int getRequestsCount();

   ItemStack getRequest(int var1);

   ItemStack offerItem(int var1, ItemStack var2);
}
