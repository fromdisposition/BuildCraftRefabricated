package buildcraft.builders;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.schematics.SchematicBlockFactoryRegistry;
import buildcraft.api.schematics.SchematicEntityFactoryRegistry;
import buildcraft.builders.snapshot.SchematicBlockAir;
import buildcraft.builders.snapshot.SchematicBlockDefault;
import buildcraft.builders.snapshot.SchematicBlockFluid;
import buildcraft.builders.snapshot.SchematicEntityDefault;
import buildcraft.transport.pipe.SchematicBlockPipe;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BCBuildersSchematics {
   public static void preInit() {
      registerSchematicFactory("buildcraftrefabricated:air", 0, SchematicBlockAir::predicate, SchematicBlockAir::new);
      registerSchematicFactory("buildcraftrefabricated:default", 100, SchematicBlockDefault::predicate, SchematicBlockDefault::new);
      registerSchematicFactory("buildcraftrefabricated:pipe", 150, SchematicBlockPipe::predicate, SchematicBlockPipe::new);
      registerSchematicFactory("buildcraftrefabricated:fluid", 200, SchematicBlockFluid::predicate, SchematicBlockFluid::new);
      SchematicEntityFactoryRegistry.registerFactory("buildcraftrefabricated:default", 100, SchematicEntityDefault::predicate, SchematicEntityDefault::new);
   }

   private static <S extends ISchematicBlock> void registerSchematicFactory(
      String name, int priority, Predicate<SchematicBlockContext> predicate, Supplier<S> supplier
   ) {
      SchematicBlockFactoryRegistry.registerFactory(name, priority, predicate, supplier);
   }
}
