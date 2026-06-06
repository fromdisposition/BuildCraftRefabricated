package buildcraft.lib.recipe;

import java.util.Arrays;
import java.util.List;

public class ChangingObject<T> {
   protected final T[] options;
   private final int hash;
   private int timeGap = 1000;

   public ChangingObject(T[] options) {
      if (options.length == 0) {
         throw new IllegalStateException("Must provide at least 1 option!");
      }

      this.options = options;
      this.hash = this.computeHash();
   }

   protected int computeHash() {
      return Arrays.hashCode(this.options);
   }

   public T get() {
      return this.get(0);
   }

   public T get(int indexOffset) {
      long now = System.currentTimeMillis() / this.timeGap % this.options.length;
      int i = (int)now + indexOffset;
      return this.options[i % this.options.length];
   }

   public List<T> getOptions() {
      return Arrays.asList(this.options);
   }

   public void setTimeGap(int timeGap) {
      this.timeGap = timeGap;
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      ChangingObject<?> other = (ChangingObject<?>)obj;
      return this.hash != other.hash ? false : Arrays.equals(this.options, other.options);
   }
}
