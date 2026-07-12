/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartItem;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;
import java.util.Arrays;
import java.util.Collections;

public class GuideAssembly extends GuidePartItem {
   public static final GuiIcon INPUT_LIST = new GuiIcon(GuiGuide.ICONS_2, 119.0, 108.0, 98.0, 54.0);
   public static final GuiRectangle[] ITEM_POSITION = new GuiRectangle[6];
   public static final GuiRectangle OUT_POSITION = new GuiRectangle(77.0, 19.0, 16.0, 16.0);
   public static final GuiRectangle MJ_POSITION = new GuiRectangle(50.0, 4.0, 6.0, 46.0);
   public static final GuiRectangle OFFSET = new GuiRectangle(
      (GuiGuide.PAGE_LEFT_TEXT.width - INPUT_LIST.width) / 2.0, 0.0, INPUT_LIST.width, INPUT_LIST.height
   );
   public static final int PIXEL_HEIGHT = 60;
   private final ChangingItemStack[] input;
   private final ChangingItemStack output;
   private final ChangingObject<Long> mjCost;
   private final int hash;

   GuideAssembly(GuiGuide gui, ChangingItemStack[] input, ChangingItemStack output, ChangingObject<Long> mjCost) {
      super(gui);
      this.input = input;
      this.output = output;
      this.mjCost = mjCost;
      this.hash = Arrays.deepHashCode(new Object[]{input, output, mjCost});
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      GuideAssembly other = (GuideAssembly)obj;
      return Arrays.equals(this.input, other.input) && this.output.equals(other.output) && this.mjCost.equals(other.mjCost);
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      if (current.pixel + 60 > height) {
         current = current.newPage();
      }

      x += (int)OFFSET.x;
      y += (int)OFFSET.y + current.pixel;
      if (current.page == index) {
         INPUT_LIST.drawAt(x, y);

         for (int i = 0; i < this.input.length; i++) {
            GuiRectangle rect = ITEM_POSITION[i];
            this.drawItemStack(this.input[i].get(), x + (int)rect.x, y + (int)rect.y);
         }

         this.drawItemStack(this.output.get(), x + (int)OUT_POSITION.x, y + (int)OUT_POSITION.y);
         if (MJ_POSITION.offset(x, y).contains(this.gui.mouse)) {
            this.gui.tooltips.add(Collections.singletonList(this.mjCost.get() + " MJ"));
         }
      }

      return current.nextLine(60, height);
   }

   @Override
   public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      if (current.pixel + 60 > height) {
         current = current.newPage();
      }

      x += (int)OFFSET.x;
      y += (int)OFFSET.y + current.pixel;
      if (current.page == index) {
         for (int i = 0; i < this.input.length; i++) {
            GuiRectangle rect = ITEM_POSITION[i];
            this.testClickItemStack(this.input[i].get(), x + (int)rect.x, y + (int)rect.y);
         }

         this.testClickItemStack(this.output.get(), x + (int)OUT_POSITION.x, y + (int)OUT_POSITION.y);
      }

      return current.nextLine(60, height);
   }

   static {
      for (int x = 0; x < 2; x++) {
         for (int y = 0; y < 3; y++) {
            ITEM_POSITION[x + y * 2] = new GuiRectangle(1 + x * 18, 1 + y * 18, 16.0, 16.0);
         }
      }
   }
}
