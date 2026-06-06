package buildcraft.api.registry;

import java.util.Collection;
import java.util.Map;

public interface IReloadableRegistry<E> {
   IReloadableRegistryManager getManager();

   default void reload() {
      this.getManager().reload(this);
   }

   <T extends E> T addPermanent(T var1);

   Collection<E> getPermanent();

   Map<Object, E> getReloadableEntryMap();

   Iterable<E> getAllEntries();

   enum PackType {
      RESOURCE_PACK("assets"),
      DATA_PACK("data");

      public final String prefix;

      PackType(String prefix) {
         this.prefix = prefix;
      }
   }
}
