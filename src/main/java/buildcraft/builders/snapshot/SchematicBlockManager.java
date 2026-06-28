/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.schematics.SchematicBlockFactory;
import buildcraft.api.schematics.SchematicBlockFactoryRegistry;
import com.google.common.collect.Lists;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public class SchematicBlockManager {
   private static final Identifier DEFAULT_FACTORY = Identifier.parse("buildcraftrefabricated:default");

   private static Identifier normalizeFactoryName(String raw) throws InvalidInputDataException {
      if (raw != null && !raw.isBlank()) {
         Identifier name;
         try {
            name = Identifier.parse(raw);
         } catch (Exception e) {
            throw new InvalidInputDataException("Invalid schematic type name '" + raw + "'", e);
         }
         return switch (name.toString()) {
            case "buildcraft:default", "buildcraftbuilders:default" -> DEFAULT_FACTORY;
            case "buildcraft:air", "buildcraftbuilders:air" -> Identifier.parse("buildcraftrefabricated:air");
            case "buildcraft:pipe", "buildcrafttransport:pipe" -> Identifier.parse("buildcraftrefabricated:pipe");
            case "buildcraft:fluid", "buildcraftbuilders:fluid" -> Identifier.parse("buildcraftrefabricated:fluid");
            default -> name;
         };
      } else {
         throw new InvalidInputDataException("Missing schematic type name");
      }
   }

   public static ISchematicBlock getSchematicBlock(SchematicBlockContext context) {
      for (SchematicBlockFactory<?> schematicBlockFactory : Lists.reverse(SchematicBlockFactoryRegistry.getFactories())) {
         if (schematicBlockFactory.predicate.test(context)) {
            ISchematicBlock schematicBlock = (ISchematicBlock)schematicBlockFactory.supplier.get();
            schematicBlock.init(context);
            return schematicBlock;
         }
      }

      SchematicBlockFactory<?> fallback = SchematicBlockFactoryRegistry.getFactoryByName(DEFAULT_FACTORY);
      if (fallback != null) {
         ISchematicBlock schematicBlock = (ISchematicBlock)fallback.supplier.get();
         schematicBlock.init(context);
         return schematicBlock;
      } else {
         throw new UnsupportedOperationException("No schematic factory matched for " + context.blockState);
      }
   }

   public static <S extends ISchematicBlock> S createCleanCopy(S schematicBlock) {
      return SchematicBlockFactoryRegistry.getFactoryByInstance(schematicBlock).supplier.get();
   }

   @Nonnull
   public static <S extends ISchematicBlock> CompoundTag writeToNBT(S schematicBlock) {
      CompoundTag schematicBlockTag = new CompoundTag();
      schematicBlockTag.putString("name", SchematicBlockFactoryRegistry.getFactoryByInstance(schematicBlock).name.toString());
      schematicBlockTag.put("data", schematicBlock.serializeNBT());
      return schematicBlockTag;
   }

   @Nonnull
   public static ISchematicBlock readFromNBT(CompoundTag schematicBlockTag) throws InvalidInputDataException {
      Identifier name = normalizeFactoryName(BcNbt.getString(schematicBlockTag, "name", ""));
      SchematicBlockFactory<?> factory = SchematicBlockFactoryRegistry.getFactoryByName(name);
      if (factory == null) {
         throw new InvalidInputDataException("Unknown schematic type " + name);
      }

      ISchematicBlock schematicBlock = (ISchematicBlock)factory.supplier.get();
      CompoundTag data = BcNbt.getCompound(schematicBlockTag, "data");

      try {
         schematicBlock.deserializeNBT(data);
         return schematicBlock;
      } catch (InvalidInputDataException e) {
         throw new InvalidInputDataException("Failed to load the schematic from " + data, e);
      }
   }

   public static boolean isSchematicTypeRegistered(Identifier name) {
      return SchematicBlockFactoryRegistry.getFactoryByName(name) != null;
   }
}
