/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

public class GuiIcon implements ISimpleDrawable {
    public final ISprite sprite;
    public final int textureSize;
    public final int width, height;

    private static BCGraphics currentGraphics;

    public static void setGuiGraphics(BCGraphics graphics) {
        currentGraphics = graphics;
    }

    public static BCGraphics getGuiGraphics() {
        return currentGraphics;
    }

    public GuiIcon(ISprite sprite, int textureSize) {
        this.sprite = sprite;
        this.textureSize = textureSize;
        this.width = (int) (Math.abs(sprite.getInterpU(1) - sprite.getInterpU(0)) * textureSize);
        this.height = (int) (Math.abs(sprite.getInterpV(1) - sprite.getInterpV(0)) * textureSize);
    }

    public GuiIcon(Identifier texture, double u, double v, double width, double height, int texSize) {
        this(new SpriteRaw(texture, u, v, width, height, texSize), texSize);
    }

    public GuiIcon(Identifier texture, double u, double v, double width, double height) {
        this(texture, u, v, width, height, 256);
    }

    public GuiIcon offset(double u, double v) {
        SpriteRaw raw = (SpriteRaw) sprite;
        double uMin = raw.uMin + u / textureSize;
        double vMin = raw.vMin + v / textureSize;
        return new GuiIcon(new SpriteRaw(raw.location, uMin, vMin, raw.width, raw.height), textureSize);
    }

    public boolean containsGuiPos(double x, double y, IGuiPosition pos) {
        return new GuiRectangle(x, y, width, height).contains(pos);
    }

    public DynamicTexture createDynamicTexture(int scale) {
        return new DynamicTexture("bc_guide_icon", width * scale, height * scale, false);
    }

    @Override
    public void drawAt(double x, double y) {
        this.drawScaledInside(x, y, this.width, this.height);
    }

    public void drawAt(IGuiArea area) {
        drawAt(area.getX(), area.getY());
    }

    public void drawScaledInside(IGuiArea element) {
        drawScaledInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawScaledInside(double x, double y, double drawnWidth, double drawnHeight) {
        draw(sprite, x, y, x + drawnWidth, y + drawnHeight);
    }

    public void drawCustomQuad(double x1, double y1, double x2, double y2,
            double x3, double y3, double x4, double y4) {
        double xMin = Math.min(Math.min(x1, x2), Math.min(x3, x4));
        double yMin = Math.min(Math.min(y1, y2), Math.min(y3, y4));
        double xMax = Math.max(Math.max(x1, x2), Math.max(x3, x4));
        double yMax = Math.max(Math.max(y1, y2), Math.max(y3, y4));
        draw(sprite, xMin, yMin, xMax, yMax);
    }

    public void drawCutInside(IGuiArea element) {
        drawCutInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawCutInside(double x, double y, double displayWidth, double displayHeight) {
        displayWidth = Math.min(this.width, displayWidth);
        displayHeight = Math.min(this.height, displayHeight);

        if (currentGraphics == null || !(sprite instanceof SpriteRaw raw)) return;

        float uPixel = (float) (raw.uMin * textureSize);
        float vPixel = (float) (raw.vMin * textureSize);

        currentGraphics.blit(
            RenderPipelines.GUI_TEXTURED, raw.location,
            (int) x, (int) y,
            uPixel, vPixel,
            (int) displayWidth, (int) displayHeight,
            textureSize, textureSize
        );
    }

    public static void drawAt(ISprite sprite, double x, double y, double size) {
        drawAt(sprite, x, y, size, size);
    }

    public static void drawAt(ISprite sprite, double x, double y, double width, double height) {
        draw(sprite, x, y, x + width, y + height);
    }

    public static void draw(ISprite sprite, double xMin, double yMin, double xMax, double yMax) {
        if (currentGraphics == null) return;
        if (sprite instanceof SpriteRaw raw) {
            float uPixel = (float) (raw.uMin * raw.texSize);
            float vPixel = (float) (raw.vMin * raw.texSize);
            float uWidth = (float) (raw.width * raw.texSize);
            float vHeight = (float) (raw.height * raw.texSize);
            int drawWidth = (int) (xMax - xMin);
            int drawHeight = (int) (yMax - yMin);

            currentGraphics.blit(
                RenderPipelines.GUI_TEXTURED, raw.location,
                (int) xMin, (int) yMin,
                uPixel, vPixel,
                drawWidth, drawHeight,
                (int) uWidth, (int) vHeight,
                raw.texSize, raw.texSize
            );
        } else if (sprite instanceof buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder holder) {
            net.minecraft.client.renderer.texture.TextureAtlasSprite tas = holder.getSprite();
            if (tas != null) {
                currentGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, tas, (int) xMin, (int) yMin, (int) (xMax - xMin), (int) (yMax - yMin));
            }
        }
    }

    public static void drawBoundQuad(double xMin, double yMin, double xMax, double yMax,
            double uMin, double vMin, double uMax, double vMax) {
        drawBoundQuadTinted(xMin, yMin, xMax, yMax, uMin, vMin, uMax, vMax, 0xFFFFFFFF);
    }

    public static void drawBoundQuadTinted(double xMin, double yMin, double xMax, double yMax,
            double uMin, double vMin, double uMax, double vMax, int colour) {
        if (currentGraphics == null || lastBoundLocation == null) return;

        int xLow  = (int) Math.round(xMin);
        int yLow  = (int) Math.round(yMin);
        int xHigh = (int) Math.round(xMax);
        int yHigh = (int) Math.round(yMax);
        int drawW = xHigh - xLow;
        int drawH = yHigh - yLow;
        float uPx = (float) (uMin * lastBoundTexSize);
        float vPx = (float) (vMin * lastBoundTexSize);
        int uW = (int) ((uMax - uMin) * lastBoundTexSize);
        int vH = (int) ((vMax - vMin) * lastBoundTexSize);

        currentGraphics.blit(
            RenderPipelines.GUI_TEXTURED, lastBoundLocation,
            xLow, yLow,
            uPx, vPx,
            drawW, drawH,
            uW, vH,
            lastBoundTexSize, lastBoundTexSize,
            colour
        );
    }

    static Identifier lastBoundLocation;
    static int lastBoundTexSize = 256;

    public static void setLastBoundLocation(Identifier location) {
        lastBoundLocation = location;
    }

    public static void setLastBoundLocation(Identifier location, int texSize) {
        lastBoundLocation = location;
        lastBoundTexSize = texSize;
    }
}
