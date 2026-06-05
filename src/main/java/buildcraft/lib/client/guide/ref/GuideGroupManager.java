package buildcraft.lib.client.guide.ref;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.statements.IStatement;

import buildcraft.lib.client.guide.entry.FluidStackValueFilter;
import buildcraft.lib.client.guide.entry.ItemStackValueFilter;
import buildcraft.lib.client.guide.entry.PageEntryFluidStack;
import buildcraft.lib.client.guide.entry.PageEntryItemStack;
import buildcraft.lib.client.guide.entry.PageEntryStatement;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.entry.PageValueType;

import buildcraft.fabric.guide.GuideFabricSupport;

public class GuideGroupManager {
    public static final List<PageValueType<?>> knownTypes = new ArrayList<>();
    public static final Map<Identifier, GuideGroupSet> sets = new HashMap<>();

    private static final Map<Class<?>, PageValueType<?>> knownClasses = new WeakHashMap<>();
    private static final Map<Class<?>, Function<Object, PageValue<?>>> transformers = new WeakHashMap<>();

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

    private static ItemStack itemToStack(Item item) {
        try {
            return new ItemStack(item);
        } catch (NullPointerException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Components not bound")) {
                return ItemStack.EMPTY;
            }
            throw ex;
        }
    }

    private static ItemStack blockToStack(Block block) {
        try {
            return new ItemStack(block);
        } catch (NullPointerException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Components not bound")) {
                return ItemStack.EMPTY;
            }
            throw ex;
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
                return;
            }
            throw new IllegalArgumentException("You cannot register a transformer to an unregistered class!");
        }
        Function<Object, PageValue<?>> realTransform = o -> {
            F from = fromClass.cast(o);
            T to = transform.apply(from);
            return destType.wrap(to);
        };
        transformers.put(fromClass, realTransform);
    }

    public static <T> void addValidClass(Class<T> clazz, PageValueType<T> type) {
        if (clazz.isArray()) {
            throw new IllegalArgumentException("Arrays are never valid!");
        }
        knownClasses.put(clazz, type);
        knownTypes.add(type);
    }

    static boolean isValidObject(Object value) {
        if (value == null) return false;
        return isValidClass(value.getClass());
    }

    public static PageValue<?> toPageValue(Object value) {
        if (value == null) return null;
        if (value instanceof PageValue) return (PageValue<?>) value;
        PageValueType<?> entryType = getEntryType(value.getClass());
        if (entryType != null) return entryType.wrap(value);
        Function<Object, PageValue<?>> transform = getTransform(value.getClass());
        if (transform != null) return transform.apply(value);
        throw new IllegalArgumentException("Unknown " + value.getClass()
            + " - is this a programming mistake, or have you forgotten to register the class as valid?");
    }

    private static boolean isValidClass(Class<?> clazz) {
        return getEntryType(clazz) != null;
    }

    @Nullable
    private static PageValueType<?> getEntryType(Class<?> clazz) {
        if (knownClasses.containsKey(clazz)) return knownClasses.get(clazz);
        PageValueType<?> type = null;
        if (!clazz.isArray()) {
            search: {
                Class<?> superClazz = clazz.getSuperclass();
                if (superClazz != null) {
                    type = getEntryType(superClazz);
                    if (type != null) break search;
                }
                for (Class<?> cls : clazz.getInterfaces()) {
                    type = getEntryType(cls);
                    if (type != null) break search;
                }
            }
            knownClasses.put(clazz, type);
        }
        return type;
    }

    private static Function<Object, PageValue<?>> getTransform(Class<? extends Object> clazz) {
        Function<Object, PageValue<?>> func = transformers.get(clazz);
        if (func != null) return func;
        if (!clazz.isArray()) {
            search: {
                Class<?> superClazz = clazz.getSuperclass();
                if (superClazz != null) {
                    func = getTransform(superClazz);
                    if (func != null) break search;
                }
                for (Class<?> cls : clazz.getInterfaces()) {
                    func = getTransform(cls);
                    if (func != null) break search;
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

        addEntries("buildcraft", "pipe_power_providers",
            buildcraft.core.BCCoreItems.ENGINE_REDSTONE,
            buildcraft.energy.BCEnergyItems.ENGINE_STONE,
            buildcraft.energy.BCEnergyItems.ENGINE_IRON);

        addEntries("buildcraft", "full_power_providers",
            buildcraft.energy.BCEnergyItems.ENGINE_STONE,
            buildcraft.energy.BCEnergyItems.ENGINE_IRON);

        addEntries("buildcraft", "area_markers",
            buildcraft.core.BCCoreItems.MARKER_VOLUME,
            buildcraft.core.BCCoreItems.VOLUME_BOX);

        addEntry("buildcraft", "fluid_shards", buildcraft.core.BCCoreItems.FRAGILE_FLUID_CONTAINER);
        addKeys("buildcraft", "fluid_shards", buildcraft.energy.BCEnergyItems.ENGINE_IRON);

        if (GuideFabricSupport.EXTENDED_MODULES) {
            populateExtendedModuleGroups();
        }

        if (buildcraft.api.fuels.BuildcraftFuelRegistry.fuel != null) {
            for (buildcraft.api.fuels.IFuel fuel : buildcraft.api.fuels.BuildcraftFuelRegistry.fuel.getFuels()) {
                buildcraft.lib.fluids.FluidStack fs = fuel.getFluid();
                if (fs == null || fs.isEmpty()) continue;
                addEntry("buildcraft", "combustion_fuels", fs);
            }
            addKey("buildcraft", "combustion_fuels", buildcraft.energy.BCEnergyItems.ENGINE_IRON);
        }

        if (buildcraft.api.fuels.BuildcraftFuelRegistry.coolant != null) {
            for (buildcraft.api.fuels.ICoolant c : buildcraft.api.fuels.BuildcraftFuelRegistry.coolant.getCoolants()) {
                buildcraft.lib.fluids.FluidStack fs = c.getRepresentativeFluid();
                if (fs == null || fs.isEmpty()) continue;
                addEntry("buildcraft", "coolants", fs);
            }
            for (buildcraft.api.fuels.ISolidCoolant sc : buildcraft.api.fuels.BuildcraftFuelRegistry.coolant.getSolidCoolants()) {
                net.minecraft.world.item.ItemStack stack = sc.getRepresentativeStack();
                if (stack == null || stack.isEmpty()) continue;
                addEntry("buildcraft", "coolants", stack);
            }
            addKey("buildcraft", "coolants", buildcraft.energy.BCEnergyItems.ENGINE_IRON);
        }

        int totalEntries = 0;
        for (GuideGroupSet set : sets.values()) {
            totalEntries += set.entries.size() + set.sources.size();
        }
        buildcraft.api.core.BCLog.logger.info(
            "[lib.guide] Populated " + sets.size() + " guide groups with " + totalEntries
                + " total members.");
    }

    private static void populateExtendedModuleGroups() {
        addEntries("buildcraft", "pipe_power_providers",
            buildcraft.silicon.BCSiliconItems.PLUG_PULSAR.get(),
            buildcraft.transport.BCTransportItems.PLUG_POWER_ADAPTOR.get());
        addKeys("buildcraft", "pipe_power_providers",
            buildcraft.transport.BCTransportItems.PIPE_WOOD_ITEM.get(),
            buildcraft.transport.BCTransportItems.PIPE_DIAMOND_WOOD_ITEM.get(),
            buildcraft.transport.BCTransportItems.PIPE_EMZULI_ITEM.get(),
            buildcraft.transport.BCTransportItems.PIPE_WOOD_FLUID.get(),
            buildcraft.transport.BCTransportItems.PIPE_DIAMOND_WOOD_FLUID.get());

        addKeys("buildcraft", "full_power_providers",
            buildcraft.builders.BCBuildersItems.BUILDER.get(),
            buildcraft.builders.BCBuildersItems.FILLER.get(),
            buildcraft.builders.BCBuildersItems.QUARRY.get(),
            buildcraft.factory.BCFactoryItems.DISTILLER,
            buildcraft.factory.BCFactoryItems.MINING_WELL,
            buildcraft.factory.BCFactoryItems.PUMP,
            buildcraft.silicon.BCSiliconItems.LASER.get());

        addEntries("buildcraft", "laser_power_providers",
            buildcraft.silicon.BCSiliconItems.LASER.get());
        addKeys("buildcraft", "laser_power_providers",
            buildcraft.silicon.BCSiliconItems.ADVANCED_CRAFTING_TABLE.get(),
            buildcraft.silicon.BCSiliconItems.ASSEMBLY_TABLE.get());
        if (buildcraft.silicon.BCSiliconItems.INTEGRATION_TABLE != null) {
            addKeys("buildcraft", "laser_power_providers",
                buildcraft.silicon.BCSiliconItems.INTEGRATION_TABLE.get());
        }

        addKeys("buildcraft", "area_markers",
            buildcraft.builders.BCBuildersItems.QUARRY.get(),
            buildcraft.builders.BCBuildersItems.ARCHITECT.get(),
            buildcraft.builders.BCBuildersItems.FILLER.get());

        addKeys("buildcraft", "fluid_shards",
            buildcraft.factory.BCFactoryItems.TANK,
            buildcraft.factory.BCFactoryItems.PUMP,
            buildcraft.factory.BCFactoryItems.FLOOD_GATE,
            buildcraft.factory.BCFactoryItems.DISTILLER,
            buildcraft.factory.BCFactoryItems.HEAT_EXCHANGE,
            buildcraft.builders.BCBuildersItems.BUILDER.get());

        if (buildcraft.api.recipes.BuildcraftRecipeRegistry.refineryRecipes != null) {
            net.minecraft.world.item.Item distiller = buildcraft.factory.BCFactoryItems.DISTILLER;
            for (var recipe : buildcraft.api.recipes.BuildcraftRecipeRegistry.refineryRecipes
                .getDistillationRegistry().getAllRecipes()) {
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

        addEntries("buildcraft", "filler_patterns",
            (Object[]) buildcraft.builders.BCBuildersStatements.PATTERNS);
        addKey("buildcraft", "filler_patterns",
            buildcraft.builders.BCBuildersItems.FILLER.get());

        addEntries("buildcraft", "extraction_presets",
            (Object[]) buildcraft.transport.BCTransportStatements.ACTION_EXTRACTION_PRESET);
        addKey("buildcraft", "extraction_presets",
            buildcraft.transport.BCTransportItems.PIPE_EMZULI_ITEM.get());

        Object[] pipeSignals = new Object[3 * buildcraft.lib.misc.ColourUtil.COLOURS.length];
        int psIdx = 0;
        for (net.minecraft.world.item.DyeColor colour : buildcraft.lib.misc.ColourUtil.COLOURS) {
            pipeSignals[psIdx++] = buildcraft.transport.BCTransportStatements.ACTION_PIPE_SIGNAL[colour.ordinal()];
            pipeSignals[psIdx++] = buildcraft.transport.BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 0];
            pipeSignals[psIdx++] = buildcraft.transport.BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 1];
        }
        addEntries("buildcraft", "pipe_signals", pipeSignals);

        addEntries("buildcraft", "paint_pipe_colour",
            (Object[]) buildcraft.transport.BCTransportStatements.ACTION_PIPE_COLOUR);
        addKeys("buildcraft", "paint_pipe_colour",
            buildcraft.transport.BCTransportItems.PIPE_LAPIS_ITEM.get(),
            buildcraft.transport.BCTransportItems.PIPE_DAIZULI_ITEM.get());

        Object[] powerLimits = new Object[
            buildcraft.transport.BCTransportStatements.ACTION_IRON_POWER_LIMIT.length
                + buildcraft.transport.BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT.length
                + buildcraft.transport.BCTransportStatements.ACTION_IRON_RF_LIMIT.length
                + buildcraft.transport.BCTransportStatements.ACTION_DIAMOND_RF_LIMIT.length];
        int plIdx = 0;
        for (var a : buildcraft.transport.BCTransportStatements.ACTION_IRON_POWER_LIMIT)    powerLimits[plIdx++] = a;
        for (var a : buildcraft.transport.BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT) powerLimits[plIdx++] = a;
        for (var a : buildcraft.transport.BCTransportStatements.ACTION_IRON_RF_LIMIT)       powerLimits[plIdx++] = a;
        for (var a : buildcraft.transport.BCTransportStatements.ACTION_DIAMOND_RF_LIMIT)    powerLimits[plIdx++] = a;
        addEntries("buildcraft", "set_power_limit", powerLimits);

        addEntries("buildcraft", "set_power_limit_iron",
            (Object[]) buildcraft.transport.BCTransportStatements.ACTION_IRON_POWER_LIMIT);
        addKeys("buildcraft", "set_power_limit_iron",
            buildcraft.transport.BCTransportItems.PIPE_IRON_POWER.get());
        addEntries("buildcraft", "set_power_limit_diamond",
            (Object[]) buildcraft.transport.BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT);
        addKeys("buildcraft", "set_power_limit_diamond",
            buildcraft.transport.BCTransportItems.PIPE_DIAMOND_POWER.get());
        addEntries("buildcraft", "set_power_limit_iron_rf",
            (Object[]) buildcraft.transport.BCTransportStatements.ACTION_IRON_RF_LIMIT);
        addKeys("buildcraft", "set_power_limit_iron_rf",
            buildcraft.transport.BCTransportItems.PIPE_IRON_RF.get());
        addEntries("buildcraft", "set_power_limit_diamond_rf",
            (Object[]) buildcraft.transport.BCTransportStatements.ACTION_DIAMOND_RF_LIMIT);
        addKeys("buildcraft", "set_power_limit_diamond_rf",
            buildcraft.transport.BCTransportItems.PIPE_DIAMOND_RF.get());

        addEntries("buildcraft", "set_pipe_direction",
            (Object[]) buildcraft.transport.BCTransportStatements.ACTION_PIPE_DIRECTION);
        addKeys("buildcraft", "set_pipe_direction",
            buildcraft.transport.BCTransportItems.PIPE_IRON_ITEM.get(),
            buildcraft.transport.BCTransportItems.PIPE_IRON_FLUID.get(),
            buildcraft.transport.BCTransportItems.PIPE_WOOD_ITEM.get(),
            buildcraft.transport.BCTransportItems.PIPE_WOOD_FLUID.get(),
            buildcraft.transport.BCTransportItems.PIPE_DIAMOND_WOOD_ITEM.get(),
            buildcraft.transport.BCTransportItems.PIPE_DIAMOND_WOOD_FLUID.get(),
            buildcraft.transport.BCTransportItems.PIPE_DAIZULI_ITEM.get(),
            buildcraft.transport.BCTransportItems.PIPE_EMZULI_ITEM.get(),
            buildcraft.transport.BCTransportItems.PIPE_STRIPES_ITEM.get());

        if (buildcraft.api.recipes.BuildcraftRecipeRegistry.refineryRecipes != null) {
            net.minecraft.world.item.Item heatExchange = buildcraft.factory.BCFactoryItems.HEAT_EXCHANGE;
            for (var recipe : buildcraft.api.recipes.BuildcraftRecipeRegistry.refineryRecipes
                .getHeatableRegistry().getAllRecipes()) {
                if (recipe.in() != null && !recipe.in().isEmpty()) {
                    addEntry("buildcraft", "heat_exchange_inputs", recipe.in());
                }
                if (recipe.out() != null && !recipe.out().isEmpty()) {
                    addEntry("buildcraft", "heat_exchange_outputs", recipe.out());
                }
            }
            for (var recipe : buildcraft.api.recipes.BuildcraftRecipeRegistry.refineryRecipes
                .getCoolableRegistry().getAllRecipes()) {
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

    public static void appendLinkedChapters(@Nullable PageValue<?> wrapped,
        buildcraft.lib.client.guide.GuiGuide gui,
        List<buildcraft.lib.client.guide.parts.GuidePart> parts) {
        if (wrapped == null) return;

        List<buildcraft.lib.client.guide.parts.GuidePartGroup> linksToOther = new ArrayList<>();
        List<buildcraft.lib.client.guide.parts.GuidePartGroup> linksToThis = new ArrayList<>();

        for (GuideGroupSet set : sets.values()) {
            if (containsValue(set.sources, wrapped)) {
                linksToOther.add(new buildcraft.lib.client.guide.parts.GuidePartGroup(
                    gui, set, GuideGroupSet.GroupDirection.SRC_TO_ENTRY));
            } else if (containsValue(set.entries, wrapped)) {
                linksToThis.add(new buildcraft.lib.client.guide.parts.GuidePartGroup(
                    gui, set, GuideGroupSet.GroupDirection.ENTRY_TO_SRC));
            }
        }

        for (buildcraft.lib.client.guide.parts.GuidePart p : parts) {
            if (p instanceof buildcraft.lib.client.guide.parts.GuidePartGroup g) {
                linksToOther.removeIf(x -> x.group == g.group);
                linksToThis.removeIf(x -> x.group == g.group);
            }
        }

        if (!linksToOther.isEmpty()) {
            parts.add(new buildcraft.lib.client.guide.parts.GuideChapterWithin(gui,
                buildcraft.lib.misc.LocaleUtil.localize("buildcraft.guide.meta.group.linking_to")));
            for (buildcraft.lib.client.guide.parts.GuidePartGroup g : linksToOther) {
                parts.add(g);
                parts.add(new buildcraft.lib.client.guide.parts.GuidePartNewPage(gui,
                    buildcraft.lib.client.guide.loader.XmlPageLoader.RECIPE_BREAK_THRESHOLD));
            }
        }
        if (!linksToThis.isEmpty()) {
            parts.add(new buildcraft.lib.client.guide.parts.GuideChapterWithin(gui,
                buildcraft.lib.misc.LocaleUtil.localize("buildcraft.guide.meta.group.linked_from")));
            for (buildcraft.lib.client.guide.parts.GuidePartGroup g : linksToThis) {
                parts.add(g);
                parts.add(new buildcraft.lib.client.guide.parts.GuidePartNewPage(gui,
                    buildcraft.lib.client.guide.loader.XmlPageLoader.RECIPE_BREAK_THRESHOLD));
            }
        }
    }

    private static boolean containsValue(List<PageValue<?>> list, PageValue<?> wrapped) {
        if (wrapped == null) return false;
        for (PageValue<?> pv : list) {
            if (pv == wrapped) return true;
            if (pv != null && pv.matches(wrapped.value)) return true;
        }
        return false;
    }
}
