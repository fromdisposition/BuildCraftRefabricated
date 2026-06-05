/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;

@SuppressWarnings("this-escape")
public class LedgerHelp extends Ledger_Neptune {
    private static final Identifier ICON_HELP = Identifier.parse("buildcraftlib:textures/icons/help.png");

    private static final int BORDER = 2;

    private IGuiElement selected = null;
    private boolean foundAny = false;
    private boolean init = false;

    private ElementHelpInfo currentHelpInfo = null;

    public LedgerHelp(BuildCraftGui gui, boolean expandPositive) {

        super(gui, 0xFF_CC_99_FF, expandPositive);
        this.title = "gui.ledger.help";
        calculateMaxSize();
    }

    @Override
    public void tick() {
        super.tick();
        if (currentWidth == CLOSED_WIDTH && currentHeight == CLOSED_HEIGHT) {
            selected = null;
            currentHelpInfo = null;
        }
    }

    @Override
    protected void drawIcon(double x, double y, BCGraphics graphics) {
        if (!init) {
            init = true;
            List<HelpPosition> elements = new ArrayList<>();
            for (IGuiElement element : gui.shownElements) {
                element.addHelpElements(elements);
            }
            foundAny = !elements.isEmpty();
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_HELP,
            (int) x, (int) y, 0f, 0f, 16, 16, 16, 16);
    }

    @Override
    public void drawBackground(float partialTicks) {

        super.drawBackground(partialTicks);

        if (!shouldDrawOpen()) {
            return;
        }

        BCGraphics graphics = GuiIcon.getGuiGraphics();
        if (graphics == null) return;

        boolean set = false;
        List<HelpPosition> elements = new ArrayList<>();
        for (IGuiElement element : gui.shownElements) {
            element.addHelpElements(elements);
            foundAny |= !elements.isEmpty();
            for (HelpPosition info : elements) {
                IGuiArea rect = info.target;
                boolean isHovered = rect.contains(gui.mouse);
                if (isHovered && !set) {
                    if (selected != element) {
                        selected = element;

                        updateHelpText(info.info);
                    }
                    set = true;
                }
                boolean isSelected = selected == element;

                drawHighlightBorder(graphics, rect, info.info.colour, isHovered, isSelected);
            }
            elements.clear();
        }
    }

    private void drawHighlightBorder(BCGraphics graphics, IGuiArea rect, int colour,
                                      boolean isHovered, boolean isSelected) {
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int w = (int) rect.getWidth();
        int h = (int) rect.getHeight();

        int alpha;
        if (isHovered && isSelected) {
            alpha = 0xDD;
        } else if (isHovered || isSelected) {
            alpha = 0xBB;
        } else {
            alpha = 0x88;
        }

        int borderColour = (alpha << 24) | (colour & 0x00FFFFFF);

        int bx = x - BORDER;
        int by = y - BORDER;
        int bw = w + BORDER * 2;
        int bh = h + BORDER * 2;

        graphics.fill(bx, by, bx + bw, by + BORDER, borderColour);

        graphics.fill(bx, y + h, bx + bw, y + h + BORDER, borderColour);

        graphics.fill(bx, by + BORDER, bx + BORDER, y + h, borderColour);

        graphics.fill(x + w, by + BORDER, x + w + BORDER, y + h, borderColour);

        if (isHovered || isSelected) {
            int fillAlpha = isHovered ? 0x33 : 0x22;
            int fillColour = (fillAlpha << 24) | (colour & 0x00FFFFFF);
            graphics.fill(x, y, x + w, y + h, fillColour);
        }
    }

    private void updateHelpText(ElementHelpInfo info) {
        if (info == currentHelpInfo) return;
        currentHelpInfo = info;

        clearTextEntries();

        String localizedTitle = LocaleUtil.localize(info.title);
        appendText(localizedTitle, info.colour & 0x00FFFFFF).setDropShadow(true);

        for (String key : info.localeKeys) {
            if (key == null) continue;
            String text;
            if (info.isPreTranslated) {
                text = key;
            } else {
                text = LocaleUtil.localize(key);
            }
            if (!text.isEmpty()) {
                appendText(text, 0xFFFFFF);
            }
        }

        calculateMaxSize();
    }
}
