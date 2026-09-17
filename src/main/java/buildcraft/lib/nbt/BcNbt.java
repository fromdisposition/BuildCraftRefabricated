/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
//? if < 1.21.10 {
/*import net.minecraft.nbt.Tag;
*///?}

// Hides the 1.21.5+ read-API rework (bare getters return Optional; getXOr/getCompoundOrEmpty were added) behind
// one call site that works on every node; BlockEntity save/load uses {@link BcValueIn}/{@link BcValueOut} instead.
public final class BcNbt {
   private BcNbt() {
   }

   public static byte getByte(CompoundTag tag, String key, byte def) {
      //? if >= 1.21.10 {
      return tag.getByteOr(key, def);
      //?} else {
      /*return tag.contains(key) ? tag.getByte(key) : def; *///?}
   }

   public static short getShort(CompoundTag tag, String key, short def) {
      //? if >= 1.21.10 {
      return  tag.getShortOr(key, def);
      //?} else {
      /*return tag.contains(key) ? tag.getShort(key) : def; *///?}
   }

   public static int getInt(CompoundTag tag, String key, int def) {
      //? if >= 1.21.10 {
      return tag.getIntOr(key, def);
      //?} else {
      /*return tag.contains(key) ? tag.getInt(key) : def; *///?}
   }

   public static long getLong(CompoundTag tag, String key, long def) {
      //? if >= 1.21.10 {
      return tag.getLongOr(key, def);
      //?} else {
      /*return tag.contains(key) ? tag.getLong(key) : def; *///?}
   }

   public static float getFloat(CompoundTag tag, String key, float def) {
      //? if >= 1.21.10 {
      return tag.getFloatOr(key, def);
      //?} else {
      /*return tag.contains(key) ? tag.getFloat(key) : def; *///?}
   }

   public static double getDouble(CompoundTag tag, String key, double def) {
      //? if >= 1.21.10 {
      return tag.getDoubleOr(key, def);
      //?} else {
      /*return tag.contains(key) ? tag.getDouble(key) : def; *///?}
   }

   public static boolean getBoolean(CompoundTag tag, String key, boolean def) {
      //? if >= 1.21.10 {
      return tag.getBooleanOr(key, def);
      //?} else {
      /*return tag.contains(key) ? tag.getBoolean(key) : def; *///?}
   }

   public static String getString(CompoundTag tag, String key, String def) {
      //? if >= 1.21.10 {
      return tag.getStringOr(key, def);
      //?} else {
      /*return tag.contains(key) ? tag.getString(key) : def; *///?}
   }

   /** Nested compound, empty if absent. */
   public static CompoundTag getCompound(CompoundTag tag, String key) {
      //? if >= 1.21.10 {
      return tag.getCompoundOrEmpty(key);
      //?} else {
      /*return tag.getCompound(key); *///?}
   }

   public static java.util.Set<String> keys(CompoundTag tag) {
      //? if >= 1.21.10 {
      return tag.keySet();
      //?} else {
      /*return tag.getAllKeys(); *///?}
   }

   public static int[] getIntArray(CompoundTag tag, String key) {
      //? if >= 1.21.10 {
      return tag.getIntArray(key).orElse(new int[0]);
      //?} else {
      /*return tag.getIntArray(key); *///?}
   }

   /** Nested list, empty if absent. (1.21.1 reads the raw tag and casts, so element type is irrelevant.) */
   public static ListTag getList(CompoundTag tag, String key) {
      //? if >= 1.21.10 {
      return tag.getListOrEmpty(key);
      //?} else {
      /*Tag t = tag.get(key);
      return t instanceof ListTag list ? list : new ListTag(); *///?}
   }

   /** Compound at a list index, empty if absent/not a compound. */
   public static CompoundTag getCompound(ListTag list, int index) {
      //? if >= 1.21.10 {
      return list.getCompound(index).orElseGet(CompoundTag::new);
      //?} else {
      /*return list.getCompound(index); *///?}
   }

   public static double getDouble(ListTag list, int index, double def) {
      //? if >= 1.21.10 {
      return list.getDouble(index).orElse(def);
      //?} else {
      /*return index < list.size() ? list.getDouble(index) : def; *///?}
   }

   public static boolean contains(CompoundTag tag, String key) {
      return tag.contains(key);
   }
}
