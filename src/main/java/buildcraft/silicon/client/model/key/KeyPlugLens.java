/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class KeyPlugLens extends PluggableModelKey {
   /** The default blue an undyed lens glass is drawn with — single source for the key tint and the baker. */
   public static final int DEFAULT_GLASS_ARGB = 0xFF3F76E4;
   @Nullable
   public final DyeColor colour;
   public final boolean isFilter;
   private final int hash;

   public KeyPlugLens(Object layer, Direction side, @Nullable DyeColor colour, boolean isFilter) {
      super(layer, side);
      this.colour = colour;
      this.isFilter = isFilter;
      this.hash = Objects.hash(layer, side, colour, isFilter);
   }

   @Override
   public int getTintColour() {
      // The dye that tints the lens glass at render time (BakedQuads carry no per-vertex colour on 26.x, so the
      // colour the baker sets per-vertex is lost through the bake). An uncoloured lens uses the default blue the
      // baker draws for it.
      if (this.colour != null) {
         return 0xFF000000 | this.colour.getTextureDiffuseColor() & 0xFFFFFF;
      }

      return DEFAULT_GLASS_ARGB;
   }

   @Override
   public int hashCode() {
      return this.hash;
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

      KeyPlugLens other = (KeyPlugLens)obj;
      return other.isFilter == this.isFilter && Objects.equals(other.layer, this.layer) && other.colour == this.colour && other.side == this.side;
   }
}
