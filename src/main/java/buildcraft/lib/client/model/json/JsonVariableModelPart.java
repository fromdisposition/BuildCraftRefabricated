/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.List;

public abstract class JsonVariableModelPart {
   public abstract void addQuads(List<MutableQuad> var1, JsonVariableModel.ITextureGetter var2);

   public static JsonVariableModelPart deserializeModelPart(JsonElement json, FunctionContext fnCtx, ResourceLoaderContext ctx) {
      if (!json.isJsonObject()) {
         throw new JsonSyntaxException("Expected an object, got " + json);
      }

      JsonObject obj = json.getAsJsonObject();
      String type = "cuboid";
      if (obj.has("type")) {
         JsonElement jType = obj.get("type");
         if (!jType.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected a string, got " + jType);
         }

         JsonPrimitive prim = jType.getAsJsonPrimitive();
         type = prim.getAsString();
      }

      if ("face".equals(type)) {
         return new VariablePartTextureExpand(obj, fnCtx);
      } else if ("led".equals(type)) {
         return new VariablePartLed(obj, fnCtx);
      } else if ("texture_expand".equals(type)) {
         return new VariablePartTextureExpand(obj, fnCtx);
      } else if ("cuboid".equals(type)) {
         return new VariablePartCuboid(obj, fnCtx);
      } else if ("container".equals(type)) {
         return new VariablePartContainer(obj, fnCtx, ctx);
      } else {
         throw new JsonSyntaxException("Unknown type '" + type + "' -- known types are [ face, led, texture_expand, cuboid, container ]");
      }
   }

   public static IExpressionNode.INodeDouble convertStringToDoubleNode(String expression, FunctionContext context) {
      try {
         return GenericExpressionCompiler.compileExpressionDouble(expression, context);
      } catch (InvalidExpressionException e) {
         throw new JsonSyntaxException("Invalid expression " + expression, e);
      }
   }

   public static IExpressionNode.INodeObject<String> convertStringToStringNode(String expression, FunctionContext context) {
      try {
         return GenericExpressionCompiler.compileExpressionString(expression, context);
      } catch (InvalidExpressionException e) {
         throw new JsonSyntaxException("Invalid expression " + expression, e);
      }
   }

   public static IExpressionNode.INodeBoolean convertStringToBooleanNode(String expression, FunctionContext context) {
      try {
         return GenericExpressionCompiler.compileExpressionBoolean(expression, context);
      } catch (InvalidExpressionException e) {
         throw new JsonSyntaxException("Invalid expression " + expression, e);
      }
   }

   public static IExpressionNode.INodeLong convertStringToLongNode(String expression, FunctionContext context) {
      try {
         return GenericExpressionCompiler.compileExpressionLong(expression, context);
      } catch (InvalidExpressionException e) {
         throw new JsonSyntaxException("Invalid expression " + expression, e);
      }
   }

   public static <T> IExpressionNode.INodeObject<T> convertStringToObjectNode(String expression, FunctionContext context, Class<T> clazz) {
      try {
         return GenericExpressionCompiler.compileExpressionObject(clazz, expression, context);
      } catch (InvalidExpressionException e) {
         throw new JsonSyntaxException("Invalid expression " + expression, e);
      }
   }

   public static IExpressionNode.INodeDouble[] readVariablePosition(JsonObject obj, String member, FunctionContext fnCtx) {
      String[] got = JsonUtil.getSubAsStringArray(obj, member);
      IExpressionNode.INodeDouble[] to = new IExpressionNode.INodeDouble[3];
      if (got.length != 3) {
         throw new JsonSyntaxException("Expected exactly 3 floats, but got " + Arrays.toString(got));
      }

      to[0] = convertStringToDoubleNode(got[0], fnCtx);
      to[1] = convertStringToDoubleNode(got[1], fnCtx);
      to[2] = convertStringToDoubleNode(got[2], fnCtx);
      return to;
   }

   public static IExpressionNode.INodeBoolean readVariableBoolean(JsonObject obj, String member, FunctionContext context) {
      if (!obj.has(member)) {
         throw new JsonSyntaxException("Required '" + member + "' in '" + obj + "'");
      } else {
         JsonElement elem = obj.get(member);
         if (elem.isJsonPrimitive()) {
            return convertStringToBooleanNode(elem.getAsString(), context);
         } else {
            throw new JsonSyntaxException("Expected a string, got " + elem);
         }
      }
   }

   public static IExpressionNode.INodeLong readVariableLong(JsonObject obj, String member, FunctionContext context) {
      if (!obj.has(member)) {
         throw new JsonSyntaxException("Required '" + member + "' in '" + obj + "'");
      } else {
         JsonElement elem = obj.get(member);
         if (elem.isJsonPrimitive()) {
            return convertStringToLongNode(elem.getAsString(), context);
         } else {
            throw new JsonSyntaxException("Expected a string, got " + elem);
         }
      }
   }

   public static IExpressionNode.INodeObject<String> readVariableString(JsonObject obj, String member, FunctionContext context) {
      if (!obj.has(member)) {
         throw new JsonSyntaxException("Required '" + member + "' in '" + obj + "'");
      } else {
         JsonElement elem = obj.get(member);
         if (elem.isJsonPrimitive()) {
            return convertStringToStringNode(elem.getAsString(), context);
         } else {
            throw new JsonSyntaxException("Expected a string, got " + elem);
         }
      }
   }

   public static float[] bakePosition(IExpressionNode.INodeDouble[] in) {
      float x = (float)in[0].evaluate() / 16.0F;
      float y = (float)in[1].evaluate() / 16.0F;
      float z = (float)in[2].evaluate() / 16.0F;
      return new float[]{x, y, z};
   }
}
