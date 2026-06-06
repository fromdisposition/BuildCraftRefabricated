package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.api.schematics.SchematicEntityFactory;
import buildcraft.api.schematics.SchematicEntityFactoryRegistry;
import com.google.common.collect.Lists;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public class SchematicEntityManager {
   public static ISchematicEntity getSchematicEntity(SchematicEntityContext context) {
      for (SchematicEntityFactory<?> schematicEntityFactory : Lists.reverse(SchematicEntityFactoryRegistry.getFactories())) {
         if (schematicEntityFactory.predicate.test(context)) {
            ISchematicEntity schematicEntity = (ISchematicEntity)schematicEntityFactory.supplier.get();
            schematicEntity.init(context);
            return schematicEntity;
         }
      }

      return null;
   }

   public static <S extends ISchematicEntity> S createCleanCopy(S schematicEntity) {
      return SchematicEntityFactoryRegistry.getFactoryByInstance(schematicEntity).supplier.get();
   }

   @Nonnull
   public static <S extends ISchematicEntity> CompoundTag writeToNBT(S schematicEntity) {
      CompoundTag schematicEntityTag = new CompoundTag();
      schematicEntityTag.putString("name", SchematicEntityFactoryRegistry.getFactoryByInstance(schematicEntity).name.toString());
      schematicEntityTag.put("data", schematicEntity.serializeNBT());
      return schematicEntityTag;
   }

   @Nonnull
   public static ISchematicEntity readFromNBT(CompoundTag schematicEntityTag) throws InvalidInputDataException {
      Identifier name = Identifier.parse(schematicEntityTag.getStringOr("name", ""));
      SchematicEntityFactory<?> factory = SchematicEntityFactoryRegistry.getFactoryByName(name);
      if (factory == null) {
         throw new InvalidInputDataException("Unknown schematic type " + name);
      }

      ISchematicEntity schematicEntity = (ISchematicEntity)factory.supplier.get();
      CompoundTag data = schematicEntityTag.getCompoundOrEmpty("data");

      try {
         schematicEntity.deserializeNBT(data);
         return schematicEntity;
      } catch (InvalidInputDataException e) {
         throw new InvalidInputDataException("Failed to load the schematic from " + data, e);
      }
   }
}
