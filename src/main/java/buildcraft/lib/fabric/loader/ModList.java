package buildcraft.lib.fabric.loader;

import java.nio.file.Path;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import buildcraft.lib.fabric.loader.IModInfo;

public final class ModList {
    private static final ModList INSTANCE = new ModList();

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId) || "minecraft".equals(modId);
    }

    public List<? extends IModInfo> getMods() {
        return FabricLoader.getInstance().getAllMods().stream()
                .map(FabricModInfo::new)
                .toList();
    }

    @javax.annotation.Nullable
    public ModFileInfo getModFileById(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(FabricModInfo::new)
                .map(FabricModInfo::toFileInfo)
                .orElse(null);
    }

    public static final class ModFileInfo {
        private final java.io.File file;

        public ModFileInfo(java.io.File file) {
            this.file = file;
        }

        public java.nio.file.Path getFilePath() {
            return file.toPath();
        }
    }

    private static final class FabricModInfo implements IModInfo {
        private final ModContainer container;

        private FabricModInfo(ModContainer container) {
            this.container = container;
        }

        @Override
        public String getModId() {
            return container.getMetadata().getId();
        }

        private ModFileInfo toFileInfo() {
            List<Path> paths;
            try {
                paths = container.getOrigin().getPaths();
            } catch (UnsupportedOperationException e) {
                // Nested/virtual mods (e.g. sub-mods bundled inside another JAR) do not
                // expose file paths. Return null so callers fall back gracefully.
                return null;
            }
            if (paths.isEmpty()) {
                return null;
            }
            return new ModFileInfo(paths.getFirst().toFile());
        }
    }
}
