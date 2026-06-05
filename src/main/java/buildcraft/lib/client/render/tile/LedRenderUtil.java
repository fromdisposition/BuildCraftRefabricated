/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.render.tile;

import net.minecraft.core.Direction;

public final class LedRenderUtil {

    public static final int COLOUR_OFF = 0xFF_1f_10_1b;

    public static final int COLOUR_GREEN_ON = 0xFF_77_DD_77;

    public static final int COLOUR_RED_ON = 0xFF_22_22_DD;

    public static void setFacePosition(RenderPartCube led, Direction face, double insetBlocks,
                                       double sideOffset, double y) {
        final double ledX, ledZ;
        final int dX, dZ;
        if (face.getAxis() == Direction.Axis.X) {
            dX = 0;
            dZ = face.getAxisDirection().getStep();
            ledZ = 0.5;
            ledX = (face == Direction.EAST) ? 1.0 - insetBlocks : insetBlocks;
        } else {
            dX = -face.getAxisDirection().getStep();
            dZ = 0;
            ledX = 0.5;
            ledZ = (face == Direction.SOUTH) ? 1.0 - insetBlocks : insetBlocks;
        }
        led.center.positiond(ledX + dX * sideOffset, y, ledZ + dZ * sideOffset);
    }

    private LedRenderUtil() {}
}
