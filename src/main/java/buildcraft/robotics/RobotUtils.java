/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class RobotUtils {
   private RobotUtils() {
   }

   public static List<DockingStation> getStations(IPipeHolder holder) {
      List<DockingStation> stations = new ArrayList<>();
      if (holder != null) {
         for (Direction face : Direction.values()) {
            PipePluggable plug = holder.getPluggable(face);
            if (plug instanceof IDockingStationProvider provider) {
               DockingStation station = provider.getStation();
               if (station != null) {
                  stations.add(station);
               }
            }
         }
      }

      return stations;
   }

   public static IPipeHolder getPipeHolder(IStatementContainer container) {
      if (container != null && container.getTile() instanceof IPipeHolder holder) {
         return holder;
      }

      if (container instanceof IPipeHolder holder) {
         return holder;
      }

      return null;
   }
}
