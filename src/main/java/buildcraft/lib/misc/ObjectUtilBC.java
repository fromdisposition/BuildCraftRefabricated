/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nullable;

public class ObjectUtilBC {
   @Nullable
   public static <T> T castOrNull(Object obj, Class<T> clazz) {
      return clazz.isInstance(obj) ? clazz.cast(obj) : null;
   }

   public static <T> T castOrDefault(Object obj, Class<T> clazz, T _default) {
      return clazz.isInstance(obj) ? clazz.cast(obj) : _default;
   }
}
