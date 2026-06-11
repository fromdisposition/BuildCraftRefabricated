package buildcraft.energy.generation.core;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

/**
 * Design-time biome ids for oil patches (advancements, config lists).
 *
 * <p>Patch sampling lives in {@link OilPatchSampler}; these ids are only surfaced for logging and design checks.
 */
public final class OilBiomePatches {
   public static final Identifier OIL_OCEAN = Identifier.parse("buildcraftenergy:oil_ocean");
   public static final Identifier OIL_DESERT = Identifier.parse("buildcraftenergy:oil_desert");

   private OilBiomePatches() {
   }

   public static Identifier effectiveBiomeId(ServerLevel level, int x, int z, Holder<Biome> biome, Identifier fallback) {
      return OilPatchSampler.sample(level, x, z, biome, fallback).effectiveBiomeId();
   }
}
