package buildcraft.lib.client.model.json;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.lib.expression.FunctionContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class VariablePartLed extends VariablePartCuboidBase {
   private static final VariablePartCuboidBase.VariableFaceData FACE_DATA = new VariablePartCuboidBase.VariableFaceData();

   public VariablePartLed(JsonObject obj, FunctionContext fnCtx) {
      super(obj, fnCtx);
   }

   @Override
   protected VariablePartCuboidBase.VariableFaceData getFaceData(Direction side, JsonVariableModel.ITextureGetter spriteLookup) {
      if (FACE_DATA.sprite == null) {
         FACE_DATA.sprite = BcTextureAtlases.getBlockSprite(MissingTextureAtlasSprite.getLocation());
      }

      FACE_DATA.uvs.minU = 0.0625F;
      FACE_DATA.uvs.minV = 0.125F;
      FACE_DATA.uvs.maxU = 0.0625F;
      FACE_DATA.uvs.maxV = 0.125F;
      return FACE_DATA;
   }

   static {
      FACE_DATA.uvs.minU = 0.0625F;
      FACE_DATA.uvs.minV = 0.125F;
      FACE_DATA.uvs.maxU = 0.0625F;
      FACE_DATA.uvs.maxV = 0.125F;
   }
}
