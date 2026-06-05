package buildcraft.transport;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import buildcraft.fabric.BCRegistries;
import buildcraft.transport.recipe.PipeColourRecipe;

public final class BCTransportRecipeSerializers {
    private BCTransportRecipeSerializers() {}

    public static void register() {
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                BCRegistries.id(BCTransport.MODID, "pipe_colour"),
                PipeColourRecipe.SERIALIZER);
    }
}
