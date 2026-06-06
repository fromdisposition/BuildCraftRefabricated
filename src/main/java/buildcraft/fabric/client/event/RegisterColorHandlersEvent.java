package buildcraft.fabric.client.event;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;

public final class RegisterColorHandlersEvent {
   private RegisterColorHandlersEvent() {
   }

   public static final class BlockTintSources {
      public void register(List<BlockTintSource> sources, Block block) {
         Minecraft.getInstance().getBlockColors().register(sources, block);
      }
   }
}
