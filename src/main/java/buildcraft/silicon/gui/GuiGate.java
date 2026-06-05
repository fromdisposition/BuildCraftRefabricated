package buildcraft.silicon.gui;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.input.MouseButtonEvent;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.statement.GuiElementStatement;
import buildcraft.lib.gui.statement.GuiElementStatementParam;
import buildcraft.lib.gui.statement.GuiElementStatementSource;

import buildcraft.silicon.container.ContainerGate;

public class GuiGate extends GuiBC8<ContainerGate> {
    private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftsilicon:textures/gui/gate_interface.png");

    private static final GuiIcon BACKGROUND_TOP = new GuiIcon(TEXTURE_BASE, 0, 0, 176, 16);
    private static final GuiIcon BACKGROUND_BOTTOM = new GuiIcon(TEXTURE_BASE, 0, 48, 176, 101);
    private static final GuiIcon BACKGROUND_ROW = new GuiIcon(TEXTURE_BASE, 0, 23, 176, 18);

    private final int numRows;

    public GuiGate(ContainerGate container, Inventory playerInventory, Component title) {

        super(container, playerInventory, title, 176, 16 + 101 + container.slotHeight * 18);
        this.numRows = container.slotHeight;
    }

    @Override
    protected void initGuiElements() {
        boolean twoColumns = menu.gate.isSplitInTwo();
        int horizontalSlotCount = twoColumns ? 2 : 1;
        int verticalSlotCount = menu.gate.variant.numSlots / horizontalSlotCount;
        int numTriggerArgs = menu.gate.variant.numTriggerArgs;
        int numActionArgs = menu.gate.variant.numActionArgs;

        int slotPairWidth = 18 * (3 + numTriggerArgs + numActionArgs);
        int slotPairStart = (162 - (slotPairWidth + (twoColumns ? slotPairWidth + 18 : 0))) / 2;

        mainGui.shownElements.add(new buildcraft.lib.gui.statement.GuiElementStatementDrag(mainGui));

        int totalHeight = verticalSlotCount * 18;
        int rowBaseY = 16;
        for (int col = 0; col < horizontalSlotCount; col++) {
            int baseX = slotPairStart + 7 + col * (18 + slotPairWidth);
            int actionStartX = baseX + 18 * (2 + numTriggerArgs);

            mainGui.shownElements.add(new DummyHelpElement(
                    new GuiRectangle(baseX, rowBaseY, 18, totalHeight).offset(mainGui.rootElement),
                    new ElementHelpInfo("buildcraft.help.gate.trigger.title", 0xFF_FF_88_88,
                            "buildcraft.help.gate.trigger.desc1",
                            "buildcraft.help.gate.trigger.desc2")));
            if (numTriggerArgs > 0) {
                mainGui.shownElements.add(new DummyHelpElement(
                        new GuiRectangle(baseX + 18, rowBaseY, 18 * numTriggerArgs, totalHeight).offset(mainGui.rootElement),
                        new ElementHelpInfo("buildcraft.help.gate.params.title", 0xFF_DD_AA_FF,
                                "buildcraft.help.gate.params.desc")));
            }
            mainGui.shownElements.add(new DummyHelpElement(
                    new GuiRectangle(baseX + 18 * (1 + numTriggerArgs), rowBaseY, 18, totalHeight).offset(mainGui.rootElement),
                    new ElementHelpInfo("buildcraft.help.gate.connection.title", 0xFF_88_FF_AA,
                            "buildcraft.help.gate.connection.desc")));
            mainGui.shownElements.add(new DummyHelpElement(
                    new GuiRectangle(actionStartX, rowBaseY, 18, totalHeight).offset(mainGui.rootElement),
                    new ElementHelpInfo("buildcraft.help.gate.action.title", 0xFF_88_CC_FF,
                            "buildcraft.help.gate.action.desc1",
                            "buildcraft.help.gate.action.desc2")));
            if (numActionArgs > 0) {
                mainGui.shownElements.add(new DummyHelpElement(
                        new GuiRectangle(actionStartX + 18, rowBaseY, 18 * numActionArgs, totalHeight).offset(mainGui.rootElement),
                        new ElementHelpInfo("buildcraft.help.gate.params.title", 0xFF_DD_AA_FF,
                                "buildcraft.help.gate.params.desc")));
            }
        }

        mainGui.shownElements.add(new GuiElementStatementSource<>(mainGui, true, menu.possibleTriggersContext));

        mainGui.shownElements.add(new GuiElementStatementSource<>(mainGui, false, menu.possibleActionsContext));

        for (int row = 0; row < verticalSlotCount; row++) {
            for (int col = 0; col < horizontalSlotCount; col++) {
                int pairIndex = row + col * verticalSlotCount;
                int baseX = slotPairStart + 7 + col * (18 + slotPairWidth);
                int baseY = 16 + row * 18;

                IGuiArea triggerArea = new GuiRectangle(baseX, baseY, 18, 18).offset(mainGui.rootElement);
                mainGui.shownElements.add(new GuiElementStatement<>(mainGui, triggerArea, menu.gate.statements[pairIndex].trigger, menu.possibleTriggersContext, true));

                for (int i = 0; i < numTriggerArgs; i++) {
                    IGuiArea paramArea = new GuiRectangle(baseX + 18 * (i + 1), baseY, 18, 18).offset(mainGui.rootElement);
                    mainGui.shownElements.add(new GuiElementStatementParam(mainGui, paramArea, menu.gate, menu.gate.statements[pairIndex].trigger, i, true));
                }

                int actionStartX = baseX + 18 * (2 + numTriggerArgs);
                IGuiArea actionArea = new GuiRectangle(actionStartX, baseY, 18, 18).offset(mainGui.rootElement);
                mainGui.shownElements.add(new GuiElementStatement<>(mainGui, actionArea, menu.gate.statements[pairIndex].action, menu.possibleActionsContext, true));

                for (int i = 0; i < numActionArgs; i++) {
                    IGuiArea paramArea = new GuiRectangle(actionStartX + 18 * (i + 1), baseY, 18, 18).offset(mainGui.rootElement);
                    mainGui.shownElements.add(new GuiElementStatementParam(mainGui, paramArea, menu.gate, menu.gate.statements[pairIndex].action, i, true));
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

        if (event.button() == 0) {
            boolean twoColumns = menu.gate.isSplitInTwo();
            int horizontalSlotCount = twoColumns ? 2 : 1;
            int verticalSlotCount = menu.gate.variant.numSlots / horizontalSlotCount;
            int numTriggerArgs = menu.gate.variant.numTriggerArgs;
            int numActionArgs = menu.gate.variant.numActionArgs;
            int slotPairWidth = 18 * (3 + numTriggerArgs + numActionArgs);
            int slotPairStart = (162 - (slotPairWidth + (twoColumns ? slotPairWidth + 18 : 0))) / 2;

            int mx = (int) event.x() - leftPos;
            int my = (int) event.y() - topPos;

            for (int row = 0; row < verticalSlotCount - 1; row++) {
                for (int col = 0; col < horizontalSlotCount; col++) {
                    int connBaseX = slotPairStart + 7 + col * (18 + slotPairWidth) + 18 * (1 + numTriggerArgs);
                    int connBaseY = 16 + 9 + row * 18;

                    if (mx >= connBaseX && mx < connBaseX + 18 && my >= connBaseY && my < connBaseY + 18) {
                        int pairIndex = row + col * verticalSlotCount;
                        boolean newState = !menu.gate.connections[pairIndex];

                        menu.gate.connections[pairIndex] = newState;

                        menu.setConnected(pairIndex, newState);
                        if (net.minecraft.client.Minecraft.getInstance().player != null) {
                            net.minecraft.client.Minecraft.getInstance().player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void init() {
        super.init();

        menu.requestValidStatements();
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {

        BACKGROUND_TOP.drawAt(leftPos, topPos);

        for (int i = 0; i < numRows; i++) {
            BACKGROUND_ROW.drawAt(leftPos, topPos + 16 + i * 18);
        }

        BACKGROUND_BOTTOM.drawAt(leftPos, topPos + 16 + numRows * 18);

        boolean twoColumns = menu.gate.isSplitInTwo();
        int horizontalSlotCount = twoColumns ? 2 : 1;
        int verticalSlotCount = menu.gate.variant.numSlots / horizontalSlotCount;
        int numTriggerArgs = menu.gate.variant.numTriggerArgs;
        int numActionArgs = menu.gate.variant.numActionArgs;
        int slotPairWidth = 18 * (3 + numTriggerArgs + numActionArgs);
        int slotPairStart = (162 - (slotPairWidth + (twoColumns ? slotPairWidth + 18 : 0))) / 2;

        for (int row = 0; row < verticalSlotCount; row++) {
            for (int col = 0; col < horizontalSlotCount; col++) {
                int pairIndex = row + col * verticalSlotCount;
                int baseX = leftPos + slotPairStart + 7 + col * (18 + slotPairWidth);
                int baseY = topPos + 16 + row * 18;

                int connectorPos = baseX + 18 * (1 + numTriggerArgs);

                boolean triggerOn = menu.gate.triggerOn[pairIndex] && menu.gate.statements[pairIndex].trigger.get() != null;
                boolean actionOn = menu.gate.actionOn[pairIndex] && menu.gate.statements[pairIndex].action.get() != null;

                boolean connectedIsOn = false;

                if (pairIndex < menu.gate.connections.length && menu.gate.connections[pairIndex]) {

                    connectedIsOn = menu.gate.actionOn[pairIndex] || menu.gate.actionOn[pairIndex + 1];
                } else if (pairIndex > 0 && menu.gate.connections[pairIndex - 1]) {
                    connectedIsOn = menu.gate.actionOn[pairIndex] || menu.gate.actionOn[pairIndex - 1];
                } else {
                    connectedIsOn = actionOn;
                }

                new GuiIcon(TEXTURE_BASE, 176 + (triggerOn ? 18 : 0), 18, 7, 18).drawAt(connectorPos, baseY);

                new GuiIcon(TEXTURE_BASE, 187 + (actionOn ? 18 : 0), 18, 7, 18).drawAt(connectorPos + 11, baseY);

                new GuiIcon(TEXTURE_BASE, 180 + (connectedIsOn ? 18 : 0), 18, 4, 18).drawAt(connectorPos + 7, baseY);

                if (row < verticalSlotCount - 1) {
                    int connBaseX = connectorPos;
                    int connBaseY = baseY + 9;
                    boolean isConnected = menu.gate.connections[pairIndex];
                    boolean actionAbove = menu.gate.actionOn[pairIndex];
                    boolean actionBelow = menu.gate.actionOn[pairIndex + 1];

                    new GuiIcon(TEXTURE_BASE, 176 + (actionAbove ? 18 : 0), 36 + (isConnected ? 18 : 0), 18, 9).drawAt(connBaseX, connBaseY);

                    new GuiIcon(TEXTURE_BASE, 176 + (actionBelow ? 18 : 0), 36 + 9 + (isConnected ? 18 : 0), 18, 9).drawAt(connBaseX, connBaseY + 9);

                    int mx = (int) this.mainGui.mouse.getX();
                    int my = (int) this.mainGui.mouse.getY();
                    boolean hovered = (mx >= connBaseX && mx < connBaseX + 18 && my >= connBaseY && my < connBaseY + 18);

                    int btnU = hovered ? 194 : 176;
                    int btnV = 72;
                    new GuiIcon(TEXTURE_BASE, btnU, btnV, 18, 18).drawAt(connBaseX, connBaseY);
                }
            }
        }
    }

    @Override
    protected void drawForegroundLayer() {
        BCGraphics graphics = GuiIcon.getGuiGraphics();

        String titleStr = menu.gate.variant.getLocalizedName().getString();
        graphics.text(font, titleStr, (imageWidth - font.width(titleStr)) / 2, 6, 0xFF404040, false);

        String invStr = Component.translatable("container.inventory").getString();
        graphics.text(font, invStr, 8, 16 + numRows * 18 + 4, 0xFF404040, false);
    }
}
