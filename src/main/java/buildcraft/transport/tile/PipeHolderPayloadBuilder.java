/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.tile;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.lib.net.BcEnvelopeCodec;
import buildcraft.transport.net.PipeReceiverPayloadCodec;
import java.util.Set;
import javax.annotation.Nullable;

public final class PipeHolderPayloadBuilder {
   private PipeHolderPayloadBuilder() {
   }

   @Nullable
   public static byte[] buildSingle(TilePipeHolder holder, IPipeHolder.PipeMessageReceiver part) {
      return BcEnvelopeCodec.encode(buffer -> PipeReceiverPayloadCodec.write(part, holder, buffer));
   }

   @Nullable
   public static byte[] buildMulti(TilePipeHolder holder, Set<IPipeHolder.PipeMessageReceiver> parts) {
      return BcEnvelopeCodec.encode(buffer -> PipeReceiverPayloadCodec.writeMulti(parts, holder, buffer));
   }
}
