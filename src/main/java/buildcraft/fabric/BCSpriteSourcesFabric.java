package buildcraft.fabric;

import buildcraft.lib.client.sprite.BcFluidBakeSpriteSource;
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

   /**
    * Registration happens natively via {@code SpriteSourcesBootstrapMixin}, which calls
    * {@link #registerInto} with the accessor-exposed {@code ID_MAPPER} at the end of
    * {@code SpriteSources.bootstrap}. This method is kept as a no-op entry point so client
    * init order stays explicit; it no longer reflects into vanilla internals.
    */
   public static void register() {
   }

   public static void registerInto(LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>> mapper) {
      if (!registered) {
         try {
            mapper.put(BcHeatWhiteSpriteSource.ID, BcHeatWhiteSpriteSource.MAP_CODEC);
            mapper.put(BcFluidBakeSpriteSource.ID, BcFluidBakeSpriteSource.MAP_CODEC);
            registered = true;
         } catch (RuntimeException e) {
            LOGGER.error("Failed to register BuildCraft sprite source types", e);
         }
      }
   }
}
