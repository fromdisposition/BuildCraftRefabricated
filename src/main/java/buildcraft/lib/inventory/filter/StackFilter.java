package buildcraft.lib.inventory.filter;

import net.minecraft.world.item.ItemStack;

import buildcraft.api.core.IStackFilter;

public enum StackFilter implements IStackFilter {
    ALL {
        @Override
        public boolean matches(ItemStack stack) {
            return true;
        }
    },
    NONE {
        @Override
        public boolean matches(ItemStack stack) {
            return false;
        }
    };
}
