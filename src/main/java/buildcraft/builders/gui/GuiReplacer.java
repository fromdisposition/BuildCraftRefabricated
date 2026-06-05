/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.gui;

import net.minecraft.ChatFormatting;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;

import buildcraft.builders.client.render.BlueprintRenderer;
import buildcraft.builders.container.ContainerReplacer;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.builders.snapshot.Snapshot;

public class GuiReplacer extends GuiBC8<ContainerReplacer> {
    private static final Identifier TEXTURE =
            Identifier.parse("buildcraftbuilders:textures/gui/replacer.png");
    private static final int SIZE_X = 176, SIZE_Y = 241;

    private static final int PREVIEW_X = 8, PREVIEW_Y = 9;
    private static final int PREVIEW_W = 160, PREVIEW_H = 100;

    private static final int NAME_X = 30, NAME_Y = 117;
    private static final int NAME_W = 138, NAME_H = 12;

    private static final int REPLACE_X = 80, REPLACE_Y = 135;
    private static final int REPLACE_W = 60, REPLACE_H = 20;

    private static final int SUMMARY_X = 8, SUMMARY_Y = 156;

    private EditBox nameField;
    private Button replaceButton;

    private Snapshot.Key lastSeededKey;

    public GuiReplacer(ContainerReplacer container, Inventory playerInv, Component title) {
        super(container, playerInv, title, SIZE_X, SIZE_Y);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void initGuiElements() {
        if (menu.tile != null) {
            mainGui.shownElements.add(new LedgerOwnership(mainGui,
                () -> menu.tile != null ? menu.tile.getOwner() : null,
                true
            ));
        }

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(PREVIEW_X, PREVIEW_Y, PREVIEW_W, PREVIEW_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.replacer.preview.title", 0xFF_88_CC_FF,
                        "buildcraft.help.replacer.preview.desc1",
                        "buildcraft.help.replacer.preview.desc2")));

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(8, 115, 16, 16).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.replacer.snapshot.title", 0xFF_88_CC_88,
                        "buildcraft.help.replacer.snapshot.desc")));

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(NAME_X, NAME_Y, NAME_W, NAME_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.replacer.name.title", 0xFF_E1_C9_2F,
                        "buildcraft.help.replacer.name.desc")));

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(8, 137, 16, 16).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.replacer.from.title", 0xFF_FF_88_88,
                        "buildcraft.help.replacer.from.desc")));

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(56, 137, 16, 16).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.replacer.to.title", 0xFF_88_FF_88,
                        "buildcraft.help.replacer.to.desc")));

        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(REPLACE_X, REPLACE_Y, REPLACE_W, REPLACE_H).offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.replacer.replace.title", 0xFF_CC_AA_88,
                        "buildcraft.help.replacer.replace.desc1",
                        "buildcraft.help.replacer.replace.desc2")));
    }

    @Override
    protected void init() {
        super.init();

        nameField = new EditBox(this.font, leftPos + NAME_X, topPos + NAME_Y, NAME_W, NAME_H,
                Component.empty());
        nameField.setMaxLength(64);
        nameField.setValue(menu.getBlueprintName());
        nameField.setFocused(false);
        lastSeededKey = currentBlueprintKey();
        addRenderableWidget(nameField);

        replaceButton = Button.builder(
                    Component.translatable("gui.buildcraft.replacer.replace"),
                    b -> onReplacePressed())
                .bounds(leftPos + REPLACE_X, topPos + REPLACE_Y, REPLACE_W, REPLACE_H)
                .build();
        addRenderableWidget(replaceButton);
        updateReplaceButtonActive();
    }

    private void onReplacePressed() {
        final String newName = nameField.getValue().trim();
        menu.sendMessage(ContainerReplacer.NET_REPLACE, buf -> buf.writeUtf(newName));
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        Snapshot.Key currentKey = currentBlueprintKey();
        boolean keyChanged = !java.util.Objects.equals(currentKey, lastSeededKey);
        if (keyChanged && nameField != null && !nameField.isFocused()) {
            nameField.setValue(menu.getBlueprintName());
            lastSeededKey = currentKey;
        } else if (keyChanged && nameField != null) {

            lastSeededKey = currentKey;
        }

        updateReplaceButtonActive();
    }

    private void updateReplaceButtonActive() {
        if (replaceButton == null) return;
        replaceButton.active = canReplace();
    }

    private boolean canReplace() {
        ItemStack snap = menu.getSlot(0).getItem();
        ItemStack from = menu.getSlot(1).getItem();
        ItemStack to = menu.getSlot(2).getItem();
        if (snap.isEmpty() || from.isEmpty() || to.isEmpty()) {
            return false;
        }
        Snapshot.Header header = ItemSnapshot.getHeader(snap);
        if (header == null) {
            return false;
        }

        return ItemSchematicSingle.getSchematicSafe(from) != null
            && ItemSchematicSingle.getSchematicSafe(to) != null;
    }

    private Snapshot.Key currentBlueprintKey() {
        if (menu.slots.isEmpty()) return null;
        ItemStack snap = menu.getSlot(0).getItem();
        if (snap.isEmpty() || !(snap.getItem() instanceof ItemSnapshot)) return null;
        Snapshot.Header h = ItemSnapshot.getHeader(snap);
        return h == null ? null : h.key;
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                leftPos, topPos,
                0f, 0f,
                imageWidth, imageHeight,
                256, 256);

        Blueprint blueprint = resolveCurrentBlueprint();
        if (blueprint == null) {
            return;
        }

        Blueprint toRender = maybeApplyPendingReplacement(blueprint);
        BlueprintRenderer.renderSnapshot(graphics, toRender,
                leftPos + PREVIEW_X, topPos + PREVIEW_Y, PREVIEW_W, PREVIEW_H);
    }

    private Blueprint resolveCurrentBlueprint() {
        ItemStack snap = menu.getSlot(0).getItem();
        if (snap.isEmpty() || !(snap.getItem() instanceof ItemSnapshot)) return null;
        Snapshot.Header header = ItemSnapshot.getHeader(snap);
        if (header == null) return null;
        Snapshot s = ClientSnapshots.INSTANCE.getSnapshot(header.key);
        return s instanceof Blueprint bp ? bp : null;
    }

    private Blueprint maybeApplyPendingReplacement(Blueprint blueprint) {
        ItemStack fromStack = menu.getSlot(1).getItem();
        ItemStack toStack = menu.getSlot(2).getItem();
        if (fromStack.isEmpty() || toStack.isEmpty()) {
            return blueprint;
        }
        ISchematicBlock from = ItemSchematicSingle.getSchematicSafe(fromStack);
        ISchematicBlock to = ItemSchematicSingle.getSchematicSafe(toStack);
        if (from == null || to == null) {
            return blueprint;
        }
        Blueprint preview = blueprint.copy();
        preview.replace(from, to);
        return preview;
    }

    @Override
    protected void drawForegroundLayer() {

        String summary = buildSummaryText();
        if (summary != null) {
            BCGraphics graphics = buildcraft.lib.gui.GuiIcon.getGuiGraphics();
            if (graphics != null) {
                int color = 0xFF_40_40_40;
                graphics.text(font, summary, SUMMARY_X, SUMMARY_Y, color, false);
            }
        }
    }

    private String buildSummaryText() {
        ItemStack fromStack = menu.getSlot(1).getItem();
        ItemStack toStack = menu.getSlot(2).getItem();
        if (fromStack.isEmpty() || toStack.isEmpty()) {
            return null;
        }
        ISchematicBlock from = ItemSchematicSingle.getSchematicSafe(fromStack);
        ISchematicBlock to = ItemSchematicSingle.getSchematicSafe(toStack);
        if (from == null || to == null) {
            return null;
        }
        Blueprint blueprint = resolveCurrentBlueprint();
        if (blueprint == null) {
            return null;
        }
        int count = blueprint.countMatchingCells(from);
        String fromName = schematicDisplayName(from);
        String toName = schematicDisplayName(to);
        return Component.translatable("gui.buildcraft.replacer.summary",
                count, fromName, toName).getString();
    }

    private static String schematicDisplayName(ISchematicBlock schematic) {
        if (schematic == null) {
            return "?";
        }

        var state = schematic.getBlockStateForRender();
        if (state == null) {
            return "?";
        }
        Block block = state.getBlock();
        return block == null ? "?" : block.getName().getString();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (nameField != null && nameField.isFocused()) {

            if (event.key() == 257 || event.key() == 335) {
                this.setFocused(null);
                return true;
            }
            if (event.key() == 256) {
                return super.keyPressed(event);
            }

            if (this.nameField.keyPressed(event) || this.nameField.canConsumeInput()) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (nameField != null && nameField.isFocused()
                && !nameField.isMouseOver(event.x(), event.y())) {
            this.setFocused(null);
        }
        return super.mouseClicked(event, doubleClick);
    }

    @SuppressWarnings("unused")
    private static String grey(String s) {
        return ChatFormatting.GRAY + s + ChatFormatting.RESET;
    }

    @SuppressWarnings("unused")
    private static boolean schematicReadable(ItemStack stack) {
        try {
            return ItemSchematicSingle.getSchematic(stack) != null;
        } catch (InvalidInputDataException e) {
            return false;
        }
    }
}
