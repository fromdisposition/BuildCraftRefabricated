/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.ref;

import buildcraft.api.core.BCLog;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.ICoolant;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.ISolidCoolant;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.api.statements.IStatement;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.core.BCCoreItems;
import buildcraft.energy.BCEnergyItems;
import buildcraft.factory.BCFactoryItems;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.entry.FluidStackValueFilter;
import buildcraft.lib.client.guide.entry.ItemStackValueFilter;
import buildcraft.lib.client.guide.entry.PageEntryFluidStack;
import buildcraft.lib.client.guide.entry.PageEntryItemStack;
import buildcraft.lib.client.guide.entry.PageEntryStatement;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.entry.PageValueType;
import buildcraft.lib.client.guide.parts.GuideChapterWithin;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartGroup;
import buildcraft.lib.client.guide.parts.GuidePartNewPage;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionPowerLimit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class GuideGroupManager {
   public static final List<PageValueType<?>> knownTypes = new ArrayList<>();
   public static final Map<Identifier, GuideGroupSet> sets = new HashMap<>();
   private static final Map<Class<?>, PageValueType<?>> knownClasses = new WeakHashMap<>();
   private static final Map<Class<?>, Function<Object, PageValue<?>>> transformers = new WeakHashMap<>();

   private static ItemStack itemToStack(Item item) {
      try {
         return new ItemStack(item);
      } catch (NullPointerException ex) {
         if (ex.getMessage() != null && ex.getMessage().contains("Components not bound")) {
            return ItemStack.EMPTY;
         } else {
            throw ex;
         }
      }
   }

   private static ItemStack blockToStack(Block block) {
      try {
         return new ItemStack(block);
      } catch (NullPointerException ex) {
         if (ex.getMessage() != null && ex.getMessage().contains("Components not bound")) {
            return ItemStack.EMPTY;
         } else {
            throw ex;
         }
      }
   }

   public static <F, T> void addTransformer(Class<F> fromClass, Class<T> toClass, Function<F, T> transform) {
      if (isValidClass(fromClass)) {
         throw new IllegalArgumentException("You cannot register a transformer from an already-registered class!");
      }

      PageValueType<?> destType = getEntryType(toClass);
      if (destType == null) {
         Function<Object, PageValue<?>> destTransform = getTransform(toClass);
         if (destTransform != null) {
            Function<Object, PageValue<?>> realTransform = o -> {
               F from = fromClass.cast(o);
               T to = transform.apply(from);
               return destTransform.apply(to);
            };
            transformers.put(fromClass, realTransform);
         } else {
            throw new IllegalArgumentException("You cannot register a transformer to an unregistered class!");
         }
      } else {
         Function<Object, PageValue<?>> realTransform = o -> {
            F from = fromClass.cast(o);
            T to = transform.apply(from);
            return destType.wrap(to);
         };
         transformers.put(fromClass, realTransform);
      }
   }

   public static <T> void addValidClass(Class<T> clazz, PageValueType<T> type) {
      if (clazz.isArray()) {
         throw new IllegalArgumentException("Arrays are never valid!");
      }

      knownClasses.put(clazz, type);
      knownTypes.add(type);
   }

   static boolean isValidObject(Object value) {
      return value == null ? false : isValidClass(value.getClass());
   }

   public static PageValue<?> toPageValue(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof PageValue) {
         return (PageValue<?>)value;
      } else {
         PageValueType<?> entryType = getEntryType(value.getClass());
         if (entryType != null) {
            return entryType.wrap(value);
         } else {
            Function<Object, PageValue<?>> transform = getTransform((Class<? extends Object>)value.getClass());
            if (transform != null) {
               return transform.apply(value);
            } else {
               throw new IllegalArgumentException(
                  "Unknown " + value.getClass() + " - is this a programming mistake, or have you forgotten to register the class as valid?"
               );
            }
         }
      }
   }

   private static boolean isValidClass(Class<?> clazz) {
      return getEntryType(clazz) != null;
   }

   @Nullable
   private static PageValueType<?> getEntryType(Class<?> clazz) {
      if (knownClasses.containsKey(clazz)) {
         return knownClasses.get(clazz);
      }

      PageValueType<?> type = null;
      if (!clazz.isArray()) {
         Class<?> superClazz = clazz.getSuperclass();
         if (superClazz != null) {
            type = getEntryType(superClazz);
         }

         if (type == null) {
            for (Class<?> cls : clazz.getInterfaces()) {
               PageValueType<?> interfaceType = getEntryType(cls);
               if (interfaceType != null) {
                  type = interfaceType;
                  break;
               }
            }
         }

         knownClasses.put(clazz, type);
      }

      return type;
   }

   private static Function<Object, PageValue<?>> getTransform(Class<? extends Object> clazz) {
      Function<Object, PageValue<?>> func = transformers.get(clazz);
      if (func != null) {
         return func;
      }

      if (!clazz.isArray()) {
         Class<?> superClazz = clazz.getSuperclass();
         if (superClazz != null) {
            func = getTransform((Class<? extends Object>)superClazz);
         }

         if (func == null) {
            for (Class<?> cls : clazz.getInterfaces()) {
               Function<Object, PageValue<?>> interfaceFunc = getTransform((Class<? extends Object>)cls);
               if (interfaceFunc != null) {
                  func = interfaceFunc;
                  break;
               }
            }
         }

         transformers.put(clazz, func);
      }

      return func;
   }

   @Nullable
   public static GuideGroupSet get(Identifier group) {
      return sets.get(group);
   }

   @Nullable
   public static GuideGroupSet get(String domain, String group) {
      return get(Identifier.fromNamespaceAndPath(domain, group));
   }

   public static GuideGroupSet getOrCreate(String domain, String group) {
      return sets.computeIfAbsent(Identifier.fromNamespaceAndPath(domain, group), GuideGroupSet::new);
   }

   public static GuideGroupSet addEntry(String domain, String group, Object value) {
      return getOrCreate(domain, group).addSingle(value);
   }

   public static GuideGroupSet addEntries(String domain, String group, Object... values) {
      return getOrCreate(domain, group).addArray(values);
   }

   public static GuideGroupSet addEntries(String domain, String group, Collection<Object> values) {
      return getOrCreate(domain, group).addCollection(values);
   }

   public static GuideGroupSet addKey(String domain, String group, Object value) {
      return getOrCreate(domain, group).addKey(value);
   }

   public static GuideGroupSet addKeys(String domain, String group, Object... values) {
      return getOrCreate(domain, group).addKeyArray(values);
   }

   public static GuideGroupSet addKeys(String domain, String group, Collection<Object> values) {
      return getOrCreate(domain, group).addKeyCollection(values);
   }

   public static void populateDefaultGroups() {
      sets.clear();
      addEntries("buildcraft", "pipe_power_providers", BCCoreItems.ENGINE_REDSTONE, BCEnergyItems.ENGINE_STONE, BCEnergyItems.ENGINE_IRON);
      addEntries("buildcraft", "full_power_providers", BCEnergyItems.ENGINE_STONE, BCEnergyItems.ENGINE_IRON);
      addEntries("buildcraft", "area_markers", BCCoreItems.MARKER_VOLUME, BCCoreItems.VOLUME_BOX);
      addEntry("buildcraft", "fluid_shards", BCCoreItems.FRAGILE_FLUID_CONTAINER);
      addKeys("buildcraft", "fluid_shards", BCEnergyItems.ENGINE_IRON);
      populateExtendedModuleGroups();
      if (BuildcraftFuelRegistry.fuel != null) {
         for (IFuel fuel : BuildcraftFuelRegistry.fuel.getFuels()) {
            FluidStack fs = fuel.getFluid();
            if (fs != null && !fs.isEmpty()) {
               addEntry("buildcraft", "combustion_fuels", fs);
            }
         }

         addKey("buildcraft", "combustion_fuels", BCEnergyItems.ENGINE_IRON);
      }

      if (BuildcraftFuelRegistry.coolant != null) {
         for (ICoolant c : BuildcraftFuelRegistry.coolant.getCoolants()) {
            FluidStack fs = c.getRepresentativeFluid();
            if (fs != null && !fs.isEmpty()) {
               addEntry("buildcraft", "coolants", fs);
            }
         }

         for (ISolidCoolant sc : BuildcraftFuelRegistry.coolant.getSolidCoolants()) {
            ItemStack stack = sc.getRepresentativeStack();
            if (stack != null && !stack.isEmpty()) {
               addEntry("buildcraft", "coolants", stack);
            }
         }

         addKey("buildcraft", "coolants", BCEnergyItems.ENGINE_IRON);
      }

      int totalEntries = 0;

      for (GuideGroupSet set : sets.values()) {
         totalEntries += set.entries.size() + set.sources.size();
      }

      BCLog.logger.info("[lib.guide] Populated " + sets.size() + " guide groups with " + totalEntries + " total members.");
   }

   private static void populateExtendedModuleGroups() {
      addEntries("buildcraft", "pipe_power_providers", BCSiliconItems.PLUG_PULSAR, BCTransportItems.PLUG_POWER_ADAPTOR);
      addKeys(
         "buildcraft",
         "pipe_power_providers",
         BCTransportItems.PIPE_WOOD_ITEM,
         BCTransportItems.PIPE_DIAMOND_WOOD_ITEM,
         BCTransportItems.PIPE_EMZULI_ITEM,
         BCTransportItems.PIPE_WOOD_FLUID,
         BCTransportItems.PIPE_DIAMOND_WOOD_FLUID
      );
      addKeys(
         "buildcraft",
         "full_power_providers",
         BCBuildersItems.BUILDER,
         BCBuildersItems.FILLER,
         BCBuildersItems.QUARRY,
         BCFactoryItems.DISTILLER,
         BCFactoryItems.MINING_WELL,
         BCFactoryItems.PUMP,
         BCSiliconItems.LASER
      );
      addEntries("buildcraft", "laser_power_providers", BCSiliconItems.LASER);
      addKeys("buildcraft", "laser_power_providers", BCSiliconItems.ADVANCED_CRAFTING_TABLE, BCSiliconItems.ASSEMBLY_TABLE);
      if (BCSiliconItems.INTEGRATION_TABLE != null) {
         addKeys("buildcraft", "laser_power_providers", BCSiliconItems.INTEGRATION_TABLE);
      }

      addKeys("buildcraft", "area_markers", BCBuildersItems.QUARRY, BCBuildersItems.ARCHITECT, BCBuildersItems.FILLER);
      addKeys(
         "buildcraft",
         "fluid_shards",
         BCFactoryItems.TANK,
         BCFactoryItems.PUMP,
         BCFactoryItems.FLOOD_GATE,
         BCFactoryItems.DISTILLER,
         BCFactoryItems.HEAT_EXCHANGE,
         BCBuildersItems.BUILDER
      );
      if (BuildcraftRecipeRegistry.refineryRecipes != null) {
         Item distiller = BCFactoryItems.DISTILLER;

         for (IRefineryRecipeManager.IDistillationRecipe recipe : BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getAllRecipes()) {
            if (recipe.in() != null && !recipe.in().isEmpty()) {
               addEntry("buildcraft", "distillation_inputs", recipe.in());
            }

            if (recipe.outGas() != null && !recipe.outGas().isEmpty()) {
               addEntry("buildcraft", "distillation_outputs", recipe.outGas());
            }

            if (recipe.outLiquid() != null && !recipe.outLiquid().isEmpty()) {
               addEntry("buildcraft", "distillation_outputs", recipe.outLiquid());
            }
         }

         addKey("buildcraft", "distillation_inputs", distiller);
         addKey("buildcraft", "distillation_outputs", distiller);
      }

      addEntries("buildcraft", "filler_patterns", (Object)BCBuildersStatements.PATTERNS);
      addKey("buildcraft", "filler_patterns", BCBuildersItems.FILLER);
      addEntries("buildcraft", "extraction_presets", (Object)BCTransportStatements.ACTION_EXTRACTION_PRESET);
      addKey("buildcraft", "extraction_presets", BCTransportItems.PIPE_EMZULI_ITEM);
      Object[] pipeSignals = new Object[3 * ColourUtil.COLOURS.length];
      int psIdx = 0;

      for (DyeColor colour : ColourUtil.COLOURS) {
         pipeSignals[psIdx++] = BCTransportStatements.ACTION_PIPE_SIGNAL[colour.ordinal()];
         pipeSignals[psIdx++] = BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 0];
         pipeSignals[psIdx++] = BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 1];
      }

      addEntries("buildcraft", "pipe_signals", pipeSignals);
      addEntries("buildcraft", "paint_pipe_colour", (Object)BCTransportStatements.ACTION_PIPE_COLOUR);
      addKeys("buildcraft", "paint_pipe_colour", BCTransportItems.PIPE_LAPIS_ITEM, BCTransportItems.PIPE_DAIZULI_ITEM);
      Object[] powerLimits = new Object[BCTransportStatements.ACTION_IRON_POWER_LIMIT.length
         + BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT.length
         + BCTransportStatements.ACTION_IRON_RF_LIMIT.length
         + BCTransportStatements.ACTION_DIAMOND_RF_LIMIT.length];
      int plIdx = 0;

      for (ActionPowerLimit a : BCTransportStatements.ACTION_IRON_POWER_LIMIT) {
         powerLimits[plIdx++] = a;
      }

      for (ActionPowerLimit a : BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT) {
         powerLimits[plIdx++] = a;
      }

      for (ActionPowerLimit a : BCTransportStatements.ACTION_IRON_RF_LIMIT) {
         powerLimits[plIdx++] = a;
      }

      for (ActionPowerLimit a : BCTransportStatements.ACTION_DIAMOND_RF_LIMIT) {
         powerLimits[plIdx++] = a;
      }

      addEntries("buildcraft", "set_power_limit", powerLimits);
      addEntries("buildcraft", "set_power_limit_iron", (Object)BCTransportStatements.ACTION_IRON_POWER_LIMIT);
      addKeys("buildcraft", "set_power_limit_iron", BCTransportItems.PIPE_IRON_POWER);
      addEntries("buildcraft", "set_power_limit_diamond", (Object)BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT);
      addKeys("buildcraft", "set_power_limit_diamond", BCTransportItems.PIPE_DIAMOND_POWER);
      addEntries("buildcraft", "set_power_limit_iron_rf", (Object)BCTransportStatements.ACTION_IRON_RF_LIMIT);
      addKeys("buildcraft", "set_power_limit_iron_rf", BCTransportItems.PIPE_IRON_RF);
      addEntries("buildcraft", "set_power_limit_diamond_rf", (Object)BCTransportStatements.ACTION_DIAMOND_RF_LIMIT);
      addKeys("buildcraft", "set_power_limit_diamond_rf", BCTransportItems.PIPE_DIAMOND_RF);
      addEntries("buildcraft", "set_pipe_direction", (Object)BCTransportStatements.ACTION_PIPE_DIRECTION);
      addKeys(
         "buildcraft",
         "set_pipe_direction",
         BCTransportItems.PIPE_IRON_ITEM,
         BCTransportItems.PIPE_IRON_FLUID,
         BCTransportItems.PIPE_WOOD_ITEM,
         BCTransportItems.PIPE_WOOD_FLUID,
         BCTransportItems.PIPE_DIAMOND_WOOD_ITEM,
         BCTransportItems.PIPE_DIAMOND_WOOD_FLUID,
         BCTransportItems.PIPE_DAIZULI_ITEM,
         BCTransportItems.PIPE_EMZULI_ITEM,
         BCTransportItems.PIPE_STRIPES_ITEM
      );
      if (BuildcraftRecipeRegistry.refineryRecipes != null) {
         Item heatExchange = BCFactoryItems.HEAT_EXCHANGE;

         for (IRefineryRecipeManager.IHeatableRecipe recipe : BuildcraftRecipeRegistry.refineryRecipes.getHeatableRegistry().getAllRecipes()) {
            if (recipe.in() != null && !recipe.in().isEmpty()) {
               addEntry("buildcraft", "heat_exchange_inputs", recipe.in());
            }

            if (recipe.out() != null && !recipe.out().isEmpty()) {
               addEntry("buildcraft", "heat_exchange_outputs", recipe.out());
            }
         }

         for (IRefineryRecipeManager.ICoolableRecipe recipe : BuildcraftRecipeRegistry.refineryRecipes.getCoolableRegistry().getAllRecipes()) {
            if (recipe.in() != null && !recipe.in().isEmpty()) {
               addEntry("buildcraft", "heat_exchange_inputs", recipe.in());
            }

            if (recipe.out() != null && !recipe.out().isEmpty()) {
               addEntry("buildcraft", "heat_exchange_outputs", recipe.out());
            }
         }

         addKey("buildcraft", "heat_exchange_inputs", heatExchange);
         addKey("buildcraft", "heat_exchange_outputs", heatExchange);
      }
   }

   public static void appendLinkedChapters(@Nullable PageValue<?> wrapped, GuiGuide gui, List<GuidePart> parts) {
      if (wrapped != null) {
         List<GuidePartGroup> linksToOther = new ArrayList<>();
         List<GuidePartGroup> linksToThis = new ArrayList<>();

         for (GuideGroupSet set : sets.values()) {
            if (containsValue(set.sources, wrapped)) {
               linksToOther.add(new GuidePartGroup(gui, set, GuideGroupSet.GroupDirection.SRC_TO_ENTRY));
            } else if (containsValue(set.entries, wrapped)) {
               linksToThis.add(new GuidePartGroup(gui, set, GuideGroupSet.GroupDirection.ENTRY_TO_SRC));
            }
         }

         for (GuidePart p : parts) {
            if (p instanceof GuidePartGroup g) {
               linksToOther.removeIf(x -> x.group == g.group);
               linksToThis.removeIf(x -> x.group == g.group);
            }
         }

         if (!linksToOther.isEmpty()) {
            parts.add(new GuideChapterWithin(gui, LocaleUtil.localize("buildcraft.guide.meta.group.linking_to")));

            for (GuidePartGroup g : linksToOther) {
               parts.add(g);
               parts.add(new GuidePartNewPage(gui, 30));
            }
         }

         if (!linksToThis.isEmpty()) {
            parts.add(new GuideChapterWithin(gui, LocaleUtil.localize("buildcraft.guide.meta.group.linked_from")));

            for (GuidePartGroup g : linksToThis) {
               parts.add(g);
               parts.add(new GuidePartNewPage(gui, 30));
            }
         }
      }
   }

   private static boolean containsValue(List<PageValue<?>> list, PageValue<?> wrapped) {
      if (wrapped == null) {
         return false;
      }

      for (PageValue<?> pv : list) {
         if (pv == wrapped) {
            return true;
         }

         if (pv != null && pv.matches(wrapped.value)) {
            return true;
         }
      }

      return false;
   }

   static {
      addValidClass(ItemStackValueFilter.class, PageEntryItemStack.INSTANCE);
      addValidClass(FluidStackValueFilter.class, PageEntryFluidStack.INSTANCE);
      addValidClass(IStatement.class, PageEntryStatement.INSTANCE);
      addTransformer(ItemStack.class, ItemStackValueFilter.class, ItemStackValueFilter::new);
      addTransformer(Item.class, ItemStack.class, GuideGroupManager::itemToStack);
      addTransformer(Block.class, ItemStack.class, GuideGroupManager::blockToStack);
      addTransformer(FluidStack.class, FluidStackValueFilter.class, FluidStackValueFilter::new);
      addTransformer(Fluid.class, FluidStack.class, fluid -> new FluidStack(fluid, 1));
   }
}
