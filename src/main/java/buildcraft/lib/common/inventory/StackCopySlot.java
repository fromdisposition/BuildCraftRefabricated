package buildcraft.lib.common.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StackCopySlot extends Slot {
    private static final Container DUMMY = new SimpleContainer(1);

    public StackCopySlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    public StackCopySlot(int slot, int x, int y) {
        super(DUMMY, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
