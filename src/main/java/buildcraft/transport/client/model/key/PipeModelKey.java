/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model.key;

import buildcraft.api.transport.pipe.EnumPipeColourType;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFaceTex;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.world.item.DyeColor;

public final class PipeModelKey {
   public static final PipeModelKey DEFAULT_KEY;
   public final PipeDefinition definition;
   public final PipeFaceTex center;
   public final PipeFaceTex[] sides;
   public final float[] connected;
   public final DyeColor colour;
   private final int hash;

   public PipeModelKey(PipeDefinition definition, PipeFaceTex center, PipeFaceTex[] sides, float[] connected, DyeColor colour) {
      this.definition = definition;
      this.center = center;
      this.sides = sides;
      this.connected = connected;
      this.colour = colour;
      this.hash = Arrays.hashCode(
         new int[]{Objects.hashCode(definition), Objects.hashCode(center), Arrays.hashCode(sides), Arrays.hashCode(connected), Objects.hashCode(colour)}
      );
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (obj.getClass() != this.getClass()) {
         return false;
      } else {
         PipeModelKey other = (PipeModelKey)obj;
         if (this.definition != other.definition) {
            return false;
         } else if (this.center != other.center) {
            return false;
         } else if (!Arrays.equals(this.sides, other.sides)) {
            return false;
         } else {
            return !Arrays.equals(this.connected, other.connected) ? false : this.colour == other.colour;
         }
      }
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   public EnumPipeColourType getColourType() {
      return this.definition != null ? this.definition.getColourType() : EnumPipeColourType.TRANSLUCENT;
   }

   static {
      PipeFaceTex sprite = PipeFaceTex.get(0);
      PipeFaceTex[] sides = new PipeFaceTex[]{sprite, sprite, sprite, sprite, sprite, sprite};
      float[] connected = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F};
      DEFAULT_KEY = new PipeModelKey(null, sprite, sides, connected, null);
   }
}
