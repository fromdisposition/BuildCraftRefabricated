package buildcraft.fabric.fluid;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class BcFluidTags {
   /** All BC oil/fuel variants — fog, overlay, and generic world detection. */
   public static final TagKey<Fluid> BC_FLUIDS = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("buildcraftenergy", "bc_fluids"));
   /** Non-gaseous BC fluids — water-like entity physics and crawl swimming. */
   public static final TagKey<Fluid> BC_LIQUIDS = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("buildcraftenergy", "bc_liquids"));

   private BcFluidTags() {
   }
}
