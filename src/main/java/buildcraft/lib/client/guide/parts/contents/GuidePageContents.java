/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import buildcraft.lib.BCLib;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.TypeOrder;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.GuidePart.PagePosition;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.search.ISuffixArray.SearchResult;

@SuppressWarnings("this-escape")
public class GuidePageContents extends GuidePageBase {
    private static final int ORDER_OFFSET_X = -10;
    private static final int ORDER_OFFSET_Y = -10;

    private ContentsNodeGui contents;
    private final EditBox searchText;
    private String lastSearchText = "";

    private int realResultCount = -1;

    public GuidePageContents(GuiGuide gui) {
        super(gui);
        loadMainGui();
        searchText = new EditBox(Minecraft.getInstance().font, 0, 0, 80, 14, Component.empty());
        searchText.setBordered(false);
        searchText.setTextColor(0xFF_00_00_00);

        searchText.setTextShadow(false);
        setupChapters();
    }

    @Override
    public GuidePageBase createReloaded() {
        GuidePageContents newPage = new GuidePageContents(gui);
        newPage.searchText.setValue(searchText.getValue());
        newPage.searchText.setCursorPosition(searchText.getCursorPosition());
        newPage.searchText.setFocused(searchText.isFocused());
        newPage.numPages = numPages;
        newPage.goToPage(getIndex());
        return newPage;
    }

    public void loadMainGui() {
        contents = GuideManager.INSTANCE.getGuiContents(gui, this, gui.sortingOrder);
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        contents.setFontRenderer(fontRenderer);
    }

    @Override
    public List<GuideChapter> getChapters() {
        return contents.getChapters();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (lastSearchText.equals(searchText.getValue())) {
            if (numPages >= 3 && getPage() >= numPages) {
                goToPage(numPages);
            }
        } else {
            lastSearchText = searchText.getValue();
            numPages = -1;
            if (lastSearchText.isEmpty()) {
                realResultCount = -1;
                contents.node.resetVisibility();
                contents.invalidate();
                setupChapters();
            } else {
                String text = lastSearchText.toLowerCase(Locale.ROOT);
                SearchResult<PageLink> ret = GuideManager.INSTANCE.quickSearcher.search(
                    text, 100
                );
                realResultCount = ret.hasAllResults() ? -1 : ret.realResultCount;
                Set<PageLink> matches = new HashSet<>(ret.results);
                contents.node.setVisible(matches);
                contents.invalidate();

                if (contents.node.isVisible()) {
                    searchText.setTextColor(0xFF_00_00_00);
                } else {
                    searchText.setTextColor(0xFF_FF_00_00);
                }
                if (getPage() < 2) {
                    goToPage(2);
                }
                setupChapters();
            }
            gui.refreshChapters();
        }
    }

    @Override
    protected void renderPage(int x, int y, int width, int height, int index) {
        IFontRenderer f = getFontRenderer();
        if (index == 0) {
            int xMiddle = x + width / 2;
            int _y = y;
            String text = gui.book == null ? "Everything" : gui.book.title.getString();
            _y += 3;
            f.drawString(text, xMiddle, _y, 0, false, true);
            _y += f.getFontHeight(text) + 5;

            String vers = "BuildCraft 26.1";
            f.drawString(vers, xMiddle, _y, 0, false, true);

            _y = y + height - 80;
            f.drawString(LocaleUtil.localize("options.title"), xMiddle, _y, 0, false, true, 2f);
            _y += 28;
            f.drawString("Show Lore " + (XmlPageLoader.SHOW_LORE ? "[x]" : "[ ]"), xMiddle, _y, 0, false, true);
            _y += 14;
            f.drawString("Show Hints " + (XmlPageLoader.SHOW_HINTS ? "[x]" : "[ ]"), xMiddle, _y, 0, false, true);
        } else if (index == 1) {
            int _height = gui.bookData.loadedMods.size() + 1;
            if (gui.bookData.loadedOther.size() > 0) {
                _height++;
                _height += gui.bookData.loadedOther.size();
            }
            int perLineHeight = f.getFontHeight("Ly") + 3;
            _height *= perLineHeight;
            int _y = y + (height - _height) / 2;

            if (gui.bookData.loadedMods.size() > 0) {
                drawCenteredText(ChatFormatting.BOLD + "Loaded Mods:", x, _y, width);
                _y += perLineHeight;
                for (String text : gui.bookData.loadedMods) {
                    drawCenteredText(text, x, _y, width);
                    _y += perLineHeight;
                }
            }
            if (gui.bookData.loadedOther.size() > 0) {
                drawCenteredText(ChatFormatting.BOLD + "Loaded Resource Packs:", x, _y, width);
                _y += perLineHeight;
                for (String text : gui.bookData.loadedOther) {
                    drawCenteredText(text, x, _y, width);
                    _y += perLineHeight;
                }
            }
        }

        if (index % 2 == 0) {
            searchText.setX(x + 23);
            searchText.setY(y - 16);
            if (!searchText.isFocused() && searchText.getValue().isEmpty()) {
                GuiGuide.SEARCH_TAB_CLOSED.drawAt(x + 8, y - 20);
                GuiGuide.SEARCH_ICON.drawAt(x + 8, y - 19);
            } else {
                GuiGuide.SEARCH_TAB_OPEN.drawAt(x - 2, y - 22);
                GuiGuide.SEARCH_ICON.drawAt(x + 8, y - 18);
            }

            if (GuiIcon.getGuiGraphics() != null) {

                searchText.extractRenderState(

                    GuiIcon.getGuiGraphics().raw,
                    (int) gui.mouse.getX(),
                    (int) gui.mouse.getY(),
                    gui.getLastPartialTicks()
                );
            }

            if (realResultCount >= 0) {
                String text = I18n.get("buildcraft.guide.too_many_results", realResultCount);
                getFontRenderer().drawString(text, x + 105, y - 23, -1);
            }

            if (index != 0) {
                int oX = x + ORDER_OFFSET_X;
                int oY = y + ORDER_OFFSET_Y;
                for (int j = 0; j < GuiGuide.ORDERS.length; j++) {
                    GuiIcon icon = GuiGuide.ORDERS[j];
                    TypeOrder typeOrder = GuiGuide.SORTING_TYPES[j];
                    if (gui.sortingOrder == typeOrder) {
                        icon = icon.offset(0, 14);
                    }
                    if (icon.containsGuiPos(oX, oY, gui.mouse)) {
                        icon = icon.offset(0, 28);
                        gui.tooltips.add(Collections.singletonList(LocaleUtil.localize(typeOrder.localeKey)));
                    }
                    icon.drawAt(oX, oY);
                    oY += 14;
                }
            }
        }

        PagePosition pos = new PagePosition(2, 0);

        pos = contents.render(x, y, width, height, pos, index);

        if (numPages == -1) {
            numPages = pos.page + 1;
        }
        super.renderPage(x, y, width, height, index);
    }

