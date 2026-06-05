/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.pos.IGuiPosition;

@SuppressWarnings("this-escape")
public class Ledger_Neptune implements IGuiElement, IInteractionElement {
    public static final int LEDGER_GAP = 4;
    public static final int CLOSED_WIDTH = 2 + 16 + LEDGER_GAP;
    public static final int CLOSED_HEIGHT = LEDGER_GAP + 16 + LEDGER_GAP;

    private static final ISprite SPRITE_LEFT = new SpriteRaw(
        Identifier.parse("buildcraftlib:textures/icons/ledger_left.png"), 0, 0, 1.0, 1.0);
    private static final ISprite SPRITE_RIGHT = new SpriteRaw(
        Identifier.parse("buildcraftlib:textures/icons/ledger_right.png"), 0, 0, 1.0, 1.0);
    private static final SpriteNineSliced SPRITE_SPLIT_LEFT =
        new SpriteNineSliced(SPRITE_LEFT, 4.0 / 16, 4.0 / 16, 12.0 / 16, 12.0 / 16, 16.0);
    private static final SpriteNineSliced SPRITE_SPLIT_RIGHT =
        new SpriteNineSliced(SPRITE_RIGHT, 4.0 / 16, 4.0 / 16, 12.0 / 16, 12.0 / 16, 16.0);

    public final BuildCraftGui gui;
    public final int colour;
    public final boolean expandPositive;

    private final IGuiPosition positionLedgerStart;
    private final IGuiPosition positionLedgerIconStart;

    private final IGuiPosition positionAnchor;

    protected double maxWidth = 96, maxHeight = 48;

    protected double currentWidth = CLOSED_WIDTH;
    protected double currentHeight = CLOSED_HEIGHT;
    protected double lastWidth = currentWidth;
    protected double lastHeight = currentHeight;
    protected double interpWidth = lastWidth;
    protected double interpHeight = lastHeight;

    private double yShift = 0;

    protected String title = "unknown";

    private int currentDifference = 0;

    private final buildcraft.lib.gui.config.GuiPropertyBoolean openProperty;

    private boolean pendingInitialOpen;
    private boolean appliedInitialState;

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

    private final List<TextEntry> textEntries = new ArrayList<>();

    public Ledger_Neptune(BuildCraftGui gui, int colour, boolean expandPositive) {
        this.gui = gui;
        this.colour = colour;
        this.expandPositive = expandPositive;

        if (expandPositive) {

            positionLedgerStart = gui.lowerRightLedgerPos;
            positionAnchor = positionLedgerStart;

            gui.lowerRightLedgerPos = positionLedgerStart.offset(0, () -> this.getHeight() + 5);
            positionLedgerIconStart = positionLedgerStart.offset(2, LEDGER_GAP);
        } else {

            positionAnchor = gui.lowerLeftLedgerPos;
            positionLedgerStart = gui.lowerLeftLedgerPos.offset(() -> -this.getWidth(), 0);

            gui.lowerLeftLedgerPos = gui.lowerLeftLedgerPos.offset(0, () -> this.getHeight() + 5);
            positionLedgerIconStart = positionLedgerStart.offset(LEDGER_GAP, LEDGER_GAP);
        }

        String guiId = gui.gui != null ? gui.gui.getClass().getName() : "unknown";
        String propName = this.getClass().getSimpleName() + ".is_open";
        this.openProperty = buildcraft.lib.gui.config.GuiConfigManager.getOrAddBoolean(guiId, propName, false);
        this.pendingInitialOpen = this.openProperty.get();
    }

    public TextEntry appendText(String text, int colour) {
        TextEntry entry = new TextEntry(() -> text, () -> colour);
        textEntries.add(entry);
        return entry;
    }

    public TextEntry appendText(Supplier<String> textSupplier, int colour) {
        TextEntry entry = new TextEntry(textSupplier, () -> colour);
        textEntries.add(entry);
        return entry;
    }

    public TextEntry appendText(Supplier<String> textSupplier, IntSupplier colour) {
        TextEntry entry = new TextEntry(textSupplier, colour);
        textEntries.add(entry);
        return entry;
    }

    protected void clearTextEntries() {
        textEntries.clear();
    }

    public String getTitle() {
        return buildcraft.lib.misc.LocaleUtil.localize(title);
    }

