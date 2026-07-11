package buildcraft.fabric.integration.rei;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.energy.BCEnergyItems;
import buildcraft.energy.recipe.CombustionFuelRecipe;
import buildcraft.energy.recipe.CoolantRecipe;
import buildcraft.energy.recipe.SolidCoolantRecipe;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiDistiller;
import buildcraft.factory.gui.GuiHeatExchange;
import buildcraft.factory.integration.jei.HeatExchangerRecipePair;
import buildcraft.energy.client.gui.GuiEngineIron_BC8;
import buildcraft.energy.client.gui.GuiEngineStone_BC8;
import buildcraft.fabric.integration.jei.BCJeiBootstrap;
import buildcraft.lib.fabric.transfer.fluid.FluidStorageSnapshot;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.elem.GuiElementFluidTank;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.integration.jei.AssemblyRecipeCollector;
import buildcraft.silicon.integration.jei.AssemblyRecipeJei;
import buildcraft.silicon.integration.jei.IntegrationRecipeCollector;
import buildcraft.silicon.integration.jei.IntegrationRecipeJei;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.gui.GuiIntegrationTable;
import buildcraft.silicon.gui.GuiProgrammingTable;
import buildcraft.silicon.integration.jei.ProgrammingRecipeCollector;
import buildcraft.silicon.integration.jei.ProgrammingRecipeJei;
import dev.architectury.event.CompoundEventResult;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Native REI integration mirroring the JEI plugin: categories/displays reuse its collectors and lang keys,
 * plus screen click areas, ledger/statement exclusion zones, hovered-tank fluid lookup and recipe transfer.
 */
public final class BCReiPlugin implements REIClientPlugin {
   static final CategoryIdentifier<BcReiDisplay> ASSEMBLY = CategoryIdentifier.of("buildcraftsilicon", "assembly_table");
   static final CategoryIdentifier<BcReiDisplay> INTEGRATION = CategoryIdentifier.of("buildcraftsilicon", "integration_table");
   static final CategoryIdentifier<BcReiDisplay> PROGRAMMING = CategoryIdentifier.of("buildcraftsilicon", "programming_table");
   static final CategoryIdentifier<BcReiDisplay> DISTILLER = CategoryIdentifier.of("buildcraftfactory", "distiller");
   static final CategoryIdentifier<BcReiDisplay> HEAT_EXCHANGER = CategoryIdentifier.of("buildcraftfactory", "heat_exchanger");
   static final CategoryIdentifier<BcReiDisplay> COMBUSTION_FUEL = CategoryIdentifier.of("buildcraftenergy", "combustion_fuel");
   static final CategoryIdentifier<BcReiDisplay> COMBUSTION_COOLANT = CategoryIdentifier.of("buildcraftenergy", "combustion_coolant");
   static final CategoryIdentifier<BcReiDisplay> STIRLING_FUEL = CategoryIdentifier.of("buildcraftenergy", "stirling_fuel");

   private static final String KEY_PREFIX = "gui.jei.category.buildcraft.";

