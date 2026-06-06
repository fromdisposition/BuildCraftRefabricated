package buildcraft.api.transport.pipe;

import buildcraft.api.mj.MjAPI;
import net.minecraft.core.Direction;

public abstract class PipeEventPower extends PipeEvent {
   public final IFlowPower flow;

   protected PipeEventPower(IPipeHolder holder, IFlowPower flow) {
      super(holder);
      this.flow = flow;
   }

   protected PipeEventPower(boolean canBeCancelled, IPipeHolder holder, IFlowPower flow) {
      super(canBeCancelled, holder);
      this.flow = flow;
   }

   public static class Configure extends PipeEventPower {
      private long maxPower = 10L * MjAPI.MJ;
      private long powerResistance = -1L;
      private long powerLoss = -1L;
      private boolean receiver = false;
      private boolean disabled = false;

      public Configure(IPipeHolder holder, IFlowPower flow) {
         super(holder, flow);
      }

      public long getMaxPower() {
         return this.maxPower;
      }

      public void setMaxPower(long maxPower) {
         this.maxPower = maxPower;
      }

      public long getPowerLoss() {
         return this.powerLoss;
      }

      public void setPowerLoss(long powerLoss) {
         this.powerLoss = powerLoss;
      }

      public long getPowerResistance() {
         return this.powerResistance;
      }

      public void setPowerResistance(long powerResistance) {
         this.powerResistance = powerResistance;
      }

      public boolean isReceiver() {
         return this.receiver;
      }

      public void setReceiver(boolean receiver) {
         this.receiver = receiver;
      }

      public void disableTransfer() {
         this.disabled = true;
      }

      public boolean isTransferDisabled() {
         return this.disabled;
      }
   }

   public static class PrimaryDirection extends PipeEventPower {
      private Direction facing;

      public PrimaryDirection(IPipeHolder holder, IFlowPower flow, Direction facing) {
         super(holder, flow);
         this.facing = facing;
      }

      public Direction getFacing() {
         return this.facing;
      }

      public void setFacing(Direction facing) {
         this.facing = facing;
      }
   }
}
