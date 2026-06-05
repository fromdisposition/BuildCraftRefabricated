package buildcraft.api.transport.pipe;

public abstract class PipeEvent {
    public final boolean canBeCancelled;
    public final IPipeHolder holder;
    private boolean canceled = false;

    public PipeEvent(IPipeHolder holder) {
        this.canBeCancelled = false;
        this.holder = holder;
    }

    protected PipeEvent(boolean canBeCancelled, IPipeHolder holder) {
        this.canBeCancelled = canBeCancelled;
        this.holder = holder;
    }

    public void cancel() {
        if (canBeCancelled) {
            canceled = true;
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    public String checkStateForErrors() {
        if (canceled & !canBeCancelled) {
            return "Somehow cancelled an event that isn't marked as such!";
        }
        return null;
    }
}
