/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.RenderUtil;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public class VariablePartTextureExpand extends JsonVariableModelPart {
   public final IExpressionNode.INodeDouble[] from;
   public final IExpressionNode.INodeDouble[] to;
   public final IExpressionNode.INodeBoolean visible;
   public final IExpressionNode.INodeBoolean shade;
   public final IExpressionNode.INodeLong light;
   public final IExpressionNode.INodeLong colour;
   public final IExpressionNode.INodeObject<String> face;
   public final JsonVariableFaceUV faceUv;
   private final Set<String> invalidFaceStrings = new HashSet<>();

   public VariablePartTextureExpand(JsonObject obj, FunctionContext fnCtx) {
      this.from = readVariablePosition(obj, "from", fnCtx);
      this.to = readVariablePosition(obj, "to", fnCtx);
      this.shade = obj.has("shade") ? readVariableBoolean(obj, "shade", fnCtx) : NodeConstantBoolean.TRUE;
      this.visible = obj.has("visible") ? readVariableBoolean(obj, "visible", fnCtx) : NodeConstantBoolean.TRUE;
      this.light = obj.has("light") ? readVariableLong(obj, "light", fnCtx) : new NodeConstantLong(0L);
      this.colour = obj.has("colour") ? readVariableLong(obj, "colour", fnCtx) : new NodeConstantLong(-1L);
      this.face = readVariableString(obj, "face", fnCtx);
      this.faceUv = new JsonVariableFaceUV(obj, fnCtx);
   }

   @Override
   public void addQuads(List<MutableQuad> addTo, JsonVariableModel.ITextureGetter spriteLookup) {
      if (this.visible.evaluate()) {
         float[] f = bakePosition(this.from);
         float[] t = bakePosition(this.to);
         float sizeX = t[0] - f[0];
         float sizeY = t[1] - f[1];
         float sizeZ = t[2] - f[2];
         boolean s = this.shade.evaluate();
         int l = (int)(this.light.evaluate() & 15L);
         int rgba = RenderUtil.swapARGBforABGR((int)this.colour.evaluate());
         VariablePartCuboidBase.VariableFaceData data = this.faceUv.evaluate(spriteLookup);
         Direction evalFace = this.evaluateFace(this.face);
         Vector3f center = new Vector3f(f[0] + sizeX / 2.0F, f[1] + sizeY / 2.0F, f[2] + sizeZ / 2.0F);
         Vector3f radius = new Vector3f(sizeX / 2.0F, sizeY / 2.0F, sizeZ / 2.0F);
         MutableQuad quad = ModelUtil.createFace(evalFace, center, radius, data.uvs);
         quad.texFromSprite(data.sprite);
         quad.setSprite(data.sprite);
         quad.rotateTextureUp(data.rotations);
         quad.setCalculatedNormal();
         quad.setShade(s);
         quad.lighti(l, 0);
         quad.colouri(rgba);
         if (data.bothSides) {
            addTo.add(quad.copyAndInvertNormal());
         } else if (data.invertNormal) {
            quad = quad.copyAndInvertNormal();
         }

         addTo.add(quad);
      }
   }

   private Direction evaluateFace(IExpressionNode.INodeObject<String> node) {
      String s = node.evaluate();
      Direction side = Direction.byName(s);
      if (side == null) {
         if (this.invalidFaceStrings.add(s)) {
            BCLog.logger.warn("Invalid facing '" + s + "' from expression '" + node + "'");
         }

         return Direction.UP;
      } else {
         return side;
      }
   }
}