   @Override
   public void registerCategories(CategoryRegistry registry) {
      registry.add(new BcReiCategory<>(ASSEMBLY, BCSiliconItems.ASSEMBLY_TABLE, KEY_PREFIX + "assembly_table", 90, (d, o, w) -> {
         List<EntryIngredient> in = d.getInputEntries();
         for (int i = 0; i < in.size(); i++) {
            w.add(BcReiCategory.slot(in.get(i), o, (i % 3) * 18, (i / 3) * 18, false));
         }
         int rows = Math.max(1, (in.size() + 2) / 3);
         int midY = (rows * 18 - 18) / 2;
         w.add(BcReiCategory.arrow(o, 62, midY));
         w.add(BcReiCategory.slot(d.getOutputEntries().get(0), o, 92, midY, true));
      }));
      registry.add(new BcReiCategory<>(INTEGRATION, BCSiliconItems.INTEGRATION_TABLE, KEY_PREFIX + "integration_table", 92, (d, o, w) -> {
         List<EntryIngredient> in = d.getInputEntries();
         // input 0 = centre, the rest ring a 3x3 around it
         int[][] ring = {{0, 0}, {18, 0}, {36, 0}, {0, 18}, {36, 18}, {0, 36}, {18, 36}, {36, 36}};
         w.add(BcReiCategory.slot(in.get(0), o, 18, 18, false));
         for (int i = 1; i < in.size() && i <= ring.length; i++) {
            w.add(BcReiCategory.slot(in.get(i), o, ring[i - 1][0], ring[i - 1][1], false));
         }
         w.add(BcReiCategory.arrow(o, 62, 18));
         w.add(BcReiCategory.slot(d.getOutputEntries().get(0), o, 92, 18, true));
      }));
      registry.add(new BcReiCategory<>(PROGRAMMING, BCSiliconItems.PROGRAMMING_TABLE, KEY_PREFIX + "programming_table", 64, (d, o, w) -> {
         w.add(BcReiCategory.slot(d.getInputEntries().get(0), o, 0, 0, false));
         w.add(BcReiCategory.arrow(o, 26, 0));
         w.add(BcReiCategory.slot(d.getOutputEntries().get(0), o, 56, 0, true));
      }));
      registry.add(new BcReiCategory<>(DISTILLER, BCFactoryItems.DISTILLER, KEY_PREFIX + "distiller", 78, (d, o, w) -> {
         w.add(BcReiCategory.slot(d.getInputEntries().get(0), o, 0, 9, false));
         w.add(BcReiCategory.arrow(o, 26, 9));
         List<EntryIngredient> out = d.getOutputEntries();
         w.add(BcReiCategory.slot(out.get(0), o, 60, 0, true));
         if (out.size() > 1) {
            w.add(BcReiCategory.slot(out.get(1), o, 60, 18, true));
         }
      }));
      registry.add(new BcReiCategory<>(HEAT_EXCHANGER, BCFactoryItems.HEAT_EXCHANGE, KEY_PREFIX + "heat_exchanger", 64, (d, o, w) -> {
         List<EntryIngredient> in = d.getInputEntries();
         List<EntryIngredient> out = d.getOutputEntries();
         w.add(BcReiCategory.slot(in.get(0), o, 0, 0, false));
         w.add(BcReiCategory.arrow(o, 26, 0));
         w.add(BcReiCategory.slot(out.get(0), o, 60, 0, true));
         w.add(BcReiCategory.slot(in.get(1), o, 0, 18, false));
         w.add(BcReiCategory.arrow(o, 26, 18));
         w.add(BcReiCategory.slot(out.get(1), o, 60, 18, true));
      }));
      registry.add(new BcReiCategory<>(COMBUSTION_FUEL, BCEnergyItems.ENGINE_IRON, KEY_PREFIX + "combustion_engine_fuel", 70, (d, o, w) -> {
         w.add(BcReiCategory.slot(d.getInputEntries().get(0), o, 0, 0, false));
         if (!d.getOutputEntries().isEmpty()) {
            w.add(BcReiCategory.slot(d.getOutputEntries().get(0), o, 60, 0, true));
         }
      }));
      registry.add(new BcReiCategory<>(COMBUSTION_COOLANT, BCEnergyItems.ENGINE_IRON, KEY_PREFIX + "combustion_engine_coolant", 60, (d, o, w) -> {
         List<EntryIngredient> in = d.getInputEntries();
         for (int i = 0; i < in.size(); i++) {
            w.add(BcReiCategory.slot(in.get(i), o, i * 18, 0, false));
         }
      }));
      registry.add(new BcReiCategory<>(STIRLING_FUEL, BCEnergyItems.ENGINE_STONE, KEY_PREFIX + "stirling_engine_fuel", 64, (d, o, w) ->
         w.add(BcReiCategory.slot(d.getInputEntries().get(0), o, 0, 0, false))
      ));

      registry.addWorkstations(ASSEMBLY, EntryStacks.of(new ItemStack(BCSiliconItems.ASSEMBLY_TABLE)));
      registry.addWorkstations(INTEGRATION, EntryStacks.of(new ItemStack(BCSiliconItems.INTEGRATION_TABLE)));
      registry.addWorkstations(PROGRAMMING, EntryStacks.of(new ItemStack(BCSiliconItems.PROGRAMMING_TABLE)));
      registry.addWorkstations(DISTILLER, EntryStacks.of(new ItemStack(BCFactoryItems.DISTILLER)));
      registry.addWorkstations(HEAT_EXCHANGER, EntryStacks.of(new ItemStack(BCFactoryItems.HEAT_EXCHANGE)));
      registry.addWorkstations(COMBUSTION_FUEL, EntryStacks.of(new ItemStack(BCEnergyItems.ENGINE_IRON)));
      registry.addWorkstations(COMBUSTION_COOLANT, EntryStacks.of(new ItemStack(BCEnergyItems.ENGINE_IRON)));
      registry.addWorkstations(STIRLING_FUEL, EntryStacks.of(new ItemStack(BCEnergyItems.ENGINE_STONE)));
      registry.addWorkstations(BuiltinPlugin.CRAFTING,
         EntryStacks.of(new ItemStack(BCFactoryItems.AUTOWORKBENCH_ITEM)),
         EntryStacks.of(new ItemStack(BCSiliconItems.ADVANCED_CRAFTING_TABLE)));
   }

