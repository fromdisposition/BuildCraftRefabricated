package buildcraft.lib.client.render;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;

public final class LightUtil {
   public static final int FULL_BRIGHT = 15728880;

   private LightUtil() {
   }

   public static int pack(int blockLight, int skyLight) {
      return LightCoordsUtil.pack(blockLight, skyLight);
   }

   public static int getLightCoords(Level level, BlockPos pos) {
      return LevelRenderer.getLightCoords(level, pos);
   }
}
