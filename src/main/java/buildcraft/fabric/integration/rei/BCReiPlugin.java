/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
import buildcraft.silicon.tile.TileProgrammingTable;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class BCReiPlugin implements REIClientPlugin {
   private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
   static final CategoryIdentifier<BcReiDisplay> ASSEMBLY = CategoryIdentifier.of("buildcraftsilicon", "assembly_table");
   static final CategoryIdentifier<BcReiDisplay> INTEGRATION = CategoryIdentifier.of("buildcraftsilicon", "integration_table");
   static final CategoryIdentifier<BcReiDisplay> PROGRAMMING = CategoryIdentifier.of("buildcraftsilicon", "programming_table");
   static final CategoryIdentifier<BcReiDisplay> DISTILLER = CategoryIdentifier.of("buildcraftfactory", "distiller");
   static final CategoryIdentifier<BcReiDisplay> HEAT_EXCHANGER = CategoryIdentifier.of("buildcraftfactory", "heat_exchanger");
   static final CategoryIdentifier<BcReiDisplay> COMBUSTION_FUEL = CategoryIdentifier.of("buildcraftenergy", "combustion_fuel");
   static final CategoryIdentifier<BcReiDisplay> COMBUSTION_COOLANT = CategoryIdentifier.of("buildcraftenergy", "combustion_coolant");
   static final CategoryIdentifier<BcReiDisplay> STIRLING_FUEL = CategoryIdentifier.of("buildcraftenergy", "stirling_fuel");

   private static final String KEY_PREFIX = "gui.jei.category.buildcraft.";

   private static final Identifier ASSEMBLY_TEX = Identifier.parse("buildcraftsilicon:textures/gui/bcr/assembly_table.png");
   private static final Identifier INTEGRATION_TEX = Identifier.parse("buildcraftsilicon:textures/gui/bcr/integration_table.png");
   private static final Identifier PROGRAMMING_TEX = Identifier.parse("buildcraftsilicon:textures/gui/bcr/programming_table.png");
   private static final Identifier DISTILLER_TEX = Identifier.parse("buildcraftfactory:textures/gui/bcr/distiller.png");
   private static final Identifier HEAT_EXCHANGER_TEX = Identifier.parse("buildcraftfactory:textures/gui/bcr/heat_exchanger.png");

   @Override
   public void registerCategories(CategoryRegistry registry) {
      registry.add(new BcReiCategory<>(ASSEMBLY, BCSiliconItems.ASSEMBLY_TABLE, KEY_PREFIX + "assembly_table", 180, 106, (d, o, w) -> {
         w.add(BcReiCategory.texture(ASSEMBLY_TEX, o, 3, 13, 170, 80));
         List<EntryIngredient> in = d.inputs();
         for (int i = 0; i < in.size() && i < 12; i++) {
            if (!in.get(i).isEmpty()) {
               w.add(BcReiCategory.texSlot(in.get(i), o, 5 + (i % 3) * 18, 5 + (i / 3) * 18, false));
            }
         }
         if (!d.outputs().isEmpty() && !d.outputs().get(0).isEmpty()) {
            w.add(BcReiCategory.texSlot(d.outputs().get(0), o, 113, 5, true));
         }
         if (!d.lines.isEmpty()) {
            w.add(BcReiCategory.textRight(o, 167, 83, d.lines.get(0)));
         }
      }));
      registry.add(new BcReiCategory<>(INTEGRATION, BCSiliconItems.INTEGRATION_TABLE, KEY_PREFIX + "integration_table", 162, 100, (d, o, w) -> {
         w.add(BcReiCategory.texture(INTEGRATION_TEX, o, 4, 14, 152, 74));
         int[][] ring = {{5, 6}, {30, 6}, {55, 6}, {5, 31}, {55, 31}, {5, 56}, {30, 56}, {55, 56}};
         List<EntryIngredient> in = d.inputs();
         if (!in.isEmpty() && !in.get(0).isEmpty()) {
            w.add(BcReiCategory.texSlot(in.get(0), o, 30, 31, false));
         }
         for (int i = 1; i < in.size() && i <= ring.length; i++) {
            if (!in.get(i).isEmpty()) {
               w.add(BcReiCategory.texSlot(in.get(i), o, ring[i - 1][0], ring[i - 1][1], false));
            }
         }
         if (!d.outputs().isEmpty() && !d.outputs().get(0).isEmpty()) {
            w.add(BcReiCategory.texSlot(d.outputs().get(0), o, 129, 31, true));
         }
         if (!d.lines.isEmpty()) {
            w.add(BcReiCategory.textRight(o, 147, 77, d.lines.get(0)));
         }
      }));
      registry.add(new BcReiCategory<>(PROGRAMMING, BCSiliconItems.PROGRAMMING_TABLE, KEY_PREFIX + "programming_table", 163, 107, (d, o, w) -> {
         w.add(BcReiCategory.texture(PROGRAMMING_TEX, o, 3, 13, 153, 81));
         if (!d.inputs().isEmpty() && !d.inputs().get(0).isEmpty()) {
            w.add(BcReiCategory.texSlot(d.inputs().get(0), o, 5, 5, false));
         }
         if (!d.outputs().isEmpty() && !d.outputs().get(0).isEmpty()) {
            EntryIngredient option = d.outputs().get(0);
            if (d.recipe instanceof ProgrammingRecipeJei r) {
               int col = r.optionIndex() % TileProgrammingTable.WIDTH;
               int row = r.optionIndex() / TileProgrammingTable.WIDTH;
               w.add(BcReiCategory.texSlot(option, o, 40 + col * 18, 5 + row * 18, true));
            }
            w.add(BcReiCategory.texSlot(option, o, 5, 59, true));
         }
         if (!d.lines.isEmpty()) {
            w.add(BcReiCategory.textRight(o, 148, 84, d.lines.get(0)));
         }
      }));
      registry.add(new BcReiCategory<>(DISTILLER, BCFactoryItems.DISTILLER, KEY_PREFIX + "distiller", 180, 96, (d, o, w) -> {
         w.add(BcReiCategory.texture(DISTILLER_TEX, o, 3, 12, 170, 74));
         BcReiCategory.tank(w, d.inputs().get(0), o, 41, 19, 16, 38, false);
         List<EntryIngredient> out = d.outputs();
         if (!out.get(0).isEmpty()) {
            BcReiCategory.tank(w, out.get(0), o, 95, 6, 34, 17, true);
         }
         if (out.size() > 1 && !out.get(1).isEmpty()) {
            BcReiCategory.tank(w, out.get(1), o, 95, 50, 34, 17, true);
         }
         if (!d.lines.isEmpty()) {
            w.add(BcReiCategory.textRight(o, 170, 77, d.lines.get(0)));
         }
      }));
      registry.add(new BcReiCategory<>(HEAT_EXCHANGER, BCFactoryItems.HEAT_EXCHANGE, KEY_PREFIX + "heat_exchanger", 180, 94, (d, o, w) -> {
         w.add(BcReiCategory.texture(HEAT_EXCHANGER_TEX, o, 3, 10, 170, 84));
         List<EntryIngredient> in = d.inputs();
         List<EntryIngredient> out = d.outputs();
         if (!in.isEmpty() && !in.get(0).isEmpty()) {
            BcReiCategory.tank(w, in.get(0), o, 41, 60, 34, 17, false);
         }
         if (in.size() > 1 && !in.get(1).isEmpty()) {
            BcReiCategory.tank(w, in.get(1), o, 41, 8, 16, 38, false);
         }
         if (!out.isEmpty() && !out.get(0).isEmpty()) {
            BcReiCategory.tank(w, out.get(0), o, 95, 8, 34, 17, true);
         }
         if (out.size() > 1 && !out.get(1).isEmpty()) {
            BcReiCategory.tank(w, out.get(1), o, 113, 39, 16, 38, true);
         }
      }));
      registry.add(new BcReiCategory<>(COMBUSTION_FUEL, BCEnergyItems.ENGINE_IRON, KEY_PREFIX + "combustion_engine_fuel", 186, 76, (d, o, w) -> {
         w.add(BcReiCategory.tankBase(o, 8, 4, 16, 40));
         BcReiCategory.tank(w, d.inputs().get(0), o, 8, 4, 16, 40, false);
         if (!d.outputs().isEmpty() && !d.outputs().get(0).isEmpty()) {
            w.add(BcReiCategory.tankBase(o, 32, 4, 16, 40));
            BcReiCategory.tank(w, d.outputs().get(0), o, 32, 4, 16, 40, true);
         }
         for (int i = 0; i < d.lines.size(); i++) {
            w.add(BcReiCategory.textLeft(o, 8, 48 + i * 10, d.lines.get(i)));
         }
      }));
      registry.add(new BcReiCategory<>(COMBUSTION_COOLANT, BCEnergyItems.ENGINE_IRON, KEY_PREFIX + "combustion_engine_coolant", 186, 68, (d, o, w) -> {
         List<EntryIngredient> in = d.inputs();
         if (in.size() > 1 && !in.get(1).isEmpty()) {
            w.add(BcReiCategory.slot(in.get(0), o, 8, 4, false));
            w.add(BcReiCategory.tankBase(o, 40, 4, 16, 40));
            BcReiCategory.tank(w, in.get(1), o, 40, 4, 16, 40, true);
         } else if (!in.isEmpty() && !in.get(0).isEmpty()) {
            w.add(BcReiCategory.tankBase(o, 8, 4, 16, 40));
            BcReiCategory.tank(w, in.get(0), o, 8, 4, 16, 40, false);
         }
         for (int i = 0; i < d.lines.size(); i++) {
            w.add(BcReiCategory.textLeft(o, 8, 48 + i * 10, d.lines.get(i)));
         }
      }));
      registry.add(new BcReiCategory<>(STIRLING_FUEL, BCEnergyItems.ENGINE_STONE, KEY_PREFIX + "stirling_engine_fuel", 186, 58, (d, o, w) -> {
         w.add(BcReiCategory.slot(d.inputs().get(0), o, 8, 4, false));
         for (int i = 0; i < d.lines.size(); i++) {
            w.add(BcReiCategory.textLeft(o, 30, 5 + i * 10, d.lines.get(i)));
         }
      }));

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
      registry.registerContainerClickArea(new Rectangle(93, 36, 22, 15), GuiAdvancedCraftingTable.class, BuiltinPlugin.CRAFTING);
      registry.registerContainerClickArea(new Rectangle(90, 48, 23, 10), GuiAutoCraftItems.class, BuiltinPlugin.CRAFTING);
      registry.registerContainerClickArea(new Rectangle(86, 18, 4, 70), GuiAssemblyTable.class, ASSEMBLY);
      registry.registerContainerClickArea(new Rectangle(84, 48, 41, 10), GuiIntegrationTable.class, INTEGRATION);
      registry.registerContainerClickArea(new Rectangle(28, 22, 11, 62), GuiProgrammingTable.class, PROGRAMMING);
      registry.registerContainerClickArea(new Rectangle(61, 20, 36, 57), GuiDistiller.class, DISTILLER);
      registry.registerContainerClickArea(new Rectangle(73, 42, 30, 21), GuiHeatExchange.class, HEAT_EXCHANGER);
      registry.registerContainerClickArea(new Rectangle(81, 25, 14, 14), GuiEngineStone_BC8.class, STIRLING_FUEL);
      registry.registerContainerClickArea(new Rectangle(44, 22, 34, 52), GuiEngineIron_BC8.class, COMBUSTION_FUEL, COMBUSTION_COOLANT);

      registry.registerDraggableStackVisitor(new BcReiGhostDrag());

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
      section("silicon", () -> registerSiliconDisplays(registry));
      section("refinery", () -> registerRefineryDisplays(registry));
      section("energy", () -> registerEnergyDisplays(registry));
   }

   private static void section(String name, Runnable body) {
      try {
         body.run();
      } catch (Exception e) {
         LOGGER.error("BuildCraft REI: {} display registration failed", name, e);
      }
   }

   private static void registerSiliconDisplays(DisplayRegistry registry) {
      BCJeiBootstrap.initSiliconRecipes();

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
            List.of(powerLine("assembly_table.power", r.microJoules())), r));
      }
   }

   private static void registerRefineryDisplays(DisplayRegistry registry) {
      if (BuildcraftRecipeRegistry.refineryRecipes == null) {
         return;
      }

      for (IRefineryRecipeManager.IDistillationRecipe r : BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getAllRecipes()) {
         EntryIngredient in = BcRei.fluid(r.in());
         EntryIngredient gas = BcRei.fluid(r.outGas());
         EntryIngredient liquid = BcRei.fluid(r.outLiquid());
         if (in.isEmpty() || (gas.isEmpty() && liquid.isEmpty())) {
            continue;
         }

         registry.add(new BcReiDisplay(DISTILLER, List.of(in), List.of(gas, liquid),
            List.of(powerLine("assembly_table.power", r.powerRequired())), r));
      }

      for (IRefineryRecipeManager.IHeatableRecipe h : BuildcraftRecipeRegistry.refineryRecipes.getHeatableRegistry().getAllRecipes()) {
         for (IRefineryRecipeManager.ICoolableRecipe c : BuildcraftRecipeRegistry.refineryRecipes.getCoolableRegistry().getAllRecipes()) {
            if (c.heatFrom() <= h.heatFrom()) {
               continue;
            }

            EntryIngredient heatIn = BcRei.fluid(h.in());
            EntryIngredient coolIn = BcRei.fluid(c.in());
            if (heatIn.isEmpty() || coolIn.isEmpty()) {
               continue;
            }

            registry.add(new BcReiDisplay(HEAT_EXCHANGER,
               List.of(heatIn, coolIn),
               List.of(BcRei.fluid(h.out()), BcRei.fluid(c.out())),
               List.of(), new HeatExchangerRecipePair(h, c)));
         }
      }
   }

   private static void registerEnergyDisplays(DisplayRegistry registry) {
      BCJeiBootstrap.initEnergyRecipes();

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
         /*for (var entry : net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.getFuel().entrySet()) {
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