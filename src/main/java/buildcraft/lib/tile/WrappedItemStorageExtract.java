/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile;

import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class WrappedItemStorageExtract implements Storage<ItemVariant> {
   private final Storage<ItemVariant> inner;

   public WrappedItemStorageExtract(Storage<ItemVariant> inner) {
      this.inner = inner;
   }

   @Override
   public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      return 0L;
   }

   @Override
   public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      return inner == null ? 0L : inner.extract(resource, maxAmount, transaction);
   }

   @Override
   public Iterator<StorageView<ItemVariant>> iterator() {
      return inner == null ? Storage.<ItemVariant>empty().iterator() : inner.iterator();
   }
}
