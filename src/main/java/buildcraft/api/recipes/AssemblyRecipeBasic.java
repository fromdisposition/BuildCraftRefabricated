package buildcraft.api.recipes;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class AssemblyRecipeBasic extends AssemblyRecipe {
   private final long requiredMicroJoules;
   private final ImmutableSet<IngredientStack> requiredStacks;
   private final ImmutableSet<ItemStack> output;

   public AssemblyRecipeBasic(String name, long requiredMicroJoules, ImmutableSet<IngredientStack> requiredStacks, @Nonnull ItemStack output) {
      this.requiredMicroJoules = requiredMicroJoules;
      this.requiredStacks = ImmutableSet.copyOf(requiredStacks);
      this.output = ImmutableSet.of(output);
      this.setRegistryName(name);
   }

   public AssemblyRecipeBasic(String name, long requiredMicroJoules, Set<IngredientStack> requiredStacks, @Nonnull ItemStack output) {
      this(name, requiredMicroJoules, ImmutableSet.copyOf(requiredStacks), output);
   }

   @Override
   public Set<ItemStack> getOutputs(NonNullList<ItemStack> inputs) {
      return (Set<ItemStack>)(this.requiredStacks
            .stream()
            .allMatch(
               definition -> inputs.stream().anyMatch(stack -> !stack.isEmpty() && definition.ingredient.test(stack) && stack.getCount() >= definition.count)
            )
         ? this.output
         : Collections.emptySet());
   }

   @Override
   public Set<ItemStack> getOutputPreviews() {
      return this.output;
   }

   @Override
   public Set<IngredientStack> getInputsFor(@Nonnull ItemStack output) {
      return this.requiredStacks;
   }

   @Override
   public long getRequiredMicroJoulesFor(@Nonnull ItemStack output) {
      return this.requiredMicroJoules;
   }
}
