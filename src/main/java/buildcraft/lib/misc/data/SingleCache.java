package buildcraft.lib.misc.data;

import com.google.common.base.Suppliers;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SingleCache<T> implements Supplier<T> {
   private final Supplier<T> delegate;
   private final long duration;
   private final TimeUnit timeUnit;
   private Supplier<T> cache;

   public SingleCache(Supplier<T> delegate, long duration, TimeUnit timeUnit) {
      this.delegate = delegate;
      this.duration = duration;
      this.timeUnit = timeUnit;
      this.clear();
   }

   public void clear() {
      this.cache = Suppliers.memoizeWithExpiration(this.delegate::get, this.duration, this.timeUnit)::get;
   }

   @Override
   public T get() {
      return this.cache.get();
   }
}