    public int getTitleColour() {
        return 0xFF_E1_C9_2F;
    }

    protected void calculateMaxSize() {
        Font font = Minecraft.getInstance().font;
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();

        int overhead = 2 + 16 + LEDGER_GAP + LEDGER_GAP + 2;

        int naturalMaxTextWidth = font.width(getTitle());
        for (TextEntry entry : textEntries) {
            int w = font.width(entry.getText());
            if (w > naturalMaxTextWidth) naturalMaxTextWidth = w;
        }
        int naturalWidth = overhead + naturalMaxTextWidth;

        int maxAllowedWidth;
        if (expandPositive) {

            maxAllowedWidth = Math.max(CLOSED_WIDTH, screenWidth - (int) positionAnchor.getX());
        } else {

            maxAllowedWidth = Math.max(CLOSED_WIDTH, (int) positionAnchor.getX());
        }

        maxWidth = Math.min(naturalWidth, maxAllowedWidth);
        maxWidth = Math.max(CLOSED_WIDTH, maxWidth);

        int textAreaWidth = Math.max(40, (int) maxWidth - overhead);

        int textHeight = font.lineHeight + 3;
        for (TextEntry entry : textEntries) {
            List<FormattedCharSequence> wrapped = font.split(Component.literal(entry.getText()), textAreaWidth);
            int lineCount = Math.max(1, wrapped.size());
            textHeight += (font.lineHeight + 3) * lineCount;
        }
        maxHeight = Math.max(CLOSED_HEIGHT, LEDGER_GAP + textHeight + LEDGER_GAP);

        double normalY = positionLedgerStart.getY();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        double bottomEdge = normalY + maxHeight;
        if (bottomEdge > screenHeight) {
            yShift = bottomEdge - screenHeight;
        } else {
            yShift = 0;
        }
    }

    @Override
    public void tick() {
        lastWidth = currentWidth;
        lastHeight = currentHeight;

        double targetWidth, targetHeight;
        if (currentDifference == 1) {
            targetWidth = maxWidth;
            targetHeight = maxHeight;
        } else if (currentDifference == -1) {
            targetWidth = CLOSED_WIDTH;
            targetHeight = CLOSED_HEIGHT;
        } else {
            return;
        }

        double maxDiff = Math.max(maxWidth - CLOSED_WIDTH, maxHeight - CLOSED_HEIGHT);
        double ldgDiff = Mth.clamp(maxDiff / 5, 1, 15);

        currentWidth = approach(currentWidth, targetWidth, ldgDiff);
        currentHeight = approach(currentHeight, targetHeight, ldgDiff);
    }

    private static double approach(double current, double target, double speed) {
        if (current < target) return Math.min(current + speed, target);
        if (current > target) return Math.max(current - speed, target);
        return target;
    }

    private static double interp(double past, double current, float partialTicks) {
        if (past == current) return current;
        if (partialTicks <= 0) return past;
        if (partialTicks >= 1) return current;
        return past * (1 - partialTicks) + current * partialTicks;
    }

    public final boolean shouldDrawOpen() {
        return currentWidth > CLOSED_WIDTH || currentHeight > CLOSED_HEIGHT;
    }

