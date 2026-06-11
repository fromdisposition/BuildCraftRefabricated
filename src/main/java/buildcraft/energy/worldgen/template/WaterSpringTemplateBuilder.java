package buildcraft.energy.worldgen.template;

import buildcraft.energy.worldgen.core.WaterSpringDefaults;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class WaterSpringTemplateBuilder {
   private static final String SPRING_BLOCK = "buildcraftcore:spring_water";

   private WaterSpringTemplateBuilder() {
   }

   public static void generate(Path structuresDir) throws IOException {
      StructureNbtWriter.write(
         structuresDir.resolve("water_spring.nbt"),
         1,
         1,
         1,
         List.of(new StructureNbtWriter.BlockEntry(0, WaterSpringDefaults.SPRING_TEMPLATE_Y, 0, SPRING_BLOCK))
      );
   }
}
