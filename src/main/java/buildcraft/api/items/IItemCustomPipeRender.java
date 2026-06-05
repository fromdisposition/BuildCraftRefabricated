package buildcraft.api.items;

import net.minecraft.world.item.ItemStack;

public interface IItemCustomPipeRender {
    float getPipeRenderScale(ItemStack stack);

    boolean renderItemInPipe(ItemStack stack, double x, double y, double z);
}
