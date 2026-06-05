package buildcraft.lib.fabric.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import buildcraft.fabric.client.event.ModelEvent;

public final class FabricModelModifyHooks {
    private static final List<Consumer<ModelEvent.ModifyBakingResult>> LISTENERS = new ArrayList<>();

    private FabricModelModifyHooks() {}

    public static void register(Consumer<ModelEvent.ModifyBakingResult> listener) {
        LISTENERS.add(listener);
    }

    public static void fire(ModelEvent.ModifyBakingResult event) {
        for (Consumer<ModelEvent.ModifyBakingResult> listener : LISTENERS) {
            listener.accept(event);
        }
    }
}

