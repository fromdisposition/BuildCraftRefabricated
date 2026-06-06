package buildcraft.lib.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public interface FabricItemStorageProvider {
   Storage<ItemVariant> fabricItemStorage();
}
