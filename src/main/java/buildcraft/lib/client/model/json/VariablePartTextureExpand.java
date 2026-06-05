package buildcraft.lib.client.model.json;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import net.minecraft.core.Direction;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraft.lib.client.model.json.VariablePartCuboidBase.VariableFaceData;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.misc.RenderUtil;

public class VariablePartTextureExpand extends JsonVariableModelPart {
    public final INodeDouble[] from;
    public final INodeDouble[] to;
    public final INodeBoolean visible;
    public final INodeBoolean shade;
    public final INodeLong light;
    public final INodeLong colour;
    public final INodeObject<String> face;
    public final JsonVariableFaceUV faceUv;
    private final Set<String> invalidFaceStrings = new HashSet<>();

    public VariablePartTextureExpand(JsonObject obj, FunctionContext fnCtx) {
        from = readVariablePosition(obj, "from", fnCtx);
        to = readVariablePosition(obj, "to", fnCtx);
        shade = obj.has("shade") ? readVariableBoolean(obj, "shade", fnCtx) : NodeConstantBoolean.TRUE;
        visible = obj.has("visible") ? readVariableBoolean(obj, "visible", fnCtx) : NodeConstantBoolean.TRUE;
        light = obj.has("light") ? readVariableLong(obj, "light", fnCtx) : new NodeConstantLong(0);
        colour = obj.has("colour") ? readVariableLong(obj, "colour", fnCtx) : new NodeConstantLong(-1);
        face = readVariableString(obj, "face", fnCtx);
        faceUv = new JsonVariableFaceUV(obj, fnCtx);
    }

    @Override
    public void addQuads(List<MutableQuad> addTo, ITextureGetter spriteLookup) {
        if (visible.evaluate()) {
            float[] f = bakePosition(from);
            float[] t = bakePosition(to);
            float sizeX = t[0] - f[0];
            float sizeY = t[1] - f[1];
            float sizeZ = t[2] - f[2];
            boolean s = shade.evaluate();
            int l = (int) (light.evaluate() & 15);
            int rgba = RenderUtil.swapARGBforABGR((int) colour.evaluate());

            VariableFaceData data = faceUv.evaluate(spriteLookup);
            Direction evalFace = evaluateFace(this.face);

            org.joml.Vector3f center = new org.joml.Vector3f(
                    f[0] + sizeX / 2, f[1] + sizeY / 2, f[2] + sizeZ / 2);
            org.joml.Vector3f radius = new org.joml.Vector3f(
                    sizeX / 2, sizeY / 2, sizeZ / 2);

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

    private Direction evaluateFace(INodeObject<String> node) {
        String s = node.evaluate();
        Direction side = Direction.byName(s);
        if (side == null) {
            if (invalidFaceStrings.add(s)) {
                BCLog.logger.warn("Invalid facing '" + s + "' from expression '" + node + "'");
            }
            return Direction.UP;
        } else {
            return side;
        }
    }
}
