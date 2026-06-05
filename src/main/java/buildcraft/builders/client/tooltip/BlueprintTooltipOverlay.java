/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.client.tooltip;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2ic;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.world.item.ItemStack;

import buildcraft.fabric.client.event.RenderTooltipEvent;

import buildcraft.builders.client.render.BlueprintRenderer;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.builders.snapshot.Snapshot;

public final class BlueprintTooltipOverlay {

    private BlueprintTooltipOverlay() {}

    private static final Logger LOGGER = LogManager.getLogger("BCBlueprintTooltipOverlay");

    public static final int PREVIEW_SIZE = 100;

    private static final int VISIBLE_GAP = 4;

    private static final int FRAME_PADDING = 3;

    private static final Set<String> LOGGED_KEYS = Collections.synchronizedSet(new HashSet<>());

    public static void onPreTooltip(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemSnapshot)) {
            return;
        }
        Snapshot.Header header = ItemSnapshot.getHeader(stack);
        if (header == null) {

            return;
        }

        Snapshot snapshot = ClientSnapshots.INSTANCE.getSnapshot(header.key);

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

        if (snapshot != null) {
            BlueprintRenderer.renderSnapshot(
                    new buildcraft.lib.gui.BCGraphics(event.getGraphics()), snapshot, pX, pY, PREVIEW_SIZE, PREVIEW_SIZE);
        }

        logOnce(header.key, snapshot, pX, pY);
    }

    private static void logOnce(Snapshot.Key key, Snapshot snapshot, int pX, int pY) {
        String hashHex = key.hash == null ? "null"
                : buildcraft.lib.misc.HashUtil.convertHashToString(key.hash);
        if (LOGGED_KEYS.add(hashHex)) {
            LOGGER.info("Overlay: hash={} snapshot={} at ({}, {}) {}x{}",
                    hashHex,
                    snapshot == null ? "pending" : snapshot.getClass().getSimpleName(),
                    pX, pY, PREVIEW_SIZE, PREVIEW_SIZE);
        }
    }
}

