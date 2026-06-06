package buildcraft.fabric;

import buildcraft.lib.client.sprite.BcFluidBakeSpriteSource;
import buildcraft.lib.client.sprite.BcHeatWhiteSpriteSource;
import buildcraft.lib.client.sprite.DyeReplaceSpriteSource;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.lang.reflect.Field;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;
import org.slf4j.Logger;

public final class BCSpriteSourcesFabric {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean registered;

   private BCSpriteSourcesFabric() {
   }

   public static void register() {
      try {
         Field field = SpriteSources.class.getDeclaredField("ID_MAPPER");
         field.setAccessible(true);
         LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>> mapper = (LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>>)field.get(
            null
         );
         registerInto(mapper);
      } catch (ReflectiveOperationException e) {
         LOGGER.error("Failed to register BuildCraft sprite source types", e);
      }
   }

   public static void registerInto(LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>> mapper) {
      if (!registered) {
         try {
            mapper.put(DyeReplaceSpriteSource.ID, DyeReplaceSpriteSource.MAP_CODEC);
            mapper.put(BcHeatWhiteSpriteSource.ID, BcHeatWhiteSpriteSource.MAP_CODEC);
            mapper.put(BcFluidBakeSpriteSource.ID, BcFluidBakeSpriteSource.MAP_CODEC);
            registered = true;
         } catch (RuntimeException e) {
            LOGGER.error("Failed to register BuildCraft sprite source types", e);
         }
      }
   }
}
