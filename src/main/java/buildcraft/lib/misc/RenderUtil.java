package buildcraft.lib.misc;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.AABB;
public class RenderUtil {
    public static class AutoTessellator implements AutoCloseable {
        public Object tessellator;
        @Override public void close() {}
    }
    public static AutoTessellator getThreadLocalUnusedTessellator() { return new AutoTessellator(); }
    public static void drawAABB(AABB box, VertexConsumer bb) {}

    public static int swapARGBforABGR(int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = (argb >> 0) & 255;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}
