package buildcraft.lib.fabric.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import buildcraft.fabric.client.event.ModelEvent;
import buildcraft.lib.fabric.client.FabricModelModifyHooks;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    @Redirect(
            method = "apply(Lnet/minecraft/client/resources/model/ModelManager$ReloadState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/ModelBakery$BakingResult;itemStackModels()Ljava/util/Map;"))
    private Map<Identifier, ItemModel> buildcraft$fireModifyBakingResult(ModelBakery.BakingResult bakedModels) {
        FabricModelModifyHooks.fire(new ModelEvent.ModifyBakingResult(bakedModels));
        return bakedModels.itemStackModels();
    }
}

