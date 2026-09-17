/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.IGuiArea;
import com.google.common.collect.ImmutableList;
import java.util.List;

public interface IGuiElement extends IGuiArea, ITooltipElement, IHelpElement {
   default void drawBackground(float partialTicks) {
   }

   default void drawForeground(float partialTicks) {
   }

   default void tick() {
   }

   @Override
   default void addToolTips(List<ToolTip> tooltips) {
   }

   @Override
   default void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
   }

   default List<IGuiElement> getThisAndChildrenAt(double x, double y) {
      return this.contains(x, y) ? ImmutableList.of(this) : ImmutableList.of();
   }

   default String getDebugInfo(List<String> info) {
      return this.toString();
   }
}
