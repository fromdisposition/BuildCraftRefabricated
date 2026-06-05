package buildcraft.lib.fabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import buildcraft.fabric.BCItemTintSourcesFabric;
import net.minecraft.client.color.item.ItemTintSources;

@Mixin(ItemTintSources.class)
public class ItemTintSourcesBootstrapMixin {
    @Inject(method = "bootstrap", at = @At("RETURN"))
    private static void buildcraft$registerCustomTints(CallbackInfo ci) {
        BCItemTintSourcesFabric.registerInto(ItemTintSources.ID_MAPPER);
    }
}
