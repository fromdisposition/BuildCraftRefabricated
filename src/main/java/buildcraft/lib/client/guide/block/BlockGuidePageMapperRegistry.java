package buildcraft.lib.client.guide.block;

import buildcraft.lib.engine.BlockEngineBase_BC8;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockGuidePageMapperRegistry {
   private static final Map<Class<? extends Block>, IBlockGuidePageMapper> BY_BLOCK_CLASS = new IdentityHashMap<>();
   private static final IBlockGuidePageMapper ENGINE_MAPPER = new EngineBlockMapper();

   private BlockGuidePageMapperRegistry() {
   }

   public static void register(Class<? extends Block> blockClass, IBlockGuidePageMapper mapper) {
      BY_BLOCK_CLASS.put(blockClass, mapper);
   }

   @Nullable
   public static Identifier resolvePageId(BlockState state) {
      if (state == null) {
         return null;
      }

      IBlockGuidePageMapper mapper = mapperFor(state.getBlock());
      if (mapper == null) {
         return null;
      }

      String pageName = mapper.getFor(null, null, state);
      return pageName != null && !pageName.isEmpty() ? Identifier.parse("buildcraft:block/" + pageName) : null;
   }

   @Nullable
   private static IBlockGuidePageMapper mapperFor(Block block) {
      for (Entry<Class<? extends Block>, IBlockGuidePageMapper> entry : BY_BLOCK_CLASS.entrySet()) {
         if (entry.getKey().isInstance(block)) {
            return entry.getValue();
         }
      }

      return null;
   }

   @Nullable
   public static Identifier resolvePageIdFromItemBlock(Block block) {
      return block != null && !block.defaultBlockState().isAir() ? resolvePageId(block.defaultBlockState()) : null;
   }

   static {
      register(BlockEngineBase_BC8.class, ENGINE_MAPPER);
   }
}
