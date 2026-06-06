package buildcraft.factory.integration.jei;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiDistiller;
import buildcraft.factory.gui.GuiHeatExchange;
import buildcraft.lib.integration.jei.BCGhostIngredientHandler;
import buildcraft.lib.integration.jei.BlueprintTransferHandler;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
public class BCFactoryJeiPlugin implements IModPlugin {
   private static final Identifier UID = Identifier.parse("buildcraftrefabricated:factory_jei_plugin");

   public Identifier getPluginUid() {
      return UID;
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
      registration.addRecipeCategories(new IRecipeCategory[]{new HeatExchangerCategory(guiHelper)});
      registration.addRecipeCategories(new IRecipeCategory[]{new DistillerCategory(guiHelper)});
   }

   public void registerRecipes(IRecipeRegistration registration) {
      registration.addRecipes(HeatExchangerRecipeTypes.PAIR, enumerateHeatExchangerPairs());
      registration.addRecipes(DistillerRecipeTypes.DISTILLER, enumerateDistillationRecipes());
   }

   private static List<HeatExchangerRecipePair> enumerateHeatExchangerPairs() {
      List<HeatExchangerRecipePair> pairs = new ArrayList<>();

      for (IRefineryRecipeManager.IHeatableRecipe h : BuildcraftRecipeRegistry.refineryRecipes.getHeatableRegistry().getAllRecipes()) {
         for (IRefineryRecipeManager.ICoolableRecipe c : BuildcraftRecipeRegistry.refineryRecipes.getCoolableRegistry().getAllRecipes()) {
            if (c.heatFrom() > h.heatFrom()) {
               pairs.add(new HeatExchangerRecipePair(h, c));
            }
         }
      }

      return pairs;
   }

   private static List<IRefineryRecipeManager.IDistillationRecipe> enumerateDistillationRecipes() {
      List<IRefineryRecipeManager.IDistillationRecipe> recipes = new ArrayList<>();

      for (IRefineryRecipeManager.IDistillationRecipe r : BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getAllRecipes()) {
         if (r.in() != null && !r.in().isEmpty()) {
            boolean hasGas = r.outGas() != null && !r.outGas().isEmpty();
            boolean hasLiquid = r.outLiquid() != null && !r.outLiquid().isEmpty();
            if (hasGas || hasLiquid) {
               recipes.add(r);
            }
         }
      }

      return recipes;
   }

   public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
      registration.addRecipeTransferHandler(
         new BlueprintTransferHandler(ContainerAutoCraftItems.class, BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS), RecipeTypes.CRAFTING
      );
      registration.addRecipeTransferHandler(new DistillerTransferHandler(registration.getTransferHelper()), DistillerRecipeTypes.DISTILLER);
      registration.addRecipeTransferHandler(new HeatExchangerTransferHandler(registration.getTransferHelper()), HeatExchangerRecipeTypes.PAIR);
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addRecipeClickArea(GuiAutoCraftItems.class, 90, 47, 23, 10, new IRecipeType[]{RecipeTypes.CRAFTING});
      registration.addGhostIngredientHandler(GuiAutoCraftItems.class, new BCGhostIngredientHandler());
      registration.addRecipeClickArea(GuiDistiller.class, 61, 12, 36, 57, new IRecipeType[]{DistillerRecipeTypes.DISTILLER});
      registration.addRecipeClickArea(GuiHeatExchange.class, 73, 36, 30, 21, new IRecipeType[]{HeatExchangerRecipeTypes.PAIR});
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addCraftingStation(RecipeTypes.CRAFTING, new ItemLike[]{BCFactoryItems.AUTOWORKBENCH_ITEM});
      registration.addCraftingStation(HeatExchangerRecipeTypes.PAIR, new ItemLike[]{BCFactoryItems.HEAT_EXCHANGE});
      registration.addCraftingStation(DistillerRecipeTypes.DISTILLER, new ItemLike[]{BCFactoryItems.DISTILLER});
   }
}
