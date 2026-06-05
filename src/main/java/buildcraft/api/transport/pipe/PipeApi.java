package buildcraft.api.transport.pipe;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import buildcraft.api.core.CapabilitiesHelper;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IStripesRegistry;
import buildcraft.api.transport.pluggable.IPluggableRegistry;
import buildcraft.api.transport.pluggable.PipePluggable;

public final class PipeApi {
    public static IPipeRegistry pipeRegistry;
    public static IPluggableRegistry pluggableRegistry;
    public static IStripesRegistry stripeRegistry;
    public static IPipeExtensionManager extensionManager;
    public static PipeFlowType flowStructure;
    public static PipeFlowType flowItems;
    public static PipeFlowType flowFluids;
    public static PipeFlowType flowPower;
    public static PipeFlowType flowRf;

    public static FluidTransferInfo fluidInfoDefault = new FluidTransferInfo(20, 10);

    public static PowerTransferInfo powerInfoDefault = PowerTransferInfo.createFromResistance(8 * MjAPI.MJ, MjAPI.MJ / 32, false);

    public static RedstoneFluxTransferInfo rfInfoDefault = new RedstoneFluxTransferInfo(80, false);

    public static final Map<PipeDefinition, FluidTransferInfo> fluidTransferData = new IdentityHashMap<>();
    public static final Map<PipeDefinition, PowerTransferInfo> powerTransferData = new IdentityHashMap<>();
    public static final Map<PipeDefinition, RedstoneFluxTransferInfo> rfTransferData = new IdentityHashMap<>();

    @Nonnull
    public static final Object CAP_PIPE_HOLDER;

    @Nonnull
    public static final Object CAP_PIPE;

    @Nonnull
    public static final Object CAP_PLUG;

    @Nonnull
    public static final Object CAP_INJECTABLE;

    public static FluidTransferInfo getFluidTransferInfo(PipeDefinition def) {
        FluidTransferInfo info = fluidTransferData.get(def);
        if (info == null) {
            return fluidInfoDefault;
        } else {
            return info;
        }
    }

    public static PowerTransferInfo getPowerTransferInfo(PipeDefinition def) {
        PowerTransferInfo info = powerTransferData.get(def);
        if (info == null) {
            return powerInfoDefault;
        } else {
            return info;
        }
    }

    public static RedstoneFluxTransferInfo getRfTransferInfo(PipeDefinition def) {
        RedstoneFluxTransferInfo info = rfTransferData.get(def);
        if (info == null) {
            return rfInfoDefault;
        } else {
            return info;
        }
    }

    public static class FluidTransferInfo {

        public final int transferPerTick;

        public final double transferDelayMultiplier;

        public FluidTransferInfo(int transferPerTick, int transferDelay) {
            this.transferPerTick = transferPerTick;
            if (transferDelay <= 0) {
                transferDelay = 1;
            }
            this.transferDelayMultiplier = transferDelay;
        }
    }

    public static class PowerTransferInfo {
        public final long transferPerTick;
        public final long lossPerTick;

        public final long resistancePerTick;
        public final boolean isReceiver;

        public static PowerTransferInfo createFromLoss(long transferPerTick, long lossPerTick, boolean isReceiver) {
            return new PowerTransferInfo(transferPerTick, lossPerTick, lossPerTick * MjAPI.MJ / transferPerTick, isReceiver);
        }

        public static PowerTransferInfo createFromResistance(long transferPerTick, long resistancePerTick, boolean isReceiver) {
            return new PowerTransferInfo(transferPerTick, resistancePerTick, resistancePerTick * transferPerTick / MjAPI.MJ, isReceiver);
        }

        public PowerTransferInfo(long transferPerTick, long lossPerTick, long resistancePerTick, boolean isReceiver) {
            if (transferPerTick < 10) {
                transferPerTick = 10;
            }
            this.transferPerTick = transferPerTick;
            this.lossPerTick = lossPerTick;
            this.resistancePerTick = resistancePerTick;
            this.isReceiver = isReceiver;
        }
    }

    public static class RedstoneFluxTransferInfo {
        public final int transferPerTick;
        public final boolean isReceiver;

        public RedstoneFluxTransferInfo(int transferPerTick, boolean isReceiver) {
            this.transferPerTick = transferPerTick;
            this.isReceiver = isReceiver;
        }
    }

    static {
        CAP_PIPE = CapabilitiesHelper.registerCapability(IPipe.class);
        CAP_PLUG = CapabilitiesHelper.registerCapability(PipePluggable.class);
        CAP_PIPE_HOLDER = CapabilitiesHelper.registerCapability(IPipeHolder.class);
        CAP_INJECTABLE = CapabilitiesHelper.registerCapability(IInjectable.class);
    }
}
