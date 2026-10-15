/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;

public enum EnumPipePart implements StringRepresentable {
   DOWN(Direction.DOWN),
   UP(Direction.UP),
   NORTH(Direction.NORTH),
   SOUTH(Direction.SOUTH),
   WEST(Direction.WEST),
   EAST(Direction.EAST),
   CENTER(null);

   public static final EnumPipePart[] VALUES = values();
   public static final EnumPipePart[] FACES;
   public static final EnumPipePart[] HORIZONTALS;
   private static final Map<Direction, EnumPipePart> facingMap = Maps.newEnumMap(Direction.class);
   private static final Map<String, EnumPipePart> nameMap = Maps.newHashMap();
   private static final int MAX_VALUES = values().length;
   public final Direction face;

   private static EnumPipePart[] fromFacingArray(Direction... faces) {
      EnumPipePart[] arr = new EnumPipePart[faces.length];

      for (int i = 0; i < faces.length; i++) {
         arr[i] = fromFacing(faces[i]);
      }

      return arr;
   }

   public static int ordinal(Direction face) {
      return face == null ? 6 : face.ordinal();
   }

   public static EnumPipePart fromFacing(Direction face) {
      return face == null ? CENTER : facingMap.get(face);
   }

   public static EnumPipePart[] validFaces() {
      return FACES;
   }

   public static EnumPipePart fromMeta(int meta) {
      return meta >= 0 && meta < MAX_VALUES ? VALUES[meta] : CENTER;
   }

   EnumPipePart(Direction face) {
      this.face = face;
   }

   public int getIndex() {
      return this.face == null ? 6 : this.face.get3DDataValue();
   }

   public String getSerializedName() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public EnumPipePart next() {
      switch (this) {
         case DOWN:
            return EAST;
         case UP:
            return WEST;
         case NORTH:
            return SOUTH;
         case SOUTH:
            return UP;
         case WEST:
            return DOWN;
         case EAST:
            return NORTH;
         default:
            return DOWN;
      }
   }

   public EnumPipePart opposite() {
      return this == CENTER ? CENTER : fromFacing(this.face.getOpposite());
   }

   public static EnumPipePart readFromNBT(Tag base) {
      if (base == null) {
         return CENTER;
      } else {
         return base instanceof StringTag ? CENTER : CENTER;
      }
   }

   public Tag writeToNBT() {
      return StringTag.valueOf(this.name());
   }

   static {
      for (EnumPipePart part : values()) {
         nameMap.put(part.name(), part);
         if (part.face != null) {
            facingMap.put(part.face, part);
         }
      }

      FACES = fromFacingArray(Direction.values());
      HORIZONTALS = fromFacingArray();
   }
}
