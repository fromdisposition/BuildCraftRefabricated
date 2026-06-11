/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.fabric.transfer.BcTransfers;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;

public final class PipeNeighborEnergyAccess {
   private PipeNeighborEnergyAccess() {
   }

   @Nullable
   public static EnergyStorage storage(IPipeHolder holder, Direction from) {
      return PipeNeighborStorageAccess.storage(
         holder, from, PipePluggable::energyStorage, PipeNeighborEnergyAccess::blockEnergyStorage, PipeFlowInternalAccess::energyStorage
      );
   }

   @Nullable
   private static EnergyStorage blockEnergyStorage(Level level, BlockPos pos, Direction side) {
      return MjAPI.isRfAutoConversionEnabled() ? BcTransfers.energy(level, pos, side) : null;
   }

   public static boolean canConnect(IPipeHolder holder, Direction from) {
      return storage(holder, from) != null;
   }
}
