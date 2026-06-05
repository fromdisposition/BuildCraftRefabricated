package buildcraft.lib.fabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.fabric.client.TooltipHoverContext;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenTooltipMixin {
    @Inject(method = "extractTooltip", at = @At("HEAD"))
    private void buildcraft$captureHoveredStack(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        if (self instanceof AbstractContainerScreenAccessor accessor) {
            var slot = accessor.buildcraft$getHoveredSlot();
            if (slot != null && slot.hasItem()) {
                TooltipHoverContext.set(slot.getItem());
                return;
            }
        }
        TooltipHoverContext.clear();
    }

    @Inject(method = "extractTooltip", at = @At("RETURN"))
    private void buildcraft$clearHoveredStack(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        TooltipHoverContext.clear();
    }
}
