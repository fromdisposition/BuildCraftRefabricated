/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class ColourUtil {
   public static final DyeColor[] COLOURS = DyeColor.values();
   private static final ChatFormatting[] FACE_TO_FORMAT = new ChatFormatting[6];
   private static final int[] LIGHT_HEX = new int[]{
      15000804, 15366197, 14238662, 6728447, 16767260, 3790126, 14250393, 8026746, 10528679, 2725785, 8271039, 2437523, 8998957, 32526, 12462887, 1578004
   };
   private static final ChatFormatting[] COLOUR_TO_FORMAT = new ChatFormatting[16];
   private static final int[] FACE_TO_COLOUR = new int[6];

   public static int getColourForSide(Direction face) {
      return FACE_TO_COLOUR[face.ordinal()];
   }

   public static String getTextFullTooltip(@Nullable DyeColor colour) {
      if (colour == null) {
         return "Clean";
      }

      String name = colour.getName();
      String[] parts = name.split("_");
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < parts.length; i++) {
         if (i > 0) {
            sb.append(' ');
         }

         sb.append(Character.toUpperCase(parts[i].charAt(0)));
         sb.append(parts[i].substring(1));
      }

      ChatFormatting format = COLOUR_TO_FORMAT[colour.ordinal()];
      return format.toString() + sb + ChatFormatting.RESET;
   }

   public static String getTextFullTooltip(Direction direction) {
      String localized = LocaleUtil.localize("direction." + direction.getName());
      ChatFormatting format = FACE_TO_FORMAT[direction.ordinal()];
      return format.toString() + localized + ChatFormatting.RESET;
   }

   public static ChatFormatting convertColourToTextFormat(DyeColor colour) {
      return COLOUR_TO_FORMAT[colour.ordinal()];
   }

   public static int getLightHex(DyeColor colour) {
      return LIGHT_HEX[colour.ordinal()];
   }

   public static int swapArgbToAbgr(int argb) {
      int a = argb >> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      return a << 24 | b << 16 | g << 8 | r;
   }

   static {
      FACE_TO_FORMAT[Direction.UP.ordinal()] = ChatFormatting.WHITE;
      FACE_TO_FORMAT[Direction.DOWN.ordinal()] = ChatFormatting.DARK_GRAY;
      FACE_TO_FORMAT[Direction.NORTH.ordinal()] = ChatFormatting.RED;
      FACE_TO_FORMAT[Direction.SOUTH.ordinal()] = ChatFormatting.BLUE;
      FACE_TO_FORMAT[Direction.EAST.ordinal()] = ChatFormatting.YELLOW;
      FACE_TO_FORMAT[Direction.WEST.ordinal()] = ChatFormatting.GREEN;
      COLOUR_TO_FORMAT[DyeColor.WHITE.ordinal()] = ChatFormatting.WHITE;
      COLOUR_TO_FORMAT[DyeColor.ORANGE.ordinal()] = ChatFormatting.GOLD;
      COLOUR_TO_FORMAT[DyeColor.MAGENTA.ordinal()] = ChatFormatting.LIGHT_PURPLE;
      COLOUR_TO_FORMAT[DyeColor.LIGHT_BLUE.ordinal()] = ChatFormatting.AQUA;
      COLOUR_TO_FORMAT[DyeColor.YELLOW.ordinal()] = ChatFormatting.YELLOW;
      COLOUR_TO_FORMAT[DyeColor.LIME.ordinal()] = ChatFormatting.GREEN;
      COLOUR_TO_FORMAT[DyeColor.PINK.ordinal()] = ChatFormatting.LIGHT_PURPLE;
      COLOUR_TO_FORMAT[DyeColor.GRAY.ordinal()] = ChatFormatting.DARK_GRAY;
      COLOUR_TO_FORMAT[DyeColor.LIGHT_GRAY.ordinal()] = ChatFormatting.GRAY;
      COLOUR_TO_FORMAT[DyeColor.CYAN.ordinal()] = ChatFormatting.DARK_AQUA;
      COLOUR_TO_FORMAT[DyeColor.PURPLE.ordinal()] = ChatFormatting.DARK_PURPLE;
      COLOUR_TO_FORMAT[DyeColor.BLUE.ordinal()] = ChatFormatting.BLUE;
      COLOUR_TO_FORMAT[DyeColor.BROWN.ordinal()] = ChatFormatting.GOLD;
      COLOUR_TO_FORMAT[DyeColor.GREEN.ordinal()] = ChatFormatting.DARK_GREEN;
      COLOUR_TO_FORMAT[DyeColor.RED.ordinal()] = ChatFormatting.DARK_RED;
      COLOUR_TO_FORMAT[DyeColor.BLACK.ordinal()] = ChatFormatting.DARK_GRAY;
      FACE_TO_COLOUR[Direction.DOWN.ordinal()] = -13421773;
      FACE_TO_COLOUR[Direction.UP.ordinal()] = -3355444;
   }
}
