package buildcraft.lib.platform.loader;

import java.nio.file.Path;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jspecify.annotations.Nullable;

public final class FabricModResources {
   private FabricModResources() {
   }

   public static boolean isModLoaded(String modId) {
      return FabricLoader.getInstance().isModLoaded(modId) || "minecraft".equals(modId);
   }

   public static List<String> getModIds() {
      return FabricLoader.getInstance().getAllMods().stream().map(container -> container.getMetadata().getId()).toList();
   }

   public static @Nullable Path getModRootPath(String modId) {
      return FabricLoader.getInstance().getModContainer(modId).map(FabricModResources::rootPathOf).orElse(null);
   }

   private static @Nullable Path rootPathOf(ModContainer container) {
      try {
         List<Path> paths = container.getOrigin().getPaths();
         return paths.isEmpty() ? null : paths.getFirst();
      } catch (UnsupportedOperationException e) {
         return null;
      }
   }
}
