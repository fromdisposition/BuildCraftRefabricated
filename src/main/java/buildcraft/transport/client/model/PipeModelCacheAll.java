/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.transport.client.render.PipeBehaviourRendererStripes;
import buildcraft.transport.client.render.PipeWireRenderer;

/**
 * Clears every pipe-model cache on a resource/model reload. Called from the model-bake hook (BCSiliconClient).
 * The old joined all-in-one caches ({@code PipeAllCutoutKey}/{@code PipeAllTranslucentKey}) were superseded by
 * the chunk-baked {@link PipeBlockStateModel} + dynamic {@link buildcraft.transport.client.render.RenderPipeHolder}
 * path and removed; only the live leaf caches remain to clear.
 */
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