    private void drawCenteredText(String text, int x, int y, int width) {
        IFontRenderer f = getFontRenderer();
        int fWidth = f.getStringWidth(text);
        f.drawString(text, (x + (width - fWidth) / 2), y, 0);
    }

    @Override
    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton,
        int index, boolean isEditing) {
        super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index, isEditing);
        if (index % 2 == 0) {
            if (index != 0) {
                int oX = x + ORDER_OFFSET_X;
                int oY = y + ORDER_OFFSET_Y;
                for (TypeOrder order : GuiGuide.SORTING_TYPES) {
                    GuiRectangle rect = new GuiRectangle(oX, oY, 14, 14);
                    if (rect.contains(gui.mouse)) {
                        gui.sortingOrder = order;
                        loadMainGui();
                        lastSearchText = "@@@@INVALID@@@";
                        gui.refreshChapters();
                        contents.setFontRenderer(getFontRenderer());
                        return;
                    }
                    oY += 14;
                }
            }
        }

        if (mouseButton == 0) {
            if (index == 0) {
                IFontRenderer f = getFontRenderer();
                String text = XmlPageLoader.SHOW_LORE ? "Show Lore [x]" : "Show Lore [ ]";
                int fWidth = f.getStringWidth(text);
                GuiRectangle rect;
                rect = new GuiRectangle(x + (width - fWidth) / 2, y + height - 52, fWidth, f.getFontHeight(text));
                if (rect.contains(mouseX, mouseY)) {
                    XmlPageLoader.SHOW_LORE = !XmlPageLoader.SHOW_LORE;
                }

                text = XmlPageLoader.SHOW_HINTS ? "Show Hints [x]" : "Show Hints [ ]";
                fWidth = f.getStringWidth(text);
                rect = new GuiRectangle(x + (width - fWidth) / 2, y + height - 38, fWidth, f.getFontHeight(text));
                if (rect.contains(mouseX, mouseY)) {
                    XmlPageLoader.SHOW_HINTS = !XmlPageLoader.SHOW_HINTS;
                }
            }
        }

        if (new GuiRectangle(x, y, width, height).contains(mouseX, mouseY)) {
            contents.onClicked(x, y, width, height, new PagePosition(2, 0), index);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int mouseButton = event.button();

        if (searchText.isFocused() && searchText.mouseClicked(event, doubleClick)) {
             return true;
        }

        if (getIndex() != 0 && isOverOrderIcon(mouseX, mouseY)) {
            return false;
        }

        if (!searchText.isFocused() && new GuiRectangle(searchText.getX() - 25, searchText.getY() - 18, 40, 34).contains(mouseX, mouseY)) {
             searchText.setFocused(true);
             return true;
        }

        if (mouseButton == 1 && mouseX >= searchText.getX() && mouseX < searchText.getX() + searchText.getWidth()
            && mouseY >= searchText.getY() && mouseY < searchText.getY() + searchText.getHeight()) {
            searchText.setValue("");
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    private boolean isOverOrderIcon(double mouseX, double mouseY) {
        int pageX = searchText.getX() - 23;
        int pageY = searchText.getY() + 16;
        int oX = pageX + ORDER_OFFSET_X;
        int oY = pageY + ORDER_OFFSET_Y;
        for (int i = 0; i < GuiGuide.SORTING_TYPES.length; i++) {
            if (new GuiRectangle(oX, oY, 14, 14).contains(mouseX, mouseY)) {
                return true;
            }
            oY += 14;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchText.isFocused()) {
            if (searchText.keyPressed(event)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchText.isFocused()) {
            if (searchText.charTyped(event)) {
                return true;
            }
        }
        return super.charTyped(event);
    }
}
