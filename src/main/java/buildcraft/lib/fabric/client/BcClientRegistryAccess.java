/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup.Provider;
import org.jspecify.annotations.Nullable;

// Kept out of BcRegistryUtil so the common class never names a client type; only loaded behind an EnvType.CLIENT guard.
public final class BcClientRegistryAccess {
   private BcClientRegistryAccess() {
   }

   @Nullable
   public static Provider levelRegistryAccess() {
      try {
         ClientLevel level = Minecraft.getInstance().level;
         return level == null ? null : level.registryAccess();
      } catch (Throwable ignored) {
         return null;
      }
   }
}
