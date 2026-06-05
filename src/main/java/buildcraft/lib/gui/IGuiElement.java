/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.util.List;

import com.google.common.collect.ImmutableList;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;

public interface IGuiElement extends IGuiArea, ITooltipElement, IHelpElement {
    default void drawBackground(float partialTicks) {}

    default void drawForeground(float partialTicks) {}

    default void tick() {}

    @Override
    default void addToolTips(List<ToolTip> tooltips) {}

    @Override
    default void addHelpElements(List<HelpPosition> elements) {}

    default List<IGuiElement> getThisAndChildrenAt(double x, double y) {
        if (contains(x, y)) {
            return ImmutableList.of(this);
        } else {
            return ImmutableList.of();
        }
    }

    default String getDebugInfo(List<String> info) {
        return toString();
    }
}
