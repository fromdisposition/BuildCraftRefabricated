package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListRegistry;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class ListMatchHandlerClass extends ListMatchHandler {
   @Override
   public boolean matches(ListMatchHandler.Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
      if (type != ListMatchHandler.Type.TYPE) {
         return false;
      }

      Class<?> kl = stack.getItem().getClass();
      return ListRegistry.itemClassAsType.contains(kl) && kl.equals(target.getClass());
   }

   @Override
   public boolean isValidSource(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      if (type == ListMatchHandler.Type.TYPE) {
         Class<?> kl = stack.getItem().getClass();
         return ListRegistry.itemClassAsType.contains(kl);
      } else {
         return false;
      }
   }
}
