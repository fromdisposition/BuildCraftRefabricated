/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;

public abstract class GuidePageBase extends GuidePart {
    private int index = 0;
    protected int numPages = -1;

    public GuidePageBase(GuiGuide gui) {
        super(gui);
    }

    protected void setupChapters() {
        List<GuideChapter> lastChapterAtLevel = new ArrayList<>();
        List<GuideChapter> chapters = getChapters();
        for (GuideChapter chapter : chapters) {
            chapter.parent = null;
            chapter.children.clear();
        }
        for (GuideChapter chapter : chapters) {
            int gap = chapter.level - lastChapterAtLevel.size();
            if (gap < 0) {
                lastChapterAtLevel.subList(chapter.level, lastChapterAtLevel.size()).clear();
            }
            for (int g = Math.min(chapter.level, lastChapterAtLevel.size()) - 1; g >= 0; g--) {
                GuideChapter parent = lastChapterAtLevel.get(g);
                if (parent != null) {
                    parent.children.add(chapter);
                    chapter.parent = parent;
                    break;
                }
            }
            for (int g = 1; g < gap; g++) {
                lastChapterAtLevel.add(null);
            }
            lastChapterAtLevel.add(chapter);
        }
        int idx = 0;
        for (GuideChapter c : chapters) {
            if (c.hasParent()) continue;
            c.colourIndex = idx++ % GuideChapter.COLOURS.length;
            if (c.hasChildren()) c.assignChildIndices();
        }
    }

    protected final int getIndex() { return index; }

    public final void nextPage() {
        if (index + 2 < numPages) index += 2;
    }

    public final void lastPage() {
        index -= 2;
        if (index < 0) index = 0;
    }

    protected final void goToPage(int page) {
        if (numPages > 0 && page >= numPages) page = numPages - 1;
        index = page / 2;
        index *= 2;
        if (index < 0) index = 0;
    }

    public int getPage() { return index; }
    public int getPageCount() { return numPages; }
    public void tick() {}

    @Override
    public final PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        return current;
    }

    public abstract String getTitle();

    public boolean shouldPersistHistory() { return true; }

    @Nullable
    public GuidePageBase createReloaded() { return null; }

    public abstract List<GuideChapter> getChapters();

    protected GuidePart getClicked(Iterable<GuidePart> iterable, int x, int y, int width, int height, int mouseX,
        int mouseY, int index) {
        PagePosition pos = new PagePosition(0, 0);
        for (GuidePart part : iterable) {
            pos = part.renderIntoArea(x, y, width, height, pos, -1);
            if (pos.page == index && part.wasHovered) {
                return part;
            }
            if (pos.page > index) return null;
        }
        return null;
    }

    public void renderFirstPage(int x, int y, int width, int height) {
        renderPage(x, y, width, height, index);
    }

    public void renderSecondPage(int x, int y, int width, int height) {
        renderPage(x, y, width, height, index + 1);
    }

    protected void renderPage(int x, int y, int width, int height, int index) {
        IFontRenderer font = getFontRenderer();
        if (font == null || numPages <= 0) return;

        if (index % 2 == 0) {
            String text = (index + 1) + " / " + numPages;
            double textX = x + GuiGuide.PAGE_LEFT_TEXT.getWidth() / 2 - font.getStringWidth(text) / 2;
            font.drawString(text, (int) textX, y + height + 6, 0xFF90816a);
        } else {
            if (index + 1 <= numPages) {
                String text = (index + 1) + " / " + numPages;
                double textX = x + (GuiGuide.PAGE_RIGHT_TEXT.getWidth() - font.getStringWidth(text)) / 2;
                font.drawString(text, (int) textX, y + height + 6, 0xFF90816a);
            }
        }
    }

    @Override
    public final PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        return current;
    }

    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton,
        int index, boolean isEditing) {

    }

    @Override
    public final void handleMouseDragPartial(int startX, int startY, int currentX, int currentY, int button) {}

    @Override
    public final void handleMouseDragFinish(int startX, int startY, int endX, int endY, int button) {}

    public boolean keyTyped(char typedChar, int keyCode) {
        return false;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        return false;
    }
}
