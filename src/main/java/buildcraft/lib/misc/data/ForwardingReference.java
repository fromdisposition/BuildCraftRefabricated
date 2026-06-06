package buildcraft.lib.misc.data;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForwardingReference<T> implements IReference<T> {
   public final Supplier<T> getter;
   public final Consumer<T> setter;
   public final Class<T> clazz;

   public ForwardingReference(Class<T> clazz, Supplier<T> getter, Consumer<T> setter) {
      this.getter = getter;
      this.setter = setter;
      this.clazz = clazz;
   }

   @Override
   public T get() {
      return this.getter.get();
   }

   @Override
   public void set(T to) {
      this.setter.accept(to);
   }

   @Override
   public boolean canSet(T value) {
      return true;
   }

   @Override
   public Class<T> getHeldType() {
      return this.clazz;
   }
}
