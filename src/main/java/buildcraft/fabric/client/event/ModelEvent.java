package buildcraft.fabric.client.event;

//? if >= 1.21.10 {
import net.minecraft.client.resources.model.ModelBakery.BakingResult;
//?} else {
/*import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelIdentifier;
*///?}

public class ModelEvent {
   public static final class ModifyBakingResult {
      //? if >= 1.21.10 {
      private final BakingResult bakingResult;

      public ModifyBakingResult(BakingResult bakingResult) {
         this.bakingResult = bakingResult;
      }

      public BakingResult getBakingResult() {
         return this.bakingResult;
      }
      //?} else {
      /*// 1.21.1: there is no ModelBakery.BakingResult / ItemModel split. The whole baked output is one
      // Map<ModelIdentifier, BakedModel>; listeners replace an item's (id,"inventory") or a block's
      // blockstate entry with a dynamic BakedModel. The ModelManager mixin hands over a mutable copy.
      private final Map<ModelIdentifier, BakedModel> models;

      public ModifyBakingResult(Map<ModelIdentifier, BakedModel> models) {
         this.models = models;
      }

      public Map<ModelIdentifier, BakedModel> getModels() {
         return this.models;
      }
      *///?}
   }
}
