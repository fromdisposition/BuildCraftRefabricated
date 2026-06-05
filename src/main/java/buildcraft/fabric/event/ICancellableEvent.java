package buildcraft.fabric.event;

public interface ICancellableEvent {
    boolean isCanceled();
    void setCanceled(boolean canceled);
}

