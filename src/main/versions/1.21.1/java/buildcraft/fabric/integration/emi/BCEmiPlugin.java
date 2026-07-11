package buildcraft.fabric.integration.emi;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.energy.BCEnergyItems;
import buildcraft.energy.integration.jei.CombustionCoolantJei;
import buildcraft.energy.integration.jei.StirlingFuelJei;
import buildcraft.energy.recipe.CombustionFuelRecipe;
import buildcraft.energy.recipe.CoolantRecipe;
import buildcraft.energy.recipe.SolidCoolantRecipe;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.integration.jei.HeatExchangerRecipePair;
import buildcraft.fabric.integration.jei.BCJeiBootstrap;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.integration.jei.AssemblyRecipeCollector;
import buildcraft.silicon.integration.jei.AssemblyRecipeJei;
import buildcraft.silicon.integration.jei.IntegrationRecipeCollector;
import buildcraft.silicon.integration.jei.IntegrationRecipeJei;
import buildcraft.silicon.integration.jei.ProgrammingRecipeCollector;
import buildcraft.silicon.integration.jei.ProgrammingRecipeJei;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * Native EMI support (1.21.1 only — EMI does not exist for the 26.x line). Categories mirror the JEI
 * ones and reuse the same collectors, records and lang keys; only the widget layout is EMI-specific.
 */
@EmiEntrypoint
public class BCEmiPlugin implements EmiPlugin {
   @Override
   public void register(EmiRegistry registry) {
      BCJeiBootstrap.initSiliconRecipes();
      BCJeiBootstrap.initEnergyRecipes();

      registerSilicon(registry);
      registerFactory(registry);
      registerEnergy(registry);
   }

   private static void registerSilicon(EmiRegistry registry) {
      EmiRecipeCategory assembly = BcEmi.category("assembly_table", BCSiliconItems.ASSEMBLY_TABLE, "gui.jei.category.buildcraft.assembly_table");
      EmiRecipeCategory integration = BcEmi.category("integration_table", BCSiliconItems.INTEGRATION_TABLE, "gui.jei.category.buildcraft.integration_table");
      EmiRecipeCategory programming = BcEmi.category("programming_table", BCSiliconItems.PROGRAMMING_TABLE, "gui.jei.category.buildcraft.programming_table");

      registry.addCategory(assembly);
      registry.addCategory(integration);
      registry.addCategory(programming);
      registry.addWorkstation(assembly, EmiStack.of(new ItemStack(BCSiliconItems.ASSEMBLY_TABLE)));
      registry.addWorkstation(integration, EmiStack.of(new ItemStack(BCSiliconItems.INTEGRATION_TABLE)));
      registry.addWorkstation(programming, EmiStack.of(new ItemStack(BCSiliconItems.PROGRAMMING_TABLE)));

      for (AssemblyRecipeJei recipe : AssemblyRecipeCollector.collect()) {
         registry.addRecipe(new AssemblyEmiRecipe(assembly, recipe));
      }

      for (IntegrationRecipeJei recipe : IntegrationRecipeCollector.collect()) {
         registry.addRecipe(new IntegrationEmiRecipe(integration, recipe));
      }

      for (ProgrammingRecipeJei recipe : ProgrammingRecipeCollector.collect()) {
         registry.addRecipe(new ProgrammingEmiRecipe(programming, recipe));
      }
   }

