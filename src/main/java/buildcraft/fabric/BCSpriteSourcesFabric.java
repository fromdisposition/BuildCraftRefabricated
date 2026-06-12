package buildcraft.fabric;

import buildcraft.lib.client.sprite.BcHeatWhiteSpriteSource;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;
import org.slf4j.Logger;

public final class BCSpriteSourcesFabric {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean registered;

   private BCSpriteSourcesFabric() {
   }

   public static void register() {
   }

   public static void registerInto(LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>> mapper) {
      if (!registered) {
         try {
            mapper.put(BcHeatWhiteSpriteSource.ID, BcHeatWhiteSpriteSource.MAP_CODEC);
            registered = true;
         } catch (RuntimeException e) {
            LOGGER.error("Failed to register BuildCraft sprite source types", e);
         }
      }
   }
}
