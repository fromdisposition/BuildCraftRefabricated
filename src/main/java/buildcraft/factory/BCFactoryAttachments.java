package buildcraft.factory;

import java.util.Arrays;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.fabric.BCRegistries;

public final class BCFactoryAttachments {
    public static AttachmentType<OilAndFuelProduction> OIL_FUEL_PRODUCTION;

    private BCFactoryAttachments() {}

    public static void register() {
        OIL_FUEL_PRODUCTION = AttachmentRegistry.create(
                BCRegistries.id(BCFactory.MODID, "oil_fuel_production"),
                builder -> builder
                        .initializer(OilAndFuelProduction::new)
                        .persistent(OilAndFuelProduction.CODEC)
                        .copyOnDeath());
    }

    public static OilAndFuelProduction get(Player player) {
        return player.getAttachedOrCreate(OIL_FUEL_PRODUCTION);
    }

    public static final class OilAndFuelProduction {
        public static final int PER_FLUID_TARGET = 16_000;

        static final Codec<OilAndFuelProduction> CODEC = Codec.INT.listOf().comapFlatMap(
                list -> {
                    int expected = BCEnergyFluidsFabric.BASE_NAMES.size();
                    if (list.size() != expected) {
                        return com.mojang.serialization.DataResult.error(
                                () -> "expected " + expected + " fluid counters, got " + list.size());
                    }
                    OilAndFuelProduction copy = new OilAndFuelProduction();
                    for (int i = 0; i < expected; i++) {
                        copy.amounts[i] = Math.min(PER_FLUID_TARGET, Math.max(0, list.get(i)));
                    }
                    return com.mojang.serialization.DataResult.success(copy);
                },
                production -> Arrays.stream(production.amounts).boxed().toList());

        private final int[] amounts = new int[BCEnergyFluidsFabric.BASE_NAMES.size()];

        public String recordProduction(String baseName, int mb) {
            if (baseName == null || mb <= 0) {
                return null;
            }
            int index = BCEnergyFluidsFabric.BASE_NAMES.indexOf(baseName);
            if (index < 0) {
                return null;
            }
            if (amounts[index] >= PER_FLUID_TARGET) {
                return null;
            }
            amounts[index] = Math.min(PER_FLUID_TARGET, amounts[index] + mb);
            return amounts[index] >= PER_FLUID_TARGET ? baseName : null;
        }

        public boolean isComplete() {
            for (int amount : amounts) {
                if (amount < PER_FLUID_TARGET) {
                    return false;
                }
            }
            return true;
        }

        public int get(String baseName) {
            int index = BCEnergyFluidsFabric.BASE_NAMES.indexOf(baseName);
            return index < 0 ? -1 : amounts[index];
        }
    }
}
