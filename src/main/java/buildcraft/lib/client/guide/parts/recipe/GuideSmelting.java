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
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class GuideSmelting extends GuidePartItem {
   public static final GuiIcon SMELTING_ICON = new GuiIcon(GuiGuide.ICONS_2, 119.0, 54.0, 80.0, 54.0);
   public static final GuiRectangle OFFSET = new GuiRectangle(
      (GuiGuide.PAGE_LEFT_TEXT.width - SMELTING_ICON.width) / 2.0, 0.0, SMELTING_ICON.width, SMELTING_ICON.height
   );
   public static final GuiRectangle IN_POS = new GuiRectangle(1.0, 1.0, 16.0, 16.0);
   public static final GuiRectangle OUT_POS = new GuiRectangle(59.0, 19.0, 16.0, 16.0);
   public static final GuiRectangle FURNACE_POS = new GuiRectangle(1.0, 37.0, 16.0, 16.0);
   public static final int PIXEL_HEIGHT = 60;
   private final ChangingItemStack input;
   private final ChangingItemStack output;
   private final ItemStack furnace;
   private final int hash;

   public GuideSmelting(GuiGuide gui, @Nonnull ItemStack input, @Nonnull ItemStack output) {
      super(gui);
      this.input = new ChangingItemStack(input);
      this.output = new ChangingItemStack(output);
      this.furnace = new ItemStack(Blocks.FURNACE);
      this.hash = Objects.hash(input, output);
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

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      GuideSmelting other = (GuideSmelting)obj;
      return this.hash != other.hash ? false : this.input.equals(other.input) && this.output.equals(other.output);
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      if (current.pixel + 60 > height) {
         current = current.newPage();
      }

      x += (int)OFFSET.x;
      y += (int)OFFSET.y + current.pixel;
      if (current.page == index) {
         SMELTING_ICON.drawAt(x, y);
         this.drawItemStack(this.input.get(), x + (int)IN_POS.x, y + (int)IN_POS.y);
         this.drawItemStack(this.output.get(), x + (int)OUT_POS.x, y + (int)OUT_POS.y);
         this.drawItemStack(this.furnace, x + (int)FURNACE_POS.x, y + (int)FURNACE_POS.y);
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
         this.testClickItemStack(this.input.get(), x + (int)IN_POS.x, y + (int)IN_POS.y);
         this.testClickItemStack(this.output.get(), x + (int)OUT_POS.x, y + (int)OUT_POS.y);
         this.testClickItemStack(this.furnace, x + (int)FURNACE_POS.x, y + (int)FURNACE_POS.y);
      }

      return current.nextLine(60, height);
   }
}
