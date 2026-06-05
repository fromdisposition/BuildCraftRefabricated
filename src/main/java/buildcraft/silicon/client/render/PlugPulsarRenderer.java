package buildcraft.silicon.client.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.silicon.plug.PluggablePulsar;
import buildcraft.transport.client.model.PipeModelCacheAll;

public enum PlugPulsarRenderer implements IPlugDynamicRenderer<PluggablePulsar> {
    INSTANCE;

    private static final float STAGE_MAX = 20.0f;

    private static List<MutableQuad> offBox;
    private static List<MutableQuad> onBox;

    private static List<MutableQuad> autoLeds;
    private static List<MutableQuad> manualLeds;

    public static void initCache() {
        if (offBox != null) return;
        offBox = new ArrayList<>();
        onBox = new ArrayList<>();
        autoLeds = new ArrayList<>();
        manualLeds = new ArrayList<>();

        TextureAtlasSprite offSprite = SpriteUtil.getSprite("buildcraftsilicon:block/plugs/pulsar_dynamic_off");
        TextureAtlasSprite onSprite = SpriteUtil.getSprite("buildcraftsilicon:block/plugs/pulsar_dynamic_on");

        ModelUtil.UvFaceData[] offUvs = new ModelUtil.UvFaceData[] {
            ModelUtil.UvFaceData.from16(4f, 6f, 6f, 10f),
            ModelUtil.UvFaceData.from16(4f, 6f, 6f, 10f),
            ModelUtil.UvFaceData.from16(4f, 6f, 6f, 10f),
            ModelUtil.UvFaceData.from16(4f, 6f, 6f, 10f),
            ModelUtil.UvFaceData.from16(6f, 6f, 10f, 10f),
            ModelUtil.UvFaceData.from16(6f, 6f, 10f, 10f)
        };
        addBox(offBox, offSprite, 0, 6f/16f, 6f/16f, 2f/16f, 10f/16f, 10f/16f, offUvs);

        ModelUtil.UvFaceData[] onUvs = new ModelUtil.UvFaceData[] {
            ModelUtil.UvFaceData.from16(4f, 6f, 6f, 10f),
            ModelUtil.UvFaceData.from16(4f, 6f, 6f, 10f),
            ModelUtil.UvFaceData.from16(4f, 6f, 6f, 10f),
            ModelUtil.UvFaceData.from16(4f, 6f, 6f, 10f),
            ModelUtil.UvFaceData.from16(6f, 6f, 10f, 10f),
            ModelUtil.UvFaceData.from16(6f, 6f, 10f, 10f)
        };
        addBox(onBox, onSprite, 0, 6f/16f, 6f/16f, 2f/16f, 10f/16f, 10f/16f, onUvs);

        TextureAtlasSprite blankSprite = SpriteUtil.getSprite("minecraft:block/white_concrete");

        addBox(autoLeds, blankSprite, 2.5f/16f, 6.5f/16f, 4.9f/16f, 3.5f/16f, 7.5f/16f, 5f/16f, null);
        addBox(autoLeds, blankSprite, 2.5f/16f, 8.5f/16f, 11f/16f, 3.5f/16f, 9.5f/16f, 11.1f/16f, null);
        addBox(autoLeds, blankSprite, 2.5f/16f, 4.9f/16f, 8.5f/16f, 3.5f/16f, 5f/16f, 9.5f/16f, null);
        addBox(autoLeds, blankSprite, 2.5f/16f, 11f/16f, 6.5f/16f, 3.5f/16f, 11.1f/16f, 7.5f/16f, null);

        addBox(manualLeds, blankSprite, 2.5f/16f, 8.5f/16f, 4.9f/16f, 3.5f/16f, 9.5f/16f, 5f/16f, null);
        addBox(manualLeds, blankSprite, 2.5f/16f, 6.5f/16f, 11f/16f, 3.5f/16f, 7.5f/16f, 11.1f/16f, null);
        addBox(manualLeds, blankSprite, 2.5f/16f, 4.9f/16f, 6.5f/16f, 3.5f/16f, 5f/16f, 7.5f/16f, null);
        addBox(manualLeds, blankSprite, 2.5f/16f, 11f/16f, 8.5f/16f, 3.5f/16f, 11.1f/16f, 9.5f/16f, null);
    }

