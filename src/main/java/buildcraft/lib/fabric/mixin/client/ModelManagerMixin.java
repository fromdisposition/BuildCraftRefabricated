package buildcraft.lib.fabric.mixin.client;

//? if >= 1.21.10 {
import buildcraft.fabric.client.event.ModelEvent;
import buildcraft.lib.fabric.client.FabricModelModifyHooks;
import java.util.Map;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBakery.BakingResult;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
//?} else {
/*import buildcraft.fabric.client.event.ModelEvent;
import buildcraft.lib.fabric.client.FabricModelModifyHooks;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelIdentifier;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
*///?}
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;

// Redirects the post-bake model map so listeners can batch-swap entries (facade, gate, lens); FacadeDeduplicator
// needs the whole block-state map at once, which Fabric's per-model AfterBakeBlock API cannot provide.
@Mixin(ModelManager.class)
public class ModelManagerMixin {
   //? if >= 1.21.10 {
   @Redirect(
      method = "apply(Lnet/minecraft/client/resources/model/ModelManager$ReloadState;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery$BakingResult;itemStackModels()Ljava/util/Map;")
   )
   private Map<Identifier, ItemModel> buildcraft$fireModifyBakingResult(BakingResult bakedModels) {
      FabricModelModifyHooks.fire(new ModelEvent.ModifyBakingResult(bakedModels));
      return bakedModels.itemStackModels();
   }
   //?} else {
   /*@Redirect(
      method = "apply(Lnet/minecraft/client/resources/model/ModelManager$ReloadState;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;getBakedTopLevelModels()Ljava/util/Map;")
   )
   private Map<ModelIdentifier, BakedModel> buildcraft$fireModifyBakingResult(ModelBakery bakery) {
      Map<ModelIdentifier, BakedModel> models = new HashMap<>(bakery.getBakedTopLevelModels());
      FabricModelModifyHooks.fire(new ModelEvent.ModifyBakingResult(models));
      return models;
   }
   *///?}
}
