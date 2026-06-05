/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;

public class GuideChapterWithin extends GuideChapter {
    private int lastPage = -1;

    private final boolean inlineRender;

    public GuideChapterWithin(GuiGuide gui, int level, String text) {
        this(gui, level, text, true);
    }

    public GuideChapterWithin(GuiGuide gui, String chapter) {
        this(gui, 0, chapter, true);
    }

    public GuideChapterWithin(GuiGuide gui, int level, String text, boolean inlineRender) {
        super(gui, level, text);
        this.inlineRender = inlineRender;
    }

    public GuideChapterWithin(GuiGuide gui, String chapter, boolean inlineRender) {
        this(gui, 0, chapter, inlineRender);
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (!inlineRender) {

            lastPage = current.page;
            return current;
        }
        PagePosition pos = super.renderIntoArea(x, y, width, height, current, index);
        lastPage = pos.page;
        if (pos.pixel == 0) {
            lastPage = pos.page - 1;
        }
        return pos;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        if (!inlineRender) return current;
        return super.handleMouseClick(x, y, width, height, current, index, mouseX, mouseY);
    }

    @Override
    protected boolean onClick() {
        if (lastPage != -1) {
            GuidePageBase page = gui.getCurrentPage();
            if (page != null && page.getChapters().contains(this)) {
                page.goToPage(lastPage);
                return true;
            }
        }
        return false;
    }
}
