package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.client.event.ModelEvent;
import buildcraft.lib.fabric.client.FabricModelModifyHooks;
import java.util.Map;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelBakery.BakingResult;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
