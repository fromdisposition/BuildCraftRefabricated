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
         //? if >= 26.1 {
         Minecraft.getInstance().getBlockColors().register(sources, block);
         //?} else {
         /*// 1.21.x registers a single BlockColor; dispatch by tint index into the source list.
         Minecraft.getInstance().getBlockColors().register(
            (state, level, pos, tintIndex) -> {
               if (tintIndex < 0 || tintIndex >= sources.size()) {
                  return -1;
               }
               BlockTintSource src = sources.get(tintIndex);
               return level != null && pos != null ? src.colorInWorld(state, level, pos) : src.color(state);
            },
            block
         );
         *///?}
      }
   }
}
