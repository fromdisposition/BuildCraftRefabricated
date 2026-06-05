package buildcraft.lib.tile.item;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class WrappedItemHandlerExtract extends DelegateItemHandler {
    public WrappedItemHandlerExtract(ResourceHandler<ItemResource> delegate) {
        super(delegate);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        return 0;
    }
}
