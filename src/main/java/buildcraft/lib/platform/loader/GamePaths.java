package buildcraft.lib.platform.loader;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class GamePaths {
   public static final Path GAMEDIR = FabricLoader.getInstance().getGameDir();
   public static final Path CONFIGDIR = FabricLoader.getInstance().getConfigDir();
   public static final Path BUILDCRAFT_CONFIG_DIR = CONFIGDIR.resolve("buildcraft");

   private GamePaths() {
   }
}
