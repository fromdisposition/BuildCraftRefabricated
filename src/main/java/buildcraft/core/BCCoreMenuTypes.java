/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.core.list.ContainerList;
import buildcraft.core.list.ListOpenContext;
import buildcraft.fabric.BCRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class BCCoreMenuTypes {
   public static MenuType<ContainerList> LIST;

   private BCCoreMenuTypes() {
   }

   public static void register() {
      LIST = BCRegistries.registerMenuType("buildcraftcore", "list", new MenuType<>((syncId, inv) -> {
         InteractionHand hand = ListOpenContext.consume(inv.player);
         if (hand == null) {
            hand = InteractionHand.MAIN_HAND;
         }

         return new ContainerList(syncId, inv, hand);
      }, FeatureFlags.DEFAULT_FLAGS));
   }
}
