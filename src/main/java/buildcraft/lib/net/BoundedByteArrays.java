/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import java.io.IOException;
import net.minecraft.network.RegistryFriendlyByteBuf;

public final class BoundedByteArrays {
   private BoundedByteArrays() {
   }

   public static void write(RegistryFriendlyByteBuf buf, byte[] data) {
      if (data == null) {
         buf.writeVarInt(0);
      } else {
         if (data.length > 65536) {
            throw new IllegalArgumentException("Payload too large: " + data.length);
         }

         buf.writeVarInt(data.length);
         buf.writeBytes(data);
      }
   }

   public static byte[] read(RegistryFriendlyByteBuf buf) throws IOException {
      int length = buf.readVarInt();
      if (length >= 0 && length <= 65536) {
         byte[] data = new byte[length];
         buf.readBytes(data);
         return data;
      } else {
         throw new IOException("Invalid payload length: " + length);
      }
   }
}
