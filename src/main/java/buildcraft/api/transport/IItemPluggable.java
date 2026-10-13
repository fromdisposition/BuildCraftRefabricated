/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public interface IItemPluggable {
   @Nullable
   PipePluggable onPlace(@Nonnull ItemStack var1, IPipeHolder var2, Direction var3, Player var4, InteractionHand var5);

   @Nonnull
   default AABB getPlacementBoundingBox(@Nonnull ItemStack stack, Direction side) {
      return IItemPluggable.DefaultPlacementBoxes.BOXES[side.get3DDataValue()];
   }

   final class DefaultPlacementBoxes {
      static final AABB[] BOXES = new AABB[6];

      private DefaultPlacementBoxes() {
      }

      static {
         double a = 0.3125;
         double b = 0.6875;
         double near = 0.125;
         double nearEnd = 0.25;
         double far = 0.75;
         double farEnd = 0.875;
         BOXES[Direction.DOWN.get3DDataValue()] = new AABB(a, near, a, b, nearEnd, b);
         BOXES[Direction.UP.get3DDataValue()] = new AABB(a, far, a, b, farEnd, b);
         BOXES[Direction.NORTH.get3DDataValue()] = new AABB(a, a, near, b, b, nearEnd);
         BOXES[Direction.SOUTH.get3DDataValue()] = new AABB(a, a, far, b, b, farEnd);
         BOXES[Direction.WEST.get3DDataValue()] = new AABB(near, a, a, nearEnd, b, b);
         BOXES[Direction.EAST.get3DDataValue()] = new AABB(far, a, a, farEnd, b, b);
      }
   }
}
