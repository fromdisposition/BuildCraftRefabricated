package buildcraft.api.core;

import net.minecraft.world.item.ItemStack;

public interface IInvSlot {
   int getIndex();

   boolean canPutStackInSlot(ItemStack var1);

   boolean canTakeStackFromSlot(ItemStack var1);

   boolean isItemValidForSlot(ItemStack var1);

   ItemStack decreaseStackInSlot(int var1);

   ItemStack getStackInSlot();

   void setStackInSlot(ItemStack var1);
}
