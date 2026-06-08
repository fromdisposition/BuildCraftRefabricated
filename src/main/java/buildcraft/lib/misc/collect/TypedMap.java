/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.collect;

public interface TypedMap<V> {
   <T extends V> T get(Class<T> var1);

   void put(V var1);

   void clear();

   void remove(V var1);
}
