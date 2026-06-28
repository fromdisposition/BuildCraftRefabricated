/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.compat;

//? if >= 1.21.10 {
import java.util.List;
//?}
import net.minecraft.world.item.component.CustomModelData;

/**
 * Bridges the {@link CustomModelData} record shape cliff. On 1.21.5+ it holds four lists
 * (floats, flags, strings, colours); on 1.21.1 it is a single legacy int. BuildCraft only ever set a single
 * float (model index) or a single string (variant name), so these factories collapse to the 1.21.1 int form
 * (the string variant has no 1.21.1 equivalent and degrades to 0 — a cosmetic-only loss).
 */
public final class BcModelData {
   /** A model selected by a single numeric index. */
   public static CustomModelData index(float value) {
      //? if >= 1.21.10 {
      return new CustomModelData(List.of(value), List.of(), List.of(), List.of());
      //?} else {
      /*return new CustomModelData((int) value);
      *///?}
   }

   /** A model selected by a string variant name (degrades to the default model on 1.21.1). */
   public static CustomModelData variant(String name) {
      //? if >= 1.21.10 {
      return new CustomModelData(List.of(), List.of(), List.of(name), List.of());
      //?} else {
      /*return new CustomModelData(0);
      *///?}
   }

   private BcModelData() {
   }
}
