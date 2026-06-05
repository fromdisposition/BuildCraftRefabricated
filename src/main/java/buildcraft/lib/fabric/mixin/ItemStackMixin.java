package buildcraft.lib.fabric.mixin;

import net.minecraft.world.item.ItemStack;
import buildcraft.lib.attachments.ItemAttachment;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Nullable
    public <T, C> T getCapability(ItemAttachment<T, C> capability, C context) {
        return capability.getCapability((ItemStack) (Object) this, context);
    }
}
