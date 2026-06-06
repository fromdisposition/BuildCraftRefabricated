package buildcraft.api.recipes;

import buildcraft.lib.fluids.FluidStack;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public interface IRefineryRecipeManager {
   IRefineryRecipeManager.IHeatableRecipe createHeatingRecipe(FluidStack var1, FluidStack var2, int var3, int var4);

   default IRefineryRecipeManager.IHeatableRecipe addHeatableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
      return this.getHeatableRegistry().addRecipe(this.createHeatingRecipe(in, out, heatFrom, heatTo));
   }

   IRefineryRecipeManager.ICoolableRecipe createCoolableRecipe(FluidStack var1, FluidStack var2, int var3, int var4);

   default IRefineryRecipeManager.ICoolableRecipe addCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
      return this.getCoolableRegistry().addRecipe(this.createCoolableRecipe(in, out, heatFrom, heatTo));
   }

   IRefineryRecipeManager.IDistillationRecipe createDistillationRecipe(FluidStack var1, FluidStack var2, FluidStack var3, long var4);

   default IRefineryRecipeManager.IDistillationRecipe addDistillationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, long powerRequired) {
      return this.getDistillationRegistry().addRecipe(this.createDistillationRecipe(in, outGas, outLiquid, powerRequired));
   }

   IRefineryRecipeManager.IRefineryRegistry<IRefineryRecipeManager.IHeatableRecipe> getHeatableRegistry();

   IRefineryRecipeManager.IRefineryRegistry<IRefineryRecipeManager.ICoolableRecipe> getCoolableRegistry();

   IRefineryRecipeManager.IRefineryRegistry<IRefineryRecipeManager.IDistillationRecipe> getDistillationRegistry();

   interface ICoolableRecipe extends IRefineryRecipeManager.IHeatExchangerRecipe {
   }

   interface IDistillationRecipe extends IRefineryRecipeManager.IRefineryRecipe {
      long powerRequired();

      FluidStack outGas();

      FluidStack outLiquid();
   }

   interface IHeatExchangerRecipe extends IRefineryRecipeManager.IRefineryRecipe {
      @Nullable
      FluidStack out();

      int heatFrom();

      int heatTo();
   }

   interface IHeatableRecipe extends IRefineryRecipeManager.IHeatExchangerRecipe {
   }

   interface IRefineryRecipe {
      FluidStack in();
   }

   interface IRefineryRegistry<R extends IRefineryRecipeManager.IRefineryRecipe> {
      Stream<R> getRecipes(Predicate<R> var1);

      Collection<R> getAllRecipes();

      @Nullable
      R getRecipeForInput(@Nullable FluidStack var1);

      Collection<R> removeRecipes(Predicate<R> var1);

      R addRecipe(R var1);
   }
}
