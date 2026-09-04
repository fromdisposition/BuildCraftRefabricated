/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pluggable;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

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
    * Packed ARGB tint for this pluggable's rendered quads, or {@code -1} for none. Needed because baked quads carry
    * no per-vertex colour, so per-vertex tinting (e.g. dyed lens glass) must be reapplied here at render time.
    */
   public int getTintColour() {
      return -1;
   }

   /**
    * True if quads carry world-dependent (biome) tint indices; such pluggables render individually and are
    * excluded from the merged untinted batch.
    */
   public boolean hasWorldTint() {
      return false;
   }

   /**
    * Resolves a baked tint index to a packed RGB for the given position, or {@code -1} for none. Called only
    * when {@link #hasWorldTint()} is true.
    */
   public int resolveWorldTint(int tintIndex, Level level, BlockPos pos) {
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
      // Objects.equals, not ==: layer may be a computed (non-interned) object.
      return Objects.equals(this.layer, other.layer) && this.side == other.side;
   }

   @Override
   public int hashCode() {
      return this.hash;
   }
}
