package buildcraft.api.registry;

import java.util.Map;
import java.util.Set;

import buildcraft.api.registry.IReloadableRegistry.PackType;

public interface IReloadableRegistryManager {

    PackType getType();

    boolean isLoadingAll();

    void reload(IReloadableRegistry<?> registry);

    void reload(IReloadableRegistry<?>... registries);

    void reload(Set<IReloadableRegistry<?>> registries);

    boolean isInReload();

    int getReloadCount();

    Map<String, IReloadableRegistry<?>> getAllRegistries();

    <R> IReloadableRegistry<R> createRegistry(String name);

    <R> IScriptableRegistry<R> createScriptableRegistry(String entryPath);

    void registerRegistry(String entryType, IScriptableRegistry<?> registry);

    default void registerRegistry(IScriptableRegistry<?> registry) {
        registerRegistry(registry.getEntryType(), registry);
    }
}
