package buildcraft.fabric.client.event;

import net.minecraft.client.resources.model.ModelBakery.BakingResult;

public class ModelEvent {
   public static class BakingCompleted extends BCClientEvents.ModelEvent.BakingCompleted {
   }

   public static final class ModifyBakingResult {
      private final BakingResult bakingResult;

      public ModifyBakingResult(BakingResult bakingResult) {
         this.bakingResult = bakingResult;
      }

      public BakingResult getBakingResult() {
         return this.bakingResult;
      }
   }
}
