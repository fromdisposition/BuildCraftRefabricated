/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.font.FontManager;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.font.MinecraftFont;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.contents.GuidePageContents;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiFluid;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.MousePosition;
import buildcraft.lib.guide.GuideBook;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.guide.GuideContentsData;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.LocaleUtil;
import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class GuiGuide extends Screen {
   public static final Identifier ICONS_1 = Identifier.parse("minecraft:textures/gui/icons.png");
   public static final Identifier ICONS_2 = Identifier.parse("buildcraftlib:textures/gui/guide/icons.png");
   public static final Identifier COVER = Identifier.parse("buildcraftlib:textures/gui/guide/cover.png");
   public static final Identifier LEFT_PAGE = Identifier.parse("buildcraftlib:textures/gui/guide/left_page.png");
   public static final Identifier RIGHT_PAGE = Identifier.parse("buildcraftlib:textures/gui/guide/right_page.png");
   public static final Identifier LEFT_PAGE_BACK = Identifier.parse("buildcraftlib:textures/gui/guide/left_page_back.png");
   public static final Identifier RIGHT_PAGE_BACK = Identifier.parse("buildcraftlib:textures/gui/guide/right_page_back.png");
   public static final Identifier LEFT_PAGE_FIRST = Identifier.parse("buildcraftlib:textures/gui/guide/left_page_first.png");
   public static final Identifier RIGHT_PAGE_LAST = Identifier.parse("buildcraftlib:textures/gui/guide/right_page_last.png");
   public static final Identifier NOTE = Identifier.parse("buildcraftlib:textures/gui/guide/note.png");
   public static final GuiIcon BOOK_COVER = new GuiIcon(COVER, 0.0, 0.0, 202.0, 248.0);
   public static final GuiIcon BOOK_BINDING = new GuiIcon(COVER, 204.0, 0.0, 11.0, 248.0);
   public static final GuiIcon PAGE_LEFT = new GuiIcon(LEFT_PAGE, 0.0, 0.0, 193.0, 248.0);
   public static final GuiIcon PAGE_RIGHT = new GuiIcon(RIGHT_PAGE, 0.0, 0.0, 193.0, 248.0);
   public static final GuiIcon PAGE_LEFT_BACK = new GuiIcon(LEFT_PAGE_BACK, 0.0, 0.0, 193.0, 248.0);
   public static final GuiIcon PAGE_RIGHT_BACK = new GuiIcon(RIGHT_PAGE_BACK, 0.0, 0.0, 193.0, 248.0);
   public static final GuiIcon PAGE_LEFT_FIRST = new GuiIcon(LEFT_PAGE_FIRST, 0.0, 0.0, 193.0, 248.0);
   public static final GuiIcon PAGE_RIGHT_LAST = new GuiIcon(RIGHT_PAGE_LAST, 0.0, 0.0, 193.0, 248.0);
   public static final int PAGE_WIDTH = 168;
   public static final int PAGE_HEIGHT = 190;
   public static final GuiRectangle PAGE_LEFT_TEXT = new GuiRectangle(23.0, 25.0, 168.0, 190.0);
   public static final GuiRectangle PAGE_RIGHT_TEXT = new GuiRectangle(4.0, 25.0, 168.0, 190.0);
   public static final GuiIcon PEN_UP = new GuiIcon(ICONS_2, 0.0, 0.0, 14.0, 135.0);
   public static final GuiIcon PEN_ANGLED = new GuiIcon(ICONS_2, 17.0, 0.0, 100.0, 100.0);
   public static final GuiIcon PEN_HIDDEN_MIN = new GuiIcon(ICONS_2, 0.0, 4.0, 10.0, 5.0);
   public static final GuiIcon PEN_HIDDEN_MAX = new GuiIcon(ICONS_2, 0.0, 4.0, 10.0, 15.0);
   public static final GuiIcon TURN_BACK = new GuiIcon(ICONS_2, 23.0, 139.0, 18.0, 10.0);
   public static final GuiIcon TURN_BACK_HOVERED = new GuiIcon(ICONS_2, 23.0, 152.0, 18.0, 10.0);
   public static final GuiIcon TURN_FORWARDS = new GuiIcon(ICONS_2, 0.0, 139.0, 18.0, 10.0);
   public static final GuiIcon TURN_FORWARDS_HOVERED = new GuiIcon(ICONS_2, 0.0, 152.0, 18.0, 10.0);
   public static final GuiIcon BACK = new GuiIcon(ICONS_2, 48.0, 139.0, 17.0, 9.0);
   public static final GuiIcon BACK_HOVERED = new GuiIcon(ICONS_2, 48.0, 152.0, 17.0, 9.0);
   public static final GuiIcon BOX_EMPTY = new GuiIcon(ICONS_2, 0.0, 164.0, 16.0, 16.0);
   public static final GuiIcon BOX_MINUS = new GuiIcon(ICONS_2, 16.0, 164.0, 16.0, 16.0);
   public static final GuiIcon BOX_PLUS = new GuiIcon(ICONS_2, 32.0, 164.0, 16.0, 16.0);
   public static final GuiIcon BOX_TICKED = new GuiIcon(ICONS_2, 48.0, 164.0, 16.0, 16.0);
   public static final GuiIcon BOX_CHAPTER = new GuiIcon(ICONS_2, 64.0, 164.0, 16.0, 16.0);
   public static final GuiIcon BOX_SELECTED_EMPTY = new GuiIcon(ICONS_2, 0.0, 180.0, 16.0, 16.0);
   public static final GuiIcon BOX_SELECTED_MINUS = new GuiIcon(ICONS_2, 16.0, 180.0, 16.0, 16.0);
   public static final GuiIcon BOX_SELECTED_PLUS = new GuiIcon(ICONS_2, 32.0, 180.0, 16.0, 16.0);
   public static final GuiIcon BOX_SELECTED_TICKED = new GuiIcon(ICONS_2, 48.0, 180.0, 16.0, 16.0);
   public static final GuiIcon BOX_SELECTED_CHAPTER = new GuiIcon(ICONS_2, 64.0, 180.0, 16.0, 16.0);
   public static final SpriteRaw BOX_CODE_SPRITE = new SpriteRaw(ICONS_2, 80.0, 164.0, 16.0, 16.0, 256.0);
   public static final GuiIcon BOX_CODE = new GuiIcon(BOX_CODE_SPRITE, 256);
   public static final SpriteNineSliced BOX_CODE_SLICED = new SpriteNineSliced(BOX_CODE_SPRITE, 4, 4, 12, 12, 16);
   public static final GuiIcon BORDER_TOP_LEFT = new GuiIcon(ICONS_2, 0.0, 196.0, 13.0, 13.0);
   public static final GuiIcon BORDER_TOP_RIGHT = new GuiIcon(ICONS_2, 13.0, 196.0, 13.0, 13.0);
   public static final GuiIcon BORDER_BOTTOM_LEFT = new GuiIcon(ICONS_2, 0.0, 209.0, 13.0, 13.0);
   public static final GuiIcon BORDER_BOTTOM_RIGHT = new GuiIcon(ICONS_2, 13.0, 209.0, 13.0, 13.0);
   public static final GuiIcon ORDER_TYPE = new GuiIcon(ICONS_2, 0.0, 0.0, 14.0, 14.0);
   public static final GuiIcon ORDER_MOD_TYPE = new GuiIcon(ICONS_2, 14.0, 0.0, 14.0, 14.0);
   public static final GuiIcon ORDER_ALPHABETICAL = new GuiIcon(ICONS_2, 28.0, 0.0, 14.0, 14.0);
   public static final GuiIcon EXPANDED_ARROW = new GuiIcon(ICONS_2, 96.0, 164.0, 16.0, 16.0);
   public static final GuiIcon CLOSED_ARROW = new GuiIcon(ICONS_2, 96.0, 180.0, 16.0, 16.0);
   public static final GuiIcon CHAPTER_MARKER = new GuiIcon(ICONS_2, 0.0, 56.0, 32.0, 32.0);
   public static final GuiIcon CHAPTER_MARKER_LEFT = new GuiIcon(ICONS_2, 0.0, 56.0, 24.0, 32.0);
   public static final GuiIcon CHAPTER_MARKER_RIGHT = new GuiIcon(ICONS_2, 8.0, 56.0, 24.0, 32.0);
   public static final SpriteNineSliced CHAPTER_MARKER_9 = new SpriteNineSliced(CHAPTER_MARKER.sprite, 8, 8, 24, 24, 32);
   public static final SpriteNineSliced CHAPTER_MARKER_9_LEFT = new SpriteNineSliced(CHAPTER_MARKER_LEFT.sprite, 8, 8, 24, 24, 24, 32);
   public static final SpriteNineSliced CHAPTER_MARKER_9_RIGHT = new SpriteNineSliced(
      new SpriteRaw(ICONS_2, 8.0, 56.0, 24.0, 32.0, 256.0), 0, 8, 16, 24, 24, 32
   );
   public static final GuiIcon NOTE_PAGE = new GuiIcon(NOTE, 0.0, 0.0, 131.0, 164.0);
   public static final GuiIcon NOTE_UNDERLAY = new GuiIcon(ICONS_2, 0.0, 1.0, 3.0, 4.0);
   public static final GuiIcon NOTE_OVERLAY = new GuiIcon(ICONS_2, 0.0, 1.0, 2.0, 3.0);
   public static final GuiIcon SEARCH_ICON = new GuiIcon(ICONS_2, 26.0, 196.0, 12.0, 12.0);
   public static final GuiIcon SEARCH_TAB_CLOSED = new GuiIcon(ICONS_2, 58.0, 196.0, 14.0, 6.0);
   public static final GuiIcon SEARCH_TAB_OPEN = new GuiIcon(ICONS_2, 40.0, 209.0, 106.0, 14.0);
   public static final GuiIcon[] ORDERS = new GuiIcon[]{ORDER_TYPE, ORDER_MOD_TYPE, ORDER_ALPHABETICAL};
   public static final GuiRectangle BACK_POSITION = new GuiRectangle(
      PAGE_LEFT.width - BACK.width / 2, PAGE_LEFT.height - BACK.height - 2, BACK.width, BACK.height
   );
   public static final TypeOrder[] SORTING_TYPES = new TypeOrder[]{
      new TypeOrder("buildcraft.guide.order.type_subtype", ETypeTag.TYPE, ETypeTag.SUB_TYPE),
      new TypeOrder("buildcraft.guide.order.mod_type", ETypeTag.MOD, ETypeTag.TYPE),
      new TypeOrder("buildcraft.guide.order.alphabetical")
   };
   public static final IGuiArea FLOATING_CHAPTER_MENU = GuiUtil.moveRectangleToCentre(
      new GuiRectangle((PAGE_LEFT_TEXT.getWidth() + PAGE_RIGHT_TEXT.getWidth()) / 2.0, PAGE_LEFT.height - 20)
   );
   private static final float BOOK_OPEN_TIME = 10.0F;
   public final MousePosition mouse = new MousePosition();
   @Nullable
   public final GuideBook book;
   public final GuideContentsData bookData;
   public TypeOrder sortingOrder = SORTING_TYPES[0];
   private boolean isOpen = false;
   private boolean isOpening = false;
   private boolean showingContentsMenu = false;
   private float openingAngleLast = -90.0F;
   private float openingAngleNext = -90.0F;
   public int minX;
   public int minY;
   @Nullable
   public ItemStack tooltipStack = null;
   public final List<List<String>> tooltips = new ArrayList<>();
   private final Deque<GuidePageBase> pages = Queues.newArrayDeque();
   private final List<GuideChapter> chapters = new ArrayList<>();
   private GuidePageBase currentPage;
   private IFontRenderer currentFont = FontManager.INSTANCE.getOrLoadFont("SansSerif", 9);
   private float lastPartialTicks;
   private int seenReloadGeneration = GuideManager.INSTANCE.getReloadGeneration();

   public GuiGuide() {
      this((GuideBook)null);
   }

   public GuiGuide(String bookName) {
      this(GuideBookRegistry.INSTANCE.getBook(bookName));
   }

   private GuiGuide(@Nullable GuideBook book) {
      super(Component.literal("BuildCraft Guide"));
      this.book = book;
      this.bookData = book != null ? book.data : GuideManager.BOOK_ALL_DATA;
      this.openPage(new GuidePageContents(this));
   }

   public void openPage(GuidePageBase page) {
      if (this.currentPage != null && this.currentPage.shouldPersistHistory()) {
         this.pages.push(this.currentPage);
      }

      this.setPageInternal(page);
   }

   public void closePage() {
      if (this.pages.isEmpty()) {
         //? if >= 26.1.3 {
         /*this.minecraft.gui.setScreen(null);*/
         //?} else {
         this.minecraft.setScreen(null);
         //?}
      } else {
         this.setPageInternal(this.pages.pop());
      }
   }

   public void goBackToMenu() {
      GuidePageBase newPage = this.currentPage;

      while (!this.pages.isEmpty()) {
         newPage = this.pages.pop();
      }

      this.setPageInternal(newPage);
   }

   private void setPageInternal(GuidePageBase page) {
      this.currentPage = page;
      this.refreshChapters();
   }

   public GuidePageBase getCurrentPage() {
      return this.currentPage;
   }

   public IFontRenderer getCurrentFont() {
      return this.currentFont;
   }

   public List<GuideChapter> getChapters() {
      return this.chapters;
   }

   public void refreshChapters() {
      this.chapters.clear();
      if (this.currentPage != null) {
         this.chapters.addAll(this.currentPage.getChapters());
      }
   }

   private void refreshAfterReload(int currentGen) {
      List<GuidePageBase> snapshot = new ArrayList<>(this.pages);
      this.pages.clear();
      List<GuidePageBase> survivors = new ArrayList<>(snapshot.size());

      for (GuidePageBase old : snapshot) {
         GuidePageBase rebuilt = null;

         try {
            rebuilt = old.createReloaded();
         } catch (Throwable t) {
            BCLog.logger.warn("[lib.guide] Failed to rebuild history page " + old.getClass().getSimpleName() + " after reload — dropping.", t);
         }

         if (rebuilt != null) {
            survivors.add(rebuilt);
         }
      }

      GuidePageBase rebuiltCurrent = null;
      if (this.currentPage != null) {
         try {
            rebuiltCurrent = this.currentPage.createReloaded();
         } catch (Throwable t) {
            BCLog.logger
               .warn(
                  "[lib.guide] Failed to rebuild current page " + this.currentPage.getClass().getSimpleName() + " after reload — falling back to contents.", t
               );
         }
      }

      if (rebuiltCurrent == null) {
         rebuiltCurrent = new GuidePageContents(this);
      }

      this.currentPage = rebuiltCurrent;

      for (GuidePageBase survivor : survivors) {
         this.pages.addLast(survivor);
      }

      this.refreshChapters();
      this.seenReloadGeneration = currentGen;
      if (GuideManager.DEBUG) {
         BCLog.logger
            .info(
               "[lib.guide] GuiGuide refreshed for reload generation "
                  + currentGen
                  + "; rebuilt "
                  + (1 + survivors.size())
                  + " page(s), "
                  + (snapshot.size() - survivors.size())
                  + " dropped."
            );
      }
   }

   public void tick() {
      int currentGen = GuideManager.INSTANCE.getReloadGeneration();
      if (currentGen != this.seenReloadGeneration) {
         this.refreshAfterReload(currentGen);
      }

      super.tick();
      if (this.isOpen) {
         this.currentPage.updateScreen();

         for (GuideChapter chapter : this.chapters) {
            chapter.updateScreen();
         }
      } else if (this.isOpening) {
         this.openingAngleLast = this.openingAngleNext;
         this.openingAngleNext += 18.0F;
      }

      if (this.currentPage != null) {
         this.setupFontRenderer();
         this.currentPage.tick();
      }
   }

   public boolean isSmallScreen() {
      return this.width < 590;
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      BCGraphics bcg = new BCGraphics(graphics);
      MinecraftFont.setGuiGraphics(bcg);
      GuiIcon.setGuiGraphics(bcg);
      GuiStack.setGuiGraphics(bcg);
      GuiFluid.setGuiGraphics(bcg);
      partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
      this.lastPartialTicks = partialTicks;
      this.minX = (this.width - PAGE_LEFT.width * 2) / 2;
      this.minY = (this.height - BOOK_COVER.height) / 2;
      this.mouse.setMousePosition(mouseX, mouseY);

      try {
         if (this.isOpen) {
            this.drawOpen(partialTicks);
         } else if (this.isOpening) {
            this.drawOpening(partialTicks);
         } else {
            this.drawCover();
         }
      } catch (Throwable t) {
         BCLog.logger.error("[lib.guide] Failed to render the guide GUI", t);
         throw new RuntimeException(t);
      }

      MinecraftFont.setGuiGraphics(null);
      GuiIcon.setGuiGraphics(null);
      GuiStack.setGuiGraphics(null);
      GuiFluid.setGuiGraphics(null);
   }

   public float getLastPartialTicks() {
      return this.lastPartialTicks;
   }

   private void drawCover() {
      this.minX = (this.width - BOOK_COVER.width) / 2;
      this.minY = (this.height - BOOK_COVER.height) / 2;
      BOOK_COVER.drawAt(this.minX, this.minY);
   }

   private void drawOpening(float partialTicks) {
      this.minX = (this.width - BOOK_COVER.width) / 2;
      this.minY = (this.height - BOOK_COVER.height) / 2;
      float openingAngle = this.openingAngleLast * (1.0F - partialTicks) + this.openingAngleNext * partialTicks;
      float sin = Mth.sin((float)(openingAngle * Math.PI / 180.0));
      if (sin < 0.0F) {
         sin *= -1.0F;
      }

      if (openingAngle >= 90.0F) {
         this.isOpen = true;
      }

      if (openingAngle < 0.0F) {
         int coverWidth = (int)(sin * BOOK_COVER.width);
         sin = 1.0F - sin;
         float offset = sin * 50.0F;
         int bindingWidth = (int)(sin * BOOK_BINDING.width);
         PAGE_RIGHT.drawAt(this.minX + BOOK_COVER.width - PAGE_RIGHT.width, this.minY);
         BOOK_COVER.drawCustomQuad(
            this.minX,
            this.minY + BOOK_COVER.height,
            this.minX + coverWidth,
            this.minY + BOOK_COVER.height + offset,
            this.minX + coverWidth,
            this.minY - offset,
            this.minX,
            this.minY
         );
         BOOK_BINDING.drawScaledInside(
            (int)(this.minX + coverWidth - bindingWidth * 0.5), (int)(this.minY - offset), bindingWidth, (int)(BOOK_BINDING.height + offset * 2.0F)
         );
      } else if (openingAngle == 0.0F) {
         PAGE_RIGHT.drawAt(this.minX + BOOK_COVER.width - PAGE_LEFT.width, this.minY);
         BOOK_COVER.drawAt(this.minX, this.minY);
      } else {
         int pageWidth = (int)(sin * PAGE_LEFT.width);
         int bindingWidth = (int)((1.0F - sin) * BOOK_BINDING.width);
         float offset = (1.0F - sin) * 50.0F;
         this.minX = (this.width - PAGE_LEFT.width - pageWidth) / 2;
         this.minY = (this.height - BOOK_COVER.height) / 2;
         PAGE_RIGHT.drawAt(this.minX + pageWidth + bindingWidth, this.minY);
         PAGE_LEFT.drawCustomQuad(
            this.minX + bindingWidth,
            this.minY + PAGE_LEFT.height + offset,
            this.minX + bindingWidth + pageWidth,
            this.minY + PAGE_LEFT.height,
            this.minX + bindingWidth + pageWidth,
            this.minY,
            this.minX + bindingWidth,
            this.minY - offset
         );
         BOOK_BINDING.drawScaledInside(
            (int)(this.minX + bindingWidth * 0.5), (int)(this.minY - offset), bindingWidth, (int)(BOOK_BINDING.height + offset * 2.0F)
         );
      }
   }

   private void drawOpen(float partialTicks) {
      int cp = this.currentPage.getPage();
      int pc = this.currentPage.getPageCount();
      boolean isHalfPageShown = cp + 1 == pc;
      (cp == 0 ? PAGE_LEFT_FIRST : PAGE_LEFT).drawAt(this.minX, this.minY);
      GuiIcon lastPageIcon;
      if (cp + 2 == pc) {
         lastPageIcon = PAGE_RIGHT_LAST;
      } else if (isHalfPageShown) {
         lastPageIcon = PAGE_RIGHT_BACK;
      } else {
         lastPageIcon = PAGE_RIGHT;
      }

      lastPageIcon.drawAt(this.minX + PAGE_LEFT.width, this.minY);
      String title = this.currentPage.getTitle();
      if (title != null) {
         int titleWidth = this.currentFont.getStringWidth(title);
         int leftX = (int)(this.minX + PAGE_LEFT_TEXT.getX() + (PAGE_LEFT_TEXT.getWidth() - titleWidth) / 2.0);
         this.currentFont.drawString(title, leftX, this.minY + 12, -7306902);
         if (!isHalfPageShown) {
            int rightX = (int)(this.minX + PAGE_LEFT.width + PAGE_RIGHT_TEXT.getX() + (PAGE_RIGHT_TEXT.getWidth() - titleWidth) / 2.0);
            this.currentFont.drawString(title, rightX, this.minY + 12, -7306902);
         }
      }

      this.tooltipStack = null;
      this.tooltips.clear();
      this.setupFontRenderer();

      for (GuideChapter chapter : this.chapters) {
         chapter.reset();
      }

      this.currentPage
         .renderFirstPage(
            this.minX + (int)PAGE_LEFT_TEXT.getX(), this.minY + (int)PAGE_LEFT_TEXT.getY(), (int)PAGE_LEFT_TEXT.getWidth(), (int)PAGE_LEFT_TEXT.getHeight()
         );
      int secondPageX = this.minX + PAGE_LEFT.width + (int)PAGE_RIGHT_TEXT.getX();
      if (!isHalfPageShown) {
         this.currentPage
            .renderSecondPage(secondPageX, this.minY + (int)PAGE_RIGHT_TEXT.getY(), (int)PAGE_RIGHT_TEXT.getWidth(), (int)PAGE_RIGHT_TEXT.getHeight());
      }

      boolean drawContents = true;
      boolean smallScreen = this.isSmallScreen();
      if (smallScreen) {
         drawContents = this.showingContentsMenu;
         String str = LocaleUtil.localize("buildcraft.guide.chapter_list");
         if (this.showingContentsMenu) {
            CHAPTER_MARKER_9.draw(FLOATING_CHAPTER_MENU);
            this.currentFont.drawString(str, (int)FLOATING_CHAPTER_MENU.getX() + 7, (int)FLOATING_CHAPTER_MENU.getY() + 7, 0);
         } else {
            boolean isHovered = new GuiRectangle(secondPageX, this.minY, 80.0, 10.0).contains(this.mouse);
            int tabY = this.minY + (isHovered ? -5 : 0);
            int strWidth = this.currentFont.getStringWidth(str);
            BCGraphics graphics = GuiIcon.getGuiGraphics();
            if (graphics != null) {
               try (GuiUtil.AutoGlScissor scissor = GuiUtil.scissor(graphics, secondPageX, 0.0, strWidth + 20, this.minY + 10)) {
                  CHAPTER_MARKER_9.draw(secondPageX, tabY, strWidth + 20, 100.0);
                  this.currentFont.drawString(str, secondPageX + 10, tabY + 3, 0);
               }
            } else {
               CHAPTER_MARKER_9.draw(secondPageX, tabY, strWidth + 20, 100.0);
               this.currentFont.drawString(str, secondPageX + 10, tabY + 3, 0);
            }
         }
      }

      if (drawContents) {
         int chapterIndex = 0;

         for (GuideChapter chapter : this.chapters) {
            if (!chapter.hasParent()) {
               chapterIndex += chapter.draw(chapterIndex, partialTicks, smallScreen);
            }
         }
      }

      if (!this.pages.isEmpty()) {
         GuiIcon icon = BACK;
         IGuiArea position = BACK_POSITION.offset(this.minX, this.minY);
         if (position.contains(this.mouse)) {
            icon = BACK_HOVERED;
         }

         icon.drawAt(position);
      }

      this.drawPageTurnArrows(cp, pc, isHalfPageShown);
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      if (graphics != null) {
         int mx = (int)this.mouse.getX();
         int my = (int)this.mouse.getY();
         if (this.tooltipStack != null && !this.tooltipStack.isEmpty()) {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.tooltipStack, mx, my);
         } else if (!this.tooltips.isEmpty()) {
            List<Component> lines = new ArrayList<>();

            for (List<String> tooltip : this.tooltips) {
               for (String line : tooltip) {
                  lines.add(Component.literal(line));
               }
            }

            if (!lines.isEmpty()) {
               graphics.setTooltipForNextFrame(Minecraft.getInstance().font, lines, Optional.empty(), mx, my);
            }
         }
      }
   }

   private void drawPageTurnArrows(int currentPageIndex, int pageCount, boolean isHalfPage) {
      if (currentPageIndex + 2 < pageCount) {
         int arrowX = this.minX + PAGE_LEFT.width + PAGE_RIGHT.width - TURN_FORWARDS.width - 10;
         int arrowY = this.minY + PAGE_RIGHT.height - TURN_FORWARDS.height - 8;
         GuiRectangle forwardRect = new GuiRectangle(arrowX, arrowY, TURN_FORWARDS.width, TURN_FORWARDS.height);
         GuiIcon icon = forwardRect.contains(this.mouse) ? TURN_FORWARDS_HOVERED : TURN_FORWARDS;
         icon.drawAt(arrowX, arrowY);
      }

      if (currentPageIndex > 0) {
         int arrowX = this.minX + 10;
         int arrowY = this.minY + PAGE_LEFT.height - TURN_BACK.height - 8;
         GuiRectangle backRect = new GuiRectangle(arrowX, arrowY, TURN_BACK.width, TURN_BACK.height);
         GuiIcon icon = backRect.contains(this.mouse) ? TURN_BACK_HOVERED : TURN_BACK;
         icon.drawAt(arrowX, arrowY);
      }
   }

   public void setupFontRenderer() {
      this.currentPage.setFontRenderer(this.currentFont);
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.currentPage != null && this.currentPage.mouseClicked(event, doubleClick)) {
         return true;
      }

      double mouseX = event.x();
      double mouseY = event.y();
      int mouseButton = event.button();
      this.mouse.setMousePosition((int)mouseX, (int)mouseY);
      if (mouseButton == 0) {
         if (this.isOpen) {
            int page0xMin = this.minX + (int)PAGE_LEFT_TEXT.getX();
            int page0xMax = page0xMin + (int)PAGE_LEFT_TEXT.getWidth();
            int page1xMin = this.minX + PAGE_LEFT.width + (int)PAGE_RIGHT_TEXT.getX();
            int page1xMax = page1xMin + (int)PAGE_RIGHT_TEXT.getWidth();
            int pageYMin = this.minY + (int)PAGE_RIGHT_TEXT.getY();
            int pageYMax = pageYMin + (int)PAGE_RIGHT_TEXT.getHeight();
            GuidePageBase current = this.currentPage;
            current.setFontRenderer(this.currentFont);
            boolean chaptersInteractive = !this.isSmallScreen() || this.showingContentsMenu;
            if (chaptersInteractive) {
               for (GuideChapter chapter : this.chapters) {
                  int clickResult = chapter.handleClick();
                  if (clickResult > 0) {
                     if (this.showingContentsMenu && clickResult == 1) {
                        this.showingContentsMenu = false;
                     }

                     return true;
                  }
               }
            }

            if (this.isSmallScreen()) {
               if (this.showingContentsMenu) {
                  if (!FLOATING_CHAPTER_MENU.contains(this.mouse)) {
                     this.showingContentsMenu = false;
                  }

                  return true;
               }

               if (new GuiRectangle(this.minX + PAGE_LEFT.width + (int)PAGE_RIGHT_TEXT.getX(), this.minY, 80.0, 10.0).contains(this.mouse)) {
                  this.showingContentsMenu = true;
                  return true;
               }
            }

            int cp = this.currentPage.getPage();
            int pc = this.currentPage.getPageCount();
            if (cp + 2 < pc) {
               int arrowX = this.minX + PAGE_LEFT.width + PAGE_RIGHT.width - TURN_FORWARDS.width - 10;
               int arrowY = this.minY + PAGE_RIGHT.height - TURN_FORWARDS.height - 8;
               GuiRectangle forwardRect = new GuiRectangle(arrowX, arrowY, TURN_FORWARDS.width, TURN_FORWARDS.height);
               if (forwardRect.contains(mouseX, mouseY)) {
                  this.currentPage.nextPage();
                  return true;
               }
            }

            if (cp > 0) {
               int arrowX = this.minX + 10;
               int arrowY = this.minY + PAGE_LEFT.height - TURN_BACK.height - 8;
               GuiRectangle backRect = new GuiRectangle(arrowX, arrowY, TURN_BACK.width, TURN_BACK.height);
               if (backRect.contains(mouseX, mouseY)) {
                  this.currentPage.lastPage();
                  return true;
               }
            }

            current.handleMouseClick(
               page0xMin, pageYMin, page0xMax - page0xMin, pageYMax - pageYMin, (int)mouseX, (int)mouseY, mouseButton, this.currentPage.getPage(), false
            );
            current.handleMouseClick(
               page1xMin, pageYMin, page1xMax - page1xMin, pageYMax - pageYMin, (int)mouseX, (int)mouseY, mouseButton, this.currentPage.getPage() + 1, false
            );
            if (!this.pages.isEmpty() && BACK_POSITION.offset(this.minX, this.minY).contains(mouseX, mouseY)) {
               this.closePage();
               return true;
            }
         } else if (mouseX >= this.minX && mouseY >= this.minY && mouseX <= this.minX + BOOK_COVER.width && mouseY <= this.minY + BOOK_COVER.height) {
            if (this.isOpening || doubleClick) {
               this.isOpen = true;
            }

            this.isOpening = true;
            return true;
         }
      }

      return super.mouseClicked(event, doubleClick);
   }

   public boolean keyPressed(KeyEvent keyEvent) {
      if (this.currentPage != null && this.currentPage.keyPressed(keyEvent)) {
         return true;
      }

      if (this.isOpen) {
         if (keyEvent.isLeft()) {
            this.currentPage.lastPage();
            return true;
         }

         if (keyEvent.isRight()) {
            this.currentPage.nextPage();
            return true;
         }
      }

      return super.keyPressed(keyEvent);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean charTyped(CharacterEvent event) {
      return this.currentPage != null && this.currentPage.charTyped(event) ? true : super.charTyped(event);
   }
}
