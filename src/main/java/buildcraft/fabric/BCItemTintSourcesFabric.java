package buildcraft.fabric;

import buildcraft.core.client.FluidShardTintSource;
import buildcraft.transport.client.PipeColourTintSource;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;
import org.slf4j.Logger;

public final class BCItemTintSourcesFabric {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Identifier FLUID_SHARD_TINT_ID = Identifier.fromNamespaceAndPath("buildcraftcore", "fluid_shard_tint");
   public static final Identifier PIPE_COLOUR_TINT_ID = Identifier.fromNamespaceAndPath("buildcrafttransport", "pipe_colour");
   private static boolean registered;

   private BCItemTintSourcesFabric() {
   }

   public static void register() {
      registerInto(ItemTintSources.ID_MAPPER);
   }

   public static void registerInto(LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>> mapper) {
      if (!registered) {
         try {
            mapper.put(FLUID_SHARD_TINT_ID, FluidShardTintSource.MAP_CODEC);
            mapper.put(PIPE_COLOUR_TINT_ID, PipeColourTintSource.MAP_CODEC);
            registered = true;
         } catch (RuntimeException e) {
            LOGGER.error("Failed to register BuildCraft item tint source types", e);
         }
      }
   }
}