    @Override
    public void drawBackground(float partialTicks) {
        BCGraphics graphics = GuiIcon.getGuiGraphics();
        if (graphics == null) return;

        if (!appliedInitialState) {
            appliedInitialState = true;
            if (pendingInitialOpen) {
                calculateMaxSize();
                currentDifference = 1;
                currentWidth = maxWidth;
                currentHeight = maxHeight;
                lastWidth = maxWidth;
                lastHeight = maxHeight;
            }
        }

        interpWidth = interp(lastWidth, currentWidth, partialTicks);
        interpHeight = interp(lastHeight, currentHeight, partialTicks);

        double rawX, rawY;
        if (expandPositive) {
            rawX = positionLedgerStart.getX();
        } else {
            rawX = positionAnchor.getX() - interpWidth;
        }
        rawY = getY();
        int x = (int) Math.floor(rawX);
        int y = (int) Math.floor(rawY);
        int w, h;
        if (expandPositive) {

            w = (int) Math.ceil(interpWidth);
        } else {

            w = (int) positionAnchor.getX() - x;
        }
        h = (int) Math.ceil(interpHeight + (rawY - y));

        if (w <= 0 || h <= 0) return;

        SpriteNineSliced split = expandPositive ? SPRITE_SPLIT_RIGHT : SPRITE_SPLIT_LEFT;
        int tintColour = 0xFF000000 | (colour & 0xFFFFFF);
        split.drawTinted(x, y, w, h, tintColour);

        int scissorX = (int) positionLedgerIconStart.getX();
        int scissorY = (int) positionLedgerIconStart.getY();
        int scissorW = (int) (interpWidth - LEDGER_GAP);
        int scissorH = (int) (interpHeight - LEDGER_GAP * 2);
        graphics.enableScissor(scissorX, scissorY, scissorX + scissorW, scissorY + scissorH);

        double iconX = positionLedgerIconStart.getX();
        double iconY = positionLedgerIconStart.getY();
        drawIcon(iconX, iconY, graphics);

        if (interpWidth > CLOSED_WIDTH + 10) {
            Font font = Minecraft.getInstance().font;
            int textAreaWidth = (int) maxWidth - 2 - 16 - LEDGER_GAP - LEDGER_GAP - 2;

            int textX = (int) iconX + 16 + LEDGER_GAP;
            int textY = (int) iconY + 1;

            graphics.text(font, getTitle(), textX, textY, getTitleColour() | 0xFF000000, true);
            textY += font.lineHeight + 3;

            for (TextEntry entry : textEntries) {
                int entryColour = entry.getColour() | 0xFF000000;
                List<FormattedCharSequence> wrapped = font.split(
                    Component.literal(entry.getText()), textAreaWidth);
                for (FormattedCharSequence line : wrapped) {
                    graphics.text(font, line, textX, textY, entryColour, entry.dropShadow);
                    textY += font.lineHeight + 3;
                }
            }
        }

        graphics.disableScissor();

        if (!shouldDrawOpen() && contains(gui.mouse.getX(), gui.mouse.getY())) {
            var titleComp = net.minecraft.network.chat.Component.literal(getTitle());
            graphics.setTooltipForNextFrame(titleComp,
                (int) gui.mouse.getX(), (int) gui.mouse.getY());
        }
    }

    protected void drawIcon(double x, double y, BCGraphics graphics) {

    }

    @Override
    public void onMouseClicked(int button) {
        double mouseX = gui.mouse.getX();
        double mouseY = gui.mouse.getY();
        if (contains(mouseX, mouseY)) {
            boolean nowOpen;
            if (currentDifference == 1) {
                currentDifference = -1;
                nowOpen = false;
            } else {
                currentDifference = 1;
                calculateMaxSize();
                nowOpen = true;
            }
            openProperty.set(nowOpen);
        }
    }

    @Override
    public void onMouseDragged(int button, long timeSinceLastClick) {}

    @Override
    public void onMouseReleased(int button) {}

    @Override
    public double getX() {
        return positionLedgerStart.getX();
    }

    @Override
    public double getY() {

        double shift = (currentDifference != 0 || currentHeight > CLOSED_HEIGHT) ? yShift : 0;
        return positionLedgerStart.getY() - shift;
    }

    @Override
    public double getWidth() {
        float partialTicks = gui.getLastPartialTicks();
        if (lastWidth == currentWidth) return currentWidth;
        else if (partialTicks <= 0) return lastWidth;
        else if (partialTicks >= 1) return currentWidth;
        else return lastWidth * (1 - partialTicks) + currentWidth * partialTicks;
    }

    @Override
    public double getHeight() {
        float partialTicks = gui.getLastPartialTicks();
        if (lastHeight == currentHeight) return currentHeight;
        else if (partialTicks <= 0) return lastHeight;
        else if (partialTicks >= 1) return currentHeight;
        else return lastHeight * (1 - partialTicks) + currentHeight * partialTicks;
    }

    public static class TextEntry {
        public final Supplier<String> textSupplier;
        public final IntSupplier colourSupplier;
        public boolean dropShadow = false;

        public TextEntry(Supplier<String> textSupplier, IntSupplier colourSupplier) {
            this.textSupplier = textSupplier;
            this.colourSupplier = colourSupplier;
        }

        public TextEntry setDropShadow(boolean shadow) {
            this.dropShadow = shadow;
            return this;
        }

        public String getText() {
            return textSupplier.get();
        }

        public int getColour() {
            return colourSupplier.getAsInt();
        }
    }
}
