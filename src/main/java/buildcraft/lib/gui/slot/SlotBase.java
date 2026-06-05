package buildcraft.lib.gui.slot;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

import buildcraft.lib.tile.item.IItemHandlerAdv;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.Transaction;

public class SlotBase extends Slot {
    public final int handlerIndex;
    public final IItemHandlerAdv itemHandler;

    public SlotBase(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY) {
        super(new DummyContainer(itemHandler), slotIndex, posX, posY);
        this.handlerIndex = slotIndex;
        this.itemHandler = itemHandler;
    }

    public boolean canShift() {
        return true;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) return false;
        return itemHandler.canSet(handlerIndex, stack);
    }

    @Override
    @Nonnull
    public ItemStack getItem() {
        return itemHandler.getResource(handlerIndex).toStack(itemHandler.getAmountAsInt(handlerIndex));
    }

    @Override
    public void set(@Nonnull ItemStack stack) {
        if (itemHandler instanceof ItemHandlerSimple) {
            ((ItemHandlerSimple) itemHandler).setStackInSlot(handlerIndex, stack);
        } else {
            try (Transaction tx = Transaction.openRoot()) {
                itemHandler.extract(handlerIndex, itemHandler.getResource(handlerIndex), itemHandler.getAmountAsInt(handlerIndex), tx);
                if (!stack.isEmpty()) {
                    itemHandler.insert(handlerIndex, ItemResource.of(stack), stack.getCount(), tx);
                }
                tx.commit();
            }
        }
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return (int) itemHandler.getCapacityAsLong(handlerIndex, ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        int slotLimit = (int) itemHandler.getCapacityAsLong(handlerIndex, ItemResource.of(stack));
        return Math.min(slotLimit, stack.getMaxStackSize());
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        ItemStack current = getItem();
        if (current.isEmpty()) return ItemStack.EMPTY;
        int extracted = 0;
        try (Transaction tx = Transaction.openRoot()) {
            extracted = itemHandler.extract(handlerIndex, ItemResource.of(current), amount, tx);
            tx.commit();
        }
        return current.copyWithCount(extracted);
    }

    private static class DummyContainer implements Container {
        private final IItemHandlerAdv handler;
        public DummyContainer(IItemHandlerAdv handler) { this.handler = handler; }
        @Override public int getContainerSize() { return handler.size(); }
        @Override public boolean isEmpty() { return false; }
        @Override public ItemStack getItem(int index) { return handler.getResource(index).toStack(handler.getAmountAsInt(index)); }
        @Override public ItemStack removeItem(int index, int count) { return ItemStack.EMPTY; }
        @Override public ItemStack removeItemNoUpdate(int index) { return ItemStack.EMPTY; }
        @Override public void setItem(int index, ItemStack stack) {}
        @Override public void setChanged() {}
        @Override public boolean stillValid(Player player) { return true; }
        @Override public void clearContent() {}
    }
}
