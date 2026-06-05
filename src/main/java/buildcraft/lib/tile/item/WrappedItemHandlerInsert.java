package buildcraft.lib.tile.item;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class WrappedItemHandlerInsert extends DelegateItemHandler {
    public WrappedItemHandlerInsert(ResourceHandler<ItemResource> delegate) {
        super(delegate);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        return 0;
    }
}
