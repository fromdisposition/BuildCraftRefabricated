package buildcraft.fabric.client.event;

import java.util.List;
import net.minecraft.world.level.block.Block;

public class RegisterColorHandlersEvent {
   public static final class BlockTintSources {
      public void register(List<?> sources, Block block) {
      }
   }

   public static class ItemTintSources extends BCClientEvents.RegisterColorHandlersEvent.ItemTintSources {
   }
}
