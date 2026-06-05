package buildcraft.lib.common;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import buildcraft.lib.fluids.FluidStack;
public final class EventHooks {
    private EventHooks() {}
    public static void onFluidTooltip(FluidStack stack, Player player, List<Component> tooltips, TooltipFlag flag, Item.TooltipContext context) {}
}
