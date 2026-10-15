/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

// 1.21.5+ authlib exposes record-style id()/name() on {@link GameProfile}; 1.21.1 uses getId()/getName(). This
// hides the accessor rename in one place instead of per call site.
public final class BcAuth {
   private BcAuth() {
   }

   @Nullable
   public static UUID id(GameProfile profile) {
      //? if >= 1.21.10 {
      return profile.id();
      //?} else {
      /*return profile.getId(); *///?}
   }

   @Nullable
   public static String name(GameProfile profile) {
      //? if >= 1.21.10 {
      return profile.name();
      //?} else {
      /*return profile.getName(); *///?}
   }
}
