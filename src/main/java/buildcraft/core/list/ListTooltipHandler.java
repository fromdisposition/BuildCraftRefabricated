package buildcraft.core.list;

import buildcraft.api.items.IList;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class ListTooltipHandler {
   private ListTooltipHandler() {
   }

   public static void register() {
      ItemTooltipCallback.EVENT.register(ListTooltipHandler::onItemTooltip);
   }

   private static void onItemTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      Player player = Minecraft.getInstance().player;
      if (!stack.isEmpty() && player != null && player.containerMenu instanceof ContainerList containerList) {
         ItemStack list = containerList.getListItemStack();
         if (!list.isEmpty() && list.getItem() instanceof IList listItem && listItem.matches(list, stack)) {
            lines.add(Component.translatable("tip.list.matches").withStyle(ChatFormatting.GREEN));
         }
      }
   }
}
