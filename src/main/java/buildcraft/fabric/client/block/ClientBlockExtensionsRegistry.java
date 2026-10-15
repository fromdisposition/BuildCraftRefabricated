package buildcraft.fabric.client.block;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class ClientBlockExtensionsRegistry {
   private static final Map<Block, ClientBlockExtensions> BLOCK_EXTENSIONS = new HashMap<>();

   public void registerBlock(ClientBlockExtensions extensions, Block block) {
      BLOCK_EXTENSIONS.put(block, extensions);
   }

   public static @Nullable ClientBlockExtensions get(Block block) {
      return BLOCK_EXTENSIONS.get(block);
   }
}
