package buildcraft.fabric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import buildcraft.core.BCCore;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.fabric.fluid.BcFluidBlock;
import buildcraft.fabric.fluid.BcOilFluid;
import buildcraft.lib.fluids.FluidTypes;

import net.minecraft.world.level.Level;

public final class BCEnergyFluidsFabric {

    private static final int[][] FLUID_DATA = {
            {  900,  2000,  3,  6, 0x50_50_50, 0x05_05_05,  1,  1 },
            { 1200,  4000,  3,  4, 0x10_0F_10, 0x42_10_42,  1,  0 },
            {  850,  1800,  3,  6, 0xA0_8F_1F, 0x42_35_20,  1,  1 },
            {  950,  1600,  3,  5, 0x87_6E_77, 0x42_24_24,  1,  1 },
            {  750,  1400,  2,  8, 0xE4_AF_78, 0xB4_7F_00,  0,  1 },
            {  600,   800,  2,  7, 0xFF_AF_3F, 0xE0_7F_00,  0,  1 },
            {  700,  1000,  2,  7, 0xF2_A7_00, 0xC4_87_00,  0,  1 },
            {  400,   600,  1,  8, 0xFF_FF_30, 0xE4_CF_00,  0,  1 },
            {  650,   900,  1,  9, 0xF6_D7_00, 0xC4_B7_00,  0,  1 },
            {  300,   500,  0, 10, 0xFA_F6_30, 0xE0_D9_00,  0,  1 },
    };

    private static final String[] FLUID_NAMES = {
            "oil", "oil_residue", "oil_heavy", "oil_dense", "oil_distilled",
            "fuel_dense", "fuel_mixed_heavy", "fuel_light", "fuel_mixed_light", "fuel_gaseous"
    };

    public static final List<String> BASE_NAMES = List.of(FLUID_NAMES);

    public record FluidEntry(
            String name,
            String baseName,
            int heat,
            int texLight,
            int texDark,
            int tintColor,
            Fluid still,
            Fluid flowing,
            Block block,
            Item bucket,
            boolean gaseous
    ) {}

    private static final List<FluidEntry> ENTRIES = new ArrayList<>();

    public static FluidEntry OIL_COOL;

    public static final List<FluidEntry> ALL = Collections.unmodifiableList(ENTRIES);

    private BCEnergyFluidsFabric() {}

    public static void register() {
        ENTRIES.clear();
        for (int i = 0; i < FLUID_DATA.length; i++) {
            int[] data = FLUID_DATA[i];
            String baseName = FLUID_NAMES[i];
            for (int heat = 0; heat < 3; heat++) {
                FluidEntry entry = registerVariant(
                        baseName, heat,
                        data[0], data[1], data[2], data[3],
                        data[4], data[5], data[6], data[7]);
                ENTRIES.add(entry);
                if (i == 0 && heat == 0) {
                    OIL_COOL = entry;
                }
            }
        }
    }

