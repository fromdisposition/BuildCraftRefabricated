package buildcraft.lib.misc.data;

import java.util.AbstractList;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;

public class NonNullMatrix<T> extends AbstractList<T> {
   private final NonNullList<T> internalList;
   private final int width;
   private final int height;

   public NonNullMatrix(int width, int height, @Nonnull T fill) {
      this.width = width;
      this.height = height;
      this.internalList = NonNullList.withSize(width * height, fill);
   }

   public NonNullMatrix(int width, int height, NonNullMatrix.IEntryFiller<T> filler) {
      this.width = width;
      this.height = height;
      this.internalList = NonNullList.withSize(width * height, filler.getEntry(0, 0));

      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            this.internalList.set(this.flatIndexOf(x, y), filler.getEntry(x, y));
         }
      }
   }

   public NonNullMatrix(T[][] from, @Nonnull T nullReplacer) {
      this.width = from.length;
      this.height = this.width == 0 ? 0 : from[0].length;
      this.internalList = NonNullList.withSize(this.width * this.height, nullReplacer);

      for (int x = 0; x < this.width; x++) {
         for (int y = 0; y < this.height; y++) {
            T val = from[x][y];
            if (val == null) {
               this.set(x, y, nullReplacer);
            } else {
               this.set(x, y, val);
            }
         }
      }
   }

   private int flatIndexOf(int x, int y) {
      return x * this.height + y;
   }

   @Nonnull
   public T get(int x, int y) {
      return this.get(this.flatIndexOf(x, y));
   }

   @Nonnull
   public T set(int x, int y, @Nonnull T element) {
      return this.set0(this.flatIndexOf(x, y), element);
   }

   @Nonnull
   @Override
   public T get(int index) {
      return (T)this.internalList.get(index);
   }

   @Override
   public int size() {
      return this.internalList.size();
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   @Nonnull
   @Override
   public T set(int index, T element) {
      if (element == null) {
         throw new NullPointerException("Element was null!");
      } else {
         return this.set0(index, element);
      }
   }

   @Nonnull
   private T set0(int flatIndex, @Nonnull T element) {
      return (T)this.internalList.set(flatIndex, element);
   }

   public interface IEntryFiller<T> {
      @Nonnull
      T getEntry(int var1, int var2);
   }
}
