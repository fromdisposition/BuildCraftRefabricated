/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.mixin.client;

//? if < 1.21.10 {
/*import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/^*
 * 1.21.1 only (registered via BCMixinPlugin.getMixins for &lt; 1.21.10). Vanilla's bulk model pre-load
 * (ModelManager.loadBlockModels, driven by MODEL_LISTER) parses every assets/&lt;ns&gt;/models/^*.json with the
 * vanilla BlockModel deserializer — including BuildCraft's expression-based "buildcraftlib:variable" engine
 * models (models/compat/engine_*.json), whose "from"/"to" arrays hold strings like "4 + progress_size". Those
 * throw "Expected from[1] to be a Float" and spam "Failed to load model ...engine..." at every resource reload.
 * The models are never baked by vanilla; only the engine BER reads them, via ModelHolderVariable.
 *
 * <p>On 1.21.10+ a Fabric {@code UnbakedModelDeserializer} (see {@link buildcraft.lib.client.model.VariableModelDeserializer})
 * handles the "fabric:type" marker during this same phase. 1.21.1's Fabric (model-loading v1 2.1.0) only offers
 * a {@code ModelResolver}, which runs later (ModelBakery.getOrLoadModel) and so cannot stop the bulk pre-parse.
 * This mixin restores the equivalent behaviour natively: it detects the marker on the raw JSON before any
 * "from"/"to" parsing and returns an empty placeholder model, so vanilla never chokes on the expression arrays.
 ^/
@Mixin(BlockModel.Deserializer.class)
public class BlockModelVariableSkipMixin {
   @Inject(
      method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;",
      at = @At("HEAD"),
      cancellable = true
   )
   private void buildcraft$skipVariableModels(JsonElement json, Type type, JsonDeserializationContext ctx, CallbackInfoReturnable<BlockModel> cir) {
      if (json.isJsonObject()) {
         JsonElement marker = json.getAsJsonObject().get("fabric:type");
         if (marker != null && marker.isJsonPrimitive() && "buildcraftlib:variable".equals(marker.getAsString())) {
            cir.setReturnValue(new BlockModel(null, List.of(), Map.of(), null, null, ItemTransforms.NO_TRANSFORMS, List.of()));
         }
      }
   }
}
*///?}
