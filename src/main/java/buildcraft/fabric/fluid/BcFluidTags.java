package buildcraft.fabric.fluid;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class BcFluidTags {
   public static final TagKey<Fluid> BC_FLUIDS = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("buildcraftenergy", "bc_fluids"));

   private BcFluidTags() {
   }
}
