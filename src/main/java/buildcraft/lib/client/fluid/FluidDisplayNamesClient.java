package buildcraft.lib.client.fluid;

import buildcraft.lib.fabric.transfer.fluid.FluidVariants;
import buildcraft.lib.fluid.display.FluidDisplayNames;
import buildcraft.lib.fluid.stack.FluidStack;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class FluidDisplayNamesClient {
   private FluidDisplayNamesClient() {
   }

   public static void register() {
      FluidDisplayNames.setClientResolver(FluidDisplayNamesClient::resolve);
   }

   @Nullable
   public static Component resolve(FluidStack stack) {
      if (stack == null || stack.isEmpty()) {
         return null;
      }

      List<Component> tooltip = FluidVariantRendering.getTooltip(FluidVariants.toVariant(stack));
      if (!tooltip.isEmpty()) {
         Component name = tooltip.getFirst();
         String text = name.getString();
         if (!text.isEmpty() && !text.equals(stack.getDescriptionId())) {
            return name;
         }
      }

      return null;
   }
}
