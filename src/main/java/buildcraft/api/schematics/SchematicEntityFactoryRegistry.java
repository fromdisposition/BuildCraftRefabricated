/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.schematics;

import buildcraft.api.core.BuildCraftAPI;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class SchematicEntityFactoryRegistry {
   private static final Set<SchematicEntityFactory<?>> FACTORIES = new TreeSet<>();

   public static <S extends ISchematicEntity> void registerFactory(String name, int priority, Predicate<SchematicEntityContext> predicate, Supplier<S> supplier) {
      FACTORIES.add(new SchematicEntityFactory<>(BuildCraftAPI.nameToResourceLocation(name), priority, predicate, supplier));
   }

   public static <S extends ISchematicEntity> void registerFactory(String name, int priority, List<Object> entities, Supplier<S> supplier) {
      registerFactory(name, priority, context -> entities.contains(BuiltInRegistries.ENTITY_TYPE.getKey(context.entity.getType())), supplier);
   }

   public static List<SchematicEntityFactory<?>> getFactories() {
      return ImmutableList.copyOf(FACTORIES);
   }

   @Nonnull
   @SuppressWarnings("unchecked")
   public static <S extends ISchematicEntity> SchematicEntityFactory<S> getFactoryByInstance(S instance) {
      return (SchematicEntityFactory<S>)FACTORIES.stream()
         .filter(schematicEntityFactory -> schematicEntityFactory.clazz == instance.getClass())
         .findFirst()
         .orElseThrow(() -> new IllegalStateException("Didn't find a factory for " + instance.getClass()));
   }

   @Nullable
   public static SchematicEntityFactory<?> getFactoryByName(Object name) {
      Identifier id = name instanceof Identifier i ? i : (name instanceof String s ? BuildCraftAPI.nameToResourceLocation(s) : null);
      return id == null ? null : FACTORIES.stream().filter(schematicEntityFactory -> id.equals(schematicEntityFactory.name)).findFirst().orElse(null);
   }
}
