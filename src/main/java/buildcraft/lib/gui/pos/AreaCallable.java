/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

import java.util.function.DoubleSupplier;

public class AreaCallable implements IGuiArea {
   public final DoubleSupplier x;
   public final DoubleSupplier y;
   public final DoubleSupplier width;
   public final DoubleSupplier height;

   public AreaCallable(DoubleSupplier x, DoubleSupplier y, DoubleSupplier width, DoubleSupplier height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public AreaCallable(DoubleSupplier width, DoubleSupplier height) {
      this(() -> 0.0, () -> 0.0, width, height);
   }

   @Override
   public double getX() {
      return this.x.getAsDouble();
   }

   @Override
   public double getY() {
      return this.y.getAsDouble();
   }

   @Override
   public double getWidth() {
      return this.width.getAsDouble();
   }

   @Override
   public double getHeight() {
      return this.height.getAsDouble();
   }
}
