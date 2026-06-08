package buildcraft.robotics.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlan;
import buildcraft.robotics.zone.ZonePlannerMapColours;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class ContainerZonePlanner extends ContainerBCTile<TileZonePlanner> {
   public static final int NET_PAINT = 200;
   public static final int NET_REQUEST_LAYERS = 201;
   public static final int NET_LAYERS = 202;
   public static final int NET_MAP_REQUEST = 203;
   public static final int NET_MAP_DATA = 204;
   public static final int NET_PAINT_RECT = 205;
   private static final int MAP_DATA_BATCH = 12;
   private static final int PLAYER_SLOTS_END = 36;
   private static final int MACHINE_SLOTS_END = 58;
   public final ZonePlannerMapColours mapColours = new ZonePlannerMapColours();

   public ContainerZonePlanner(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerZonePlanner(int containerId, Inventory playerInv, TileZonePlanner tile) {
      super(BCRoboticsMenuTypes.ZONE_PLANNER, containerId, playerInv.player, tile);
      this.addFullPlayerInventory(88, 146);

      for (int x = 0; x < 4; x++) {
         for (int y = 0; y < 4; y++) {
            this.addSlot(new SlotBase(tile.invPaintbrushes, x * 4 + y, 8 + x * 18, 146 + y * 18));
         }
      }

      this.addSlot(new SlotBase(tile.invInputPaintbrush, 0, 8, 125));
      this.addSlot(new SlotBase(tile.invInputMapLocation, 0, 26, 125));
      this.addSlot(new SlotOutput(tile.invInputResult, 0, 74, 125));
      this.addSlot(new SlotBase(tile.invOutputPaintbrush, 0, 233, 9));
      this.addSlot(new SlotBase(tile.invOutputMapLocation, 0, 233, 27));
      this.addSlot(new SlotOutput(tile.invOutputResult, 0, 233, 75));

      this.addDataSlot(new DataSlot() {
         @Override
         public int get() {
            return tile != null ? tile.getProgressInput() : 0;
         }

         @Override
         public void set(int value) {
            if (tile != null) {
               tile.setProgressInput(value);
            }
         }
      });
      this.addDataSlot(new DataSlot() {
         @Override
         public int get() {
            return tile != null ? tile.getProgressOutput() : 0;
         }

         @Override
         public void set(int value) {
            if (tile != null) {
               tile.setProgressOutput(value);
            }
         }
      });
   }

   public void sendPaint(int layer, int x, int z, boolean set) {
      this.sendMessage(200, buf -> {
         buf.writeByte(layer);
         buf.writeVarInt(x);
         buf.writeVarInt(z);
         buf.writeBoolean(set);
      });
   }

   public void requestLayers() {
      this.sendMessage(201, buf -> {});
   }

   public void sendPaintRect(int layer, int x0, int z0, int x1, int z1, boolean set) {
      this.sendMessage(205, buf -> {
         buf.writeByte(layer);
         buf.writeVarInt(x0);
         buf.writeVarInt(z0);
         buf.writeVarInt(x1);
         buf.writeVarInt(z1);
         buf.writeBoolean(set);
      });
   }

   public void requestChunks(List<Long> keys) {
      if (keys != null && !keys.isEmpty()) {
         this.sendMessage(203, buf -> {
            buf.writeVarInt(keys.size());
            for (long key : keys) {
               buf.writeLong(key);
            }
         });
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 200 && !isClient) {
         int layer = buffer.readByte() & 255;
         int x = buffer.readVarInt();
         int z = buffer.readVarInt();
         boolean set = buffer.readBoolean();
         if (this.tile != null) {
            this.tile.applyPaint(layer, x, z, set);
         }
      } else if (id == 205 && !isClient) {
         int layer = buffer.readByte() & 255;
         int x0 = buffer.readVarInt();
         int z0 = buffer.readVarInt();
         int x1 = buffer.readVarInt();
         int z1 = buffer.readVarInt();
         boolean set = buffer.readBoolean();
         if (this.tile != null) {
            int minX = Math.min(x0, x1);
            int maxX = Math.max(x0, x1);
            int minZ = Math.min(z0, z1);
            int maxZ = Math.max(z0, z1);

            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  this.tile.applyPaint(layer, x, z, set);
               }
            }
         }
      } else if (id == 203 && !isClient) {
         this.handleMapRequest(buffer);
      } else if (id == 204 && isClient) {
         int count = buffer.readVarInt();

         for (int i = 0; i < count; i++) {
            long key = buffer.readLong();
            int[] col = new int[256];
            int[] height = new int[256];

            for (int k = 0; k < 256; k++) {
               col[k] = buffer.readInt();
            }

            for (int k = 0; k < 256; k++) {
               height[k] = buffer.readInt();
            }

            this.mapColours.put(key, col, height);
         }
      } else if (id == 201 && !isClient) {
         if (this.tile != null) {
            this.sendMessage(202, buf -> {
               for (ZonePlan planx : this.tile.layers) {
                  (planx == null ? new ZonePlan() : planx).writeToByteBuf(buf);
               }
            });
         }
      } else if (id == 202 && isClient) {
         if (this.tile != null) {
            for (int i = 0; i < this.tile.layers.length; i++) {
               ZonePlan plan = new ZonePlan();
               plan.readFromByteBuf(buffer);
               this.tile.layers[i] = plan;
            }
         }
      } else {
         super.readMessage(id, buffer, isClient, ctx);
      }
   }

   private void handleMapRequest(FriendlyByteBuf buffer) {
      int count = buffer.readVarInt();
      long[] keys = new long[count];

      for (int i = 0; i < count; i++) {
         keys[i] = buffer.readLong();
      }

      Level level = this.tile != null ? this.tile.getLevel() : null;
      if (level != null) {
         List<Long> okKeys = new ArrayList<>();
         List<int[]> cols = new ArrayList<>();
         List<int[]> heights = new ArrayList<>();

         for (long key : keys) {
            int cx = ChunkPos.getX(key);
            int cz = ChunkPos.getZ(key);
            if (level.getChunkSource().hasChunk(cx, cz)) {
               int[] col = new int[256];
               int[] height = new int[256];
               computeChunk(level, cx, cz, col, height);
               okKeys.add(key);
               cols.add(col);
               heights.add(height);
            }
         }

         int total = okKeys.size();

         for (int start = 0; start < total; start += MAP_DATA_BATCH) {
            int end = Math.min(total, start + MAP_DATA_BATCH);
            int fromIdx = start;
            int toIdx = end;
            this.sendMessage(204, buf -> {
               buf.writeVarInt(toIdx - fromIdx);

               for (int n = fromIdx; n < toIdx; n++) {
                  buf.writeLong(okKeys.get(n));
                  int[] c = cols.get(n);
                  int[] h = heights.get(n);

                  for (int k = 0; k < 256; k++) {
                     buf.writeInt(c[k]);
                  }

                  for (int k = 0; k < 256; k++) {
                     buf.writeInt(h[k]);
                  }
               }
            });
         }
      }
   }

   private static void computeChunk(Level level, int cx, int cz, int[] colOut, int[] heightOut) {
      BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

      for (int lz = 0; lz < 16; lz++) {
         for (int lx = 0; lx < 16; lx++) {
            int wx = (cx << 4) + lx;
            int wz = (cz << 4) + lz;
            int idx = lz * 16 + lx;
            int topY = level.getHeight(Types.WORLD_SURFACE, wx, wz);
            int y = Math.max(level.getMinY(), topY - 1);
            mpos.set(wx, y, wz);
            BlockState state = level.getBlockState(mpos);

            int rgb;
            try {
               rgb = state.getMapColor(level, mpos).col;
            } catch (Throwable t) {
               rgb = 0;
            }

            heightOut[idx] = topY;
            colOut[idx] = rgb == 0 ? 0 : 0xFF000000 | shadeByHeight(rgb & 16777215, level, topY);
         }
      }
   }

   private static int shadeByHeight(int rgb, Level level, int topY) {
      int range = level.getHeight();
      double norm = range <= 0 ? 0.5 : (topY - level.getMinY()) / (double)range;
      norm = Math.max(0.0, Math.min(1.0, norm));
      double shade = 0.6 + 0.4 * norm;
      int r = (int)Math.min(255.0, ((rgb >> 16) & 0xFF) * shade);
      int g = (int)Math.min(255.0, ((rgb >> 8) & 0xFF) * shade);
      int b = (int)Math.min(255.0, (rgb & 0xFF) * shade);
      return r << 16 | g << 8 | b;
   }

   private static TileZonePlanner getTile(Inventory playerInv, BlockPos pos) {
      return playerInv.player.level() != null && playerInv.player.level().getBlockEntity(pos) instanceof TileZonePlanner planner ? planner : null;
   }

   @Override
   public ItemStack quickMoveStack(Player player, int slotIndex) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(slotIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         result = stack.copy();
         if (slotIndex < 36) {
            if (!this.moveItemStackTo(stack, 36, 58, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 0, 36, true)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
      }

      return result;
   }
}
