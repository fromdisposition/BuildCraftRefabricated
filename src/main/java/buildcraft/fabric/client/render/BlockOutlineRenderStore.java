package buildcraft.fabric.client.render;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;

public final class BlockOutlineRenderStore {
   public static final Map<BlockOutlineRenderState, List<BlockOutlineRenderer>> CUSTOM_OUTLINES = new WeakHashMap<>();

   private BlockOutlineRenderStore() {
   }
}
