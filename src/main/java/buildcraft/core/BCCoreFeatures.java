package buildcraft.core;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import buildcraft.core.gen.SpringFeature;
import buildcraft.fabric.BCRegistries;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;

public final class BCCoreFeatures {
    public static final ResourceKey<PlacedFeature> WATER_SPRING_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, BCRegistries.id(BCCore.MODID, "water_spring"));

    public static Feature<NoneFeatureConfiguration> SPRING;

    private BCCoreFeatures() {}

    public static void register() {
        SPRING = Registry.register(
                BuiltInRegistries.FEATURE,
                BCRegistries.id(BCCore.MODID, "spring"),
                new SpringFeature(NoneFeatureConfiguration.CODEC));

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                WATER_SPRING_PLACED);
    }
}
