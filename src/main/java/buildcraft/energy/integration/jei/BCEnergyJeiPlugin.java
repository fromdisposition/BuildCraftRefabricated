package buildcraft.energy.integration.jei;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.ICoolant;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.ISolidCoolant;
import buildcraft.energy.BCEnergyItems;
import buildcraft.energy.client.gui.GuiEngineIron_BC8;
import buildcraft.energy.client.gui.GuiEngineStone_BC8;
import buildcraft.lib.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;

@JeiPlugin
public class BCEnergyJeiPlugin implements IModPlugin {
   private static final Identifier UID = Identifier.parse("buildcraftrefabricated:energy_jei_plugin");

   public Identifier getPluginUid() {
      return UID;
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
      registration.addRecipeCategories(new IRecipeCategory[]{new CombustionFuelCategory(guiHelper)});
      registration.addRecipeCategories(new IRecipeCategory[]{new CombustionCoolantCategory(guiHelper)});
      registration.addRecipeCategories(new IRecipeCategory[]{new StirlingFuelCategory(guiHelper)});
   }

   public void registerRecipes(IRecipeRegistration registration) {
      registration.addRecipes(EngineFuelJeiTypes.COMBUSTION_FUEL, collectCombustionFuels());
      registration.addRecipes(EngineFuelJeiTypes.COMBUSTION_COOLANT, collectCoolants());
      registration.addRecipes(EngineFuelJeiTypes.STIRLING_FUEL, collectStirlingFuels());
   }

   private static List<IFuel> collectCombustionFuels() {
      List<IFuel> fuels = new ArrayList<>();
      if (BuildcraftFuelRegistry.fuel == null) {
         return fuels;
      }

      for (IFuel fuel : BuildcraftFuelRegistry.fuel.getFuels()) {
         FluidStack fluid = fuel.getFluid();
         if (fluid != null && !fluid.isEmpty()) {
            fuels.add(fuel);
         }
      }

      return fuels;
   }

   private static List<CombustionCoolantJei> collectCoolants() {
      List<CombustionCoolantJei> out = new ArrayList<>();
      if (BuildcraftFuelRegistry.coolant == null) {
         return out;
      }

      for (ICoolant coolant : BuildcraftFuelRegistry.coolant.getCoolants()) {
         FluidStack rep = coolant.getRepresentativeFluid();
         if (rep != null && !rep.isEmpty()) {
            out.add(new CombustionCoolantJei(ItemStack.EMPTY, rep, coolant.getDegreesCoolingPerMB(rep, 1.0F)));
         }
      }

      for (ISolidCoolant solid : BuildcraftFuelRegistry.coolant.getSolidCoolants()) {
         ItemStack rep = solid.getRepresentativeStack();
         if (rep != null && !rep.isEmpty()) {
            FluidStack produced = solid.getFluidFromSolidCoolant(rep);
            out.add(new CombustionCoolantJei(rep, produced == null ? FluidStack.EMPTY : produced, 0.0F));
         }
      }

      return out;
   }

   private static List<StirlingFuelJei> collectStirlingFuels() {
      List<StirlingFuelJei> out = new ArrayList<>();
      Level level = Minecraft.getInstance().level;
      if (level == null) {
         return out;
      }

      FuelValues fuelValues = level.fuelValues();

      for (Item item : fuelValues.fuelItems()) {
         ItemStack stack = new ItemStack(item);
         int burnTime = fuelValues.burnDuration(stack);
         if (burnTime > 0) {
            out.add(new StirlingFuelJei(stack, burnTime));
         }
      }

      return out;
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addCraftingStation(EngineFuelJeiTypes.COMBUSTION_FUEL, new ItemLike[]{BCEnergyItems.ENGINE_IRON});
      registration.addCraftingStation(EngineFuelJeiTypes.COMBUSTION_COOLANT, new ItemLike[]{BCEnergyItems.ENGINE_IRON});
      registration.addCraftingStation(EngineFuelJeiTypes.STIRLING_FUEL, new ItemLike[]{BCEnergyItems.ENGINE_STONE});
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addRecipeClickArea(GuiEngineStone_BC8.class, 81, 25, 14, 14, new IRecipeType[]{EngineFuelJeiTypes.STIRLING_FUEL});
      registration.addRecipeClickArea(
         GuiEngineIron_BC8.class, 44, 22, 34, 52, new IRecipeType[]{EngineFuelJeiTypes.COMBUSTION_FUEL, EngineFuelJeiTypes.COMBUSTION_COOLANT}
      );
   }
}
