/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

public interface IFastAddonRenderer<T extends Addon> {

    void renderAddonFast(T addon, Player player, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource);

    default IFastAddonRenderer<T> then(IFastAddonRenderer<? super T> after) {
        return (addon, player, partialTicks, poseStack, bufferSource) -> {
            renderAddonFast(addon, player, partialTicks, poseStack, bufferSource);
            after.renderAddonFast(addon, player, partialTicks, poseStack, bufferSource);
        };
    }
}
