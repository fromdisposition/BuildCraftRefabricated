package buildcraft.lib.fabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.fabric.client.TooltipHoverContext;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorItemTooltipMixin {
    @Inject(
            method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD"))
    private void buildcraft$captureItemStackTooltip(Font font, ItemStack stack, int xo, int yo, CallbackInfo ci) {
        TooltipHoverContext.set(stack);
    }
}
