package buildcraft.lib.misc.data;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class DelayedList<E> {
   protected final List<List<E>> elements;
   private final Supplier<List<E>> innerListSupplier;

   public DelayedList() {
      this(new ArrayList<>(), ArrayList::new);
   }

   public static <E> DelayedList<E> createConcurrent() {
      return new DelayedList<E>(Collections.synchronizedList(new ArrayList<>()), () -> Collections.synchronizedList(new ArrayList<>())) {
         @Override
         public List<E> advance() {
            synchronized (this.elements) {
               return super.advance();
            }
         }
      };
   }

   private DelayedList(List<List<E>> actualList, Supplier<List<E>> innerList) {
      this.elements = actualList;
      this.innerListSupplier = innerList;
   }

   public int getMaxDelay() {
      return this.elements.size();
   }

   public List<E> advance() {
      return (List<E>)(this.elements.isEmpty() ? ImmutableList.of() : this.elements.remove(0));
   }

   public void add(int delay, E element) {
      if (delay < 0) {
         delay = 0;
      }

      while (this.elements.size() < delay + 1) {
         this.elements.add(this.innerListSupplier.get());
      }

      this.elements.get(delay).add(element);
   }

   public List<List<E>> getAllElements() {
      return this.elements;
   }

   public void clear() {
      this.elements.clear();
   }
}
