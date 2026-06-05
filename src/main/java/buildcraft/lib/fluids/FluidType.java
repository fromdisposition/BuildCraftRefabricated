package buildcraft.lib.fluids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import buildcraft.lib.common.SoundAction;
import buildcraft.lib.common.SoundActions;
import buildcraft.lib.misc.FluidUtilBC;
import org.jspecify.annotations.Nullable;

public class FluidType {
    public static final int BUCKET_VOLUME = FluidConstants.BUCKET_VOLUME;
    public static final FluidType EMPTY = new FluidType(Fluids.EMPTY, 1000, 1000);

    private static final int WATER_VISCOSITY = 1000;
    private static final int LAVA_VISCOSITY = 3000;

    private final Fluid fluid;
    private final String descriptionId;
    private final int viscosity;
    private final int density;
    private final Map<SoundAction, SoundEvent> sounds = new ConcurrentHashMap<>();

    public FluidType(Fluid fluid) {
        this(fluid, defaultViscosity(fluid), defaultDensity(fluid));
    }

    public FluidType(Fluid fluid, int viscosity, int density) {
        this.fluid = fluid;
        this.viscosity = viscosity;
        this.density = density;
        var key = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(FluidUtilBC.canonicalFluid(fluid));
        this.descriptionId = key == null ? "fluid_type.minecraft.empty" : key.toLanguageKey("fluid_type");
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            sounds.put(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL);
            sounds.put(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
        } else if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            sounds.put(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA);
            sounds.put(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA);
        }
    }

    private static int defaultViscosity(Fluid fluid) {
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return LAVA_VISCOSITY;
        }
        return WATER_VISCOSITY;
    }

    private static int defaultDensity(Fluid fluid) {
        return WATER_VISCOSITY;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public int getViscosity() {
        return viscosity;
    }

    public int getViscosity(FluidStack stack) {
        return getViscosity();
    }

    public int getViscosity(FluidState state, net.minecraft.world.level.BlockAndLightGetter getter, BlockPos pos) {
        return getViscosity();
    }

    public final boolean isLighterThanAir() {
        return density <= 0;
    }

    public String getDescriptionId() {
        return descriptionId;
    }

    public String getDescriptionId(FluidStack stack) {
        return getDescriptionId();
    }

    public Component getDescription(FluidStack stack) {
        return Component.translatable(getDescriptionId());
    }

    @Nullable
    public SoundEvent getSound(SoundAction action) {
        return sounds.get(action);
    }

    @Nullable
    public Item getBucket(FluidStack stack) {
        return fluid.getBucket();
    }

    public boolean canBePlacedInLevel(Level level, BlockPos pos, FluidStack stack) {
        return !stack.isEmpty() && fluid != Fluids.EMPTY;
    }

    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return fluid == Fluids.WATER && level.dimension() == Level.NETHER;
    }

    public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack) {
        level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 2.6F);
    }

    public BlockState getStateForPlacement(Level level, BlockPos pos, FluidStack stack) {
        return fluid.defaultFluidState().createLegacyBlock();
    }

    public BlockState getBlockForFluidState(Level level, BlockPos pos, FluidState fluidState) {
        return fluidState.createLegacyBlock();
    }

    @Override
    public String toString() {
        return descriptionId;
    }
}
