/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.core.gen.SpringFeature;
import buildcraft.fabric.BCRegistries;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class BCCoreFeatures {
   public static final ResourceKey<PlacedFeature> WATER_SPRING_PLACED = ResourceKey.create(
      Registries.PLACED_FEATURE, BCRegistries.id("buildcraftcore", "water_spring")
   );
   public static Feature<NoneFeatureConfiguration> SPRING;

   private BCCoreFeatures() {
   }

   public static void register() {
      SPRING = (Feature<NoneFeatureConfiguration>)Registry.register(
         BuiltInRegistries.FEATURE, BCRegistries.id("buildcraftcore", "spring"), new SpringFeature(NoneFeatureConfiguration.CODEC)
      );
      BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), Decoration.UNDERGROUND_DECORATION, WATER_SPRING_PLACED);
   }
}
