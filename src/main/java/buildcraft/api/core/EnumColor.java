/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import java.util.Locale;
import java.util.Random;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.StringRepresentable;

@Deprecated
public enum EnumColor implements StringRepresentable {
   BLACK,
   RED,
   GREEN,
   BROWN,
   BLUE,
   PURPLE,
   CYAN,
   LIGHT_GRAY,
   GRAY,
   PINK,
   LIME,
   YELLOW,
   LIGHT_BLUE,
   MAGENTA,
   ORANGE,
   WHITE;

   public static final EnumColor[] VALUES = values();
   public static final String[] DYES = new String[]{
      "dyeBlack",
      "dyeRed",
      "dyeGreen",
      "dyeBrown",
      "dyeBlue",
      "dyePurple",
      "dyeCyan",
      "dyeLightGray",
      "dyeGray",
      "dyePink",
      "dyeLime",
      "dyeYellow",
      "dyeLightBlue",
      "dyeMagenta",
      "dyeOrange",
      "dyeWhite"
   };
   public static final String[] NAMES = new String[]{
      "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "LightGray", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White"
   };
   public static final int[] DARK_HEX = new int[]{
      2960685, 10696757, 3755038, 6044196, 3424674, 8667071, 3571870, 8947848, 4473924, 15041952, 4172342, 13615665, 8362705, 16737535, 16738816, 16777215
   };
   public static final int[] LIGHT_HEX = new int[]{
      1578004, 12462887, 32526, 8998957, 2437523, 8271039, 2725785, 10528679, 8026746, 14250393, 3790126, 16767260, 6728447, 14238662, 15366197, 15000804
   };
   private static Object iconSheet;
   private static TextureAtlasSprite[] brushSprites;

   public int getDarkHex() {
      return DARK_HEX[this.ordinal()];
   }

   public int getLightHex() {
      return LIGHT_HEX[this.ordinal()];
   }

   public static EnumColor fromId(int id) {
      return id >= 0 && id < VALUES.length ? VALUES[id] : WHITE;
   }

   public static EnumColor fromDye(String dyeTag) {
      for (int id = 0; id < DYES.length; id++) {
         if (DYES[id].equals(dyeTag)) {
            return VALUES[id];
         }
      }

      return null;
   }

   public static EnumColor fromName(String name) {
      for (int id = 0; id < NAMES.length; id++) {
         if (NAMES[id].equals(name)) {
            return VALUES[id];
         }
      }

      return null;
   }

   public static EnumColor getRand() {
      return VALUES[new Random().nextInt(VALUES.length)];
   }

   public EnumColor getNext() {
      return VALUES[(this.ordinal() + 1) % VALUES.length];
   }

   public EnumColor getPrevious() {
      return VALUES[(this.ordinal() + VALUES.length - 1) % VALUES.length];
   }

   public EnumColor inverse() {
      return VALUES[15 - this.ordinal()];
   }

   public String getTag() {
      return "color." + this.name().replace("_", ".").toLowerCase(Locale.ENGLISH);
   }

   public String getBasicTag() {
      return this.name().replace("_", ".").toLowerCase(Locale.ENGLISH);
   }

   public String getSerializedName() {
      return NAMES[this.ordinal()];
   }

   public String getLocalizedName() {
      return this.getTag();
   }

   public String getDye() {
      return DYES[this.ordinal()];
   }

   @Override
   public String toString() {
      String s = this.name().replace("_", " ");
      String[] words = s.split(" ");
      StringBuilder b = new StringBuilder();

      for (String word : words) {
         b.append(word.charAt(0)).append(word.substring(1).toLowerCase(Locale.ENGLISH)).append(" ");
      }

      return b.toString().trim();
   }

   public static void registerSprites(TextureAtlasSprite[] sprites) {
      brushSprites = sprites;
   }

   public TextureAtlasSprite getSprite() {
      return brushSprites[this.ordinal()];
   }
}
