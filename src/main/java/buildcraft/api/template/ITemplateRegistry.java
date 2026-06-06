package buildcraft.api.template;

import buildcraft.api.core.EnumHandlerPriority;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ITemplateRegistry {
   default void addHandler(ITemplateHandler handler) {
      this.addHandler(handler, EnumHandlerPriority.NORMAL);
   }

   void addHandler(ITemplateHandler var1, EnumHandlerPriority var2);

   boolean handle(Level var1, BlockPos var2, Player var3, ItemStack var4);
}
