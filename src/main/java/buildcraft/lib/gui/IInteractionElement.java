/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

public interface IInteractionElement extends IGuiElement {
   default void onMouseClicked(int button) {
   }

   default void onMouseDragged(int button, long ticksSinceClick) {
   }

   default void onMouseReleased(int button) {
   }

   default boolean onKeyPress(char typedChar, int keyCode) {
      return false;
   }

   default boolean onMouseScroll(double amount) {
      return false;
   }
}
