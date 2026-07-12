/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class RegistryKeyUtil {
   public static Identifier id(ResourceKey<?> key) {
      //? if >= 1.21.11 {
      return key.identifier();
      //?} else {
      /*// ResourceKey.location() was renamed to identifier() in 1.21.11.
      return key.location();
      *///?}
   }

   //? if < 1.21.10 {
   /*// On 1.21.1 holder ids (e.g. RecipeHolder.id()) are already an Identifier, not a ResourceKey.
   public static Identifier id(Identifier id) {
      return id;
   }
   *///?}

   private RegistryKeyUtil() {
   }
}
