/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import java.util.Arrays;

public class PipeFaceTex {
   private static final int SINGLE_WHITE_SPRITES_COUNT = 64;
   private static final PipeFaceTex[] SINGLE_WHITE_SPRITES = new PipeFaceTex[64];
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   public static final PipeFaceTex NO_SPRITE = new PipeFaceTex();
   private final int[] textures;
   private final int[] colours;
   private final int hash;

   public static PipeFaceTex get(int[] textures, int... colours) {
      switch (textures.length) {
         case 0:
            return NO_SPRITE;
         case 1:
            if (colours.length != 0 && colours[0] != -1) {
               return new PipeFaceTex(textures, colours);
            }

            return get(textures[0]);
         default:
            return new PipeFaceTex(textures, colours);
      }
   }

   public static PipeFaceTex get(int... textures) {
      return get(textures, EMPTY_INT_ARRAY);
   }

   public static PipeFaceTex get(int singleTexture) {
      return singleTexture < 64 ? SINGLE_WHITE_SPRITES[singleTexture] : new PipeFaceTex(singleTexture);
   }

   private PipeFaceTex(int[] textures, int... colours) {
      this.textures = textures;
      this.colours = colours;

      for (int i = 0; i < colours.length; i++) {
         colours[i] &= 16777215;
      }

      if (textures.length == 0) {
         this.hash = -1;
      } else if (textures.length == 1) {
         this.hash = textures[0] + this.getColour(0) + 1;
      } else {
         int[] cArr = colours;
         int prevLength = cArr.length;
         int count = this.getCount();
         if (prevLength < count) {
            cArr = Arrays.copyOf(cArr, count);

            for (int i = prevLength; i < count; i++) {
               cArr[i] = -1;
            }
         }

         this.hash = Arrays.hashCode(cArr) + 31 * Arrays.hashCode(textures);
      }
   }

   private PipeFaceTex(int... textures) {
      this(textures, EMPTY_INT_ARRAY);
   }

   private PipeFaceTex(int singleTexture) {
      this.textures = new int[]{singleTexture};
      this.colours = EMPTY_INT_ARRAY;
      this.hash = singleTexture;
   }

   public int getCount() {
      return this.textures.length;
   }

   public int getTexture(int index) {
      return this.textures[index];
   }

   public int getColour(int index) {
      return index >= this.colours.length ? -1 : this.colours[index];
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      PipeFaceTex other = (PipeFaceTex)obj;
      if (this.hash != other.hash) {
         return false;
      }

      if (!Arrays.equals(this.textures, other.textures)) {
         return false;
      }

      int i = this.textures.length;

      while (i > 0) {
         i--;
         if (this.getColour(i) != other.getColour(i)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   public static PipeFaceTex ___testing_create_single(int single) {
      return new PipeFaceTex(single);
   }

   static {
      for (int i = 0; i < 64; i++) {
         SINGLE_WHITE_SPRITES[i] = new PipeFaceTex(i);
      }
   }
}
