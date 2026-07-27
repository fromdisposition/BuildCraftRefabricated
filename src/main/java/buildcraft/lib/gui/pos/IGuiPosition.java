/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

import java.util.function.DoubleSupplier;

public interface IGuiPosition {
   double getX();

   double getY();

   default IGuiPosition offset(DoubleSupplier x, DoubleSupplier y) {
      return this.offset(new PositionCallable(x, y));
   }

   default IGuiPosition offset(double x, DoubleSupplier y) {
      return this.offset(new PositionCallable(x, y));
   }

   default IGuiPosition offset(DoubleSupplier x, double y) {
      return this.offset(new PositionCallable(x, y));
   }

   default IGuiPosition offset(double x, double y) {
      return PositionOffset.createOffset(this, x, y);
   }

   default IGuiPosition offset(IGuiPosition by) {
      return new PositionAdded(this, by);
   }

   static IGuiPosition create(DoubleSupplier x, DoubleSupplier y) {
      return new PositionCallable(x, y);
   }
}
