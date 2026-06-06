package buildcraft.lib.misc.collect;

import javax.annotation.Nullable;

public class OrderedEnumMap<E extends Enum<E>> {
   private final byte[] indexes;
   private final E[] order;

   @SafeVarargs
   public OrderedEnumMap(Class<E> clazz, E... order) {
      this.order = order;
      E[] values = clazz.getEnumConstants();
      this.indexes = new byte[values.length];
      int max = order.length;
      byte i = 0;

      while (i < max) {
         this.indexes[order[i].ordinal()] = i++;
      }
   }

   public int indexOf(@Nullable E val) {
      return this.indexes[val == null ? 0 : val.ordinal()];
   }

   public E get(int index) {
      return this.order[index];
   }

   public E[] getOrder() {
      return this.order;
   }

   public int getOrderLength() {
      return this.order.length;
   }

   public E next(E val) {
      int index = this.indexOf(val) + 1;
      return this.get(index % this.order.length);
   }

   public E previous(E val) {
      int index = this.indexOf(val) - 1;
      return this.get((index + this.order.length) % this.order.length);
   }
}
