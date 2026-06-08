/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.common.util;

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public final class Lazy<T> implements Supplier<T> {
   private final Supplier<T> delegate;
   private volatile @Nullable T cachedValue;

   public static <T> Lazy<T> of(Supplier<T> supplier) {
      return new Lazy<>(supplier);
   }

   public synchronized void invalidate() {
      this.cachedValue = null;
   }

   private Lazy(Supplier<T> delegate) {
      this.delegate = delegate;
   }

   @Override
   public T get() {
      T ret = this.cachedValue;
      if (ret == null) {
         synchronized (this) {
            ret = this.cachedValue;
            if (ret == null) {
               this.cachedValue = ret = this.delegate.get();
               if (ret == null) {
                  throw new IllegalStateException("Lazy value cannot be null, but supplier returned null: " + this.delegate);
               }
            }
         }
      }

      return ret;
   }
}
