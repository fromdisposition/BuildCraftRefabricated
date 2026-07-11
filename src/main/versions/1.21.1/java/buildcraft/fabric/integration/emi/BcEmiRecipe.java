package buildcraft.fabric.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Base for BC's EMI recipes: category + ingredient lists; subclasses only lay out widgets. */
abstract class BcEmiRecipe implements EmiRecipe {
   private final EmiRecipeCategory category;
   private final List<EmiIngredient> inputs;
   private final List<EmiStack> outputs;
   private final int width;
   private final int height;

   BcEmiRecipe(EmiRecipeCategory category, List<EmiIngredient> inputs, List<EmiStack> outputs, int width, int height) {
      this.category = category;
      this.inputs = inputs;
      this.outputs = outputs;
      this.width = width;
      this.height = height;
   }

   @Override
   public EmiRecipeCategory getCategory() {
      return this.category;
   }

   @Override
   public @Nullable Identifier getId() {
      return null;
   }

   @Override
   public List<EmiIngredient> getInputs() {
      return this.inputs;
   }

   @Override
   public List<EmiStack> getOutputs() {
      return this.outputs;
   }

   @Override
   public int getDisplayWidth() {
      return this.width;
   }

   @Override
   public int getDisplayHeight() {
      return this.height;
   }
}
