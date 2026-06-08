/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.api.facades.IFacadeState;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class FacadeBlockStateInfo implements IFacadeState {
   public final BlockState state;
   public final ItemStack requiredStack;
   public final ImmutableSet<Property<?>> varyingProperties;
   public final boolean isTransparent;
   public final boolean isVisible;
   public final boolean[] isSideSolid = new boolean[6];

   public FacadeBlockStateInfo(BlockState state, ItemStack requiredStack, ImmutableSet<Property<?>> varyingProperties) {
      this.state = Objects.requireNonNull(state, "state must not be null!");
      Objects.requireNonNull(state.getBlock(), "state.getBlock must not be null!");
      this.requiredStack = requiredStack;
      this.varyingProperties = varyingProperties;
      this.isTransparent = !state.canOcclude();
      this.isVisible = !requiredStack.isEmpty();

      for (Direction side : Direction.values()) {
         this.isSideSolid[side.ordinal()] = state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, side);
      }
   }

   public FacadePhasedState createPhased(DyeColor activeColour) {
      return new FacadePhasedState(this, activeColour);
   }

   @Override
   public String toString() {
      return "StateInfo [id=" + System.identityHashCode(this) + ", block = " + this.state.getBlock() + ", state =  " + this.state.toString() + "]";
   }

   @Override
   public BlockState getBlockState() {
      return this.state;
   }

   @Override
   public boolean isTransparent() {
      return this.isTransparent;
   }

   @Override
   public ItemStack getRequiredStack() {
      return this.requiredStack;
   }
}
