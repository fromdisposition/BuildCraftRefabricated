package buildcraft.fabric.client.render;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
//?}

public final class BlockOutlineRenderStore {
   //? if >= 1.21.10 {
   public static final Map<BlockOutlineRenderState, List<BlockOutlineRenderer>> CUSTOM_OUTLINES = new WeakHashMap<>();
   //?} else {
   /*public static final Map<Object, List<BlockOutlineRenderer>> CUSTOM_OUTLINES = new WeakHashMap<>();
   *///?}

   private BlockOutlineRenderStore() {
   }
}
