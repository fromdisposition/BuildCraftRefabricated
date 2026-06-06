package buildcraft.lib.list;

import buildcraft.api.lists.ListRegistry;

public final class VanillaListHandlers {
   private VanillaListHandlers() {
   }

   public static void register() {
      ListRegistry.registerHandler(new ListMatchHandlerTags());
      ListRegistry.registerHandler(new ListMatchHandlerTools());
      ListRegistry.registerHandler(new ListMatchHandlerArmor());
      ListRegistry.registerHandler(new ListMatchHandlerFluid());
      ListRegistry.registerHandler(new ListMatchHandlerClass());
   }
}
