/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** 1.21.1 stub (versions/1.21.1): no-op base; the 1.21.5 render-state pipeline is unavailable. */
public abstract class BcBlockEntityRenderer<T extends BlockEntity, S extends BcBerState<T>> implements BlockEntityRenderer<T> {
   @Override
   public void render(T tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
   }

   protected void extract(T tile, S state, float partialTick) {
   }
}
