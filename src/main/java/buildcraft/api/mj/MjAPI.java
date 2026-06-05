package buildcraft.api.mj;

import java.text.DecimalFormat;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import buildcraft.lib.attachments.BlockAttachment;

import org.jspecify.annotations.Nullable;

import buildcraft.lib.BCLibConfig;

public class MjAPI {

    public static final long ONE_MINECRAFT_JOULE = getMjValue();

    public static final long MJ = ONE_MINECRAFT_JOULE;

    public static final DecimalFormat MJ_DISPLAY_FORMAT = new DecimalFormat("#,##0.##");

    public static IMjEffectManager EFFECT_MANAGER = NullaryEffectManager.INSTANCE;

    public static String formatMj(long microMj) {
        return formatMjInternal(microMj / (double) MJ);
    }

    private static String formatMjInternal(double val) {
        return MJ_DISPLAY_FORMAT.format(val);
    }

    public static MjRfConversion getRfConversion() {
        return MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get());
    }

    public static boolean isRfAutoConversionEnabled() {
        return BCLibConfig.powerMode.get().autoconvert;
    }

    public enum NullaryEffectManager implements IMjEffectManager {
        INSTANCE;
        @Override
        public void createPowerLossEffect(Level world, Vec3 center, long microJoulesLost) {}

        @Override
        public void createPowerLossEffect(Level world, Vec3 center, Direction direction, long microJoulesLost) {}

        @Override
        public void createPowerLossEffect(Level world, Vec3 center, Vec3 direction, long microJoulesLost) {}
    }

    @Nonnull
    public static final BlockAttachment<IMjConnector, @Nullable Direction> CAP_CONNECTOR =
        BlockAttachment.createSided(Identifier.fromNamespaceAndPath("buildcraftcore", "mj_connector"), IMjConnector.class);

    @Nonnull
    public static final BlockAttachment<IMjReceiver, @Nullable Direction> CAP_RECEIVER =
        BlockAttachment.createSided(Identifier.fromNamespaceAndPath("buildcraftcore", "mj_receiver"), IMjReceiver.class);

    @Nonnull
    public static final BlockAttachment<IMjRedstoneReceiver, @Nullable Direction> CAP_REDSTONE_RECEIVER =
        BlockAttachment.createSided(Identifier.fromNamespaceAndPath("buildcraftcore", "mj_redstone_receiver"), IMjRedstoneReceiver.class);

    @Nonnull
    public static final BlockAttachment<IMjReadable, @Nullable Direction> CAP_READABLE =
        BlockAttachment.createSided(Identifier.fromNamespaceAndPath("buildcraftcore", "mj_readable"), IMjReadable.class);

    @Nonnull
    public static final BlockAttachment<IMjPassiveProvider, @Nullable Direction> CAP_PASSIVE_PROVIDER =
        BlockAttachment.createSided(Identifier.fromNamespaceAndPath("buildcraftcore", "mj_passive_provider"), IMjPassiveProvider.class);

    private static long getMjValue() {
        return 1_000_000L;
    }
}
