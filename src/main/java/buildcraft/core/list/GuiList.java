/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import net.minecraft.client.Minecraft;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.button.BCButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.core.item.ItemList_BC8;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;

public class GuiList extends GuiBC8<ContainerList> {
    private static final Identifier TEXTURE_BASE =
        Identifier.parse("buildcraftcore:textures/gui/list_new.png");
    private static final int SIZE_X = 176, SIZE_Y = 191;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_ONE_STACK = new GuiIcon(TEXTURE_BASE, 0, 191, 20, 20);

    private static final GuiIcon ICON_HIGHLIGHT = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 16);

    private static final int BTN_W = 14, BTN_H = 14;
    private static final int BUTTON_COUNT = 3;

    private ToggleButton[][] toggleButtons;

    private EditBox labelField;

    private final java.util.Map<Integer, GhostCache> ghostCache = new java.util.HashMap<>();

    private static final class GhostCache {
        final long signature;
        final java.util.List<net.minecraft.world.item.ItemStack> shuffled;

        GhostCache(long signature, java.util.List<net.minecraft.world.item.ItemStack> shuffled) {
            this.signature = signature;
            this.shuffled = shuffled;
        }
    }

    public GuiList(ContainerList menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void initGuiElements() {

        mainGui.shownElements.add(new LedgerListMatch(mainGui, menu));

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(10, 10, 156, 12).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.list.label.title", 0xFF_E1_C9_2F,
                        "buildcraft.help.list.label.desc")));

        for (int line = 0; line < menu.lines.length; line++) {
            int rowY = 32 + line * 34;
            mainGui.shownElements.add(new DummyHelpElement(
                    new GuiRectangle(8, rowY, 9 * 18, 16).offset(mainGui.rootElement),
                    new ElementHelpInfo("buildcraft.help.list.slots.title", 0xFF_88_CC_88,
                            "buildcraft.help.list.slots.desc1",
                            "buildcraft.help.list.slots.desc2")));

            int btnRowY = rowY + 18;
            int bOffX = 8 + 9 * 18 - BUTTON_COUNT * BTN_W - 1;
            mainGui.shownElements.add(new DummyHelpElement(
                    new GuiRectangle(bOffX, btnRowY, BTN_W, BTN_H).offset(mainGui.rootElement),
                    new ElementHelpInfo("buildcraft.help.list.button.precise.title", 0xFF_88_AA_FF,
                            "buildcraft.help.list.button.precise.desc")));
            mainGui.shownElements.add(new DummyHelpElement(
                    new GuiRectangle(bOffX + BTN_W, btnRowY, BTN_W, BTN_H).offset(mainGui.rootElement),
                    new ElementHelpInfo("buildcraft.help.list.button.by_type.title", 0xFF_FF_BB_55,
                            "buildcraft.help.list.button.by_type.desc1",
                            "buildcraft.help.list.button.by_type.desc2")));
            mainGui.shownElements.add(new DummyHelpElement(
                    new GuiRectangle(bOffX + 2 * BTN_W, btnRowY, BTN_W, BTN_H).offset(mainGui.rootElement),
                    new ElementHelpInfo("buildcraft.help.list.button.by_material.title", 0xFF_CC_88_FF,
                            "buildcraft.help.list.button.by_material.desc1",
                            "buildcraft.help.list.button.by_material.desc2")));
        }

        labelField = new EditBox(this.font, leftPos + 10, topPos + 10, 156, 12, Component.empty());
        labelField.setMaxLength(32);
        labelField.setBordered(true);

        if (menu.getListItemStack().getItem() instanceof ItemList_BC8 listItem) {
            String name = listItem.getLocationName(menu.getListItemStack());
            if (name != null && !name.isEmpty()) {
                labelField.setValue(name);
            }
        }

        labelField.setFocused(false);
        labelField.setResponder(newText -> menu.setLabel(newText));
        addRenderableWidget(labelField);

        toggleButtons = new ToggleButton[menu.lines.length][BUTTON_COUNT];
        for (int line = 0; line < menu.lines.length; line++) {
            int bOffX = this.leftPos + 8 + 9 * 18 - BUTTON_COUNT * BTN_W - 1;
            int bOffY = this.topPos + 32 + line * 34 + 18;

            for (int btn = 0; btn < BUTTON_COUNT; btn++) {
                final int lineIdx = line;
                final int btnIdx = btn;
                String letter = btn == 0 ? "P" : (btn == 1 ? "T" : "M");
                String tooltipKey = btn == 0 ? "gui.list.nbt" : (btn == 1 ? "gui.list.metadata" : "gui.list.oredict");

                ToggleButton button = new ToggleButton(
                        bOffX + btn * BTN_W, bOffY, BTN_W, BTN_H,
                        Component.literal(letter),
                        () -> {
                            menu.switchButton(lineIdx, btnIdx);

                            for (int i = 0; i < BUTTON_COUNT; i++) {
                                toggleButtons[lineIdx][i].setToggled(menu.lines[lineIdx].getOption(i));
                            }
                        });
                button.setToggled(menu.lines[lineIdx].getOption(btnIdx));
                button.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
                toggleButtons[line][btn] = button;
                addRenderableWidget(button);
            }
        }
    }

    private static class ToggleButton extends BCButton {
        private static final Identifier SPRITE_NORMAL = Identifier.withDefaultNamespace("widget/button");
        private static final Identifier SPRITE_DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");
        private static final Identifier SPRITE_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");

        private final Runnable onPressAction;
        private boolean toggled;

        ToggleButton(int x, int y, int width, int height, Component message, Runnable onPressAction) {
            super(x, y, width, height, message);
            this.onPressAction = onPressAction;
        }

        @Override
        public void onPress(InputWithModifiers modifiers) {
            onPressAction.run();
        }

        void setToggled(boolean toggled) {
            this.toggled = toggled;
        }

        @Override
        protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {

            Identifier sprite;
            if (toggled) {
                sprite = SPRITE_DISABLED;
            } else if (this.isHoveredOrFocused()) {
                sprite = SPRITE_HIGHLIGHTED;
            } else {
                sprite = SPRITE_NORMAL;
            }
            graphics.raw.blitSprite(RenderPipelines.GUI_TEXTURED, sprite,
                    getX(), getY(), getWidth(), getHeight(),
                    ARGB.white(this.alpha));

            drawDefaultButtonLabel(graphics);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

            if (this.visible
                    && this.isValidClickButton(event.buttonInfo())
                    && this.isMouseOver(event.x(), event.y())) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                this.onClick(event, doubleClick);
                return true;
            }
            return false;
        }
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);

        for (int i = 0; i < menu.lines.length; i++) {
            buildcraft.lib.list.ListHandler.Line line = menu.lines[i];
            if (!line.isOneStackMode()) continue;

            ICON_ONE_STACK.drawAt(leftPos + 6, topPos + 30 + i * 34);

            java.util.List<net.minecraft.world.item.ItemStack> examples = ghostExamplesFor(i);
            for (int slot = 1; slot < buildcraft.lib.list.ListHandler.WIDTH; slot++) {
                int x = leftPos + 8 + slot * 18;
                int y = topPos + 32 + i * 34;
                ICON_HIGHLIGHT.drawAt(x, y);
                int exampleIdx = slot - 1;
                if (exampleIdx < examples.size()) {
                    net.minecraft.world.item.ItemStack ex = examples.get(exampleIdx);
                    if (!ex.isEmpty()) {
                        graphics.fakeItem(ex, x, y);
                    }
                }
            }
        }
    }

    @Override
    protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
        BCGraphics graphics = GuiIcon.getGuiGraphics();

        for (int line = 0; line < menu.lines.length; line++) {
            if (!menu.lines[line].isOneStackMode()) continue;
            java.util.List<net.minecraft.world.item.ItemStack> examples = ghostExamplesFor(line);
            for (int slot = 1; slot < buildcraft.lib.list.ListHandler.WIDTH; slot++) {
                int idx = slot - 1;
                if (idx >= examples.size()) break;
                net.minecraft.world.item.ItemStack ex = examples.get(idx);
                if (ex.isEmpty()) continue;
                int x = leftPos + 8 + slot * 18;
                int y = topPos + 32 + line * 34;
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    graphics.setTooltipForNextFrame(font, ex, mouseX, mouseY);
                    return;
                }
            }
        }
    }

    private java.util.List<net.minecraft.world.item.ItemStack> ghostExamplesFor(int lineIdx) {
        buildcraft.lib.list.ListHandler.Line line = menu.lines[lineIdx];
        long sig = ghostSignature(line);
        GhostCache cached = ghostCache.get(lineIdx);
        if (cached != null && cached.signature == sig) {
            return cached.shuffled;
        }
        java.util.List<net.minecraft.world.item.ItemStack> all = new java.util.ArrayList<>(line.getExamples());
        java.util.Collections.shuffle(all);
        ghostCache.put(lineIdx, new GhostCache(sig, all));
        return all;
    }

    private static long ghostSignature(buildcraft.lib.list.ListHandler.Line line) {
        net.minecraft.world.item.ItemStack source = line.getStack(0);
        int itemHash = source.isEmpty() ? 0 : System.identityHashCode(source.getItem());
        int flags = (line.byType ? 1 : 0) | (line.byMaterial ? 2 : 0) | (line.precise ? 4 : 0);
        return ((long) itemHash << 8) | flags;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (labelField != null && labelField.isFocused()) {

            if (event.key() == 257 || event.key() == 335) {
                this.setFocused(null);
                return true;
            }

            if (this.minecraft.options.keyInventory.matches(event)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean entered) {

        if (labelField != null && labelField.isFocused()
                && !labelField.isMouseOver(event.x(), event.y())) {
            this.setFocused(null);
        }
        return super.mouseClicked(event, entered);
    }

}
