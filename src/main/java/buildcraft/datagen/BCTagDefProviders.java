package buildcraft.datagen;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/** Emits the handwritten convention/vanilla tags from {@link BCAssetDefs}; a value prefixed with '?' is an
 * optional entry (a block that only registers when its companion mod is loaded). */
public final class BCTagDefProviders {
   public static final class Blocks extends FabricTagsProvider<Block> {
      public Blocks(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
         super(output, Registries.BLOCK, registries);
      }

      @Override
      protected void addTags(HolderLookup.Provider provider) {
         for (BCAssetDefs.Tag tag : BCAssetDefs.TAGS_BLOCK) {
            var builder = builder(TagKey.create(Registries.BLOCK, Identifier.parse(tag.id())));
            for (String value : tag.values()) {
               if (value.startsWith("#")) {
                  builder.addTag(TagKey.create(Registries.BLOCK, Identifier.parse(value.substring(1))));
               } else if (value.startsWith("?")) {
                  builder.addOptional(ResourceKey.create(Registries.BLOCK, Identifier.parse(value.substring(1))));
               } else {
                  builder.add(ResourceKey.create(Registries.BLOCK, Identifier.parse(value)));
               }
            }
         }
      }
   }

   public static final class Items extends FabricTagsProvider<Item> {
      public Items(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
         super(output, Registries.ITEM, registries);
      }

      @Override
      protected void addTags(HolderLookup.Provider provider) {
         for (BCAssetDefs.Tag tag : BCAssetDefs.TAGS_ITEM) {
            var builder = builder(TagKey.create(Registries.ITEM, Identifier.parse(tag.id())));
            for (String value : tag.values()) {
               if (value.startsWith("#")) {
                  builder.addTag(TagKey.create(Registries.ITEM, Identifier.parse(value.substring(1))));
               } else if (value.startsWith("?")) {
                  builder.addOptional(ResourceKey.create(Registries.ITEM, Identifier.parse(value.substring(1))));
               } else {
                  builder.add(ResourceKey.create(Registries.ITEM, Identifier.parse(value)));
               }
            }
         }
      }
   }

   public static final class Fluids extends FabricTagsProvider<Fluid> {
      public Fluids(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
         super(output, Registries.FLUID, registries);
      }

      @Override
      protected void addTags(HolderLookup.Provider provider) {
         for (BCAssetDefs.Tag tag : BCAssetDefs.TAGS_FLUID) {
            var builder = builder(TagKey.create(Registries.FLUID, Identifier.parse(tag.id())));
            for (String value : tag.values()) {
               if (value.startsWith("#")) {
                  builder.addTag(TagKey.create(Registries.FLUID, Identifier.parse(value.substring(1))));
               } else if (value.startsWith("?")) {
                  builder.addOptional(ResourceKey.create(Registries.FLUID, Identifier.parse(value.substring(1))));
               } else {
                  builder.add(ResourceKey.create(Registries.FLUID, Identifier.parse(value)));
               }
            }
         }
      }
   }

   private BCTagDefProviders() {
   }
}
