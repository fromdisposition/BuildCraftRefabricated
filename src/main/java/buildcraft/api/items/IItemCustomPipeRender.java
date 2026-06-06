package buildcraft.api.items;

import net.minecraft.world.item.ItemStack;

public interface IItemCustomPipeRender {
   float getPipeRenderScale(ItemStack var1);

   boolean renderItemInPipe(ItemStack var1, double var2, double var4, double var6);
}
