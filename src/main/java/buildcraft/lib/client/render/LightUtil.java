/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.render;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.util.LightCoordsUtil;

public final class LightUtil {
    private LightUtil() {}

    public static final int FULL_BRIGHT = LightCoordsUtil.FULL_BRIGHT;

    public static int pack(int blockLight, int skyLight) {

        return LightCoordsUtil.pack(blockLight, skyLight);

    }

    public static int getLightCoords(Level level, BlockPos pos) {

        return LevelRenderer.getLightCoords(level, pos);

    }
}
