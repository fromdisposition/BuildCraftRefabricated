/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.lists;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public abstract class ListMatchHandler {
   public abstract boolean matches(ListMatchHandler.Type var1, @Nonnull ItemStack var2, @Nonnull ItemStack var3, boolean var4);

   public abstract boolean isValidSource(ListMatchHandler.Type var1, @Nonnull ItemStack var2);

   @Nullable
   public NonNullList<ItemStack> getClientExamples(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      return null;
   }

   @Nonnull
   public List<String> describeMatch(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      return List.of();
   }

   public enum Type {
      TYPE,
      MATERIAL,
      CLASS;
   }
}
