package buildcraft.fabric.client.event;

import net.minecraft.client.resources.model.ModelBakery;

public class ModelEvent {
    public static class BakingCompleted extends BCClientEvents.ModelEvent.BakingCompleted {}

    public static final class ModifyBakingResult {
        private final ModelBakery.BakingResult bakingResult;

        public ModifyBakingResult(ModelBakery.BakingResult bakingResult) {
            this.bakingResult = bakingResult;
        }

        public ModelBakery.BakingResult getBakingResult() {
            return bakingResult;
        }
    }
}

