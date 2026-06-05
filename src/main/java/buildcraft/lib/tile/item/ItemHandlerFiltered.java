package buildcraft.lib.tile.item;

import net.minecraft.world.item.ItemStack;

import buildcraft.api.inventory.IItemHandlerFiltered;

import buildcraft.lib.misc.StackUtil;

@SuppressWarnings("this-escape")
public class ItemHandlerFiltered extends ItemHandlerSimple implements IItemHandlerFiltered {
    private final ItemHandlerSimple filter;
    private final boolean emptyIsAnything;

    public ItemHandlerFiltered(ItemHandlerSimple filter, boolean emptyIsAnything) {
        super(filter.size());
        this.emptyIsAnything = emptyIsAnything;
        this.filter = filter;
        setChecker((slot, stack) -> {
            ItemStack inSlot = filter.getStackInSlot(slot);
            if (inSlot.isEmpty()) {
                return emptyIsAnything;
            } else {
                return StackUtil.canMerge(stack, inSlot);
            }
        });
    }

    @Override
    public int getSlotLimit(int slot) {
        if (emptyIsAnything || !getFilter(slot).isEmpty()) {
            return super.getSlotLimit(slot);
        } else {
            return 0;
        }
    }

    @Override
    public ItemStack getFilter(int slot) {
        ItemStack current = getStackInSlot(slot);
        if (!current.isEmpty()) {
            return current;
        }
        return filter.getStackInSlot(slot);
    }
}
