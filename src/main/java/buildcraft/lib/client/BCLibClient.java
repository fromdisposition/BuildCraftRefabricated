/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client;

import buildcraft.fabric.BCLibFabricClient;
import buildcraft.lib.fabric.BCLibClientBridge;

public final class BCLibClient {
   private static boolean initialized;

   private BCLibClient() {
   }

   public static void initClient() {
      if (!initialized) {
         initialized = true;
         BCLibFabricClient.init();
      }
   }

   public static void openGuideScreen(String bookName) {
      BCLibClientBridge.openGuideScreen(bookName);
   }
}
