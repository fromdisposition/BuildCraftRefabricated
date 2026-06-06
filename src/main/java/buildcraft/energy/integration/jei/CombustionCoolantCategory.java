package buildcraft.energy.integration.jei;

import buildcraft.energy.BCEnergyItems;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.integration.jei.JeiFluids;
import java.util.List;
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

public class CombustionCoolantCategory extends AbstractRecipeCategory<CombustionCoolantJei> {
   private static final int WIDTH = 176;
   private static final int HEIGHT = 58;
   private static final int BUCKET = 1000;
   private static final int TEXT_COLOR = -12566464;
   private static final int IN_X = 8;
   private static final int IN_Y = 4;
   private static final int TANK_W = 16;
   private static final int TANK_H = 40;
   private static final int SOLID_Y = 4;
   private static final int OUT_X = 40;
   private static final int OUT_Y = 4;

   public CombustionCoolantCategory(IGuiHelper guiHelper) {
      super(
         EngineFuelJeiTypes.COMBUSTION_COOLANT,
         Component.translatable("gui.jei.category.buildcraft.combustion_engine_coolant"),
         guiHelper.createDrawableItemLike(BCEnergyItems.ENGINE_IRON),
         176,
         58
      );
   }

   public void setRecipe(IRecipeLayoutBuilder builder, CombustionCoolantJei recipe, IFocusGroup focuses) {
      if (recipe.isSolid()) {
         builder.addInputSlot(8, 4).addItemStacks(List.of(recipe.item()));
         FluidStack water = recipe.fluid();
         if (water != null && !water.isEmpty()) {
            IRecipeSlotBuilder waterSlot = builder.addOutputSlot(40, 4).setFluidRenderer(water.getAmount(), false, 16, 40);
            JeiFluids.addFluidStack(waterSlot, water);
         }
      } else {
         FluidStack fluid = recipe.fluid();
         if (fluid != null && !fluid.isEmpty()) {
            IRecipeSlotBuilder fluidSlot = builder.addInputSlot(8, 4).setFluidRenderer(1000L, false, 16, 40);
            JeiFluids.addFluidStack(fluidSlot, fluid, 1000L);
         }
      }
   }

   public void draw(CombustionCoolantJei recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
      Font font = Minecraft.getInstance().font;
      BCGraphics g = new BCGraphics(graphics);
      String line;
      if (recipe.isSolid()) {
         line = Component.translatable("gui.jei.category.buildcraft.combustion_engine_coolant.melts", new Object[]{recipe.fluid().getAmount()}).getString();
      } else {
         line = Component.translatable(
               "gui.jei.category.buildcraft.combustion_engine_coolant.cooling", new Object[]{String.format("%.4f", recipe.coolingPerMb())}
            )
            .getString();
      }

      g.text(font, line, 8, 48, -12566464, false);
   }
}
