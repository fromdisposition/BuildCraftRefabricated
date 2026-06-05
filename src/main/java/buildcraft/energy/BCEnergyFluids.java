package buildcraft.energy;

import java.util.List;

import net.minecraft.world.level.material.Fluid;

import buildcraft.fabric.BCEnergyFluidsFabric;

public final class BCEnergyFluids {
    public record FluidEntry(
            String name,
            String baseName,
            int heat,
            int density,
            int viscosity,
            int temperature,
            boolean gaseous,
            int tintColor,
            int texLight,
            int texDark,
            Fluid source,
            Fluid flowing,
            net.minecraft.world.level.block.Block block,
            net.minecraft.world.item.Item bucket) {}

    public static final List<String> BASE_NAMES = BCEnergyFluidsFabric.BASE_NAMES;
    public static List<FluidEntry> ALL = List.of();
    public static FluidEntry OIL_COOL;

    private BCEnergyFluids() {}

    public static void init(Object unusedModEventBus) {
        refreshSnapshot();
    }

    public static int getHeat(Fluid fluid) {
        return BCEnergyFluidsFabric.getHeat(fluid);
    }

    public static String getBaseName(Fluid fluid) {
        return BCEnergyFluidsFabric.getBaseName(fluid);
    }

    public static void refreshSnapshot() {
        ALL = BCEnergyFluidsFabric.ALL.stream()
                .map(e -> new FluidEntry(
                        e.name(),
                        e.baseName(),
                        e.heat(),
                        0,
                        0,
                        0,
                        e.gaseous(),
                        e.tintColor(),
                        e.texLight(),
                        e.texDark(),
                        e.still(),
                        e.flowing(),
                        e.block(),
                        e.bucket()))
                .toList();
        OIL_COOL = ALL.stream()
                .filter(e -> "oil".equals(e.baseName()) && e.heat() == 0)
                .findFirst()
                .orElse(null);
    }
}
