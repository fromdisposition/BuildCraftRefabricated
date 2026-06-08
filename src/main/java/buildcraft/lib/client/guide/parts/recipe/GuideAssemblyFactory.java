/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ArrayUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;
import java.util.Arrays;
import net.minecraft.world.item.ItemStack;

public class GuideAssemblyFactory implements GuidePartFactory {
   private final ChangingItemStack[] input;
   private final ChangingItemStack output;
   private final ChangingObject<Long> mjCost;
   private final int hash;

   public GuideAssemblyFactory(ChangingItemStack[] input, ChangingItemStack output, ChangingObject<Long> mjCost) {
      this.input = input;
      this.output = output;
      this.mjCost = mjCost;
      this.hash = this.computeHash();
   }

   public GuideAssemblyFactory(ItemStack[] input, ItemStack output, long mjCost) {
      this.input = ArrayUtil.map(input, ChangingItemStack::new, ChangingItemStack[]::new);
      this.output = new ChangingItemStack(output);
      this.mjCost = new ChangingObject<>(new Long[]{mjCost});
      this.hash = this.computeHash();
   }

   private int computeHash() {
      return Arrays.deepHashCode(new Object[]{this.input, this.output, this.mjCost});
   }

   public GuideAssembly createNew(GuiGuide gui) {
      return new GuideAssembly(gui, this.input, this.output, this.mjCost);
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (obj.getClass() != this.getClass()) {
         return false;
      } else {
         GuideAssemblyFactory other = (GuideAssemblyFactory)obj;
         if (this.hash != other.hash) {
            return false;
         } else {
            return this.input.length != other.input.length ? false : Arrays.equals(this.input, other.input) && this.output.equals(other.output);
         }
      }
   }
}
