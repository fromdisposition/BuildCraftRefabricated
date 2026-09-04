package buildcraft.fabric;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
//? if >= 1.21.10 {
import buildcraft.core.client.FluidShardTintSource;
import buildcraft.transport.client.PipeColourTintSource;
import net.minecraft.client.color.item.ItemTintSources;
//?}
//? if < 1.21.10 {
/*import buildcraft.core.BCCoreItems;
import buildcraft.core.client.FluidShardTintSource;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
*///?}

public final class BCItemTintSourcesFabric {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Identifier FLUID_SHARD_TINT_ID = Identifier.fromNamespaceAndPath("buildcraftcore", "fluid_shard_tint");
   public static final Identifier PIPE_COLOUR_TINT_ID = Identifier.fromNamespaceAndPath("buildcrafttransport", "pipe_colour");
   private static boolean registered;

   private BCItemTintSourcesFabric() {
   }

   public static void register() {
      //? if >= 1.21.10 {
      // ID_MAPPER is opened by fabric-api's access widener and only read at resource reload, so no bootstrap hook is needed.
      if (!registered) {
         try {
            ItemTintSources.ID_MAPPER.put(FLUID_SHARD_TINT_ID, FluidShardTintSource.MAP_CODEC);
            ItemTintSources.ID_MAPPER.put(PIPE_COLOUR_TINT_ID, PipeColourTintSource.MAP_CODEC);
            registered = true;
         } catch (RuntimeException e) {
            LOGGER.error("Failed to register BuildCraft item tint source types", e);
         }
      }
      //?} else {
      /*// 1.21.1 has no data-driven ItemTintSource registry; the fluid-shard tint is registered natively via the
      // classic Fabric ItemColor API instead so the dropped fragile container still shows its fluid colour.
      if (!registered) {
         ColorProviderRegistry.ITEM.register(
            (stack, tintIndex) -> tintIndex == 1 ? FluidShardTintSource.INSTANCE.calculate(stack, null, null) : -1,
            BCCoreItems.FRAGILE_FLUID_CONTAINER
         );
         registered = true;
      }
      *///?}
   }
}
