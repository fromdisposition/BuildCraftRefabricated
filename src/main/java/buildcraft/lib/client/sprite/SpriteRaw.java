/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import net.minecraft.resources.Identifier;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.gui.GuiIcon;

public class SpriteRaw implements ISprite {
    public final Identifier location;
    public final double uMin, vMin, width, height;
    public final int texSize;

    public SpriteRaw(Identifier location, double xMin, double yMin, double width, double height, double textureSize) {
        this.location = location;
        this.uMin = xMin / textureSize;
        this.vMin = yMin / textureSize;
        this.width = width / textureSize;
        this.height = height / textureSize;
        this.texSize = (int) textureSize;
    }

    public SpriteRaw(Identifier location, double xMin, double yMin, double width, double height) {
        this.location = location;
        this.uMin = xMin;
        this.vMin = yMin;
        this.width = width;
        this.height = height;
        this.texSize = 256;
    }

    @Override
    public void bindTexture() {
        GuiIcon.setLastBoundLocation(location, texSize);
    }

    @Override
    public double getInterpU(double u) {
        return uMin + u * width;
    }

    @Override
    public double getInterpV(double v) {
        return vMin + v * height;
    }
}
