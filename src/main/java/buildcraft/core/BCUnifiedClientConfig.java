/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.transport.client.model.PipeBaseModelGenStandard;

public final class BCUnifiedClientConfig {
   private BCUnifiedClientConfig() {
   }

   public static void onDisplayConfigReloaded() {
      PipeBaseModelGenStandard.clearSpriteCaches();
      PipeBaseModelGenStandard.onColorBlindToggle();
   }
}
