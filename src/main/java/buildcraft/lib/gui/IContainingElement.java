/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.IGuiPosition;
import java.util.ArrayList;
import java.util.List;

public interface IContainingElement extends IInteractionElement {
   List<IGuiElement> getChildElements();

   default IGuiPosition getChildElementPosition() {
      return this;
   }

   default void calculateSizes() {
   }

   @Override
   default void addToolTips(List<ToolTip> tooltips) {
      for (IGuiElement elem : this.getChildElements()) {
         elem.addToolTips(tooltips);
      }
   }

   @Override
   default void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
      for (IGuiElement elem : this.getChildElements()) {
         elem.addHelpElements(elements);
      }
   }

   @Override
   default List<IGuiElement> getThisAndChildrenAt(double x, double y) {
      List<IGuiElement> list = new ArrayList<>();
      if (this.contains(x, y)) {
         list.add(this);

         for (IGuiElement elem : this.getChildElements()) {
            list.addAll(elem.getThisAndChildrenAt(x, y));
         }
      }

      return list;
   }

   @Override
   default void onMouseClicked(int button) {
      for (IGuiElement elem : this.getChildElements()) {
         if (elem instanceof IInteractionElement) {
            ((IInteractionElement)elem).onMouseClicked(button);
         }
      }
   }

   @Override
   default void onMouseReleased(int button) {
      for (IGuiElement elem : this.getChildElements()) {
         if (elem instanceof IInteractionElement) {
            ((IInteractionElement)elem).onMouseReleased(button);
         }
      }
   }

   @Override
   default void onMouseDragged(int button, long ticksSinceClick) {
      for (IGuiElement elem : this.getChildElements()) {
         if (elem instanceof IInteractionElement) {
            ((IInteractionElement)elem).onMouseDragged(button, ticksSinceClick);
         }
      }
   }

   @Override
   default boolean onKeyPress(char typedChar, int keyCode) {
      boolean action = false;

      for (IGuiElement elem : this.getChildElements()) {
         if (elem instanceof IInteractionElement) {
            action |= ((IInteractionElement)elem).onKeyPress(typedChar, keyCode);
         }
      }

      return action;
   }
}
