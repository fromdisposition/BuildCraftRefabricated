package buildcraft.api.inventory;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;

public interface IItemHandlerFiltered extends ResourceHandler<ItemResource> {

    default ItemStack getFilter(int slot) {
        return getResource(slot).toStack(getAmountAsInt(slot));
    }
}
