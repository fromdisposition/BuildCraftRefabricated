/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class BcBlockEntityRenderer<T extends BlockEntity, S extends BcBerState<T>> implements BlockEntityRenderer<T, S> {
   public void extractRenderState(T blockEntity, S state, float partialTick, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
      BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
      state.tile = blockEntity;
      state.partialTick = partialTick;
      state.light = state.lightCoords;
      this.extract(blockEntity, state, partialTick);
   }

   protected void extract(T tile, S state, float partialTick) {
   }
}
