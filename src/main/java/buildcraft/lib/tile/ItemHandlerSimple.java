/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile;

import buildcraft.lib.misc.INBTSerializable;
import org.jspecify.annotations.Nullable;
import net.minecraft.nbt.CompoundTag;

public class ItemHandlerSimple extends BcItemInventory implements INBTSerializable<CompoundTag> {
   public ItemHandlerSimple(int size) {
      super(size);
   }

   public ItemHandlerSimple(int size, int maxStackSize) {
      super(size, maxStackSize);
   }

   public ItemHandlerSimple(int size, @Nullable StackChangeCallback callback) {
      super(size, callback);
   }

   public ItemHandlerSimple(int size, StackInsertionChecker checker, StackInsertionFunction insertionFunction, @Nullable StackChangeCallback callback) {
      super(size, checker, insertionFunction, callback);
   }

}
