package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.BCSpriteSourcesFabric;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteSources.class)
public class SpriteSourcesBootstrapMixin {
   @Inject(method = "bootstrap", at = @At("RETURN"))
   private static void buildcraft$registerCustomSources(CallbackInfo ci) {
      BCSpriteSourcesFabric.registerInto(SpriteSourcesAccessor.buildcraft$mapper());
   }
}
