package buildcraft.lib.integration.jei;

import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;

public final class BucketJeiTransfer {
   private BucketJeiTransfer() {
   }

   public static void apply(FriendlyByteBuf buffer, Inventory playerInv, ItemHandlerSimple machineSlots) {
      int count = buffer.readVarInt();

      for (int i = 0; i < count; i++) {
         int slot = buffer.readVarInt();
         Item bucket = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(buffer.readUtf()));
         JeiTransferUtil.moveBucketToSlot(playerInv, bucket, machineSlots, slot);
      }
   }

   public static void sendSingle(BcMenu menu, int slot, Item bucket) {
      menu.sendMessage(BcMenu.NET_JEI_TRANSFER_BUCKETS, buf -> writeSingle(buf, slot, BuiltInRegistries.ITEM.getKey(bucket).toString()));
   }

   public static void sendPair(BcMenu menu, int slot0, Item bucket0, int slot1, Item bucket1) {
      menu.sendMessage(
         BcMenu.NET_JEI_TRANSFER_BUCKETS,
         buf -> writePair(buf, slot0, BuiltInRegistries.ITEM.getKey(bucket0).toString(), slot1, BuiltInRegistries.ITEM.getKey(bucket1).toString())
      );
   }

   public static void writeSingle(FriendlyByteBuf writer, int slot, String bucketId) {
      writer.writeVarInt(1);
      writer.writeVarInt(slot);
      writer.writeUtf(bucketId);
   }

   public static void writePair(FriendlyByteBuf writer, int slot0, String bucketId0, int slot1, String bucketId1) {
      writer.writeVarInt(2);
      writer.writeVarInt(slot0);
      writer.writeUtf(bucketId0);
      writer.writeVarInt(slot1);
      writer.writeUtf(bucketId1);
   }
}
