package buildcraft.lib.client.model;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.core.BCLog;
import buildcraft.lib.client.model.json.JsonTexture;
import buildcraft.lib.client.model.json.JsonVariableModel;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.ITickableNode;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class ModelHolderVariable extends ModelHolder {
   public final Map<String, TextureAtlasSprite> customSprites = new HashMap<>();
   private final FunctionContext context;
   private JsonVariableModel rawModel;
   private boolean unseen = true;
   private boolean loadAttempted = false;

   public ModelHolderVariable(String modelLocation, FunctionContext context) {
      super(modelLocation);
      this.context = context;
   }

   @Override
   public boolean hasBakedQuads() {
      return this.rawModel != null;
   }

   @Override
   protected void onTextureStitchPre(Set<Identifier> toRegisterSprites) {
      this.rawModel = null;
      this.failReason = null;
      this.loadAttempted = false;
      this.loadModelFromDisk();
      if (this.rawModel != null) {
         this.rawModel.onTextureStitchPre(this.modelLocation, toRegisterSprites);
      }
   }

   private void ensureLoaded() {
      if (this.rawModel == null && !this.loadAttempted) {
         this.loadModelFromDisk();
      }
   }

   private void loadModelFromDisk() {
      this.loadAttempted = true;

      try {
         this.rawModel = JsonVariableModel.deserialize(this.modelLocation, this.context);
         BCLog.logger.info("[lib.model.holder] Successfully loaded variable model " + this.modelLocation);
      } catch (JsonParseException jse) {
         this.rawModel = null;
         this.failReason = "The model had errors: " + jse.getMessage();
         BCLog.logger.warn("[lib.model.holder] Failed to load the model " + this.modelLocation + " because ", jse);
      } catch (IOException io) {
         this.rawModel = null;
         this.failReason = "The model did not exist in any resource pack: " + io.getMessage();
         BCLog.logger.warn("[lib.model.holder] Failed to load the model " + this.modelLocation + " because ", io);
      }
   }

   @Override
   protected void onModelBake() {
   }

   private ModelUtil.TexturedFace lookupTexture(String lookup) {
      int attempts = 0;

      JsonTexture texture;
      for (texture = new JsonTexture(lookup); texture.location.startsWith("#") && attempts < 10; attempts++) {
         JsonTexture tex = this.rawModel.textures.get(texture.location);
         if (tex == null) {
            break;
         }

         texture = texture.inParent(tex);
      }

      lookup = texture.location;
      TextureAtlasSprite sprite;
      if (lookup.startsWith("~")) {
         sprite = this.customSprites.get(lookup.substring(1));
         if (sprite == null) {
            sprite = BcTextureAtlases.getBlockSprite(MissingTextureAtlasSprite.getLocation());
         }
      } else {
         sprite = BcTextureAtlases.getBlockSprite(Identifier.parse(lookup));
      }

      ModelUtil.TexturedFace face = new ModelUtil.TexturedFace();
      face.sprite = sprite;
      face.faceData = texture.faceData;
      return face;
   }

   private void printNoModelWarning() {
      if (this.unseen) {
         this.unseen = false;
         String warnText = "[lib.model.holder] Tried to use the model " + this.modelLocation + " but it failed to load!";
         if (this.failReason != null) {
            warnText = warnText + " Reason: " + this.failReason;
         }

         if (ModelHolderRegistry.DEBUG) {
            BCLog.logger.warn(warnText, new Throwable());
         } else {
            BCLog.logger.warn(warnText);
         }
      }
   }

   @Nullable
   public JsonVariableModel getModel() {
      this.ensureLoaded();
      if (this.rawModel == null) {
         this.printNoModelWarning();
      }

      return this.rawModel;
   }

   public ITickableNode[] createTickableNodes() {
      this.ensureLoaded();
      if (this.rawModel == null) {
         this.printNoModelWarning();
         return new ITickableNode[0];
      } else {
         return this.rawModel.createTickableNodes();
      }
   }

   public MutableQuad[] getCutoutQuads() {
      this.ensureLoaded();
      if (this.rawModel == null) {
         this.printNoModelWarning();
         return MutableQuad.EMPTY_ARRAY;
      } else {
         return this.rawModel.bakePart(this.rawModel.cutoutElements, this::lookupTexture);
      }
   }

   public MutableQuad[] getTranslucentQuads() {
      this.ensureLoaded();
      if (this.rawModel == null) {
         this.printNoModelWarning();
         return MutableQuad.EMPTY_ARRAY;
      } else {
         return this.rawModel.bakePart(this.rawModel.translucentElements, this::lookupTexture);
      }
   }
}
