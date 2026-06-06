package buildcraft.api.recipes;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public abstract class AssemblyRecipe implements Comparable<AssemblyRecipe> {
   private String registryName;

   public abstract Set<ItemStack> getOutputs(NonNullList<ItemStack> var1);

   public abstract Set<ItemStack> getOutputPreviews();

   public abstract Set<IngredientStack> getInputsFor(@Nonnull ItemStack var1);

   public abstract long getRequiredMicroJoulesFor(@Nonnull ItemStack var1);

   public final AssemblyRecipe setRegistryName(String name) {
      this.registryName = name;
      return this;
   }

   public final AssemblyRecipe setRegistryName(Object name) {
      return this;
   }

   public String getRegistryName() {
      return this.registryName;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssemblyRecipe that) ? false : Objects.equals(this.registryName, that.registryName);
      }
   }

   @Override
   public int hashCode() {
      return this.registryName != null ? this.registryName.hashCode() : 0;
   }

   public int compareTo(AssemblyRecipe o) {
      return this.registryName != null && o.registryName != null ? this.registryName.toString().compareTo(o.registryName.toString()) : 0;
   }
}
