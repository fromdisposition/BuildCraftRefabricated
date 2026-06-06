package buildcraft.lib.client.model;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.model.json.JsonModel;
import buildcraft.lib.client.model.json.JsonModelPart;
import buildcraft.lib.client.model.json.JsonQuad;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class ModelHolderStatic extends ModelHolder {
   private final ImmutableMap<String, String> textureLookup;
   private final boolean allowTextureFallthrough;
   private MutableQuad[][] quads;
   private JsonModel rawModel;
   private boolean unseen = true;

   public ModelHolderStatic(String location) {
      this(location, ImmutableMap.of(), false);
   }

   public ModelHolderStatic(String location, String[][] textures, boolean allowTextureFallthrough) {
      this(location, genTextureMap(textures), allowTextureFallthrough);
   }

   public ModelHolderStatic(String modelLocation, ImmutableMap<String, String> textureLookup, boolean allowTextureFallthrough) {
      super(modelLocation);
      this.textureLookup = textureLookup;
      this.allowTextureFallthrough = allowTextureFallthrough;
   }

   @Override
   public boolean hasBakedQuads() {
      return this.quads != null;
   }

   private static ImmutableMap<String, String> genTextureMap(String[][] textures) {
      if (textures != null && textures.length != 0) {
         Builder<String, String> builder = ImmutableMap.builder();

         for (String[] ar : textures) {
            if (ar.length != 2) {
               throw new IllegalArgumentException("Must have 2 elements (key,value) but got " + Arrays.toString(ar));
            }

            if (!ar[0].startsWith("~")) {
               throw new IllegalArgumentException("Key must start with '~' otherwise it will never be used!");
            }

            builder.put(ar[0], ar[1]);
         }

         return builder.build();
      } else {
         return ImmutableMap.of();
      }
   }

   @Override
   protected void onTextureStitchPre(Set<Identifier> toRegisterSprites) {
      this.rawModel = null;
      this.quads = null;
      this.failReason = null;

      try {
         this.rawModel = JsonModel.deserialize(this.modelLocation);
      } catch (JsonParseException jse) {
         this.rawModel = null;
         this.failReason = "The model had errors: " + jse.getMessage();
         BCLog.logger.warn("[lib.model.holder] Failed to load the model " + this.modelLocation + " because " + jse.getMessage());
      } catch (IOException io) {
         this.rawModel = null;
         this.failReason = "The model did not exist in any resource pack: " + io.getMessage();
         BCLog.logger.warn("[lib.model.holder] Failed to load the model " + this.modelLocation + " because " + io.getMessage());
      }

      if (this.rawModel != null) {
         if (ModelHolderRegistry.DEBUG) {
            BCLog.logger.info("[lib.model.holder] The model " + this.modelLocation + " requires these sprites:");
         }

         for (Entry<String, String> entry : this.rawModel.textures.entrySet()) {
            String lookup = entry.getValue();
            if (!lookup.startsWith("#")) {
               if (lookup.startsWith("~") && this.textureLookup.containsKey(lookup)) {
                  lookup = (String)this.textureLookup.get(lookup);
               }

               if (lookup != null && !lookup.startsWith("#") && !lookup.startsWith("~")) {
                  toRegisterSprites.add(Identifier.parse(lookup));
               } else if (!this.allowTextureFallthrough) {
                  this.failReason = "The sprite lookup '" + lookup + "' did not exist in any of the maps";
                  this.rawModel = null;
                  break;
               }

               if (ModelHolderRegistry.DEBUG) {
                  BCLog.logger.info("[lib.model.holder]  - " + lookup);
               }
            }
         }
      }
   }

   @Override
   protected void onModelBake() {
      if (this.rawModel == null) {
         this.quads = null;
      } else {
         MutableQuad[] cut = this.bakePart(this.rawModel.cutoutElements);
         MutableQuad[] trans = this.bakePart(this.rawModel.translucentElements);
         this.quads = new MutableQuad[][]{cut, trans};
         this.rawModel = null;
      }
   }

   private MutableQuad[] bakePart(JsonModelPart[] a) {
      TextureAtlas atlas = (TextureAtlas)Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
      TextureAtlasSprite missingSprite = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
      List<MutableQuad> list = new ArrayList<>();

      for (JsonModelPart part : a) {
         for (JsonQuad quad : part.quads) {
            String lookup = quad.texture;

            for (int attempts = 0; lookup.startsWith("#") && this.rawModel.textures.containsKey(lookup) && attempts < 10; attempts++) {
               lookup = this.rawModel.textures.get(lookup);
            }

            if (lookup.startsWith("~") && this.textureLookup.containsKey(lookup)) {
               lookup = (String)this.textureLookup.get(lookup);
            }

            TextureAtlasSprite sprite;
            if (!lookup.startsWith("#") && !lookup.startsWith("~")) {
               sprite = atlas.getSprite(Identifier.parse(lookup));
            } else if (this.allowTextureFallthrough) {
               sprite = null;
            } else {
               sprite = missingSprite;
            }

            list.add(quad.toQuad(sprite));
         }
      }

      return list.toArray(new MutableQuad[list.size()]);
   }

   private MutableQuad[][] getQuadsChecking() {
      if (this.quads == null) {
         if (this.unseen) {
            this.unseen = false;
            String warnText = "[lib.model.holder] Tried to use the model " + this.modelLocation + " before it was baked!";
            if (ModelHolderRegistry.DEBUG) {
               BCLog.logger.warn(warnText, new Throwable());
            } else {
               BCLog.logger.warn(warnText);
            }
         }

         return new MutableQuad[][]{MutableQuad.EMPTY_ARRAY, MutableQuad.EMPTY_ARRAY};
      } else {
         return this.quads;
      }
   }

   public MutableQuad[] getCutoutQuads() {
      return this.getQuadsChecking()[0];
   }

   public MutableQuad[] getTranslucentQuads() {
      return this.getQuadsChecking()[1];
   }
}
