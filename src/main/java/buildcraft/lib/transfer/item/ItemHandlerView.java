package buildcraft.lib.transfer.item;

public interface ItemHandlerView {
    int getSlots();
    Object getStackInSlot(int slot);
    Object insertItem(int slot, Object stack, boolean simulate);
    Object extractItem(int slot, int amount, boolean simulate);
    int getSlotLimit(int slot);
    boolean isItemValid(int slot, Object stack);
}
