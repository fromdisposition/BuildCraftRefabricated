package buildcraft.api.transport.pipe;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.IStripesRegistry;
import buildcraft.api.transport.pluggable.IPluggableRegistry;
import buildcraft.api.transport.pluggable.PipePluggable;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Central transport API: pipe/flow registries, default transfer rates, and capability tokens. */
public final class PipeApi {
   @Nullable
   public static IPipeRegistry pipeRegistry;
   @Nullable
   public static IPluggableRegistry pluggableRegistry;
   @Nullable
   public static IStripesRegistry stripeRegistry;
   @Nullable
   public static IPipeExtensionManager extensionManager;
   public static PipeFlowType flowStructure;
   public static PipeFlowType flowItems;
   public static PipeFlowType flowFluids;
   public static PipeFlowType flowPower;
   public static PipeFlowType flowRf;
   public static PipeApi.FluidTransferInfo fluidInfoDefault = new PipeApi.FluidTransferInfo(20, 10);
   public static PipeApi.PowerTransferInfo powerInfoDefault = PipeApi.PowerTransferInfo.createFromResistance(8L * MjAPI.MJ, MjAPI.MJ / 32L, false);
   public static PipeApi.RedstoneFluxTransferInfo rfInfoDefault = new PipeApi.RedstoneFluxTransferInfo(80, false);
   public static final Map<PipeDefinition, PipeApi.FluidTransferInfo> fluidTransferData = new IdentityHashMap<>();
   public static final Map<PipeDefinition, PipeApi.PowerTransferInfo> powerTransferData = new IdentityHashMap<>();
   public static final Map<PipeDefinition, PipeApi.RedstoneFluxTransferInfo> rfTransferData = new IdentityHashMap<>();
   @Nonnull
   public static final Object CAP_PIPE_HOLDER = IPipeHolder.class;
   @Nonnull
   public static final Object CAP_PIPE = IPipe.class;
   @Nonnull
   public static final Object CAP_PLUG = PipePluggable.class;

   public static PipeApi.FluidTransferInfo getFluidTransferInfo(PipeDefinition def) {
      PipeApi.FluidTransferInfo info = fluidTransferData.get(def);
      return info == null ? fluidInfoDefault : info;
   }

   public static PipeApi.PowerTransferInfo getPowerTransferInfo(PipeDefinition def) {
      PipeApi.PowerTransferInfo info = powerTransferData.get(def);
      return info == null ? powerInfoDefault : info;
   }

   public static PipeApi.RedstoneFluxTransferInfo getRfTransferInfo(PipeDefinition def) {
      PipeApi.RedstoneFluxTransferInfo info = rfTransferData.get(def);
      return info == null ? rfInfoDefault : info;
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

      public static PipeApi.PowerTransferInfo createFromLoss(long transferPerTick, long lossPerTick, boolean isReceiver) {
         return new PipeApi.PowerTransferInfo(transferPerTick, lossPerTick, lossPerTick * MjAPI.MJ / transferPerTick, isReceiver);
      }

      public static PipeApi.PowerTransferInfo createFromResistance(long transferPerTick, long resistancePerTick, boolean isReceiver) {
         return new PipeApi.PowerTransferInfo(transferPerTick, resistancePerTick, resistancePerTick * transferPerTick / MjAPI.MJ, isReceiver);
      }

      public PowerTransferInfo(long transferPerTick, long lossPerTick, long resistancePerTick, boolean isReceiver) {
         if (transferPerTick < 10L) {
            transferPerTick = 10L;
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
}
