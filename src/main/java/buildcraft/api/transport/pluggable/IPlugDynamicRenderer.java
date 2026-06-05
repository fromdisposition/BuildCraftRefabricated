package buildcraft.api.transport.pluggable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IPlugDynamicRenderer<P extends PipePluggable> {
    void render(P plug, double x, double y, double z, float partialTicks, VertexConsumer bb, PoseStack poseStack);
}
