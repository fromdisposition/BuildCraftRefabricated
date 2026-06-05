/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.IGuiArea;

public class SpriteNineSliced {
    public final ISprite sprite;
    public final double xMin, yMin, xMax, yMax;
    public final double xScale, yScale;

    public SpriteNineSliced(ISprite sprite, int xMin, int yMin, int xMax, int yMax, int textureSize) {
        this(sprite, xMin, yMin, xMax, yMax, textureSize, textureSize);
    }

    public SpriteNineSliced(ISprite sprite, int xMin, int yMin, int xMax, int yMax, int xScale, int yScale) {
        this.sprite = sprite;
        this.xMin = xMin / (double) xScale;
        this.yMin = yMin / (double) yScale;
        this.xMax = xMax / (double) xScale;
        this.yMax = yMax / (double) yScale;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public SpriteNineSliced(ISprite sprite, double xMin, double yMin, double xMax, double yMax, double scale) {
        this(sprite, xMin, yMin, xMax, yMax, scale, scale);
    }

    public SpriteNineSliced(ISprite sprite, double xMin, double yMin, double xMax, double yMax, double xScale,
            double yScale) {
        this.sprite = sprite;
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public void draw(IGuiArea element) {
        draw(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void draw(double x, double y, double width, double height) {
        drawTinted(x, y, width, height, 0xFFFFFFFF);
    }

    public void drawTinted(double x, double y, double width, double height, int colour) {
        sprite.bindTexture();

        double leftBorder = xMin * xScale;
        double topBorder = yMin * yScale;
        double rightBorder = (1.0 - xMax) * xScale;
        double bottomBorder = (1.0 - yMax) * yScale;

        double x0 = x;
        double x1 = x + leftBorder;
        double x2 = x + width - rightBorder;
        double x3 = x + width;

        double y0 = y;
        double y1 = y + topBorder;
        double y2 = y + height - bottomBorder;
        double y3 = y + height;

        double u0 = sprite.getInterpU(0);
        double u1 = sprite.getInterpU(xMin);
        double u2 = sprite.getInterpU(xMax);
        double u3 = sprite.getInterpU(1);

        double v0 = sprite.getInterpV(0);
        double v1 = sprite.getInterpV(yMin);
        double v2 = sprite.getInterpV(yMax);
        double v3 = sprite.getInterpV(1);

        drawSlice(x0, y0, x1, y1, u0, v0, u1, v1, colour);
        drawSlice(x1, y0, x2, y1, u1, v0, u2, v1, colour);
        drawSlice(x2, y0, x3, y1, u2, v0, u3, v1, colour);

        drawSlice(x0, y1, x1, y2, u0, v1, u1, v2, colour);
        drawSlice(x1, y1, x2, y2, u1, v1, u2, v2, colour);
        drawSlice(x2, y1, x3, y2, u2, v1, u3, v2, colour);

        drawSlice(x0, y2, x1, y3, u0, v2, u1, v3, colour);
        drawSlice(x1, y2, x2, y3, u1, v2, u2, v3, colour);
        drawSlice(x2, y2, x3, y3, u2, v2, u3, v3, colour);
    }

    private static void drawSlice(double xMin, double yMin, double xMax, double yMax,
            double uMin, double vMin, double uMax, double vMax, int colour) {
        if (xMax <= xMin || yMax <= yMin) return;
        GuiIcon.drawBoundQuadTinted(xMin, yMin, xMax, yMax, uMin, vMin, uMax, vMax, colour);
    }
}
