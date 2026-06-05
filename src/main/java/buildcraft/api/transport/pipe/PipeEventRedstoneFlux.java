package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;

public abstract class PipeEventRedstoneFlux extends PipeEvent {
    public final IFlowRedstoneFlux flow;

    protected PipeEventRedstoneFlux(IPipeHolder holder, IFlowRedstoneFlux flow) {
        super(holder);
        this.flow = flow;
    }

    protected PipeEventRedstoneFlux(boolean canBeCancelled, IPipeHolder holder, IFlowRedstoneFlux flow) {
        super(canBeCancelled, holder);
        this.flow = flow;
    }

    public static class Configure extends PipeEventRedstoneFlux {
        private int maxPower = 100;
        private boolean receiver = false;
        private boolean disabled = false;

        public Configure(IPipeHolder holder, IFlowRedstoneFlux flow) {
            super(holder, flow);
        }

        public int getMaxPower() {
            return this.maxPower;
        }

        public void setMaxPower(int maxPower) {
            this.maxPower = maxPower;
        }

        public boolean isReceiver() {
            return this.receiver;
        }

        public void setReceiver(boolean receiver) {
            this.receiver = receiver;
        }

        public void disableTransfer() {
            disabled = true;
        }

        public boolean isTransferDisabled() {
            return disabled;
        }
    }

    public static class PrimaryDirection extends PipeEventRedstoneFlux {
        private Direction facing;

        public PrimaryDirection(IPipeHolder holder, IFlowRedstoneFlux flow, Direction facing) {
            super(holder, flow);
            this.facing = facing;
        }

        public Direction getFacing() {
            return facing;
        }

        public void setFacing(Direction facing) {
            this.facing = facing;
        }
    }
}
