/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.contents.PageLink;

public class GuidePartLink extends GuidePart {
   public final PageLink link;

   public GuidePartLink(GuiGuide gui, PageLink link) {
      super(gui);
      this.link = link;
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      return this.renderLine(current, this.link.text, x, y, width, height, index);
   }

   @Override
   public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      GuidePart.PagePosition pos = this.renderLine(current, this.link.text, x, y, width, height, -1);
      if (pos.page == index && this.wasHovered()) {
         GuidePageFactory factory = this.link.getFactoryLink();
         if (factory != null) {
            this.gui.openPage(factory.createNew(this.gui));
         }
      }

      return pos;
   }
}
