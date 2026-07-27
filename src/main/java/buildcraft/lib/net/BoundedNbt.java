/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import net.minecraft.nbt.NbtAccounter;

public final class BoundedNbt {
   private BoundedNbt() {
   }

   public static NbtAccounter networkQuota() {
      return NbtAccounter.create(2097152L);
   }
}
