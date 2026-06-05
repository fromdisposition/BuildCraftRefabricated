/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.client.tooltip;

import java.util.List;

import org.joml.Vector2ic;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import buildcraft.fabric.client.event.RenderTooltipEvent;

import buildcraft.api.schematics.ISchematicBlock;

import buildcraft.builders.client.render.BlueprintRenderer;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.snapshot.Blueprint;

public final class SchematicSingleTooltipOverlay {

    private SchematicSingleTooltipOverlay() {}

    public static final int PREVIEW_SIZE = 100;

    private static final int VISIBLE_GAP = 4;

    private static final int FRAME_PADDING = 3;

    @Nullable
    private static ISchematicBlock cachedSchematic;
    @Nullable
    private static Blueprint cachedSynthetic;

    public static void onPreTooltip(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemSchematicSingle schemItem) || !schemItem.isUsed()) {
            return;
        }
        ISchematicBlock schematic = ItemSchematicSingle.getSchematicSafe(stack);
        if (schematic == null) {
            return;
        }

        Font font = event.getFont();
        List<ClientTooltipComponent> components = event.getComponents();
        if (components.isEmpty()) {
            return;
        }
        int textWidth = 0;
        int contentHeight = components.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent c : components) {
            int w = c.getWidth(font);
            if (w > textWidth) {
                textWidth = w;
            }
            contentHeight += c.getHeight(font);
        }
        ClientTooltipPositioner positioner = event.getTooltipPositioner();
        Vector2ic finalPos = positioner.positionTooltip(
                event.getScreenWidth(), event.getScreenHeight(),
                event.getX(), event.getY(), textWidth, contentHeight);
        int finalX = finalPos.x();
        int finalY = finalPos.y();
        int pX = finalX;
        int pY = finalY + contentHeight + FRAME_PADDING + VISIBLE_GAP + FRAME_PADDING;

        TooltipRenderUtil.extractTooltipBackground(
                event.getGraphics(), pX, pY, PREVIEW_SIZE, PREVIEW_SIZE, null);

        Blueprint synthetic = getOrBuildSynthetic(schematic);

        BlueprintRenderer.renderSnapshot(
                new buildcraft.lib.gui.BCGraphics(event.getGraphics()), synthetic, pX, pY, PREVIEW_SIZE, PREVIEW_SIZE);
    }

    private static Blueprint getOrBuildSynthetic(ISchematicBlock schematic) {
        Blueprint cached = cachedSynthetic;
        if (cached != null && schematic.equals(cachedSchematic)) {
            return cached;
        }
        Blueprint synthetic = new Blueprint();
        synthetic.size = new BlockPos(1, 1, 1);
        synthetic.offset = BlockPos.ZERO;
        synthetic.facing = Direction.NORTH;
        synthetic.data = new int[] { 0 };
        synthetic.palette.add(schematic);
        cachedSchematic = schematic;
        cachedSynthetic = synthetic;
        return synthetic;
    }
}

