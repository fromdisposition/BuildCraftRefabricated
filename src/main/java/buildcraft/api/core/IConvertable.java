/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import javax.annotation.Nullable;

public interface IConvertable {
   @Nullable
   default <T> T convertTo(Class<T> clazz) {
      return clazz.isInstance(this) ? clazz.cast(this) : null;
   }
}
