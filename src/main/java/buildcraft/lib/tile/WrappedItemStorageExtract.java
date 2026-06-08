/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile;

import buildcraft.lib.fabric.transfer.FabricItemStorageProvider;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class WrappedItemStorageExtract implements FabricItemStorageProvider {
   private final FabricItemStorageProvider delegate;
   private final Storage<ItemVariant> storage;

   public WrappedItemStorageExtract(FabricItemStorageProvider delegate) {
      this.delegate = delegate;
      final Storage<ItemVariant> inner = delegate.fabricItemStorage();
      this.storage = inner == null ? null : new ExtractionOnlyStorage<ItemVariant>() {
         public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return inner.extract(resource, maxAmount, transaction);
         }

         public Iterator<StorageView<ItemVariant>> iterator() {
            return inner.iterator();
         }
      };
   }

   public FabricItemStorageProvider delegate() {
      return this.delegate;
   }

   @Override
   public Storage<ItemVariant> fabricItemStorage() {
      return this.storage;
   }
}
