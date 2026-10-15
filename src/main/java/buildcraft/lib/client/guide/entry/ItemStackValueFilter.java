/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.entry;

import buildcraft.lib.misc.ItemStackKey;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public class ItemStackValueFilter {
   public final ItemStackKey stack;
   public final boolean matchNbt;
   public final boolean matchMeta;

   public ItemStackValueFilter(ItemStack stack) {
      this(new ItemStackKey(stack), false, false);
   }

   public ItemStackValueFilter(ItemStackKey stack, boolean matchMeta, boolean matchNbt) {
      this.stack = stack;
      this.matchNbt = matchNbt;
      this.matchMeta = matchMeta;
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

      ItemStackValueFilter other = (ItemStackValueFilter)obj;
      return this.stack.equals(other.stack) && this.matchMeta == other.matchMeta && this.matchNbt == other.matchNbt;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.stack, this.matchNbt, this.matchMeta);
   }

   @Override
   public String toString() {
      String matchString;
      if (this.matchMeta) {
         matchString = this.matchNbt ? "Matching meta+NBT of " : "Matching meta of ";
      } else if (this.matchNbt) {
         matchString = "Matching NBT of ";
      } else {
         matchString = "";
      }

      return matchString + this.stack.baseStack;
   }
}
