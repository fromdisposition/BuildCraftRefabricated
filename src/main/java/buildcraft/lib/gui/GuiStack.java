/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import net.minecraft.world.item.ItemStack;

public class GuiStack implements ISimpleDrawable {
   private final ItemStack stack;
   private static BCGraphics currentGraphics;

   public static void setGuiGraphics(BCGraphics graphics) {
      currentGraphics = graphics;
   }

   public GuiStack(ItemStack stack) {
      this.stack = stack;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   @Override
   public void drawAt(double x, double y) {
      if (currentGraphics != null && this.stack != null && !this.stack.isEmpty()) {
         currentGraphics.fakeItem(this.stack, (int)x, (int)y);
      }
   }
}