   @Override
   public void registerScreens(ScreenRegistry registry) {
      // Same gui-relative rects as the JEI click areas.
      registry.registerContainerClickArea(new Rectangle(93, 36, 22, 15), GuiAdvancedCraftingTable.class, BuiltinPlugin.CRAFTING);
      registry.registerContainerClickArea(new Rectangle(90, 48, 23, 10), GuiAutoCraftItems.class, BuiltinPlugin.CRAFTING);
      registry.registerContainerClickArea(new Rectangle(86, 18, 4, 70), GuiAssemblyTable.class, ASSEMBLY);
      registry.registerContainerClickArea(new Rectangle(84, 48, 41, 10), GuiIntegrationTable.class, INTEGRATION);
      registry.registerContainerClickArea(new Rectangle(28, 22, 11, 62), GuiProgrammingTable.class, PROGRAMMING);
      registry.registerContainerClickArea(new Rectangle(61, 20, 36, 57), GuiDistiller.class, DISTILLER);
      registry.registerContainerClickArea(new Rectangle(73, 42, 30, 21), GuiHeatExchange.class, HEAT_EXCHANGER);
      registry.registerContainerClickArea(new Rectangle(81, 25, 14, 14), GuiEngineStone_BC8.class, STIRLING_FUEL);
      registry.registerContainerClickArea(new Rectangle(44, 22, 34, 52), GuiEngineIron_BC8.class, COMBUSTION_FUEL, COMBUSTION_COOLANT);

      // Hovered fluid tanks become focusable entries (R/U lookups), like JEI's clickable ingredients.
      registry.registerFocusedStack((screen, mouse) -> {
         if (screen instanceof BcScreen<?> bcScreen) {
            for (IGuiElement element : bcScreen.mainGui.shownElements) {
               if (element instanceof GuiElementFluidTank tankElem
                  && mouse.x >= tankElem.getX() && mouse.y >= tankElem.getY()
                  && mouse.x < tankElem.getX() + tankElem.getWidth() && mouse.y < tankElem.getY() + tankElem.getHeight()) {
                  FluidStorageSnapshot snapshot = FluidStorageSnapshot.of(tankElem.getTankStorage());
                  if (!snapshot.isEmpty()) {
                     return CompoundEventResult.interruptTrue(BcRei.fluid(snapshot.fluid()).get(0));
                  }
               }
            }
         }
         return CompoundEventResult.pass();
      });
   }

   @Override
   @SuppressWarnings({"unchecked", "rawtypes"})
   public void registerExclusionZones(ExclusionZones zones) {
      // Ledgers on every BC screen plus the gate statement-source panels push the REI overlay aside.
      zones.register(BcScreen.class, (BcScreen screen) -> {
         List<Rectangle> areas = new ArrayList<>();
         for (Object obj : screen.mainGui.shownElements) {
            IGuiElement element = (IGuiElement) obj;
            if (element instanceof Ledger_Neptune || element instanceof GuiElementStatementSource) {
               areas.add(new Rectangle((int)element.getX(), (int)element.getY(),
                  (int)Math.ceil(element.getWidth()), (int)Math.ceil(element.getHeight())));
            }
         }
         return areas;
      });
   }

   @Override
   public void registerTransferHandlers(TransferHandlerRegistry registry) {
      BcReiTransfer.registerAll(registry);
   }

