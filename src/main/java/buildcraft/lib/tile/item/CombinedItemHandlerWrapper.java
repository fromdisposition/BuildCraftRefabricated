package buildcraft.lib.tile.item;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.TransactionContext;

import buildcraft.api.inventory.IItemHandlerFiltered;

public class CombinedItemHandlerWrapper implements ResourceHandler<ItemResource>, IItemHandlerFiltered {
    private final ResourceHandler<ItemResource>[] itemHandler;
    private final int[] baseIndex;
    private final int slotCount;

    @SafeVarargs
    @SuppressWarnings("varargs")
    public CombinedItemHandlerWrapper(ResourceHandler<ItemResource>... itemHandler) {
        this.itemHandler = itemHandler;
        this.baseIndex = new int[itemHandler.length];
        int index = 0;
        for (int i = 0; i < itemHandler.length; i++) {
            index += itemHandler[i].size();
            baseIndex[i] = index;
        }
        this.slotCount = index;
    }

    protected int getIndexForSlot(int slot) {
        if (slot < 0 || slot >= slotCount) {
            throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + slotCount + ")");
        }
        for (int i = 0; i < baseIndex.length; i++) {
            if (slot < baseIndex[i]) {
                return i;
            }
        }
        return -1;
    }

    protected ResourceHandler<ItemResource> getHandlerFromIndex(int index) {
        return itemHandler[index];
    }

    protected int getSlotFromIndex(int slot, int index) {
        if (index == -1 || index >= baseIndex.length) {
            return -1;
        }
        return index == 0 ? slot : slot - baseIndex[index - 1];
    }

    @Override
    public int size() {
        return slotCount;
    }

    @Override
    public ItemResource getResource(int index) {
        int handlerIndex = getIndexForSlot(index);
        int localSlot = getSlotFromIndex(index, handlerIndex);
        return getHandlerFromIndex(handlerIndex).getResource(localSlot);
    }

    @Override
    public long getAmountAsLong(int index) {
        int handlerIndex = getIndexForSlot(index);
        int localSlot = getSlotFromIndex(index, handlerIndex);
        return getHandlerFromIndex(handlerIndex).getAmountAsLong(localSlot);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        int handlerIndex = getIndexForSlot(index);
        int localSlot = getSlotFromIndex(index, handlerIndex);
        return getHandlerFromIndex(handlerIndex).getCapacityAsLong(localSlot, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        int handlerIndex = getIndexForSlot(index);
        int localSlot = getSlotFromIndex(index, handlerIndex);
        return getHandlerFromIndex(handlerIndex).isValid(localSlot, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        int handlerIndex = getIndexForSlot(index);
        int localSlot = getSlotFromIndex(index, handlerIndex);
        return getHandlerFromIndex(handlerIndex).insert(localSlot, resource, amount, tx);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        int handlerIndex = getIndexForSlot(index);
        int localSlot = getSlotFromIndex(index, handlerIndex);
        return getHandlerFromIndex(handlerIndex).extract(localSlot, resource, amount, tx);
    }

    @Override
    public net.minecraft.world.item.ItemStack getFilter(int slot) {
        int index = getIndexForSlot(slot);
        ResourceHandler<ItemResource> handler = getHandlerFromIndex(index);
        slot = getSlotFromIndex(slot, index);
        if (handler instanceof IItemHandlerFiltered) {
            return ((IItemHandlerFiltered) handler).getFilter(slot);
        }
        return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
    }
}
