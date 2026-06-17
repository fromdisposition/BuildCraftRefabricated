package buildcraft.lib.platform.mixin.client;

import buildcraft.fabric.client.event.ModelEvent;
import buildcraft.lib.platform.client.FabricModelModifyHooks;
import java.util.Map;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelBakery.BakingResult;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fires {@link FabricModelModifyHooks} after the entire baking pass completes so that BC can
 * replace item and block-state models in batch (pipe models, facade, gate, lens).
 *
 * No Fabric API equivalent: {@code ModelLoadingPlugin.modifyModelAfterBake} fires per-model
 * and has no "after all models applied" stage. This mixin stays until upstream ships such a hook.
 * {@code require = 0} so it degrades silently if the injection point shifts between MC versions.
 */
@Mixin(ModelManager.class)
public class ModelManagerMixin {
   @Redirect(
      method = "apply(Lnet/minecraft/client/resources/model/ModelManager$ReloadState;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery$BakingResult;itemStackModels()Ljava/util/Map;")
   )
   private Map<Identifier, ItemModel> buildcraft$fireModifyBakingResult(BakingResult bakedModels) {
      FabricModelModifyHooks.fire(new ModelEvent.ModifyBakingResult(bakedModels));
      return bakedModels.itemStackModels();
   }
}
