/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import java.util.Arrays;

public final class JavaTools {
   private JavaTools() {
   }

   public static <T> T[] concat(T[] first, T[] second) {
      T[] result = (T[])Arrays.copyOf(first, first.length + second.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
   }

   public static int[] concat(int[] first, int[] second) {
      int[] result = Arrays.copyOf(first, first.length + second.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
   }

   public static float[] concat(float[] first, float[] second) {
      float[] result = Arrays.copyOf(first, first.length + second.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
   }

   public static String surroundWithQuotes(String stringToSurroundWithQuotes) {
      return String.format("\"%s\"", stringToSurroundWithQuotes);
   }

   public static String stripSurroundingQuotes(String stringToStripQuotes) {
      return stringToStripQuotes.replaceAll("^\"|\"$", "");
   }
}
