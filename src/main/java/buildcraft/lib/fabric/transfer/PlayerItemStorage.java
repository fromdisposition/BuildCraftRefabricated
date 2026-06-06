package buildcraft.lib.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class PlayerItemStorage {
   private PlayerItemStorage() {
   }

   public static Storage<ItemVariant> of(Player player) {
      return PlayerInventoryStorage.of(player);
   }

   public static @Nullable Storage<ItemVariant> ofNullable(@Nullable Player player) {
      return player == null ? null : of(player);
   }
}
