package buildcraft.builders.snapshot;

import buildcraft.api.core.EnumHandlerPriority;
import buildcraft.api.template.ITemplateHandler;
import buildcraft.api.template.ITemplateRegistry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public enum TemplateRegistry implements ITemplateRegistry {
   INSTANCE;

   private final EnumMap<EnumHandlerPriority, List<ITemplateHandler>> handlers = new EnumMap<>(EnumHandlerPriority.class);

   TemplateRegistry() {
      for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
         this.handlers.put(priority, new ArrayList<>());
      }
   }

   @Override
   public void addHandler(ITemplateHandler handler, EnumHandlerPriority priority) {
      this.handlers.get(priority).add(handler);
   }

   @Override
   public boolean handle(Level world, BlockPos pos, Player player, ItemStack stack) {
      for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
         for (ITemplateHandler handler : this.handlers.get(priority)) {
            if (handler.handle(world, pos, player, stack)) {
               return true;
            }
         }
      }

      return false;
   }
}
