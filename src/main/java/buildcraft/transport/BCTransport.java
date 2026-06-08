/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.fabric.BCTransportFabric;

public final class BCTransport {
   public static final String MODID = "buildcrafttransport";

   private BCTransport() {
   }

   public static void init() {
      BCTransportFabric.register();
   }
}
