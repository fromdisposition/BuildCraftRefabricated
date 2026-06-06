package buildcraft.lib.fabric.loader;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class GamePaths {
   public static final Path GAMEDIR = FabricLoader.getInstance().getGameDir();
   public static final Path CONFIGDIR = FabricLoader.getInstance().getConfigDir();

   private GamePaths() {
   }
}
