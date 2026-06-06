package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;

public class JsonVariableFaceUV {
   final IExpressionNode.INodeDouble[] uv;
   final IExpressionNode.INodeLong textureRotation;
   final IExpressionNode.INodeBoolean visible;
   final IExpressionNode.INodeBoolean invert;
   final IExpressionNode.INodeBoolean bothSides;
   final IExpressionNode.INodeObject<String> texture;

   public JsonVariableFaceUV(JsonObject json, FunctionContext fnCtx) {
      this.uv = readVariableUV(json, "uv", fnCtx);
      if (json.has("visible")) {
         this.visible = JsonVariableModelPart.readVariableBoolean(json, "visible", fnCtx);
      } else {
         this.visible = NodeConstantBoolean.TRUE;
      }

      if (json.has("invert")) {
         this.invert = JsonVariableModelPart.readVariableBoolean(json, "invert", fnCtx);
      } else {
         this.invert = NodeConstantBoolean.FALSE;
      }

      if (json.has("both_sides")) {
         this.bothSides = JsonVariableModelPart.readVariableBoolean(json, "both_sides", fnCtx);
      } else {
         this.bothSides = NodeConstantBoolean.FALSE;
      }

      this.texture = readVariableString(json, "texture", fnCtx);
      if (json.has("rotation")) {
         this.textureRotation = JsonVariableModelPart.readVariableLong(json, "rotation", fnCtx);
      } else {
         this.textureRotation = NodeConstantLong.ZERO;
      }
   }

   private static IExpressionNode.INodeObject<String> readVariableString(JsonObject json, String member, FunctionContext fnCtx) {
      if (!json.has(member)) {
         throw new JsonSyntaxException("Required member " + member + " in '" + json + "'");
      }

      JsonElement elem = json.get(member);
      if (!elem.isJsonPrimitive()) {
         throw new JsonSyntaxException("Expected a string, but got '" + json + "'");
      }

      String asString = elem.getAsString();
      if (asString.startsWith("#")) {
         asString = "'" + asString + "'";
      }

      return JsonVariableModelPart.convertStringToStringNode(asString, fnCtx);
   }

   public static IExpressionNode.INodeDouble[] readVariableUV(JsonObject obj, String member, FunctionContext fnCtx) {
      String[] got = JsonUtil.getSubAsStringArray(obj, member);
      IExpressionNode.INodeDouble[] to = new IExpressionNode.INodeDouble[4];
      if (got.length != 4) {
         throw new JsonSyntaxException("Expected exactly 4 doubles, but got " + Arrays.toString(got));
      }

      to[0] = JsonVariableModelPart.convertStringToDoubleNode(got[0], fnCtx);
      to[1] = JsonVariableModelPart.convertStringToDoubleNode(got[1], fnCtx);
      to[2] = JsonVariableModelPart.convertStringToDoubleNode(got[2], fnCtx);
      to[3] = JsonVariableModelPart.convertStringToDoubleNode(got[3], fnCtx);
      return to;
   }

   public VariablePartCuboidBase.VariableFaceData evaluate(JsonVariableModel.ITextureGetter spriteLookup) {
      VariablePartCuboidBase.VariableFaceData data = new VariablePartCuboidBase.VariableFaceData();
      ModelUtil.TexturedFace face = spriteLookup.get(this.texture.evaluate());
      data.sprite = face.sprite;
      data.rotations = (int)this.textureRotation.evaluate();
      data.uvs.minU = (float)(this.uv[0].evaluate() / 16.0);
      data.uvs.minV = (float)(this.uv[1].evaluate() / 16.0);
      data.uvs.maxU = (float)(this.uv[2].evaluate() / 16.0);
      data.uvs.maxV = (float)(this.uv[3].evaluate() / 16.0);
      data.uvs = data.uvs.inParent(face.faceData);
      data.invertNormal = this.invert.evaluate();
      data.bothSides = this.bothSides.evaluate();
      return data;
   }
}
