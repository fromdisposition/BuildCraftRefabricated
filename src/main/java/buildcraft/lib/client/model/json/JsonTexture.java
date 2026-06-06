package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.misc.MathUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.GsonHelper;

public class JsonTexture {
   public final String location;
   public final ModelUtil.UvFaceData faceData;

   public JsonTexture(String location, ModelUtil.UvFaceData faceData) {
      this.location = location;
      this.faceData = faceData;
   }

   public JsonTexture(String location, double minU, double minV, double maxU, double maxV) {
      this.location = location;
      this.faceData = new ModelUtil.UvFaceData(minU, minV, maxU, maxV);
   }

   public JsonTexture(String location) {
      this(location, 0.0, 0.0, 1.0, 1.0);
   }

   public JsonTexture(JsonObject obj) {
      try {
         this.location = GsonHelper.getAsString(obj, "location");
         JsonArray uvs = GsonHelper.getAsJsonArray(obj, "uv");
         if (uvs.size() != 4) {
            throw new JsonSyntaxException("Must have 4 elements (uMin, vMin, uMax, vMax)");
         }

         double[] arr = new double[4];

         for (int i = 0; i < 4; i++) {
            JsonElement elem = uvs.get(i);
            if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber()) {
               arr[i] = elem.getAsDouble();
            } else {
               if (!elem.isJsonPrimitive() || !elem.getAsJsonPrimitive().isString()) {
                  throw new JsonSyntaxException("Expected a number or a double expression!");
               }

               try {
                  arr[i] = GenericExpressionCompiler.compileExpressionDouble(elem.getAsString(), DefaultContexts.createWithAll()).evaluate();
               } catch (InvalidExpressionException e) {
                  throw new JsonSyntaxException("in " + elem, e);
               }
            }
         }

         this.faceData = new ModelUtil.UvFaceData();
         this.faceData.minU = (float)MathUtil.clamp(arr[0] / 16.0, 0.0, 1.0);
         this.faceData.minV = (float)MathUtil.clamp(arr[1] / 16.0, 0.0, 1.0);
         this.faceData.maxU = (float)MathUtil.clamp(arr[2] / 16.0, 0.0, 1.0);
         this.faceData.maxV = (float)MathUtil.clamp(arr[3] / 16.0, 0.0, 1.0);
      } catch (JsonSyntaxException jse) {
         throw new JsonSyntaxException("in " + obj, jse);
      }
   }

   public JsonTexture andSub(JsonTexture sub) {
      ModelUtil.UvFaceData data = this.faceData.andSub(sub.faceData);
      return new JsonTexture(this.location, data);
   }

   public JsonTexture inParent(JsonTexture parent) {
      return parent.andSub(this);
   }

   @Override
   public String toString() {
      return "location = " + this.location + ", uvs = " + this.faceData;
   }
}
