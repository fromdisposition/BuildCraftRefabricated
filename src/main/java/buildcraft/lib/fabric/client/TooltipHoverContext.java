package buildcraft.lib.fabric.client;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

public final class TooltipHoverContext {
    private static final ThreadLocal<ItemStack> HOVERED = new ThreadLocal<>();

    private TooltipHoverContext() {}

    public static void set(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            HOVERED.remove();
        } else {
            HOVERED.set(stack);
        }
    }

    @Nullable
    public static ItemStack get() {
        return HOVERED.get();
    }

    public static void clear() {
        HOVERED.remove();
    }
}
