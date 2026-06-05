/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListMatchHandler.Type;
import buildcraft.api.lists.ListRegistry;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.list.ListHandler;

@SuppressWarnings("this-escape")
public class LedgerListMatch extends Ledger_Neptune {
    private static final Identifier ICON = Identifier.parse("buildcraftlib:textures/icons/help.png");

    private static final int SLOT_X0 = 8;
    private static final int SLOT_Y0 = 32;
    private static final int SLOT_PITCH_X = 18;
    private static final int SLOT_PITCH_Y = 34;
    private static final int SLOT_SIZE = 16;

    private final ContainerList container;

    private int cachedLine = -2;
    private String cachedSig = "";

    public LedgerListMatch(BuildCraftGui gui, ContainerList container) {

        super(gui, 0xFF_99_CC_99, false);
        this.container = container;
        this.title = "gui.ledger.list_match";
        appendIdleText();
        calculateMaxSize();
    }

    @Override
    protected void drawIcon(double x, double y, BCGraphics graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON,
                (int) x, (int) y, 0f, 0f, 16, 16, 16, 16);
    }

    @Override
    public void drawBackground(float partialTicks) {

        if (shouldDrawOpen()) {
            updateHoverContent();
        }
        super.drawBackground(partialTicks);
    }

    private void updateHoverContent() {
        int hoveredLine = getHoveredLineForSlotZero();

        if (hoveredLine < 0 || hoveredLine >= container.lines.length) {
            setIdleIfChanged();
            return;
        }

        ListHandler.Line line = container.lines[hoveredLine];
        if (!line.isOneStackMode()) {
            setIdleIfChanged();
            return;
        }

        ItemStack exemplar = line.getStack(0);
        if (exemplar.isEmpty()) {
            setIdleIfChanged();
            return;
        }

        String sig = hoveredLine + ":" + System.identityHashCode(exemplar.getItem())
                + ":" + line.byType + ":" + line.byMaterial + ":" + line.precise;
        if (sig.equals(cachedSig) && cachedLine == hoveredLine) return;
        cachedSig = sig;
        cachedLine = hoveredLine;

        clearTextEntries();

        boolean any = false;
        if (line.byType) {
            appendText(buildcraft.lib.misc.LocaleUtil.localize("gui.list.match.mode_type"),
                    0xFFFFFF).setDropShadow(true);
            any |= appendHandlerDescriptions(Type.TYPE, exemplar);
        }
        if (line.byMaterial) {
            appendText(buildcraft.lib.misc.LocaleUtil.localize("gui.list.match.mode_material"),
                    0xFFFFFF).setDropShadow(true);
            any |= appendHandlerDescriptions(Type.MATERIAL, exemplar);
        }

        if (!any) {
            appendText(buildcraft.lib.misc.LocaleUtil.localize("gui.list.match.no_handlers"),
                    0xFFAAAA);
            appendText(buildcraft.lib.misc.LocaleUtil.localize("gui.list.match.no_handlers_hint"),
                    0xCCCCCC);
        }

        if (line.precise) {
            appendText(buildcraft.lib.misc.LocaleUtil.localize("gui.list.match.precise_inactive"),
                    0xFFAAAA);
        }

        calculateMaxSize();
    }

    private boolean appendHandlerDescriptions(Type mode, ItemStack exemplar) {
        boolean any = false;
        for (ListMatchHandler handler : ListRegistry.getHandlers()) {
            if (!handler.isValidSource(mode, exemplar)) continue;
            List<String> descriptions = handler.describeMatch(mode, exemplar);
            if (descriptions.isEmpty()) {
                appendText("- " + handler.getClass().getSimpleName(), 0xCCCCCC);
                any = true;
                continue;
            }
            for (String desc : descriptions) {
                appendText("- " + desc, 0xCCCCCC);
            }
            any = true;
        }
        return any;
    }

    private void setIdleIfChanged() {
        if (cachedLine == -1 && "idle".equals(cachedSig)) return;
        cachedLine = -1;
        cachedSig = "idle";
        clearTextEntries();
        appendIdleText();
        calculateMaxSize();
    }

    private void appendIdleText() {
        appendText(buildcraft.lib.misc.LocaleUtil.localize("gui.list.match.idle"), 0xCCCCCC);
    }

    private int getHoveredLineForSlotZero() {
        double mx = gui.mouse.getX() - gui.rootElement.getX();
        double my = gui.mouse.getY() - gui.rootElement.getY();

        if (mx < SLOT_X0 || mx >= SLOT_X0 + SLOT_SIZE) return -1;

        for (int line = 0; line < container.lines.length; line++) {
            int y = SLOT_Y0 + line * SLOT_PITCH_Y;
            if (my >= y && my < y + SLOT_SIZE) return line;
        }
        return -1;
    }
}
