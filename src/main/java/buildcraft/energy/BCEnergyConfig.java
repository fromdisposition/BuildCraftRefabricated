package buildcraft.energy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;

import buildcraft.core.BCCoreConfig;

public class BCEnergyConfig {

    public static final BCCoreConfig.BooleanValue oilIsSticky = new BCCoreConfig.BooleanValue(false);

    public static final BCCoreConfig.BooleanValue enableOilBurn = new BCCoreConfig.BooleanValue(true);
    public static final BCCoreConfig.BooleanValue useRfNaming = new BCCoreConfig.BooleanValue(false);
    public static final BCCoreConfig.BooleanValue useFullUnitNames = new BCCoreConfig.BooleanValue(true);

    public static final BCCoreConfig.BooleanValue enableOilGeneration = new BCCoreConfig.BooleanValue(true);

    public static final BCCoreConfig.BooleanValue enableNetherOilGeneration = new BCCoreConfig.BooleanValue(true);

    public static final BCCoreConfig.DoubleValue netherOilGenRateMultiplier = new BCCoreConfig.DoubleValue(4.0);
    public static final BCCoreConfig.DoubleValue oilWellGenerationRate = new BCCoreConfig.DoubleValue(1.0);

    public static final BCCoreConfig.BooleanValue enableOilSpouts = new BCCoreConfig.BooleanValue(true);
    public static final BCCoreConfig.IntValue finiteSpoutMinHeight = new BCCoreConfig.IntValue(7);
    public static final BCCoreConfig.IntValue finiteSpoutMaxHeight = new BCCoreConfig.IntValue(10);
    public static final BCCoreConfig.IntValue largeSpoutMinHeight = new BCCoreConfig.IntValue(13);
    public static final BCCoreConfig.IntValue largeSpoutMaxHeight = new BCCoreConfig.IntValue(20);

    public static final BCCoreConfig.DoubleValue mediumOilGenProb = new BCCoreConfig.DoubleValue(0.1 / 100);
    public static final BCCoreConfig.DoubleValue largeOilGenProb = new BCCoreConfig.DoubleValue(0.04 / 100);

    public static final BCCoreConfig.StringListValue forceExcessiveOilBiomes =
            new BCCoreConfig.StringListValue(List.of());
    public static final BCCoreConfig.StringListValue richSurfaceDepositBiomes =
            new BCCoreConfig.StringListValue(List.of(
                    "minecraft:deep_ocean", "minecraft:deep_lukewarm_ocean",
                    "minecraft:deep_cold_ocean", "minecraft:deep_frozen_ocean",
                    "minecraft:desert",
                    "minecraft:badlands", "minecraft:wooded_badlands"));
    public static final BCCoreConfig.StringListValue surfaceDepositBiomes =
            new BCCoreConfig.StringListValue(List.of(
                    "minecraft:ocean", "minecraft:warm_ocean", "minecraft:lukewarm_ocean",
                    "minecraft:cold_ocean", "minecraft:frozen_ocean"));
    public static final BCCoreConfig.StringListValue standardSurfaceDepositBiomes =
            new BCCoreConfig.StringListValue(List.of(
                    "minecraft:jungle", "minecraft:sparse_jungle", "minecraft:bamboo_jungle",
                    "minecraft:ice_spikes", "minecraft:snowy_beach", "minecraft:frozen_river"));
    public static final BCCoreConfig.StringListValue mountainousSurfaceDepositBiomes =
            new BCCoreConfig.StringListValue(List.of(
                    "minecraft:windswept_hills", "minecraft:windswept_gravelly_hills",
                    "minecraft:windswept_forest", "minecraft:jagged_peaks",
                    "minecraft:frozen_peaks", "minecraft:stony_peaks",
                    "minecraft:snowy_slopes", "minecraft:meadow", "minecraft:grove",
                    "minecraft:cherry_grove"));
    public static final BCCoreConfig.StringListValue excludedBiomes =
            new BCCoreConfig.StringListValue(List.of("minecraft:the_void", "minecraft:river"));
    public static final BCCoreConfig.EnumValue<ListMode> biomeListMode =
            new BCCoreConfig.EnumValue<>(ListMode.BLACKLIST);
    public static final BCCoreConfig.StringListValue excludedDimensions =
            new BCCoreConfig.StringListValue(List.of("minecraft:the_nether", "minecraft:the_end"));
    public static final BCCoreConfig.EnumValue<ListMode> dimensionListMode =
            new BCCoreConfig.EnumValue<>(ListMode.BLACKLIST);

    public enum ListMode {
        BLACKLIST,
        WHITELIST
    }

    public static void buildWorldgen(Object builder) {}

    public static void buildGeneral(Object builder) {}

    public static void buildDisplay(Object builder) {}

    public static String rfFeKey(String baseKey) {
        return useRfNaming.get() ? baseKey + ".rf" : baseKey;
    }

    public static Set<Identifier> getForceExcessiveOilBiomes() {
        return forceExcessiveOilBiomes.get().stream().map(Identifier::parse).collect(Collectors.toSet());
    }

    public static Set<Identifier> getSurfaceDepositBiomes() {
        return surfaceDepositBiomes.get().stream().map(Identifier::parse).collect(Collectors.toSet());
    }

    public static Set<Identifier> getRichSurfaceDepositBiomes() {
        return richSurfaceDepositBiomes.get().stream().map(Identifier::parse).collect(Collectors.toSet());
    }

    public static Set<Identifier> getStandardSurfaceDepositBiomes() {
        return standardSurfaceDepositBiomes.get().stream().map(Identifier::parse).collect(Collectors.toSet());
    }

    public static Set<Identifier> getMountainousSurfaceDepositBiomes() {
        return mountainousSurfaceDepositBiomes.get().stream().map(Identifier::parse).collect(Collectors.toSet());
    }

    public static Set<Identifier> getExcludedBiomes() {
        return excludedBiomes.get().stream().map(Identifier::parse).collect(Collectors.toSet());
    }

    public static Set<Identifier> getExcludedDimensions() {
        return excludedDimensions.get().stream().map(Identifier::parse).collect(Collectors.toSet());
    }
}
