package buildcraft.builders.container;

import buildcraft.api.core.BCLog;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.nbt.NbtSquisher;
import buildcraft.lib.net.BCPacketLimits;
import buildcraft.lib.net.PacketBufferBC;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerElectronicLibrary extends ContainerBCTile<TileElectronicLibrary> {
   public static final int NET_SELECTED = 1;
   public static final int NET_DOWNLOAD = 2;
   public static final int NET_UPLOAD_REQUEST = 3;
   public static final int NET_UPLOAD_DATA = 4;
   private static final int DATA_PROGRESS_DOWN = 0;
   private static final int DATA_PROGRESS_UP = 1;
   private static final int DATA_COUNT = 2;
   private final ContainerData data;
   private final List<byte[]> uploadChunks = new ArrayList<>();
   private final List<byte[]> downloadChunks = new ArrayList<>();

   public ContainerElectronicLibrary(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerElectronicLibrary(int containerId, Inventory playerInv, final TileElectronicLibrary tile) {
      super(BCBuildersMenuTypes.LIBRARY, containerId, playerInv.player, tile);
      if (tile.getLevel() != null && !tile.getLevel().isClientSide()) {
         this.data = new ContainerData() {
            public int get(int index) {
               return switch (index) {
                  case 0 -> tile.progressDown;
                  case 1 -> tile.progressUp;
                  default -> 0;
               };
            }

            public void set(int index, int value) {
            }

            public int getCount() {
               return 2;
            }
         };
      } else {
         this.data = new SimpleContainerData(2);
      }

      this.addDataSlots(this.data);
      this.addSlot(new SlotOutput(tile.invDownOut, 0, 175, 57));
      this.addSlot(new SlotBase(tile.invDownIn, 0, 219, 57));
      this.addSlot(new SlotBase(tile.invUpIn, 0, 175, 79));
      this.addSlot(new SlotOutput(tile.invUpOut, 0, 219, 79));
      this.addFullPlayerInventory(8, 138, playerInv);
   }

   private static TileElectronicLibrary getTile(Inventory playerInv, BlockPos pos) {
      Level level = playerInv.player.level();
      return level != null && level.getBlockEntity(pos) instanceof TileElectronicLibrary lib ? lib : null;
   }

   public int getSyncedProgressDown() {
      return this.data.get(0);
   }

   public int getSyncedProgressUp() {
      return this.data.get(1);
   }

   public void sendSelectedToServer(Snapshot.Key selected) {
      this.sendMessage(1, buf -> {
         buf.writeByte(selected != null ? 1 : 0);
         if (selected != null) {
            selected.writeToByteBuf(buf);
         }
      });
   }

   public void sendDownloadData(byte[] data) {
      if (data.length == 0) {
         this.sendMessage(2, buf -> {
            buf.writeByte(1);
            buf.writeByteArray(new byte[0]);
         });
      } else {
         int offset = 0;

         while (offset < data.length) {
            int end = Math.min(offset + 32768, data.length);
            byte[] chunk = Arrays.copyOfRange(data, offset, end);
            boolean last = end >= data.length;
            this.sendMessage(2, buf -> {
               buf.writeByte(last ? 1 : 0);
               buf.writeByteArray(chunk);
            });
            offset = end;
         }
      }
   }

   @Override
   public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
      super.readMessage(id, buffer, isClient, ctx);
      if (id == 1 && !isClient) {
         this.tile.selected = buffer.readByte() != 0 ? new Snapshot.Key(buffer) : null;
      } else if (id == 2 && isClient) {
         boolean last = buffer.readByte() != 0;
         byte[] chunk = readBoundedChunk(buffer);
         this.downloadChunks.add(chunk);
         if (last) {
            this.assembleDownload();
         }
      } else if (id == 3 && isClient) {
         Snapshot.Key key = new Snapshot.Key(buffer);
         this.sendSnapshotToServer(key);
      } else {
         if (id == 4 && !isClient) {
            boolean last = buffer.readByte() != 0;
            byte[] chunk = readBoundedChunk(buffer);
            this.uploadChunks.add(chunk);
            if (last) {
               this.assembleUpload();
            }
         }
      }
   }

   private static byte[] readBoundedChunk(PacketBufferBC buffer) {
      byte[] chunk = buffer.readByteArray();
      BCPacketLimits.validateChunkSize(chunk.length);
      return chunk;
   }

   private void sendSnapshotToServer(Snapshot.Key key) {
      Snapshot snapshot = GlobalSavedDataSnapshots.get(GlobalSavedDataSnapshots.Side.CLIENT).getSnapshot(key);
      if (snapshot == null) {
         BCLog.logger.warn("[library] Upload requested for unknown snapshot key: " + key);
      } else {
         byte[] data;
         try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtSquisher.squishVanilla(Snapshot.writeToNBT(snapshot), baos);
            data = baos.toByteArray();
         } catch (IOException e) {
            BCLog.logger.warn("[library] Failed to serialize snapshot for upload", e);
            return;
         }

         this.sendChunkedData(4, data);
      }
   }

   private void sendChunkedData(int messageId, byte[] data) {
      if (data.length == 0) {
         this.sendMessage(messageId, buf -> {
            buf.writeByte(1);
            buf.writeByteArray(new byte[0]);
         });
      } else {
         int offset = 0;

         while (offset < data.length) {
            int end = Math.min(offset + 32768, data.length);
            byte[] chunk = Arrays.copyOfRange(data, offset, end);
            boolean last = end >= data.length;
            this.sendMessage(messageId, buf -> {
               buf.writeByte(last ? 1 : 0);
               buf.writeByteArray(chunk);
            });
            offset = end;
         }
      }
   }

   private void assembleDownload() {
      byte[] assembled = this.assembleChunks(this.downloadChunks);
      this.downloadChunks.clear();
      if (assembled != null) {
         try {
            Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(new ByteArrayInputStream(assembled)));
            snapshot.computeKey();
            GlobalSavedDataSnapshots.get(GlobalSavedDataSnapshots.Side.CLIENT).addSnapshot(snapshot);
         } catch (IOException e) {
            BCLog.logger.warn("[library] Failed to deserialize downloaded snapshot", e);
         }
      }
   }

   private void assembleUpload() {
      byte[] assembled = this.assembleChunks(this.uploadChunks);
      this.uploadChunks.clear();
      if (assembled != null) {
         try {
            Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(new ByteArrayInputStream(assembled)));
            snapshot.computeKey();
            this.tile.onUploadReceived(snapshot);
         } catch (IOException e) {
            BCLog.logger.warn("[library] Failed to deserialize uploaded snapshot", e);
         }
      }
   }

   private byte[] assembleChunks(List<byte[]> chunks) {
      int total = chunks.stream().mapToInt(c -> c.length).sum();

      try {
         BCPacketLimits.validateAssembledSize(total);
      } catch (IllegalArgumentException e) {
         BCLog.logger.warn("[library] Rejected oversized assembled snapshot: {} bytes", total);
         return null;
      }

      byte[] assembled = new byte[total];
      int pos = 0;

      for (byte[] chunk : chunks) {
         System.arraycopy(chunk, 0, assembled, pos, chunk.length);
         pos += chunk.length;
      }

      return assembled;
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }
}
