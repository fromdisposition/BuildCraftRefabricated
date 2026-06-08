/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.fabric.transfer.BcTransfers;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import team.reborn.energy.api.EnergyStorage;

public final class PipeNeighborEnergyAccess {
   private PipeNeighborEnergyAccess() {
   }

   @Nullable
   public static EnergyStorage storage(IPipeHolder holder, Direction from) {
      return PipeNeighborStorageAccess.storage(holder, from, PipePluggable::energyStorage, BcTransfers::energy, PipeFlowInternalAccess::energyStorage);
   }

   public static boolean canConnect(IPipeHolder holder, Direction from) {
      return storage(holder, from) != null;
   }
}