    private static void addBox(List<MutableQuad> quads, TextureAtlasSprite sprite,
            float x0, float y0, float z0, float x1, float y1, float z1, ModelUtil.UvFaceData[] faceUvs) {
        Vector3f center = new Vector3f((x0 + x1) / 2f, (y0 + y1) / 2f, (z0 + z1) / 2f);
        Vector3f radius = new Vector3f((x1 - x0) / 2f, (y1 - y0) / 2f, (z1 - z0) / 2f);

        for (Direction face : Direction.values()) {
            ModelUtil.UvFaceData uvs = faceUvs != null ? faceUvs[face.ordinal()] : new ModelUtil.UvFaceData();
            if (faceUvs == null) {
                ModelUtil.mapBoxToUvs(new net.minecraft.world.phys.AABB(x0, y0, z0, x1, y1, z1), face, uvs);
            }
            MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
            q.setSprite(sprite);
            q.vertex_0.texFromSprite(sprite);
            q.vertex_1.texFromSprite(sprite);
            q.vertex_2.texFromSprite(sprite);
            q.vertex_3.texFromSprite(sprite);
            quads.add(q);
        }
    }

    @Override
    public void render(PluggablePulsar plug, double x, double y, double z, float partialTicks, VertexConsumer bb, PoseStack ps) {
        initCache();
        boolean on = plug.getIsPulsingClient();
        int stage = plug.getPulseStageClient();
        float fraction = (stage + (on ? partialTicks : 0)) / STAGE_MAX;
        if (fraction > 1f) fraction = 1f;

        float mirroredStage = fraction > 0.5f ? 1f - fraction : fraction;
        float mirroredPos = 2f * mirroredStage;
        float posDiff = (1f - mirroredPos) * 2f - 0.001f;

        ps.pushPose();
        ps.translate(x, y, z);

        ps.translate(0.5f, 0.5f, 0.5f);
        Direction side = plug.side;
        switch (side) {
            case EAST -> ps.mulPose(Axis.YP.rotationDegrees(180));
            case NORTH -> ps.mulPose(Axis.YP.rotationDegrees(-90));
            case SOUTH -> ps.mulPose(Axis.YP.rotationDegrees(90));
            case DOWN -> ps.mulPose(Axis.ZP.rotationDegrees(90));
            case UP -> ps.mulPose(Axis.ZP.rotationDegrees(-90));
            case WEST -> {}
        }
        ps.translate(-0.5f, -0.5f, -0.5f);

        ps.pushPose();
        ps.translate(posDiff / 16f, 0, 0);
        List<MutableQuad> pistonBox = on ? onBox : offBox;
        for (MutableQuad q : pistonBox) {
            MutableQuad mq = new MutableQuad(q);
            if (on) mq.lighti(15, 15);
            mq.render(ps.last(), bb);
        }
        ps.popPose();

        boolean autoOn = plug.getAutoEnabledClient() && on;
        int autoR = autoOn ? 0x99 : 0x22;
        int autoG = autoOn ? 0xFF : 0x22;
        int autoB = autoOn ? 0x99 : 0x22;
        for (MutableQuad q : autoLeds) {
            MutableQuad mq = new MutableQuad(q);
            mq.multColouri(autoR, autoG, autoB, 255);
            if (autoOn) mq.lighti(15, 15);
            mq.render(ps.last(), bb);
        }

        boolean manOn = plug.getManuallyEnabledClient();
        int manR = manOn ? 0x99 : 0x22;
        int manG = manOn ? 0xFF : 0x22;
        int manB = manOn ? 0x99 : 0x22;
        for (MutableQuad q : manualLeds) {
            MutableQuad mq = new MutableQuad(q);
            mq.multColouri(manR, manG, manB, 255);
            if (manOn) mq.lighti(15, 15);
            mq.render(ps.last(), bb);
        }

        ps.popPose();
    }
}
