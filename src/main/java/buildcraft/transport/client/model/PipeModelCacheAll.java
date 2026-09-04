/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.transport.client.render.PipeBehaviourRendererStripes;
import buildcraft.transport.client.render.PipeWireRenderer;

public final class PipeModelCacheAll {
   private PipeModelCacheAll() {
   }

   public static void clearAll() {
      PipeMutableQuadCache.clearCaches();
      PipeBaseModelGenStandard.clearSpriteCaches();
      PipeWireRenderer.clearCaches();
      PipeBehaviourRendererStripes.clearCaches();
      PipePluggableQuadCache.clearCaches();
   }
}
