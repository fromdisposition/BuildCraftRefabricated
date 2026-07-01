/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Version-neutral authlib accessors. The authlib bundled with 1.21.5+ exposes record-style {@code id()} /
 * {@code name()} on {@link GameProfile}; the 1.21.1 authlib uses {@code getId()} / {@code getName()}. Both
 * versions share the type, so this hides the accessor rename in ONE place instead of per call site.
 */
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