    private static FluidEntry registerVariant(
            String baseName, int heat,
            int baseDensity, int baseViscosity, int boilPoint, int baseSpread,
            int texLight, int texDark, int stickyFlag, int flammableFlag) {

        int viscosity = baseViscosity * (4 - heat) / 4;
        int density = baseDensity * (heat >= boilPoint ? -1 : 1);
        boolean gaseous = density < 0;

        boolean sticky = buildcraft.energy.BCEnergyConfig.oilIsSticky.get() && stickyFlag == 1;
        boolean flammable = buildcraft.energy.BCEnergyConfig.enableOilBurn.get() && flammableFlag == 1;

        int quanta = baseSpread + (baseSpread > 6 ? heat : heat / 2);

        int tintColor = buildcraft.lib.client.fluid.BcFluidTintUtil.RENDER_TINT_WHITE;

        String regName = baseName + (heat == 0 ? "" : "_heat_" + heat);
        Identifier id = Identifier.fromNamespaceAndPath("buildcraftenergy", regName);

        BcOilFluid.Holder holder = new BcOilFluid.Holder();
        holder.baseName = baseName;
        holder.denseFluid = baseName.contains("oil_heavy")
                || baseName.contains("oil_dense")
                || baseName.contains("oil_residue");
        holder.gaseous = gaseous;
        holder.sticky = sticky;
        holder.flammable = flammable;
        holder.viscosity = viscosity;
        holder.density = density;
        holder.tickDelay = Math.max(1, viscosity / 200);
        if (quanta <= 5) {
            holder.slopeFindDistance = 2;
            holder.dropOff = 2;
        } else if (quanta <= 6) {
            holder.slopeFindDistance = 3;
            holder.dropOff = 1;
        } else {
            holder.slopeFindDistance = 4;
            holder.dropOff = 1;
        }

        holder.still = Registry.register(
                BuiltInRegistries.FLUID,
                id,
                new BcOilFluid.Source(holder));
        holder.flowing = Registry.register(
                BuiltInRegistries.FLUID,
                Identifier.fromNamespaceAndPath("buildcraftenergy", regName + "_flowing"),
                new BcOilFluid.Flowing(holder));

        ResourceKey<Block> blockKey = ResourceKey.create(
                net.minecraft.core.registries.Registries.BLOCK, id);
        holder.block = Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                new BcFluidBlock((FlowingFluid) holder.still, BlockBehaviour.Properties.of()
                        .setId(blockKey)
                        .mapColor(gaseous ? MapColor.NONE : MapColor.COLOR_BLACK)
                        .replaceable()
                        .strength(100.0F)
                        .pushReaction(PushReaction.DESTROY)
                        .noLootTable()
                        .liquid(), sticky));

        if (flammable) {
            FlammableBlockRegistry.getDefaultInstance().add(holder.block, 200, 200);
        }

        holder.bucket = BCRegistries.registerItem("buildcraftenergy", 
                regName + "_bucket",
                props -> new BucketItem(holder.still, props
                        .craftRemainder(Items.BUCKET)
                        .stacksTo(1)));

        FluidTypes.register(holder.still, viscosity, density);
        FluidTypes.register(holder.flowing, viscosity, density);

        return new FluidEntry(regName, baseName, heat, texLight, texDark, tintColor,
                holder.still, holder.flowing, holder.block, holder.bucket, gaseous);
    }

    @javax.annotation.Nullable
    public static FluidEntry findEntry(Fluid fluid) {
        if (fluid == null) {
            return null;
        }
        for (FluidEntry entry : ENTRIES) {
            if (entry.still() == fluid || entry.flowing() == fluid) {
                return entry;
            }
        }
        return null;
    }

    public static Fluid findFluid(String baseName, int heat) {
        String regName = baseName + (heat == 0 ? "" : "_heat_" + heat);
        for (FluidEntry entry : ENTRIES) {
            if (entry.name().equals(regName)) {
                return entry.still();
            }
        }
        return null;
    }

    public static int getHeat(Fluid fluid) {
        if (fluid == null) {
            return -1;
        }
        for (FluidEntry entry : ENTRIES) {
            if (entry.still() == fluid || entry.flowing() == fluid) {
                return entry.heat();
            }
        }
        return -1;
    }

    public static BlockState sourceBlockState(FluidEntry entry) {
        return entry.still().defaultFluidState().createLegacyBlock();
    }

    public static BlockState oilSourceBlockStateForLevel(Level level) {
        if (level != null
                && level.dimension() == Level.NETHER
                && BCEnergyConfig.enableNetherOilGeneration.get()) {
            Fluid searing = findFluid("oil", 2);
            if (searing != null) {
                return searing.defaultFluidState().createLegacyBlock();
            }
        }
        return OIL_COOL != null ? sourceBlockState(OIL_COOL) : null;
    }

    public static String getBaseName(Fluid fluid) {
        if (fluid == null) {
            return null;
        }
        for (FluidEntry entry : ENTRIES) {
            if (entry.still() == fluid || entry.flowing() == fluid) {
                return entry.baseName();
            }
        }
        return null;
    }
}
