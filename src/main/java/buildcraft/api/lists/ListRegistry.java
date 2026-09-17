/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.Item;

public final class ListRegistry {
   public static final List<Class<? extends Item>> itemClassAsType = new ArrayList<>();
   private static final List<ListMatchHandler> handlers = new ArrayList<>();

   private ListRegistry() {
   }

   public static void registerHandler(ListMatchHandler h) {
      if (h != null) {
         handlers.add(h);
      }
   }

   public static List<ListMatchHandler> getHandlers() {
      return Collections.unmodifiableList(handlers);
   }
}
