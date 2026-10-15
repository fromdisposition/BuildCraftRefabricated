/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.pos;

public class PositionOffset implements IGuiPosition {
   public final IGuiPosition parent;
   public final double xOffset;
   public final double yOffset;

   private PositionOffset(IGuiPosition parent, double xOffset, double yOffset) {
      this.parent = parent;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
   }

   public static IGuiPosition createOffset(IGuiPosition from, double x, double y) {
      if (from == null) {
         return new PositionAbsolute(x, y);
      } else if (from instanceof PositionOffset parent) {
         double oX = x + parent.xOffset;
         double oY = y + parent.yOffset;
         return parent.parent.offset(oX, oY);
      } else {
         return new PositionOffset(from, x, y);
      }
   }

   @Override
   public double getX() {
      return this.parent.getX() + this.xOffset;
   }

   @Override
   public double getY() {
      return this.parent.getY() + this.yOffset;
   }

   @Override
   public IGuiPosition offset(double x, double y) {
      return new PositionOffset(this.parent, x + this.xOffset, y + this.yOffset);
   }

   @Override
   public IGuiPosition offset(IGuiPosition by) {
      return by instanceof PositionOffset other
         ? new PositionOffset(this.parent.offset(other.parent), this.xOffset + other.xOffset, this.yOffset + other.yOffset)
         : IGuiPosition.super.offset(by);
   }
}
