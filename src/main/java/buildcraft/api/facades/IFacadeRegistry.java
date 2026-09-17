/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.facades;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IFacadeRegistry {
   Collection<? extends IFacadeState> getValidFacades();

   IFacadePhasedState createPhasedState(IFacadeState var1, @Nullable DyeColor var2);

   IFacade createPhasedFacade(IFacadePhasedState[] var1, boolean var2);

   default IFacade createBasicFacade(IFacadeState state, boolean isHollow) {
      return this.createPhasedFacade(new IFacadePhasedState[]{this.createPhasedState(state, null)}, isHollow);
   }

   default void disableBlock(Block block, String source) {
   }

   default void mapStateToStack(BlockState state, ItemStack stack) {
   }
}
