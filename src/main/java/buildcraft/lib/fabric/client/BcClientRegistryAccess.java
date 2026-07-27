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

/**
 * Client-only holder for the {@link Minecraft} client-level registry access. Kept out of the common
 * {@link buildcraft.lib.fabric.BcRegistryUtil} so that class never names a client type in its bytecode
 * (which the verifier would resolve and fail on a dedicated server). Only invoked behind an
 * {@code EnvType.CLIENT} guard, so it is never loaded server-side.
 */
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
