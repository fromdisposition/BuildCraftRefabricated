/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client;

import java.util.Map;
import buildcraft.silicon.plug.FacadeStateManager;

/**
 * 1.21.1 stub (versions/1.21.1). The shared FacadeDeduplicator works on the 1.21.5 BlockStateModel/QuadCollection
 * baking API to merge visually identical facade block models and compute crafting redirects; none of that exists
 * on 1.21.1. Here the dedup pass is skipped and no facade redirects are published (each facade keeps its own
 * recipe). Only {@code applyRedirectAuthority()} is referenced on 1.21.1 (from the client-login hook).
 */
public class FacadeDeduplicator {
   public static void applyRedirectAuthority() {
      FacadeStateManager.stackRedirects = Map.of();
   }
}
