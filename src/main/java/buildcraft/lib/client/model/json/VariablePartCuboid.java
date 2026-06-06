package buildcraft.lib.client.model.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;

public class VariablePartCuboid extends VariablePartCuboidBase {
   public final Map<Direction, JsonVariableFaceUV> faces = new HashMap<>();

   public VariablePartCuboid(JsonObject obj, FunctionContext fnCtx) {
      super(obj, fnCtx);
      if (!obj.has("faces")) {
         throw new JsonSyntaxException("Expected between 1 and 6 faces, got nothing");
      }

      String invertDefault = null;
      if (obj.has("invert")) {
         invertDefault = JsonUtil.getAsString(obj.get("invert"));
      }

      String bothSides = null;
      if (obj.has("both_sides")) {
         bothSides = JsonUtil.getAsString(obj.get("both_sides"));
      }

      JsonElement elem = obj.get("faces");
      if (!elem.isJsonObject()) {
         throw new JsonSyntaxException("Expected between 1 and 6 faces, got '" + elem + "'");
      }

      JsonObject jFaces = elem.getAsJsonObject();

      for (Direction face : Direction.values()) {
         if (jFaces.has(face.getName())) {
            JsonElement jFace = jFaces.get(face.getName());
            if (!jFace.isJsonObject()) {
               throw new JsonSyntaxException("Expected an object, but got " + jFace);
            }

            JsonObject jFaceObj = jFace.getAsJsonObject();
            if (invertDefault != null && !jFaceObj.has("invert")) {
               jFaceObj.addProperty("invert", invertDefault);
            }

            if (bothSides != null && !jFaceObj.has("both_sides")) {
               jFaceObj.addProperty("both_sides", bothSides);
            }

            this.faces.put(face, new JsonVariableFaceUV(jFaceObj, fnCtx));
         }
      }

      if (this.faces.size() == 0) {
         throw new JsonSyntaxException("Expected between 1 and 6 faces, got an empty object " + jFaces);
      }
   }

   @Override
   protected VariablePartCuboidBase.VariableFaceData getFaceData(Direction side, JsonVariableModel.ITextureGetter spriteLookup) {
      JsonVariableFaceUV var = this.faces.get(side);
      return var != null && var.visible.evaluate() ? var.evaluate(spriteLookup) : null;
   }
}
