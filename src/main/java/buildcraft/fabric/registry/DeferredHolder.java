package buildcraft.fabric.registry;

import java.util.function.Supplier;

public class DeferredHolder<T, I extends T> implements Supplier<I> {
    private I value;

    void bind(I value) {
        this.value = value;
    }

    @Override
    public I get() {
        if (value == null) {
            throw new IllegalStateException("DeferredHolder not yet registered");
        }
        return value;
    }
}
