/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

public class NbtSquisher {
   public static CompoundTag expand(InputStream stream) throws IOException {
      return NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
   }

   public static void squishVanilla(CompoundTag nbt, OutputStream stream) throws IOException {
      NbtIo.writeCompressed(nbt, stream);
   }
}
