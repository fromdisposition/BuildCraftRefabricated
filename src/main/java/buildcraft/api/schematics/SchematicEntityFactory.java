package buildcraft.api.schematics;

import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class SchematicEntityFactory<S extends ISchematicEntity> implements Comparable<SchematicEntityFactory<?>> {
   @Nonnull
   public final Object name;
   public final int priority;
   @Nonnull
   public final Predicate<SchematicEntityContext> predicate;
   @Nonnull
   public final Supplier<S> supplier;
   @Nonnull
   public final Class<S> clazz;

   public SchematicEntityFactory(@Nonnull Object name, int priority, @Nonnull Predicate<SchematicEntityContext> predicate, @Nonnull Supplier<S> supplier) {
      this.name = name;
      this.priority = priority;
      this.predicate = predicate;
      this.supplier = supplier;
      this.clazz = (Class<S>)supplier.get().getClass();
   }

   public int compareTo(@Nonnull SchematicEntityFactory<?> o) {
      return this.priority != o.priority ? Integer.compare(this.priority, o.priority) : this.name.toString().compareTo(o.name.toString());
   }
}
