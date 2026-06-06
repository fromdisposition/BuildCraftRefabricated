package buildcraft.transport;

import buildcraft.fabric.BCRegistries;
import buildcraft.transport.recipe.PipeColourRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class BCTransportRecipeSerializers {
   private BCTransportRecipeSerializers() {
   }

   public static void register() {
      Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BCRegistries.id("buildcrafttransport", "pipe_colour"), PipeColourRecipe.SERIALIZER);
   }
}
