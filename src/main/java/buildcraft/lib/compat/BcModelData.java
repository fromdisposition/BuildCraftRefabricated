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

public final class BcModelData {
   public static CustomModelData index(float value) {
      //? if >= 1.21.10 {
      return new CustomModelData(List.of(value), List.of(), List.of(), List.of());
      //?} else {
      /*return new CustomModelData((int) value);
      *///?}
   }

   // A string variant has no 1.21.1 form; falling back to the default model there is a cosmetic-only loss.
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
