package buildcraft.energy.worldgen.template;

import buildcraft.energy.worldgen.core.WaterSpringDefaults;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public final class WaterSpringTemplateBuilder {
   private static final Identifier SPRING_BLOCK_ID = Identifier.parse("buildcraftcore:spring_water");

   private WaterSpringTemplateBuilder() {
   }

   public static void generate(final Path structuresDir, final HolderGetter<Block> blocks) throws IOException {
      StructureTemplateExporter.write(
         structuresDir.resolve("water_spring.nbt"),
         blocks,
         1,
         1,
         1,
         List.of(
            new StructureTemplateExporter.BlockEntry(
               0,
               WaterSpringDefaults.SPRING_TEMPLATE_Y,
               0,
               blocks.getOrThrow(ResourceKey.create(Registries.BLOCK, SPRING_BLOCK_ID)).value().defaultBlockState()
            )
         )
      );
   }
}
