/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.config.GuiPropertyBoolean;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.LocaleUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class Ledger_Neptune implements IGuiElement, IInteractionElement {
   public static final int LEDGER_GAP = 4;
   public static final int CLOSED_WIDTH = 22;
   public static final int CLOSED_HEIGHT = 24;
   private static final ISprite SPRITE_LEFT = new SpriteRaw(Identifier.parse("buildcraftlib:textures/icons/ledger_left.png"), 0.0, 0.0, 1.0, 1.0);
   private static final ISprite SPRITE_RIGHT = new SpriteRaw(Identifier.parse("buildcraftlib:textures/icons/ledger_right.png"), 0.0, 0.0, 1.0, 1.0);
   private static final SpriteNineSliced SPRITE_SPLIT_LEFT = new SpriteNineSliced(SPRITE_LEFT, 0.25, 0.25, 0.75, 0.75, 16.0);
   private static final SpriteNineSliced SPRITE_SPLIT_RIGHT = new SpriteNineSliced(SPRITE_RIGHT, 0.25, 0.25, 0.75, 0.75, 16.0);
   public final BuildCraftGui gui;
   public final int colour;
   public final boolean expandPositive;
   private final IGuiPosition positionLedgerStart;
   private final IGuiPosition positionLedgerIconStart;
   private final IGuiPosition positionAnchor;
   protected double maxWidth = 96.0;
   protected double maxHeight = 48.0;
   protected double currentWidth = 22.0;
   protected double currentHeight = 24.0;
   protected double lastWidth = this.currentWidth;
   protected double lastHeight = this.currentHeight;
   protected double interpWidth = this.lastWidth;
   protected double interpHeight = this.lastHeight;
   private double yShift = 0.0;
   protected String title = "unknown";
   private int currentDifference = 0;
   private final GuiPropertyBoolean openProperty;
   private boolean pendingInitialOpen;
   private boolean appliedInitialState;
   private final List<Ledger_Neptune.TextEntry> textEntries = new ArrayList<>();

   public void copyAnimationStateFrom(Ledger_Neptune other) {
      this.currentDifference = other.currentDifference;
      this.currentWidth = other.currentWidth;
      this.currentHeight = other.currentHeight;
      this.lastWidth = other.lastWidth;
      this.lastHeight = other.lastHeight;
      this.interpWidth = other.interpWidth;
      this.interpHeight = other.interpHeight;
      this.calculateMaxSize();
      this.currentWidth = Math.min(this.currentWidth, this.maxWidth);
      this.currentHeight = Math.min(this.currentHeight, this.maxHeight);
      this.lastWidth = Math.min(this.lastWidth, this.maxWidth);
      this.lastHeight = Math.min(this.lastHeight, this.maxHeight);
      this.appliedInitialState = true;
   }

   public Ledger_Neptune(BuildCraftGui gui, int colour, boolean expandPositive) {
      this.gui = gui;
      this.colour = colour;
      this.expandPositive = expandPositive;
      if (expandPositive) {
         this.positionLedgerStart = gui.lowerRightLedgerPos;
         this.positionAnchor = this.positionLedgerStart;
         gui.lowerRightLedgerPos = this.positionLedgerStart.offset(0.0, () -> this.getHeight() + 5.0);
         this.positionLedgerIconStart = this.positionLedgerStart.offset(2.0, 4.0);
      } else {
         this.positionAnchor = gui.lowerLeftLedgerPos;
         this.positionLedgerStart = gui.lowerLeftLedgerPos.offset(() -> -this.getWidth(), 0.0);
         gui.lowerLeftLedgerPos = gui.lowerLeftLedgerPos.offset(0.0, () -> this.getHeight() + 5.0);
         this.positionLedgerIconStart = this.positionLedgerStart.offset(4.0, 4.0);
      }

      String guiId = gui.gui != null ? gui.gui.getClass().getName() : "unknown";
      String propName = this.getClass().getSimpleName() + ".is_open";
      this.openProperty = GuiConfigManager.getOrAddBoolean(guiId, propName, false);
      this.pendingInitialOpen = this.openProperty.get();
   }

   public Ledger_Neptune.TextEntry appendText(String text, int colour) {
      Ledger_Neptune.TextEntry entry = new Ledger_Neptune.TextEntry(() -> text, () -> colour);
      this.textEntries.add(entry);
      return entry;
   }

   public Ledger_Neptune.TextEntry appendText(Supplier<String> textSupplier, int colour) {
      Ledger_Neptune.TextEntry entry = new Ledger_Neptune.TextEntry(textSupplier, () -> colour);
      this.textEntries.add(entry);
      return entry;
   }

   public Ledger_Neptune.TextEntry appendText(Supplier<String> textSupplier, IntSupplier colour) {
      Ledger_Neptune.TextEntry entry = new Ledger_Neptune.TextEntry(textSupplier, colour);
      this.textEntries.add(entry);
      return entry;
   }

   protected void clearTextEntries() {
      this.textEntries.clear();
   }

   public String getTitle() {
      return LocaleUtil.localize(this.title);
   }

   public int getTitleColour() {
      return -1980113;
   }

   protected void calculateMaxSize() {
      Font font = Minecraft.getInstance().font;
      int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
      int overhead = 28;
      int naturalMaxTextWidth = font.width(this.getTitle());

      for (Ledger_Neptune.TextEntry entry : this.textEntries) {
         int w = font.width(entry.getText());
         if (w > naturalMaxTextWidth) {
            naturalMaxTextWidth = w;
         }
      }

      int naturalWidth = overhead + naturalMaxTextWidth;
      int maxAllowedWidth;
      if (this.expandPositive) {
         maxAllowedWidth = Math.max(22, screenWidth - (int)this.positionAnchor.getX());
      } else {
         maxAllowedWidth = Math.max(22, (int)this.positionAnchor.getX());
      }

      this.maxWidth = Math.min(naturalWidth, maxAllowedWidth);
      this.maxWidth = Math.max(22.0, this.maxWidth);
      int textAreaWidth = Math.max(40, (int)this.maxWidth - overhead);
      int textHeight = 9 + 3;

      for (Ledger_Neptune.TextEntry entry : this.textEntries) {
         List<FormattedCharSequence> wrapped = font.split(Component.literal(entry.getText()), textAreaWidth);
         int lineCount = Math.max(1, wrapped.size());
         textHeight += (9 + 3) * lineCount;
      }

      this.maxHeight = Math.max(24, 4 + textHeight + 4);
      double normalY = this.positionLedgerStart.getY();
      int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
      double bottomEdge = normalY + this.maxHeight;
      if (bottomEdge > screenHeight) {
         this.yShift = bottomEdge - screenHeight;
      } else {
         this.yShift = 0.0;
      }
   }

   @Override
   public void tick() {
      this.lastWidth = this.currentWidth;
      this.lastHeight = this.currentHeight;
      double targetWidth;
      double targetHeight;
      if (this.currentDifference == 1) {
         targetWidth = this.maxWidth;
         targetHeight = this.maxHeight;
      } else {
         if (this.currentDifference != -1) {
            return;
         }

         targetWidth = 22.0;
         targetHeight = 24.0;
      }

      double maxDiff = Math.max(this.maxWidth - 22.0, this.maxHeight - 24.0);
      double ldgDiff = Mth.clamp(maxDiff / 5.0, 1.0, 15.0);
      this.currentWidth = approach(this.currentWidth, targetWidth, ldgDiff);
      this.currentHeight = approach(this.currentHeight, targetHeight, ldgDiff);
   }

   private static double approach(double current, double target, double speed) {
      if (current < target) {
         return Math.min(current + speed, target);
      } else {
         return current > target ? Math.max(current - speed, target) : target;
      }
   }

   private static double interp(double past, double current, float partialTicks) {
      if (past == current) {
         return current;
      } else if (partialTicks <= 0.0F) {
         return past;
      } else {
         return partialTicks >= 1.0F ? current : past * (1.0F - partialTicks) + current * partialTicks;
      }
   }

   public final boolean shouldDrawOpen() {
      return this.currentWidth > 22.0 || this.currentHeight > 24.0;
   }

   @Override
   public void drawBackground(float partialTicks) {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      if (graphics != null) {
         if (!this.appliedInitialState) {
            this.appliedInitialState = true;
            if (this.pendingInitialOpen) {
               this.calculateMaxSize();
               this.currentDifference = 1;
               this.currentWidth = this.maxWidth;
               this.currentHeight = this.maxHeight;
               this.lastWidth = this.maxWidth;
               this.lastHeight = this.maxHeight;
            }
         }

         this.interpWidth = interp(this.lastWidth, this.currentWidth, partialTicks);
         this.interpHeight = interp(this.lastHeight, this.currentHeight, partialTicks);
         double rawX;
         if (this.expandPositive) {
            rawX = this.positionLedgerStart.getX();
         } else {
            rawX = this.positionAnchor.getX() - this.interpWidth;
         }

         double rawY = this.getY();
         int x = (int)Math.floor(rawX);
         int y = (int)Math.floor(rawY);
         int w;
         if (this.expandPositive) {
            w = (int)Math.ceil(this.interpWidth);
         } else {
            w = (int)this.positionAnchor.getX() - x;
         }

         int h = (int)Math.ceil(this.interpHeight + (rawY - y));
         if (w > 0 && h > 0) {
            SpriteNineSliced split = this.expandPositive ? SPRITE_SPLIT_RIGHT : SPRITE_SPLIT_LEFT;
            int tintColour = 0xFF000000 | this.colour & 16777215;
            split.drawTinted(x, y, w, h, tintColour);
            int scissorX = (int)this.positionLedgerIconStart.getX();
            int scissorY = (int)this.positionLedgerIconStart.getY();
            int scissorW = (int)(this.interpWidth - 4.0);
            int scissorH = (int)(this.interpHeight - 8.0);
            graphics.enableScissor(scissorX, scissorY, scissorX + scissorW, scissorY + scissorH);
            double iconX = this.positionLedgerIconStart.getX();
            double iconY = this.positionLedgerIconStart.getY();
            this.drawIcon(iconX, iconY, graphics);
            if (this.interpWidth > 32.0) {
               Font font = Minecraft.getInstance().font;
               int textAreaWidth = (int)this.maxWidth - 2 - 16 - 4 - 4 - 2;
               int textX = (int)iconX + 16 + 4;
               int textY = (int)iconY + 1;
               graphics.text(font, this.getTitle(), textX, textY, this.getTitleColour() | 0xFF000000, true);
               textY += 9 + 3;

               for (Ledger_Neptune.TextEntry entry : this.textEntries) {
                  int entryColour = entry.getColour() | 0xFF000000;

                  for (FormattedCharSequence line : font.split(Component.literal(entry.getText()), textAreaWidth)) {
                     graphics.text(font, line, textX, textY, entryColour, entry.dropShadow);
                     textY += 9 + 3;
                  }
               }
            }

            graphics.disableScissor();
            if (!this.shouldDrawOpen() && this.contains(this.gui.mouse.getX(), this.gui.mouse.getY())) {
               MutableComponent titleComp = Component.literal(this.getTitle());
               graphics.setTooltipForNextFrame(titleComp, (int)this.gui.mouse.getX(), (int)this.gui.mouse.getY());
            }
         }
      }
   }

   protected void drawIcon(double x, double y, BCGraphics graphics) {
   }

   @Override
   public void onMouseClicked(int button) {
      double mouseX = this.gui.mouse.getX();
      double mouseY = this.gui.mouse.getY();
      if (this.contains(mouseX, mouseY)) {
         boolean nowOpen;
         if (this.currentDifference == 1) {
            this.currentDifference = -1;
            nowOpen = false;
         } else {
            this.currentDifference = 1;
            this.calculateMaxSize();
            nowOpen = true;
         }

         this.openProperty.set(nowOpen);
      }
   }

   @Override
   public void onMouseDragged(int button, long timeSinceLastClick) {
   }

   @Override
   public void onMouseReleased(int button) {
   }

   @Override
   public double getX() {
      return this.positionLedgerStart.getX();
   }

   @Override
   public double getY() {
      double shift = this.currentDifference == 0 && !(this.currentHeight > 24.0) ? 0.0 : this.yShift;
      return this.positionLedgerStart.getY() - shift;
   }

   @Override
   public double getWidth() {
      float partialTicks = this.gui.getLastPartialTicks();
      if (this.lastWidth == this.currentWidth) {
         return this.currentWidth;
      } else if (partialTicks <= 0.0F) {
         return this.lastWidth;
      } else {
         return partialTicks >= 1.0F ? this.currentWidth : this.lastWidth * (1.0F - partialTicks) + this.currentWidth * partialTicks;
      }
   }

   @Override
   public double getHeight() {
      float partialTicks = this.gui.getLastPartialTicks();
      if (this.lastHeight == this.currentHeight) {
         return this.currentHeight;
      } else if (partialTicks <= 0.0F) {
         return this.lastHeight;
      } else {
         return partialTicks >= 1.0F ? this.currentHeight : this.lastHeight * (1.0F - partialTicks) + this.currentHeight * partialTicks;
      }
   }

   public static class TextEntry {
      public final Supplier<String> textSupplier;
      public final IntSupplier colourSupplier;
      public boolean dropShadow = false;

      public TextEntry(Supplier<String> textSupplier, IntSupplier colourSupplier) {
         this.textSupplier = textSupplier;
         this.colourSupplier = colourSupplier;
      }

      public Ledger_Neptune.TextEntry setDropShadow(boolean shadow) {
         this.dropShadow = shadow;
         return this;
      }

      public String getText() {
         return this.textSupplier.get();
      }

      public int getColour() {
         return this.colourSupplier.getAsInt();
      }
   }
}
