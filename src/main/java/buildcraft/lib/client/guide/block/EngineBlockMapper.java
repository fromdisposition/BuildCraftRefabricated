package buildcraft.lib.client.guide.block;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.properties.BuildCraftProperties;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EngineBlockMapper implements IBlockGuidePageMapper {
   @Override
   public String getFor(Level world, BlockPos pos, BlockState state) {
      if (state.hasProperty(BuildCraftProperties.ENGINE_TYPE)) {
         EnumEngineType type = (EnumEngineType)state.getValue(BuildCraftProperties.ENGINE_TYPE);
         return "engine_" + type.unlocalizedTag;
      } else {
         Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
         String path = id.getPath();
         return !path.startsWith("engine_") && !"mj_dynamo".equals(path) ? path : path;
      }
   }

   @Override
   public List<String> getAllPossiblePages() {
      List<String> list = Lists.newArrayList();

      for (EnumEngineType type : EnumEngineType.values()) {
         list.add("engine_" + type.unlocalizedTag);
      }

      list.add("engine_stone");
      list.add("engine_iron");
      list.add("engine_redstone");
      list.add("engine_rf");
      list.add("engine_creative");
      list.add("mj_dynamo");
      return list;
   }
}
