/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.TypeOrder;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.search.ISuffixArray;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
//? if >= 1.21.10 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class GuidePageContents extends GuidePageBase {
   private static final int ORDER_OFFSET_X = -10;
   private static final int ORDER_OFFSET_Y = -10;
   private ContentsNodeGui contents;
   private final EditBox searchText;
   private String lastSearchText = "";
   private int realResultCount = -1;

   public GuidePageContents(GuiGuide gui) {
      super(gui);
      this.loadMainGui();
      this.searchText = new EditBox(Minecraft.getInstance().font, 0, 0, 80, 14, Component.empty());
      this.searchText.setBordered(false);
      this.searchText.setTextColor(-16777216);
      //? if >= 1.21.10 {
      this.searchText.setTextShadow(false);
      //?}
      this.setupChapters();
   }

   @Override
   public GuidePageBase createReloaded() {
      GuidePageContents newPage = new GuidePageContents(this.gui);
      newPage.searchText.setValue(this.searchText.getValue());
      newPage.searchText.setCursorPosition(this.searchText.getCursorPosition());
      newPage.searchText.setFocused(this.searchText.isFocused());
      newPage.numPages = this.numPages;
      newPage.goToPage(this.getIndex());
      return newPage;
   }

   public void loadMainGui() {
      this.contents = GuideManager.INSTANCE.getGuiContents(this.gui, this, this.gui.sortingOrder);
   }

   @Override
   public void setFontRenderer(IFontRenderer fontRenderer) {
      super.setFontRenderer(fontRenderer);
      this.contents.setFontRenderer(fontRenderer);
   }

   @Override
   public List<GuideChapter> getChapters() {
      return this.contents.getChapters();
   }

   @Override
   public String getTitle() {
      return null;
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
      if (this.lastSearchText.equals(this.searchText.getValue())) {
         if (this.numPages >= 3 && this.getPage() >= this.numPages) {
            this.goToPage(this.numPages);
         }
      } else {
         this.lastSearchText = this.searchText.getValue();
         this.numPages = -1;
         if (this.lastSearchText.isEmpty()) {
            this.realResultCount = -1;
            this.contents.node.resetVisibility();
            this.contents.invalidate();
            this.setupChapters();
         } else {
            String text = this.lastSearchText.toLowerCase(Locale.ROOT);
            ISuffixArray.SearchResult<PageLink> ret = GuideManager.INSTANCE.quickSearcher.search(text, BCLibConfig.maxGuideSearchCount.get());
            this.realResultCount = ret.hasAllResults() ? -1 : ret.realResultCount;
            Set<PageLink> matches = new HashSet<>(ret.results);
            this.contents.node.setVisible(matches);
            this.contents.invalidate();
            if (this.contents.node.isVisible()) {
               this.searchText.setTextColor(-16777216);
            } else {
               this.searchText.setTextColor(-65536);
            }

            if (this.getPage() < 2) {
               this.goToPage(2);
            }

            this.setupChapters();
         }

         this.gui.refreshChapters();
      }
   }

   @Override
   protected void renderPage(int x, int y, int width, int height, int index) {
      IFontRenderer f = this.getFontRenderer();
      if (index == 0) {
         int xMiddle = x + width / 2;
         int _y = y;
         String text = this.gui.book == null ? "Everything" : this.gui.book.title.getString();
         _y += 3;
         f.drawString(text, xMiddle, _y, 0, false, true);
         _y += f.getFontHeight(text) + 5;
         String vers = "BuildCraft";
         f.drawString(vers, xMiddle, _y, 0, false, true);
         _y = y + height - 80;
         f.drawString(LocaleUtil.localize("options.title"), xMiddle, _y, 0, false, true, 2.0F);
         _y += 28;
         f.drawString("Show Lore " + (XmlPageLoader.SHOW_LORE ? "[x]" : "[ ]"), xMiddle, _y, 0, false, true);
         _y += 14;
         f.drawString("Show Hints " + (XmlPageLoader.SHOW_HINTS ? "[x]" : "[ ]"), xMiddle, _y, 0, false, true);
      } else if (index == 1) {
         int _height = this.gui.bookData.loadedMods.size() + 1;
         if (this.gui.bookData.loadedOther.size() > 0) {
            _height = ++_height + this.gui.bookData.loadedOther.size();
         }

         int perLineHeight = f.getFontHeight("Ly") + 3;
         _height *= perLineHeight;
         int _y = y + (height - _height) / 2;
         if (this.gui.bookData.loadedMods.size() > 0) {
            this.drawCenteredText(ChatFormatting.BOLD + "Loaded Mods:", x, _y, width);
            _y += perLineHeight;

            for (String text : this.gui.bookData.loadedMods) {
               this.drawCenteredText(text, x, _y, width);
               _y += perLineHeight;
            }
         }

         if (this.gui.bookData.loadedOther.size() > 0) {
            this.drawCenteredText(ChatFormatting.BOLD + "Loaded Resource Packs:", x, _y, width);
            _y += perLineHeight;

            for (String text : this.gui.bookData.loadedOther) {
               this.drawCenteredText(text, x, _y, width);
               _y += perLineHeight;
            }
         }
      }

      if (index % 2 == 0) {
         this.searchText.setX(x + 23);
         this.searchText.setY(y - 16);
         if (!this.searchText.isFocused() && this.searchText.getValue().isEmpty()) {
            GuiGuide.SEARCH_TAB_CLOSED.drawAt(x + 8, y - 20);
            GuiGuide.SEARCH_ICON.drawAt(x + 8, y - 19);
         } else {
            GuiGuide.SEARCH_TAB_OPEN.drawAt(x - 2, y - 22);
            GuiGuide.SEARCH_ICON.drawAt(x + 8, y - 18);
         }

         if (GuiIcon.getGuiGraphics() != null) {
            //? if >= 26.1 {
            this.searchText
               .extractRenderState(GuiIcon.getGuiGraphics().raw, (int)this.gui.mouse.getX(), (int)this.gui.mouse.getY(), this.gui.getLastPartialTicks());
            //?} else {
            /*this.searchText
               .render(GuiIcon.getGuiGraphics().raw, (int)this.gui.mouse.getX(), (int)this.gui.mouse.getY(), this.gui.getLastPartialTicks());
            *///?}
         }

         if (this.realResultCount >= 0) {
            String text = I18n.get("buildcraft.guide.too_many_results", new Object[]{this.realResultCount});
            this.getFontRenderer().drawString(text, x + 105, y - 23, -1);
         }

         if (index != 0) {
            int oX = x + -10;
            int oY = y + -10;

            for (int j = 0; j < GuiGuide.ORDERS.length; j++) {
               GuiIcon icon = GuiGuide.ORDERS[j];
               TypeOrder typeOrder = GuiGuide.SORTING_TYPES[j];
               if (this.gui.sortingOrder == typeOrder) {
                  icon = icon.offset(0.0, 14.0);
               }

               if (icon.containsGuiPos(oX, oY, this.gui.mouse)) {
                  icon = icon.offset(0.0, 28.0);
                  this.gui.tooltips.add(Collections.singletonList(LocaleUtil.localize(typeOrder.localeKey)));
               }

               icon.drawAt(oX, oY);
               oY += 14;
            }
         }
      }

      GuidePart.PagePosition pos = new GuidePart.PagePosition(2, 0);
      pos = this.contents.render(x, y, width, height, pos, index);
      if (this.numPages == -1) {
         this.numPages = pos.page + 1;
      }

      super.renderPage(x, y, width, height, index);
   }

   private void drawCenteredText(String text, int x, int y, int width) {
      IFontRenderer f = this.getFontRenderer();
      int fWidth = f.getStringWidth(text);
      f.drawString(text, x + (width - fWidth) / 2, y, 0);
   }

   @Override
   public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton, int index, boolean isEditing) {
      super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index, isEditing);
      if (index % 2 == 0 && index != 0) {
         int oX = x + -10;
         int oY = y + -10;

         for (TypeOrder order : GuiGuide.SORTING_TYPES) {
            GuiRectangle rect = new GuiRectangle(oX, oY, 14.0, 14.0);
            if (rect.contains(this.gui.mouse)) {
               this.gui.sortingOrder = order;
               this.loadMainGui();
               this.lastSearchText = "@@@@INVALID@@@";
               this.gui.refreshChapters();
               this.contents.setFontRenderer(this.getFontRenderer());
               return;
            }

            oY += 14;
         }
      }

      if (mouseButton == 0 && index == 0) {
         IFontRenderer f = this.getFontRenderer();
         String text = XmlPageLoader.SHOW_LORE ? "Show Lore [x]" : "Show Lore [ ]";
         int fWidth = f.getStringWidth(text);
         GuiRectangle rect = new GuiRectangle(x + (width - fWidth) / 2, y + height - 52, fWidth, f.getFontHeight(text));
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

      if (new GuiRectangle(x, y, width, height).contains(mouseX, mouseY)) {
         this.contents.onClicked(x, y, width, height, new GuidePart.PagePosition(2, 0), index);
      }
   }

   @Override
   //? if >= 1.21.10 {
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x();
      double mouseY = event.y();
      int mouseButton = event.button();
      if (this.searchText.isFocused() && this.searchText.mouseClicked(event, doubleClick)) {
         return true;
      } else if (this.getIndex() != 0 && this.isOverOrderIcon(mouseX, mouseY)) {
         return false;
      } else if (!this.searchText.isFocused()
         && new GuiRectangle(this.searchText.getX() - 25, this.searchText.getY() - 18, 40.0, 34.0).contains(mouseX, mouseY)) {
         this.searchText.setFocused(true);
         return true;
      } else if (mouseButton == 1
         && mouseX >= this.searchText.getX()
         && mouseX < this.searchText.getX() + this.searchText.getWidth()
         && mouseY >= this.searchText.getY()
         && mouseY < this.searchText.getY() + this.searchText.getHeight()) {
         this.searchText.setValue("");
         return true;
      } else {
         return super.mouseClicked(event, doubleClick);
      }
   }
   //?} else {
   /*public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
      if (this.searchText.isFocused() && this.searchText.mouseClicked(mouseX, mouseY, mouseButton)) {
         return true;
      } else if (this.getIndex() != 0 && this.isOverOrderIcon(mouseX, mouseY)) {
         return false;
      } else if (!this.searchText.isFocused()
         && new GuiRectangle(this.searchText.getX() - 25, this.searchText.getY() - 18, 40.0, 34.0).contains(mouseX, mouseY)) {
         this.searchText.setFocused(true);
         return true;
      } else if (mouseButton == 1
         && mouseX >= this.searchText.getX()
         && mouseX < this.searchText.getX() + this.searchText.getWidth()
         && mouseY >= this.searchText.getY()
         && mouseY < this.searchText.getY() + this.searchText.getHeight()) {
         this.searchText.setValue("");
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, mouseButton);
      }
   }
   *///?}

   private boolean isOverOrderIcon(double mouseX, double mouseY) {
      int pageX = this.searchText.getX() - 23;
      int pageY = this.searchText.getY() + 16;
      int oX = pageX + -10;
      int oY = pageY + -10;

      for (int i = 0; i < GuiGuide.SORTING_TYPES.length; i++) {
         if (new GuiRectangle(oX, oY, 14.0, 14.0).contains(mouseX, mouseY)) {
            return true;
         }

         oY += 14;
      }

      return false;
   }

   @Override
   //? if >= 1.21.10 {
   public boolean keyPressed(KeyEvent event) {
      return this.searchText.isFocused() && this.searchText.keyPressed(event) ? true : super.keyPressed(event);
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      return this.searchText.isFocused() && this.searchText.charTyped(event) ? true : super.charTyped(event);
   }
   //?} else {
   /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.searchText.isFocused() && this.searchText.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.searchText.isFocused() && this.searchText.charTyped(chr, modifiers) ? true : super.charTyped(chr, modifiers);
   }
   *///?}
}
