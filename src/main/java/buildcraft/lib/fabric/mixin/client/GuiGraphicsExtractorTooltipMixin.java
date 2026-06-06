package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.client.event.RenderTooltipEvent;
import buildcraft.lib.fabric.client.TooltipHoverContext;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorTooltipMixin {
   @Inject(
      method = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
      at = @At("HEAD")
   )
   private void buildcraft$firePreTooltip(
      Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner positioner, Identifier style, CallbackInfo ci
   ) {
      if (!components.isEmpty()) {
         ItemStack stack = TooltipHoverContext.get();
         if (stack != null && !stack.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            GuiGraphicsExtractor graphics = (GuiGraphicsExtractor)(Object)this;
            RenderTooltipEvent.Pre.fire(
               new RenderTooltipEvent.Pre(graphics, stack, font, components, positioner, graphics.guiWidth(), graphics.guiHeight(), mouseX, mouseY)
            );
         }
      }
   }
}
