/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.robotics.plug.PluggableRobotStation;
import net.minecraft.resources.Identifier;

public class BCRoboticsPlugs {
   public static PluggableDefinition robotStation;

   public static void preInit() {
      if (PipeApi.pluggableRegistry != null) {
         robotStation = register("robot_station", PluggableRobotStation::new);
      }
   }

   private static PluggableDefinition register(String name, PluggableDefinition.IPluggableCreator creator) {
      PluggableDefinition def = new PluggableDefinition(idFor(name), creator);
      PipeApi.pluggableRegistry.register(def);
      return def;
   }

   private static Identifier idFor(String name) {
      return Identifier.fromNamespaceAndPath("buildcraftrobotics", name);
   }
}
