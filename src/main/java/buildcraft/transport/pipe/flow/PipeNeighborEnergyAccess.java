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
      // RF<->RF transport is not conversion: an RF pipe always connects to a neighbour's E storage. Whether a BC
      // machine exposes an E port at all is decided at the machine (getSidedEnergyStorage, gated on the power
      // mode) -- so in MJ_ONLY, MJ machines expose nothing here while native E machines and the converter engines
      // still connect.
      return BcTransfers.energy(level, pos, side);
   }

   public static boolean canConnect(IPipeHolder holder, Direction from) {
      return storage(holder, from) != null;
   }
}
