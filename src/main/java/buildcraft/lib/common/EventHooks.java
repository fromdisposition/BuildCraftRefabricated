package buildcraft.lib.common;

import buildcraft.lib.fluids.FluidStack;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class EventHooks {
   private EventHooks() {
   }

   public static void onFluidTooltip(FluidStack stack, Player player, List<Component> tooltips, TooltipFlag flag, TooltipContext context) {
   }
}
