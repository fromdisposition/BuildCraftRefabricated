package buildcraft.api.registry;

import java.util.Map;
import java.util.Set;

public interface IReloadableRegistryManager {
   IReloadableRegistry.PackType getType();

   boolean isLoadingAll();

   void reload(IReloadableRegistry<?> var1);

   void reload(IReloadableRegistry<?>... var1);

   void reload(Set<IReloadableRegistry<?>> var1);

   boolean isInReload();

   int getReloadCount();

   Map<String, IReloadableRegistry<?>> getAllRegistries();

   <R> IReloadableRegistry<R> createRegistry(String var1);

   <R> IScriptableRegistry<R> createScriptableRegistry(String var1);

   void registerRegistry(String var1, IScriptableRegistry<?> var2);

   default void registerRegistry(IScriptableRegistry<?> registry) {
      this.registerRegistry(registry.getEntryType(), registry);
   }
}
