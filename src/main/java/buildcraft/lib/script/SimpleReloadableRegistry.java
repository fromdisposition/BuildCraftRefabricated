package buildcraft.lib.script;

import buildcraft.api.registry.IReloadableRegistry;
import buildcraft.api.registry.IReloadableRegistryManager;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class SimpleReloadableRegistry<E> implements IReloadableRegistry<E> {
   public final IReloadableRegistryManager manager;
   public final List<E> permanent = new ArrayList<>();
   public final Map<Object, E> reloadable = new HashMap<>();

   public SimpleReloadableRegistry(IReloadableRegistryManager manager) {
      this.manager = manager;
   }

   @Override
   public IReloadableRegistryManager getManager() {
      return this.manager;
   }

   @Override
   public <T extends E> T addPermanent(T recipe) {
      if (this.manager.isInReload()) {
         throw new IllegalStateException("Don't add permanent recipes during reload events! (Register them once literally any other time)");
      }

      this.permanent.add((E)recipe);
      return recipe;
   }

   @Override
   public Collection<E> getPermanent() {
      return this.permanent;
   }

   @Override
   public Map<Object, E> getReloadableEntryMap() {
      return this.reloadable;
   }

   @Override
   public Iterable<E> getAllEntries() {
      return Iterables.concat(this.getPermanent(), this.getReloadableEntryMap().values());
   }

   @Nullable
   public E getFirstMatch(Predicate<E> filter) {
      for (E recipe : this.reloadable.values()) {
         if (filter.test(recipe)) {
            return recipe;
         }
      }

      for (E recipe : this.permanent) {
         if (filter.test(recipe)) {
            return recipe;
         }
      }

      return null;
   }
}
