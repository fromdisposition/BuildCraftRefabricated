package buildcraft.lib.fabric.mixin;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapelessRecipe.class)
public interface ShapelessRecipeAccessor {
   // ShapelessRecipe.ingredients is List<Ingredient> from 1.21.10 onward, but NonNullList<Ingredient> on
   // 1.21.1. The @Accessor descriptor must match the real field type exactly or Mixin/tiny-remapper cannot
   // locate it (the back-compat path remaps mixin refs in place against the intermediary class).
   @Accessor("ingredients")
   //? if >= 1.21.10 {
   List<Ingredient> buildcraft$getIngredients();
   //?} else {
   /*NonNullList<Ingredient> buildcraft$getIngredients();
   *///?}
}
