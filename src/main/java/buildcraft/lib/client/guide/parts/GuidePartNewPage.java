/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;

public class GuidePartNewPage extends GuidePart {
   private final int minPixelThreshold;

   public GuidePartNewPage(GuiGuide gui) {
      this(gui, 0);
   }

   public GuidePartNewPage(GuiGuide gui, int minPixelThreshold) {
      super(gui);
      this.minPixelThreshold = minPixelThreshold;
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      return current.pixel < this.minPixelThreshold ? current : current.newPage();
   }

   @Override
   public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      return current.pixel < this.minPixelThreshold ? current : current.newPage();
   }
}
