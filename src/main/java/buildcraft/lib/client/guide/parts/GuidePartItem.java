/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.ItemStackKey;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public abstract class GuidePartItem extends GuidePart {
   public static final GuiRectangle STACK_RECT = new GuiRectangle(0.0, 0.0, 16.0, 16.0);

   public GuidePartItem(GuiGuide gui) {
      super(gui);
   }

   protected void drawItemStack(ItemStackKey stack, int x, int y) {
      this.drawItemStack(stack.baseStack, x, y);
   }

   protected void drawItemStack(ItemStack stack, int x, int y) {
      if (stack != null && !stack.isEmpty()) {
         BCGraphics graphics = GuiIcon.getGuiGraphics();
         if (graphics != null) {
            graphics.fakeItem(stack, x, y);
            graphics.itemDecorations(Minecraft.getInstance().font, stack, x, y);
         }

         if (STACK_RECT.offset(x, y).contains(this.gui.mouse)) {
            this.gui.tooltipStack = stack;
         }
      }
   }

   protected void testClickItemStack(ItemStackKey stack, int x, int y) {
      this.testClickItemStack(stack.baseStack, x, y);
   }

   protected void testClickItemStack(ItemStack stack, int x, int y) {
      if (stack != null && !stack.isEmpty() && STACK_RECT.offset(x, y).contains(this.gui.mouse)) {
         GuidePageFactory factory = GuideManager.INSTANCE.getPageFor(stack);
         if (factory != null) {
            this.gui.openPage(factory.createNew(this.gui));
         }
      }
   }
}
