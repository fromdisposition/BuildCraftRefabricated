package buildcraft.lib.fabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
//? if >= 1.21.10 {
import buildcraft.fabric.BCItemTintSourcesFabric;
import net.minecraft.client.color.item.ItemTintSources;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemTintSources.class)
public class ItemTintSourcesBootstrapMixin {
   @Inject(method = "bootstrap", at = @At("RETURN"))
   private static void buildcraft$registerCustomTints(CallbackInfo ci) {
      BCItemTintSourcesFabric.registerInto(ItemTintSources.ID_MAPPER);
   }
}
//?} else {
/*// 1.21.1 has no ItemTintSources class; this mixin targets a harmless class and does nothing
// (BuildCraft item tints are a 1.21.5+ data-driven feature, a cosmetic-only loss on 1.21.1).
@Mixin(net.minecraft.client.Minecraft.class)
public class ItemTintSourcesBootstrapMixin {
}
*///?}
