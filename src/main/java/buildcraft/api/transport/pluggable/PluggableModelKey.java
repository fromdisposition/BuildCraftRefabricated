/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pluggable;

import java.util.Objects;
import net.minecraft.core.Direction;

public abstract class PluggableModelKey {
   public final Object layer;
   public final Direction side;
   private final int hash;

   public PluggableModelKey(Object layer, Direction side) {
      if (side == null) {
         throw new NullPointerException("side");
      }

      this.layer = layer;
      this.side = side;
      this.hash = Objects.hash(layer, side);
   }

   /**
    * A packed ARGB colour to tint this pluggable's rendered quads with, or {@code -1} for none. Needed because
    * modern (26.x/1.21.11) {@code BakedQuad}s carry no per-vertex colour, so a dye baked per-vertex (e.g. lens
    * glass) is lost through the bake; the tint is instead applied at render time from this key.
    */
   public int getTintColour() {
      return -1;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      PluggableModelKey other = (PluggableModelKey)obj;
      return this.layer != other.layer ? false : this.side == other.side;
   }

   @Override
   public int hashCode() {
      return this.hash;
   }
}
