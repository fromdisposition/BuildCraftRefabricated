/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.node.FormatString;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;

public abstract class GuidePart {
    public static final int INDENT_WIDTH = 16;
    public static final int LINE_HEIGHT = 16;

    public static class PagePosition {
        public final int page;
        public final int pixel;

        public PagePosition(int page, int pixel) {
            this.page = page;
            this.pixel = pixel;
        }

        public PagePosition nextLine(int pixelDifference, int maxHeight) {
            int added = pixel + pixelDifference;
            if (added >= maxHeight) {
                return nextPage();
            }
            return new PagePosition(page, added);
        }

        public PagePosition guaranteeSpace(int required, int maxPageHeight) {
            PagePosition next = nextLine(required, maxPageHeight);
            if (next.page == page) return this;
            return next;
        }

        public PagePosition nextPage() {
            return new PagePosition(page + 1, 0);
        }

        public PagePosition newPage() {
            if (pixel != 0) {
                return nextPage();
            }
            return this;
        }
    }

    protected final GuiGuide gui;
    private IFontRenderer fontRenderer;
    protected boolean wasHovered = false;
    protected boolean wasIconHovered = false;
    protected boolean didRender = false;

    public GuidePart(GuiGuide gui) {
        this.gui = gui;
    }

    public IFontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void setFontRenderer(IFontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }

    public boolean wasHovered() {
        return wasHovered;
    }

    public void updateScreen() {}

    protected void renderTextLine(String text, int x, int y, int colour) {
        if (fontRenderer != null) {
            fontRenderer.drawString(text, x, y + 8 - (fontRenderer.getFontHeight(text) / 2), colour);
        }
    }

    public abstract PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index);

    public abstract PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY);

    public void handleMouseDragPartial(int startX, int startY, int currentX, int currentY, int button) {}

    public void handleMouseDragFinish(int startX, int startY, int endX, int endY, int button) {}

    protected PagePosition renderLine(PagePosition current, PageLine line, int x, int y, int width, int height,
        int pageRenderIndex) {
        wasHovered = false;
        wasIconHovered = false;
        int allowedWidth = width - INDENT_WIDTH * line.indent;
        if (allowedWidth <= 0) {
            throw new IllegalStateException("Was indented too far");
        }

        String toRender = line.text;
        ISimpleDrawable icon = line.startIcon;
        FormatString next = FormatString.split(line.text);
        int neededSpace = fontRenderer != null ? fontRenderer.getFontHeight(line.text) : 9;
        if (icon != null) {
            neededSpace = Math.max(16, neededSpace);
        }
        current = current.guaranteeSpace(neededSpace, height);

        int _x = x + INDENT_WIDTH * line.indent;

        int iconX = _x - 18;
        int iconY = y + current.pixel - 5;

        GuiRectangle iconRect = (icon != null) ? new GuiRectangle(_x - 20, iconY, 20, 16) : null;
        boolean iconVisibleHere = (icon != null) && (current.page == pageRenderIndex);

        didRender = false;

        java.util.List<WrapSegment> segments = new java.util.ArrayList<>();
        PagePosition cursor = current;
        while (next != null) {
            FormatString[] strings = fontRenderer != null
                ? next.wrap(fontRenderer, allowedWidth)
                : new FormatString[] { next };

            String text = strings[0].getFormatted();
            int _y = y + cursor.pixel;
            int _w = fontRenderer != null ? fontRenderer.getStringWidth(text) : text.length() * 6;

            int rowTop = _y - 5;

            GuiRectangle rect = new GuiRectangle(_x - 2, rowTop, _w + 4, LINE_HEIGHT);
            boolean rendered = cursor.page == pageRenderIndex;
            segments.add(new WrapSegment(text, rect, _y, rowTop, _w, rendered));

            next = strings.length == 1 ? null : strings[1];
            int fontHeight = fontRenderer != null ? fontRenderer.getFontHeight(text) : 9;
            cursor = cursor.nextLine(fontHeight + 3, height);
        }
        current = cursor;

        boolean iconHovered = iconRect != null && iconRect.contains(gui.mouse);
        boolean entryHovered = iconHovered;
        if (!entryHovered) {
            for (WrapSegment seg : segments) {
                if (seg.rect.contains(gui.mouse)) {
                    entryHovered = true;
                    break;
                }
            }
        }
        wasHovered = entryHovered;
        wasIconHovered = iconHovered;

        boolean drewAny = false;
        if (entryHovered && line.link) {
            buildcraft.lib.gui.BCGraphics g = buildcraft.lib.gui.GuiIcon.getGuiGraphics();
            if (g != null) {
                boolean isFirstRendered = true;
                for (WrapSegment seg : segments) {
                    if (!seg.rendered) continue;
                    int fillLeft = (isFirstRendered && iconVisibleHere) ? _x - 20 : _x - 2;
                    g.fill(fillLeft, seg.rowTop, _x + seg.width + 2, seg.rowTop + LINE_HEIGHT, 0xFFD3AD6C);
                    isFirstRendered = false;
                }
            }
        }
        if (iconVisibleHere) {
            ISimpleDrawable toDraw = (entryHovered && line.startIconHovered != null)
                ? line.startIconHovered
                : icon;
            toDraw.drawAt(iconX, iconY);
        }
        for (WrapSegment seg : segments) {
            if (!seg.rendered) continue;
            drewAny = true;
            if (fontRenderer != null) {
                fontRenderer.drawString(seg.text, _x, seg.y, 0xFF000000);
            }
        }
        didRender = drewAny;

        if (entryHovered && didRender) {
            renderTooltip();
        }

        int fontHeight = fontRenderer != null ? fontRenderer.getFontHeight(toRender) : 9;
        int additional = LINE_HEIGHT - fontHeight - 3;
        current = current.nextLine(additional, height);
        return current;
    }

    private static final class WrapSegment {
        final String text;
        final GuiRectangle rect;
        final int y;
        final int rowTop;
        final int width;
        final boolean rendered;

        WrapSegment(String text, GuiRectangle rect, int y, int rowTop, int width, boolean rendered) {
            this.text = text;
            this.rect = rect;
            this.y = y;
            this.rowTop = rowTop;
            this.width = width;
            this.rendered = rendered;
        }
    }

    protected PagePosition renderLines(Iterable<PageLine> lines, PagePosition part, int x, int y, int width, int height,
        int index) {
        for (PageLine line : lines) {
            part = renderLine(part, line, x, y, width, height, index);
        }
        return part;
    }

    protected PagePosition renderLines(Iterable<PageLine> lines, int x, int y, int width, int height, int index) {
        return renderLines(lines, new PagePosition(0, 0), x, y, width, height, index);
    }

    protected void renderTooltip() {}
}
