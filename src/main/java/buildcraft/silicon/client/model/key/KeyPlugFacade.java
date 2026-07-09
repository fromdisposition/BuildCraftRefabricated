/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import java.util.Objects;
import net.minecraft.core.Direction;
import buildcraft.silicon.client.model.plug.PlugBakerFacade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class KeyPlugFacade extends PluggableModelKey {
   public final BlockState state;
   public final boolean isHollow;
   private final int hash;

   public KeyPlugFacade(Object layer, Direction side, BlockState state, boolean isHollow) {
      super(layer, side);
      this.state = state;
      this.isHollow = isHollow;
      this.hash = Objects.hash(layer, side, state, isHollow);
   }

   @Override
   public boolean hasWorldTint() {
      return true;
   }

   /**
    * Reverse of the bake-time remap in PlugBakerFacade (2 + originalIndex * 6 + mountSide): recover the source
    * block's tint index and ask its own colour provider with the real world position, so biome-tinted facades
    * (grass, leaves) match the terrain around them. Mirrors FacadeTintSource, which handles the chunk path.
    */
   @Override
   public int resolveWorldTint(int tintIndex, Level level, BlockPos pos) {
      if (tintIndex < PlugBakerFacade.FACADE_TINT_BASE) {
         return -1;
      }

      int original = (tintIndex - PlugBakerFacade.FACADE_TINT_BASE) / Direction.values().length;
      //? if >= 26.1 {
      BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(this.state, original);
      if (source == null) {
         return -1;
      }

      // ClientLevel implements the render-side BlockAndTintGetter; color(state) is the world-less fallback.
      return level instanceof net.minecraft.client.renderer.block.BlockAndTintGetter tintGetter
         ? source.colorInWorld(this.state, tintGetter, pos)
         : source.color(this.state);
      //?} else {
      /*return Minecraft.getInstance().getBlockColors().getColor(this.state, level, pos, original);
      *///?}
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

      KeyPlugFacade other = (KeyPlugFacade)obj;
      return other.isHollow == this.isHollow && other.layer == this.layer && other.state == this.state && other.side == this.side;
   }
}
