package buildcraft.transport.pipe.behaviour;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.transport.BCTransportConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourWood extends PipeBehaviourDirectional implements IMjRedstoneReceiver {
   public PipeBehaviourWood(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourWood(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @Override
   public int getTextureIndex(@Nullable Direction face) {
      return face != null && face == this.currentDir.face ? 1 : 0;
   }

   @Override
   protected boolean canFaceDirection(Direction dir) {
      return dir != null && this.pipe.isConnected(dir) && this.pipe.getConnectedType(dir) == IPipe.ConnectedType.TILE;
   }

   @PipeEventHandler
   public void fluidSideCheck(PipeEventFluid.SideCheck sideCheck) {
      if (this.currentDir.face != null) {
         sideCheck.disallow(this.currentDir.face);
      }
   }

   protected long extract(long power, boolean simulate) {
      if (power > 0L && this.getCurrentDir() != null) {
         PipeFlow flow = this.pipe.getFlow();
         if (flow instanceof IFlowItems itemFlow) {
            int maxItems = (int)(power / BCTransportConfig.mjPerItem.get());
            if (maxItems > 0) {
               int extracted = this.extractItems(itemFlow, this.getCurrentDir(), maxItems, simulate);
               if (extracted > 0) {
                  return power - extracted * BCTransportConfig.mjPerItem.get();
               }
            }
         } else if (flow instanceof IFlowFluid fluidFlow) {
            int maxMillibuckets = (int)(power / BCTransportConfig.mjPerMillibucket.get());
            if (maxMillibuckets > 0) {
               FluidStack extracted = this.extractFluid(fluidFlow, this.getCurrentDir(), maxMillibuckets, simulate);
               if (extracted != null && !extracted.isEmpty()) {
                  return power - extracted.getAmount() * BCTransportConfig.mjPerMillibucket.get();
               }
            }
         }
      }

      return power;
   }

   protected int extractItems(IFlowItems flow, Direction dir, int count, boolean simulate) {
      return flow.tryExtractItems(count, dir, null, stack -> true, simulate);
   }

   @Nullable
   protected FluidStack extractFluid(IFlowFluid flow, Direction dir, int millibuckets, boolean simulate) {
      return flow.tryExtractFluid(millibuckets, dir, null, simulate);
   }

   @Override
   public boolean canConnect(@Nonnull IMjConnector other) {
      return true;
   }

   @Override
   public long getPowerRequested() {
      long power = 512L * MjAPI.MJ;
      return power - this.extract(power, true);
   }

   @Override
   public long receivePower(long microJoules, boolean simulate) {
      return this.extract(microJoules, simulate);
   }

   @Override
   public <T> T getCapability(@Nonnull Object capability, Direction facing) {
      return (T)(capability != MjAPI.CAP_RECEIVER && capability != MjAPI.CAP_CONNECTOR ? super.getCapability(capability, facing) : this);
   }

   @Override
   public boolean canConnect(Direction face, PipeBehaviour other) {
      return !(other instanceof PipeBehaviourWood);
   }

   @PipeEventHandler
   public void sideCheck(PipeEventItem.SideCheck sideCheck) {
      if (this.currentDir.face != null) {
         sideCheck.disallow(this.currentDir.face);
      }
   }
}
