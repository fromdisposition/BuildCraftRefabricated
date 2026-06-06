package buildcraft.energy.integration.jei;

import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager;
import buildcraft.energy.BCEnergyItems;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.integration.jei.JeiFluids;
import buildcraft.lib.misc.LocaleUtil;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class CombustionFuelCategory extends AbstractRecipeCategory<IFuel> {
   private static final int WIDTH = 176;
   private static final int HEIGHT = 66;
   private static final int BUCKET = 1000;
   private static final int TEXT_COLOR = -12566464;
   private static final int IN_X = 8;
   private static final int IN_Y = 4;
   private static final int TANK_W = 16;
   private static final int TANK_H = 40;
   private static final int RESIDUE_X = 32;
   private static final int RESIDUE_Y = 4;

   public CombustionFuelCategory(IGuiHelper guiHelper) {
      super(
         EngineFuelJeiTypes.COMBUSTION_FUEL,
         Component.translatable("gui.jei.category.buildcraft.combustion_engine_fuel"),
         guiHelper.createDrawableItemLike(BCEnergyItems.ENGINE_IRON),
         176,
         66
      );
   }

   public void setRecipe(IRecipeLayoutBuilder builder, IFuel recipe, IFocusGroup focuses) {
      FluidStack fuel = recipe.getFluid();
      if (fuel != null && !fuel.isEmpty()) {
         IRecipeSlotBuilder fuelSlot = builder.addInputSlot(8, 4).setFluidRenderer(1000L, false, 16, 40);
         JeiFluids.addFluidStack(fuelSlot, fuel, 1000L);
      }

      if (recipe instanceof IFuelManager.IDirtyFuel dirty) {
         FluidStack residue = dirty.getResidue();
         if (residue != null && !residue.isEmpty()) {
            IRecipeSlotBuilder residueSlot = builder.addOutputSlot(32, 4).setFluidRenderer(residue.getAmount(), false, 16, 40);
            JeiFluids.addFluidStack(residueSlot, residue);
         }
      }
   }

   public void draw(IFuel recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      Font font = Minecraft.getInstance().font;
      BCGraphics g = new BCGraphics(graphics);
      g.text(font, LocaleUtil.localizeMjFlow(recipe.getPowerPerCycle()), 8, 48, -12566464, false);
      String burn = Component.translatable("gui.jei.category.buildcraft.combustion_engine_fuel.burn", new Object[]{recipe.getTotalBurningTime()}).getString();
      g.text(font, burn, 8, 58, -12566464, false);
   }
}