   @Override
   public void registerDisplays(DisplayRegistry registry) {
      BCJeiBootstrap.initSiliconRecipes();
      BCJeiBootstrap.initEnergyRecipes();

      for (AssemblyRecipeJei r : AssemblyRecipeCollector.collect()) {
         List<EntryIngredient> inputs = new ArrayList<>(r.inputSlots().size());
         for (List<ItemStack> slot : r.inputSlots()) {
            inputs.add(BcRei.itemAlternatives(slot));
         }
         registry.add(new BcReiDisplay(ASSEMBLY, inputs, List.of(BcRei.itemAlternatives(r.outputs())),
            List.of(powerLine("assembly_table.power", r.microJoules())), r));
      }

      for (IntegrationRecipeJei r : IntegrationRecipeCollector.collect()) {
         List<EntryIngredient> inputs = new ArrayList<>();
         inputs.add(BcRei.item(r.center()));
         for (ItemStack stack : r.ring()) {
            inputs.add(BcRei.item(stack));
         }
         registry.add(new BcReiDisplay(INTEGRATION, inputs, List.of(BcRei.item(r.output())),
            List.of(powerLine("assembly_table.power", r.microJoules()))));
      }

      for (ProgrammingRecipeJei r : ProgrammingRecipeCollector.collect()) {
         registry.add(new BcReiDisplay(PROGRAMMING, List.of(BcRei.item(r.input())), List.of(BcRei.item(r.option())),
            List.of(powerLine("assembly_table.power", r.microJoules()))));
      }

      if (BuildcraftRecipeRegistry.refineryRecipes != null) {
         for (IRefineryRecipeManager.IDistillationRecipe r : BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getAllRecipes()) {
            if (r.in() == null || r.in().isEmpty()) {
               continue;
            }
            List<EntryIngredient> outputs = new ArrayList<>(2);
            if (r.outGas() != null && !r.outGas().isEmpty()) {
               outputs.add(BcRei.fluid(r.outGas()));
            }
            if (r.outLiquid() != null && !r.outLiquid().isEmpty()) {
               outputs.add(BcRei.fluid(r.outLiquid()));
            }
            if (outputs.isEmpty()) {
               continue;
            }
            registry.add(new BcReiDisplay(DISTILLER, List.of(BcRei.fluid(r.in())), outputs,
               List.of(powerLine("assembly_table.power", r.powerRequired())), r));
         }

         for (IRefineryRecipeManager.IHeatableRecipe h : BuildcraftRecipeRegistry.refineryRecipes.getHeatableRegistry().getAllRecipes()) {
            for (IRefineryRecipeManager.ICoolableRecipe c : BuildcraftRecipeRegistry.refineryRecipes.getCoolableRegistry().getAllRecipes()) {
               if (c.heatFrom() > h.heatFrom()) {
                  registry.add(new BcReiDisplay(HEAT_EXCHANGER,
                     List.of(BcRei.fluid(h.in()), BcRei.fluid(c.in())),
                     List.of(BcRei.fluid(h.out()), BcRei.fluid(c.out())),
                     List.of(), new HeatExchangerRecipePair(h, c)));
               }
            }
         }
      }

      for (RecipeHolder<?> holder : clientRecipes()) {
         if (holder.value() instanceof CombustionFuelRecipe r) {
            List<EntryIngredient> outputs = r.residueFluid() != null
               ? List.of(BcRei.fluid(r.residueFluid(), r.residueAmountPer1000Mb()))
               : List.of();
            registry.add(new BcReiDisplay(COMBUSTION_FUEL, List.of(BcRei.fluid(r.fluid(), 1000)), outputs, List.of(
               Component.literal(LocaleUtil.localizeMjFlow(r.powerPerCycle())),
               Component.literal(LocaleUtil.localize(KEY_PREFIX + "combustion_engine_fuel.burn", r.totalBurningTime()))
            )));
         } else if (holder.value() instanceof CoolantRecipe r) {
            registry.add(new BcReiDisplay(COMBUSTION_COOLANT, List.of(BcRei.fluid(r.fluid(), 1000)), List.of(), List.of(
               Component.literal(LocaleUtil.localize(KEY_PREFIX + "combustion_engine_coolant.cooling", String.format("%.4f", r.degreesCoolingPerMb())))
            )));
         } else if (holder.value() instanceof SolidCoolantRecipe r) {
            registry.add(new BcReiDisplay(COMBUSTION_COOLANT,
               List.of(BcRei.item(new ItemStack(r.item())), BcRei.fluid(r.coolantFluid(), r.coolantAmountPerItem())), List.of(), List.of(
                  Component.literal(LocaleUtil.localize(KEY_PREFIX + "combustion_engine_coolant.melts", r.coolantAmountPerItem()))
               )));
         }
      }

      var level = Minecraft.getInstance().level;
      if (level != null) {
         //? if >= 1.21.10 {
         net.minecraft.world.level.block.entity.FuelValues fuelValues = level.fuelValues();
         for (Item item : fuelValues.fuelItems()) {
            addStirlingDisplay(registry, new ItemStack(item), fuelValues.burnDuration(new ItemStack(item)));
         }
         //?} else {
         /*// 1.21.1 has no FuelValues registry; the fuel->burn-time map lives on AbstractFurnaceBlockEntity.
         for (var entry : net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.getFuel().entrySet()) {
            addStirlingDisplay(registry, new ItemStack(entry.getKey()), entry.getValue());
         }
         *///?}
      }
   }

   private static void addStirlingDisplay(DisplayRegistry registry, ItemStack stack, int burnTime) {
      if (burnTime <= 0) {
         return;
      }
      registry.add(new BcReiDisplay(STIRLING_FUEL, List.of(BcRei.item(stack)), List.of(), List.of(
         Component.literal(LocaleUtil.localize(KEY_PREFIX + "stirling_engine_fuel.rate", LocaleUtil.localizeMjFlow(buildcraft.api.mj.MjAPI.MJ))),
         Component.literal(LocaleUtil.localize(KEY_PREFIX + "stirling_engine_fuel.burn", burnTime))
      )));
   }

   private static Component powerLine(String subKey, long microJoules) {
      return Component.literal(LocaleUtil.localize(KEY_PREFIX + subKey, LocaleUtil.localizeMj(microJoules)));
   }

   private static Iterable<RecipeHolder<?>> clientRecipes() {
      var server = Minecraft.getInstance().getSingleplayerServer();
      return server != null ? server.getRecipeManager().getRecipes() : List.of();
   }
}
