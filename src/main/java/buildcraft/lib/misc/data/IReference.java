/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import buildcraft.api.core.IConvertable;
import javax.annotation.Nullable;

public interface IReference<T> {
   T get();

   void set(T var1);

   boolean canSet(@Nullable T var1);

   Class<T> getHeldType();

   default void setIfCan(Object value) {
      T obj = this.convertToType(value);
      if (obj != null || value == null) {
         if (this.canSet(obj)) {
            this.set(obj);
         }
      }
   }

   default T convertToType(Object value) {
      if (this.getHeldType().isInstance(value)) {
         return this.getHeldType().cast(value);
      } else {
         return value instanceof IConvertable ? ((IConvertable)value).convertTo(this.getHeldType()) : null;
      }
   }
}
