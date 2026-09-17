/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.sync.ClientKeyedCache;

public enum ClientSnapshots {
   INSTANCE;

   private final ClientKeyedCache<Snapshot.Key, Snapshot> cache = new ClientKeyedCache<>(ClientSnapshots::requestSnapshot);

   private static void requestSnapshot(Snapshot.Key key) {
      BcPacketDistributor.sendToServer(BuildersClientRequestPayload.snapshot(key));
   }

   public Snapshot getSnapshot(Snapshot.Key key) {
      return this.cache.get(key);
   }

   public void onSnapshotReceived(Snapshot snapshot) {
      this.cache.put(snapshot.key, snapshot);
   }
}
