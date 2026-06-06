package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.misc.ExpressionCompat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public abstract class JsonModelRule {
   public final IExpressionNode.INodeBoolean when;

   public JsonModelRule(IExpressionNode.INodeBoolean when) {
      this.when = when;
   }

   public static JsonModelRule deserialize(JsonElement json, FunctionContext fnCtx, ResourceLoaderContext ctx) {
      if (!json.isJsonObject()) {
         throw new JsonSyntaxException("Expected an object, got " + json);
      }

      JsonObject obj = json.getAsJsonObject();
      String when = GsonHelper.getAsString(obj, "when");
      IExpressionNode.INodeBoolean nodeWhen = JsonVariableModelPart.convertStringToBooleanNode(when, fnCtx);
      String type = GsonHelper.getAsString(obj, "type");
      if (type.startsWith("builtin:")) {
         String builtin = type.substring("builtin:".length());
         if ("rotate_facing".equals(builtin)) {
            fnCtx = new FunctionContext(fnCtx, ExpressionCompat.ENUM_FACING);
            String from = GsonHelper.getAsString(obj, "from");
            IExpressionNode.INodeObject<Direction> nodeFrom = JsonVariableModelPart.convertStringToObjectNode(from, fnCtx, Direction.class);
            String to = GsonHelper.getAsString(obj, "to");
            IExpressionNode.INodeObject<Direction> nodeTo = JsonVariableModelPart.convertStringToObjectNode(to, fnCtx, Direction.class);
            IExpressionNode.INodeDouble[] origin;
            if (obj.has("origin")) {
               origin = JsonVariableModelPart.readVariablePosition(obj, "origin", fnCtx);
            } else {
               origin = JsonModelRule.RuleRotateFacing.DEFAULT_ORIGIN;
            }

            return new JsonModelRule.RuleRotateFacing(nodeWhen, nodeFrom, nodeTo, origin);
         } else if ("rotate".equals(builtin)) {
            IExpressionNode.INodeDouble[] origin;
            if (obj.has("origin")) {
               origin = JsonVariableModelPart.readVariablePosition(obj, "origin", fnCtx);
            } else {
               origin = JsonModelRule.RuleRotate.DEFAULT_ORIGIN;
            }

            IExpressionNode.INodeDouble[] angles = JsonVariableModelPart.readVariablePosition(obj, "angle", fnCtx);
            return new JsonModelRule.RuleRotate(nodeWhen, origin, angles);
         } else if ("scale".equals(builtin)) {
            IExpressionNode.INodeDouble[] origin;
            if (obj.has("origin")) {
               origin = JsonVariableModelPart.readVariablePosition(obj, "origin", fnCtx);
            } else {
               origin = JsonModelRule.RuleRotate.DEFAULT_ORIGIN;
            }

            IExpressionNode.INodeDouble[] scales = JsonVariableModelPart.readVariablePosition(obj, "scale", fnCtx);
            return new JsonModelRule.RuleScale(nodeWhen, origin, scales);
         } else {
            throw new JsonSyntaxException("Unknown built in rule type '" + builtin + "'");
         }
      } else {
         throw new JsonSyntaxException("Unknown rule type '" + type + "'");
      }
   }

   public abstract void apply(List<MutableQuad> var1);

   public static class RuleRotate extends JsonModelRule {
      private static final NodeConstantDouble CONST_ORIGIN = new NodeConstantDouble(0.5);
      public static final IExpressionNode.INodeDouble[] DEFAULT_ORIGIN = new IExpressionNode.INodeDouble[]{CONST_ORIGIN, CONST_ORIGIN, CONST_ORIGIN};
      public final IExpressionNode.INodeDouble[] origin;
      public final IExpressionNode.INodeDouble[] angle;

      public RuleRotate(IExpressionNode.INodeBoolean when, IExpressionNode.INodeDouble[] origin, IExpressionNode.INodeDouble[] angle) {
         super(when);
         this.origin = origin;
         this.angle = angle;
      }

      @Override
      public void apply(List<MutableQuad> quads) {
         float ox = (float)this.origin[0].evaluate() / 16.0F;
         float oy = (float)this.origin[1].evaluate() / 16.0F;
         float oz = (float)this.origin[2].evaluate() / 16.0F;
         float ax = (float)Math.toRadians(this.angle[0].evaluate());
         float ay = (float)Math.toRadians(this.angle[1].evaluate());
         float az = (float)Math.toRadians(this.angle[2].evaluate());
         if (ax != 0.0F || ay != 0.0F || az != 0.0F) {
            float cx = Mth.cos(ax);
            float cy = Mth.cos(ay);
            float cz = Mth.cos(az);
            float sx = Mth.sin(ax);
            float sy = Mth.sin(ay);
            float sz = Mth.sin(az);

            for (MutableQuad q : quads) {
               q.translatef(-ox, -oy, -oz);
               if (cx != 1.0F) {
                  q.rotateDirectlyX(cx, sx);
               }

               if (cy != 1.0F) {
                  q.rotateDirectlyY(cy, sy);
               }

               if (cz != 1.0F) {
                  q.rotateDirectlyZ(cz, sz);
               }

               q.translatef(ox, oy, oz);
            }
         }
      }
   }

   public static class RuleRotateFacing extends JsonModelRule {
      private static final NodeConstantDouble CONST_ORIGIN = new NodeConstantDouble(8.0);
      public static final IExpressionNode.INodeDouble[] DEFAULT_ORIGIN = new IExpressionNode.INodeDouble[]{CONST_ORIGIN, CONST_ORIGIN, CONST_ORIGIN};
      public final IExpressionNode.INodeObject<Direction> from;
      public final IExpressionNode.INodeObject<Direction> to;
      public final IExpressionNode.INodeDouble[] origin;

      public RuleRotateFacing(
         IExpressionNode.INodeBoolean when,
         IExpressionNode.INodeObject<Direction> from,
         IExpressionNode.INodeObject<Direction> to,
         IExpressionNode.INodeDouble[] origin
      ) {
         super(when);
         this.from = from;
         this.to = to;
         this.origin = origin;
      }

      @Override
      public void apply(List<MutableQuad> quads) {
         Direction faceFrom = this.from.evaluate();
         Direction faceTo = this.to.evaluate();
         if (faceFrom != faceTo) {
            float ox = (float)this.origin[0].evaluate() / 16.0F;
            float oy = (float)this.origin[1].evaluate() / 16.0F;
            float oz = (float)this.origin[2].evaluate() / 16.0F;

            for (MutableQuad q : quads) {
               q.rotate(faceFrom, faceTo, ox, oy, oz);
            }
         }
      }
   }

   public static class RuleScale extends JsonModelRule {
      private static final NodeConstantDouble CONST_ORIGIN = new NodeConstantDouble(0.5);
      public static final IExpressionNode.INodeDouble[] DEFAULT_ORIGIN = new IExpressionNode.INodeDouble[]{CONST_ORIGIN, CONST_ORIGIN, CONST_ORIGIN};
      public final IExpressionNode.INodeDouble[] origin;
      public final IExpressionNode.INodeDouble[] scale;

      public RuleScale(IExpressionNode.INodeBoolean when, IExpressionNode.INodeDouble[] origin, IExpressionNode.INodeDouble[] scale) {
         super(when);
         this.origin = origin;
         this.scale = scale;
      }

      @Override
      public void apply(List<MutableQuad> quads) {
         float ox = (float)this.origin[0].evaluate() / 16.0F;
         float oy = (float)this.origin[1].evaluate() / 16.0F;
         float oz = (float)this.origin[2].evaluate() / 16.0F;
         float sx = (float)this.scale[0].evaluate();
         float sy = (float)this.scale[1].evaluate();
         float sz = (float)this.scale[2].evaluate();
         if (sx != 1.0F || sy != 1.0F || sz != 1.0F) {
            for (MutableQuad q : quads) {
               q.translatef(-ox, -oy, -oz);
               q.scalef(sx, sy, sz);
               q.translatef(ox, oy, oz);
            }
         }
      }
   }
}
