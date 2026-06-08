/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.sync.ClientKeyedCache;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public enum ClientArchitectPreviews {
   INSTANCE;

   private final ClientKeyedCache<BlockPos, Blueprint> cache = new ClientKeyedCache<>(
      pos -> BcPacketDistributor.sendToServer(BuildersClientRequestPayload.architectPreview(pos.immutable()))
   );

   @Nullable
   public Blueprint get(BlockPos pos) {
      return this.cache.get(pos.immutable());
   }

   public void requestRefresh(BlockPos pos) {
      this.cache.request(pos.immutable());
   }

   public void onReceived(BlockPos pos, @Nullable Blueprint blueprint) {
      BlockPos key = pos.immutable();
      if (blueprint == null) {
         this.cache.remove(key);
      } else {
         this.cache.putIfAbsentOrEquals(key, blueprint, ClientArchitectPreviews::sameContent);
      }
   }

   public void invalidate(BlockPos pos) {
      this.cache.remove(pos.immutable());
   }

   private static boolean sameContent(Blueprint a, Blueprint b) {
      if (a.key != null && b.key != null) {
         byte[] ah = a.key.hash;
         byte[] bh = b.key.hash;
         return ah != null && bh != null && ah.length != 0 && bh.length != 0 ? Arrays.equals(ah, bh) : false;
      } else {
         return false;
      }
   }
}
