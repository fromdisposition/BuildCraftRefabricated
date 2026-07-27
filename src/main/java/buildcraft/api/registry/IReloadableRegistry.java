/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.registry;

import java.util.Collection;
import java.util.Map;

public interface IReloadableRegistry<E> {
   IReloadableRegistryManager getManager();

   default void reload() {
      this.getManager().reload(this);
   }

   <T extends E> T addPermanent(T var1);

   Collection<E> getPermanent();

   Map<Object, E> getReloadableEntryMap();

   Iterable<E> getAllEntries();

   enum PackType {
      RESOURCE_PACK("assets"),
      DATA_PACK("data");

      public final String prefix;

      PackType(String prefix) {
         this.prefix = prefix;
      }
   }
}