   private static void registerFactory(EmiRegistry registry) {
      EmiRecipeCategory heatExchanger = BcEmi.category("heat_exchanger", BCFactoryItems.HEAT_EXCHANGE, "gui.jei.category.buildcraft.heat_exchanger");
      EmiRecipeCategory distiller = BcEmi.category("distiller", BCFactoryItems.DISTILLER, "gui.jei.category.buildcraft.distiller");

      registry.addCategory(heatExchanger);
      registry.addCategory(distiller);
      registry.addWorkstation(heatExchanger, EmiStack.of(new ItemStack(BCFactoryItems.HEAT_EXCHANGE)));
      registry.addWorkstation(distiller, EmiStack.of(new ItemStack(BCFactoryItems.DISTILLER)));

      IRefineryRecipeManager refinery = BuildcraftRecipeRegistry.refineryRecipes;
      if (refinery == null) {
         return;
      }

      for (IRefineryRecipeManager.IHeatableRecipe h : refinery.getHeatableRegistry().getAllRecipes()) {
         for (IRefineryRecipeManager.ICoolableRecipe c : refinery.getCoolableRegistry().getAllRecipes()) {
            if (c.heatFrom() > h.heatFrom()) {
               registry.addRecipe(new HeatExchangerEmiRecipe(heatExchanger, new HeatExchangerRecipePair(h, c)));
            }
         }
      }

      for (IRefineryRecipeManager.IDistillationRecipe r : refinery.getDistillationRegistry().getAllRecipes()) {
         boolean hasIn = r.in() != null && !r.in().isEmpty();
         boolean hasGas = r.outGas() != null && !r.outGas().isEmpty();
         boolean hasLiquid = r.outLiquid() != null && !r.outLiquid().isEmpty();
         if (hasIn && (hasGas || hasLiquid)) {
            registry.addRecipe(new DistillerEmiRecipe(distiller, r));
         }
      }
   }

   private static void registerEnergy(EmiRegistry registry) {
      EmiRecipeCategory combustionFuel = BcEmi.category("combustion_fuel", BCEnergyItems.ENGINE_IRON, "gui.jei.category.buildcraft.combustion_engine_fuel");
      EmiRecipeCategory combustionCoolant = BcEmi.category("combustion_coolant", BCEnergyItems.ENGINE_IRON, "gui.jei.category.buildcraft.combustion_engine_coolant");
      EmiRecipeCategory stirlingFuel = BcEmi.category("stirling_fuel", BCEnergyItems.ENGINE_STONE, "gui.jei.category.buildcraft.stirling_engine_fuel");

      registry.addCategory(combustionFuel);
      registry.addCategory(combustionCoolant);
      registry.addCategory(stirlingFuel);
      registry.addWorkstation(combustionFuel, EmiStack.of(new ItemStack(BCEnergyItems.ENGINE_IRON)));
      registry.addWorkstation(combustionCoolant, EmiStack.of(new ItemStack(BCEnergyItems.ENGINE_IRON)));
      registry.addWorkstation(stirlingFuel, EmiStack.of(new ItemStack(BCEnergyItems.ENGINE_STONE)));

      for (RecipeHolder<?> holder : registry.getRecipeManager().getRecipes()) {
         if (holder.value() instanceof CombustionFuelRecipe fuel) {
            registry.addRecipe(new CombustionFuelEmiRecipe(combustionFuel, fuel));
         } else if (holder.value() instanceof CoolantRecipe coolant) {
            registry.addRecipe(new CombustionCoolantEmiRecipe(
               combustionCoolant, new CombustionCoolantJei(ItemStack.EMPTY, new FluidStack(coolant.fluid(), 1000), coolant.degreesCoolingPerMb())
            ));
         } else if (holder.value() instanceof SolidCoolantRecipe solid) {
            registry.addRecipe(new CombustionCoolantEmiRecipe(
               combustionCoolant, new CombustionCoolantJei(new ItemStack(solid.item()), new FluidStack(solid.coolantFluid(), solid.coolantAmountPerItem()), 0.0F)
            ));
         }
      }

      for (Map.Entry<Item, Integer> entry : AbstractFurnaceBlockEntity.getFuel().entrySet()) {
         if (entry.getValue() > 0) {
            registry.addRecipe(new StirlingFuelEmiRecipe(stirlingFuel, new StirlingFuelJei(new ItemStack(entry.getKey()), entry.getValue())));
         }
      }
   }
}
