package buildcraft.lib.client.model.json;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.core.BCLog;
import buildcraft.lib.client.model.ModelHolderRegistry;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.json.JsonVariableObject;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public class JsonVariableModel extends JsonVariableObject {
   public final boolean ambientOcclusion;
   public final Map<String, JsonTexture> textures;
   public final JsonModelRule[] rules;
   public final JsonVariableModelPart[] cutoutElements;
   public final JsonVariableModelPart[] translucentElements;

   public static JsonVariableModel deserialize(Identifier from, FunctionContext fnCtx) throws JsonParseException, IOException {
      return deserialize(from, fnCtx, new ResourceLoaderContext());
   }

   public static JsonVariableModel deserialize(Identifier from, FunctionContext fnCtx, ResourceLoaderContext ctx) throws JsonParseException, IOException {
      try (InputStreamReader isr = ctx.startLoading(from)) {
         return new JsonVariableModel(JsonUtil.inlineCustom((JsonObject)new Gson().fromJson(isr, JsonObject.class)), fnCtx, ctx);
      } finally {
         ctx.finishLoading();
      }
   }

   static JsonVariableModelPart[] deserializePartArray(JsonObject json, String member, FunctionContext fnCtx, ResourceLoaderContext ctx, boolean require) {
      if (!json.has(member)) {
         if (require) {
            throw new JsonSyntaxException("Did not have '" + member + "' in '" + json + "'");
         } else {
            return new JsonVariableModelPart[0];
         }
      } else {
         JsonElement elem = json.get(member);
         if (!elem.isJsonArray()) {
            throw new JsonSyntaxException("Expected an array, got '" + elem + "'");
         }

         JsonArray array = elem.getAsJsonArray();
         JsonVariableModelPart[] to = new JsonVariableModelPart[array.size()];

         for (int i = 0; i < to.length; i++) {
            to[i] = JsonVariableModelPart.deserializeModelPart(array.get(i), fnCtx, ctx);
         }

         return to;
      }
   }

   public JsonVariableModel(JsonObject obj, FunctionContext fnCtx, ResourceLoaderContext ctx) throws JsonParseException {
      boolean ambf = false;
      this.textures = new HashMap<>();
      this.variables = new LinkedHashMap<>();
      List<JsonVariableModelPart> cutout = new ArrayList<>();
      List<JsonVariableModelPart> translucent = new ArrayList<>();
      List<JsonModelRule> rulesP = new ArrayList<>();
      if (obj.has("values")) {
         fnCtx = new FunctionContext(fnCtx);
         this.putVariables(GsonHelper.getAsJsonObject(obj, "values"), fnCtx);
      }

      if (obj.has("parent")) {
         String parentName = GsonHelper.getAsString(obj, "parent");
         parentName = parentName + ".json";
         Identifier from = Identifier.parse(parentName);

         JsonVariableModel parent;
         try {
            parent = deserialize(from, fnCtx, ctx);
         } catch (IOException e) {
            throw new JsonParseException("Didn't find the parent '" + parentName + "'!", e);
         }

         ambf = parent.ambientOcclusion;
         if (!GsonHelper.getAsBoolean(obj, "textures_reset", false)) {
            this.textures.putAll(parent.textures);
         }

         this.variables.putAll(parent.variables);
         if (!GsonHelper.getAsBoolean(obj, "cutout_replace", false)) {
            Collections.addAll(cutout, parent.cutoutElements);
         }

         if (!GsonHelper.getAsBoolean(obj, "translucent_replace", false)) {
            Collections.addAll(translucent, parent.translucentElements);
         }

         if (!GsonHelper.getAsBoolean(obj, "rules_replace", false)) {
            Collections.addAll(rulesP, parent.rules);
         }
      }

      this.ambientOcclusion = GsonHelper.getAsBoolean(obj, "ambientocclusion", ambf);
      this.deserializeTextures(obj.get("textures"));
      if (obj.has("variables")) {
         fnCtx = new FunctionContext(fnCtx);
         this.putVariables(GsonHelper.getAsJsonObject(obj, "variables"), fnCtx);
      }

      this.finaliseVariables();
      boolean require = cutout.isEmpty() && translucent.isEmpty();
      if (obj.has("elements")) {
         Collections.addAll(cutout, deserializePartArray(obj, "elements", fnCtx, ctx, require));
      } else {
         Collections.addAll(cutout, deserializePartArray(obj, "cutout", fnCtx, ctx, require));
         Collections.addAll(translucent, deserializePartArray(obj, "translucent", fnCtx, ctx, require));
      }

      this.cutoutElements = cutout.toArray(new JsonVariableModelPart[cutout.size()]);
      this.translucentElements = translucent.toArray(new JsonVariableModelPart[translucent.size()]);
      if (obj.has("rules")) {
         JsonElement elem = obj.get("rules");
         if (!elem.isJsonArray()) {
            throw new JsonSyntaxException("Expected an array, got " + elem + " for 'rules'");
         }

         JsonArray arr = elem.getAsJsonArray();

         for (int i = 0; i < arr.size(); i++) {
            rulesP.add(JsonModelRule.deserialize(arr.get(i), fnCtx, ctx));
         }
      }

      this.rules = rulesP.toArray(new JsonModelRule[rulesP.size()]);
   }

   public JsonVariableModel(JsonVariableModel from) {
      this.textures = new HashMap<>(from.textures);
      this.cutoutElements = from.cutoutElements;
      this.translucentElements = from.translucentElements;
      this.rules = from.rules;
      this.ambientOcclusion = from.ambientOcclusion;
   }

   public void onTextureStitchPre(Identifier modelLocation, Set<Identifier> toRegisterSprites) {
      if (ModelHolderRegistry.DEBUG) {
         BCLog.logger.info("[lib.model] The model " + modelLocation + " requires these sprites:");
      }

      for (Entry<String, JsonTexture> entry : this.textures.entrySet()) {
         JsonTexture lookup = entry.getValue();
         String location = lookup.location;
         if (!location.startsWith("#") && !location.startsWith("~")) {
            Identifier textureLoc = Identifier.parse(location);
            toRegisterSprites.add(textureLoc);
            if (ModelHolderRegistry.DEBUG) {
               BCLog.logger.info("[lib.model]  - " + location);
            }
         }
      }
   }

   private void deserializeTextures(JsonElement elem) {
      if (elem != null) {
         if (!elem.isJsonObject()) {
            throw new JsonSyntaxException("Expected to find an object for 'textures', but found " + elem);
         }

         JsonObject obj = elem.getAsJsonObject();

         for (Entry<String, JsonElement> entry : obj.entrySet()) {
            String name = entry.getKey();
            JsonElement tex = entry.getValue();
            JsonTexture texture;
            if (tex.isJsonPrimitive() && tex.getAsJsonPrimitive().isString()) {
               String location = tex.getAsString();
               texture = new JsonTexture(location);
            } else {
               if (!tex.isJsonObject()) {
                  throw new JsonSyntaxException("Expected a string or an object, but got " + tex);
               }

               texture = new JsonTexture(tex.getAsJsonObject());
            }

            this.textures.put(name, texture);
         }
      }
   }

   private ModelUtil.TexturedFace lookupTexture(String lookup) {
      int attempts = 0;

      JsonTexture texture;
      for (texture = new JsonTexture(lookup); texture.location.startsWith("#") && attempts < 10; attempts++) {
         JsonTexture tex = this.textures.get(texture.location);
         if (tex == null) {
            break;
         }

         texture = texture.inParent(tex);
      }

      lookup = texture.location;
      TextureAtlasSprite sprite = BcTextureAtlases.getBlockSprite(Identifier.parse(lookup));
      ModelUtil.TexturedFace face = new ModelUtil.TexturedFace();
      face.sprite = sprite;
      face.faceData = texture.faceData;
      return face;
   }

   public MutableQuad[] bakePart(JsonVariableModelPart[] a, JsonVariableModel.ITextureGetter spriteLookup) {
      List<MutableQuad> list = new ArrayList<>();

      for (JsonVariableModelPart part : a) {
         part.addQuads(list, spriteLookup);
      }

      for (JsonModelRule rule : this.rules) {
         if (rule.when.evaluate()) {
            rule.apply(list);
         }
      }

      return list.toArray(new MutableQuad[list.size()]);
   }

   public MutableQuad[] getCutoutQuads() {
      return this.bakePart(this.cutoutElements, this::lookupTexture);
   }

   public MutableQuad[] getTranslucentQuads() {
      return this.bakePart(this.translucentElements, this::lookupTexture);
   }

   public interface ITextureGetter {
      ModelUtil.TexturedFace get(String var1);
   }
}
