/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.help;

import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiArea;
import java.util.List;

public class DummyHelpElement implements IGuiElement {
   public final IGuiArea area;
   public final ElementHelpInfo help;

   public DummyHelpElement(IGuiArea area, ElementHelpInfo help) {
      this.area = area;
      this.help = help;
   }

   @Override
   public double getX() {
      return this.area.getX();
   }

   @Override
   public double getY() {
      return this.area.getY();
   }

   @Override
   public double getWidth() {
      return this.area.getWidth();
   }

   @Override
   public double getHeight() {
      return this.area.getHeight();
   }

   @Override
   public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
      elements.add(this.help.target(this.area));
   }
}
