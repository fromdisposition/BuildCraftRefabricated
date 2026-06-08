/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import java.util.Arrays;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class GuideSmeltingFactory implements GuidePartFactory {
   @Nonnull
   private final ItemStack input;
   @Nonnull
   private final ItemStack output;
   private final int hash;

   public GuideSmeltingFactory(ItemStack input, ItemStack output) {
      this.input = input.isEmpty() ? ItemStack.EMPTY : input;
      this.output = output.isEmpty() ? ItemStack.EMPTY : output;
      this.hash = Arrays.hashCode(new int[]{ItemStack.hashItemAndComponents(this.input), ItemStack.hashItemAndComponents(this.output)});
   }

   public GuideSmelting createNew(GuiGuide gui) {
      return new GuideSmelting(gui, this.input, this.output);
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

      GuideSmeltingFactory other = (GuideSmeltingFactory)obj;
      return this.hash != other.hash
         ? false
         : ItemStack.isSameItemSameComponents(this.input, other.input) && ItemStack.isSameItemSameComponents(this.output, other.output);
   }
}
