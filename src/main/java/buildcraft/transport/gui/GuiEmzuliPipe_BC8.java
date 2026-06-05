package buildcraft.transport.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.button.BCButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.ColourUtil;

import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

public class GuiEmzuliPipe_BC8 extends GuiBC8<ContainerEmzuliPipe_BC8> {
    private static final Identifier TEXTURE =
            Identifier.parse("buildcrafttransport:textures/gui/pipe_emzuli.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);

    private PaintButton[] paintButtons = new PaintButton[4];

    public GuiEmzuliPipe_BC8(ContainerEmzuliPipe_BC8 menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);
    }

    private static final int[][] FILTER_SLOTS = { {25, 21}, {25, 49}, {134, 21}, {134, 49} };

    private static final int[][] PAINT_BUTTONS = { {49, 19}, {49, 47}, {106, 19}, {106, 47} };

    @Override
    protected void initGuiElements() {
        paintButtons[0] = addPaintButton(SlotIndex.SQUARE, 49, 19);
        paintButtons[1] = addPaintButton(SlotIndex.CIRCLE, 49, 47);
        paintButtons[2] = addPaintButton(SlotIndex.TRIANGLE, 106, 19);
        paintButtons[3] = addPaintButton(SlotIndex.CROSS, 106, 47);

        for (int[] pos : FILTER_SLOTS) {
            mainGui.shownElements.add(new DummyHelpElement(
                    new GuiRectangle(pos[0], pos[1], 16, 16).offset(mainGui.rootElement),
                    new ElementHelpInfo("buildcraft.help.emzuli.filter.title", 0xFF_88_CC_FF,
                            "buildcraft.help.emzuli.filter.desc")));
        }
        for (int[] pos : PAINT_BUTTONS) {
            mainGui.shownElements.add(new DummyHelpElement(
                    new GuiRectangle(pos[0], pos[1], 20, 20).offset(mainGui.rootElement),
                    new ElementHelpInfo("buildcraft.help.emzuli.paint.title", 0xFF_DD_AA_FF,
                            "buildcraft.help.emzuli.paint.desc1",
                            "buildcraft.help.emzuli.paint.desc2")));
        }
    }

    private PaintButton addPaintButton(SlotIndex index, int x, int y) {
        int bx = leftPos + x;
        int by = topPos + y;
        PaintButton btn = new PaintButton(index, bx, by);
        addRenderableWidget(btn);
        return btn;
    }

    private PaintButton activePressedButton = null;

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        int button = event.button();
        for (PaintButton btn : paintButtons) {
            if (btn != null && btn.isMouseOver(mouseX, mouseY)) {
                btn.handleClick(button);
                activePressedButton = btn;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (activePressedButton != null) {
            activePressedButton = null;
            return true;
        }
        return super.mouseReleased(event);
    }

    private class PaintButton extends BCButton {
        private final SlotIndex index;
        private int pressedButton = -1;

        public PaintButton(SlotIndex index, int x, int y) {
            super(x, y, 20, 20, Component.empty());
            this.index = index;
            updateTooltip();
        }

        @Override
        public void onPress(net.minecraft.client.input.InputWithModifiers input) {

        }

        public void handleClick(int button) {
            DyeColor current = menu.behaviour.slotColours.get(index);
            DyeColor next;
            switch (button) {
                case 0 -> next = cycleColour(current);
                case 1 -> next = cycleColourBackward(current);
                case 2 -> next = null;
                default -> { return; }
            }
            menu.paintWidgets.get(index).setColour(next);
            if (next == null) menu.behaviour.slotColours.remove(index);
            else menu.behaviour.slotColours.put(index, next);
            updateTooltip();

            pressedButton = button;
        }

        private void updateTooltip() {
            DyeColor colour = menu.behaviour.slotColours.get(index);
            Component tooltip;
            if (colour == null) {
                tooltip = Component.translatable("gui.pipes.emzuli.nopaint");
            } else {
                tooltip = Component.translatable("gui.pipes.emzuli.paint",
                        ColourUtil.getTextFullTooltip(colour));
            }
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        }

        @Override
        protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {

            int v = (activePressedButton == this) ? 20 : 0;
            GuiIcon bgIcon = new GuiIcon(TEXTURE, 176, v, 20, 20, 256);
            bgIcon.drawAt(getX(), getY());

            DyeColor colour = menu.behaviour.slotColours.get(index);
            if (colour == null) {
                GuiIcon noPaint = new GuiIcon(TEXTURE, 176, 40, 16, 16, 256);
                noPaint.drawAt(getX() + 2, getY() + 2);
            } else {
                Identifier brushTex = Identifier.parse("buildcraftcore:textures/item/paintbrush/" + colour.getName() + ".png");
                GuiIcon brushIcon = new GuiIcon(brushTex, 0, 0, 16, 16, 16);
                brushIcon.drawAt(getX() + 2, getY() + 2);
            }
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static DyeColor cycleColour(DyeColor current) {
        if (current == null) return DyeColor.WHITE;
        int next = current.ordinal() + 1;
        if (next >= 16) return null;
        return DyeColor.byId(next);
    }

    private static DyeColor cycleColourBackward(DyeColor current) {
        if (current == null) return DyeColor.byId(15);
        int next = current.ordinal() - 1;
        if (next < 0) return null;
        return DyeColor.byId(next);
    }

    @Override
    protected void drawForegroundLayer() {
        BCGraphics graphics = GuiIcon.getGuiGraphics();

        String titleStr = Component.translatable("gui.pipes.emzuli.title").getString();
        int titleX = (imageWidth - font.width(titleStr)) / 2;
        graphics.text(font, titleStr, titleX, 6, 0xFF404040, false);

        graphics.text(font, playerInventoryTitle, 8, imageHeight - 93, 0xFF404040, false);

        SlotIndex currentSlot = menu.behaviour.getCurrentSlot();
        for (SlotIndex index : menu.behaviour.getActiveSlots()) {
            boolean current = index == currentSlot;
            int ix = (index.ordinal() < 2 ? 4 : 155);
            int iy = (index.ordinal() % 2 == 0 ? 21 : 49);
            int colour = current ? 0xFF00FF00 : 0xFFFFFF00;
            graphics.fill(ix, iy, ix + 4, iy + 16, colour);
        }
    }
}
