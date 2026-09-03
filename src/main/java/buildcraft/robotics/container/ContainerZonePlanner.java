/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.core.BCLog;
import buildcraft.core.BCCore;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlan;
import buildcraft.robotics.zone.ZonePlannerMapColours;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.MapColor;

public class ContainerZonePlanner extends ContainerBCTile<TileZonePlanner> {
   public static final int NET_REQUEST_LAYERS = 201;
   public static final int NET_LAYERS = 202;
   public static final int NET_MAP_REQUEST = 203;
   public static final int NET_MAP_DATA = 204;
   public static final int NET_PAINT_RECT = 205;
   private static final int MAP_CHUNKS_PER_TICK = 32;
   private static final int MAP_DEQUEUES_PER_TICK = 512;
   private static final int MAP_QUEUE_LIMIT = 4096;
   private static final int PLAYER_SLOTS_END = 36;
   private static final int MACHINE_SLOTS_END = 58;
   public final ZonePlannerMapColours mapColours = new ZonePlannerMapColours();
   
   public int clientLayerVersion;
   private int lastLayersVersion = -1;
   private final LongArrayFIFOQueue mapQueue = new LongArrayFIFOQueue();
   private final LongOpenHashSet mapQueued = new LongOpenHashSet();
   private static final Predicate<ItemStack> IS_BRUSH = stack -> stack.getItem() instanceof ItemPaintbrush_BC8;
   private static final Predicate<ItemStack> IS_MAP = stack -> stack.getItem() instanceof ItemMapLocation;

   public ContainerZonePlanner(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerZonePlanner(int containerId, Inventory playerInv, TileZonePlanner tile) {
      super(BCRoboticsMenuTypes.ZONE_PLANNER, containerId, playerInv.player, tile);
      this.addFullPlayerInventory(88, 162);

      for (int x = 0; x < 4; x++) {
         for (int y = 0; y < 4; y++) {
            this.addSlot(filtered(tile.invPaintbrushes, x * 4 + y, 8 + x * 18, 162 + y * 18, IS_BRUSH));
         }
      }

      this.addSlot(filtered(tile.invInputPaintbrush, 0, 8, 130, IS_BRUSH));
      this.addSlot(filtered(tile.invInputMapLocation, 0, 26, 130, IS_MAP));
      this.addSlot(new SlotOutput(tile.invInputResult, 0, 74, 130));
      this.addSlot(filtered(tile.invOutputPaintbrush, 0, 233, 18, IS_BRUSH));
      this.addSlot(filtered(tile.invOutputMapLocation, 0, 233, 36, IS_MAP));
      this.addSlot(new SlotOutput(tile.invOutputResult, 0, 233, 84));

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

   private static SlotBase filtered(ItemHandlerSimple inv, int idx, int x, int y, Predicate<ItemStack> accepts) {
      return new SlotBase(inv, idx, x, y) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return !stack.isEmpty() && accepts.test(stack) && super.mayPlace(stack);
         }
      };
   }

   public void requestLayers() {
      this.sendMessage(NET_REQUEST_LAYERS, buf -> {});
   }

   public void sendPaintRect(int layer, int x0, int z0, int x1, int z1, boolean set) {
      this.sendMessage(NET_PAINT_RECT, buf -> {
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
         this.sendMessage(NET_MAP_REQUEST, buf -> {
            buf.writeVarInt(keys.size());
            for (long key : keys) {
               buf.writeLong(key);
            }
         });
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == NET_PAINT_RECT && !isClient) {
         int layer = buffer.readByte() & 255;
         int x0 = buffer.readVarInt();
         int z0 = buffer.readVarInt();
         int x1 = buffer.readVarInt();
         int z1 = buffer.readVarInt();
         boolean set = buffer.readBoolean();
         if (!this.canPaint(ctx.player(), layer)) {
            this.pushLayersToClient();
         } else if (this.tile != null) {
            int minX = Math.min(x0, x1);
            int maxX = Math.max(x0, x1);
            int minZ = Math.min(z0, z1);
            int maxZ = Math.max(z0, z1);
            // The on-screen map can only select a few hundred blocks per axis, so anything larger is a forged
            // packet — without this cap a single message with extreme coordinates would spin the paint loop
            // over billions of cells and freeze the server. (Long math: the span itself can overflow int.)
            if ((long)maxX - minX > 1024L || (long)maxZ - minZ > 1024L) {
               BCLog.logger.warn("[robotics.zone] Rejected oversized paint rect from {}", ctx.player().getName().getString());
               return;
            }

            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  this.tile.applyPaint(layer, x, z, set);
               }
            }
         }
      } else if (id == NET_MAP_REQUEST && !isClient) {
         this.handleMapRequest(buffer);
      } else if (id == NET_MAP_DATA && isClient) {
         Level level = this.tile != null ? this.tile.getLevel() : null;
         int count = Math.min(buffer.readVarInt(), buffer.readableBytes() / (Long.BYTES + 256 * 3));

         for (int i = 0; i < count; i++) {
            long key = buffer.readLong();
            int[] col = new int[256];
            int[] height = new int[256];

            for (int k = 0; k < 256; k++) {
               int colourId = buffer.readUnsignedByte();
               int y = buffer.readShort();
               height[k] = y;
               col[k] = colourId == 0 ? 0 : 0xFF000000 | shadeByHeight(MapColor.byId(colourId).col & 0xFFFFFF, level, y);
            }

            this.mapColours.put(key, col, height);
         }

         int denied = Math.min(buffer.readVarInt(), buffer.readableBytes() / Long.BYTES);

         for (int i = 0; i < denied; i++) {
            this.mapColours.markDenied(buffer.readLong());
         }
      } else if (id == NET_REQUEST_LAYERS && !isClient) {
         if (this.tile != null) {
            this.sendMessage(NET_LAYERS, buf -> {
               for (ZonePlan planx : this.tile.layers) {
                  (planx == null ? new ZonePlan() : planx).writeToByteBuf(buf);
               }
            });
         }
      } else if (id == NET_LAYERS && isClient) {
         if (this.tile != null) {
            for (int i = 0; i < this.tile.layers.length; i++) {
               ZonePlan plan = new ZonePlan();
               plan.readFromByteBuf(buffer);
               this.tile.layers[i] = plan;
            }

            this.clientLayerVersion++;
         }
      } else {
         super.readMessage(id, buffer, isClient, ctx);
      }
   }

   
   @Override
   public void broadcastChanges() {
      super.broadcastChanges();
      this.serveMapQueue();
      if (this.tile != null && this.tile.getLevel() != null && !this.tile.getLevel().isClientSide() && this.tile.layersVersion != this.lastLayersVersion) {
         this.lastLayersVersion = this.tile.layersVersion;
         this.sendMessage(NET_LAYERS, buf -> {
            for (ZonePlan plan : this.tile.layers) {
               (plan == null ? new ZonePlan() : plan).writeToByteBuf(buf);
            }
         });
      }
   }

   private boolean canPaint(Player player, int layer) {
      if (this.tile == null || !this.tile.canInteractWith(player)) {
         return false;
      }

      return layer >= 0 && layer < this.tile.layers.length && this.hasBrushForLayer(player, layer);
   }

   private boolean hasBrushForLayer(Player player, int layer) {
      if (brushMatchesLayer(this.getCarried(), layer)) {
         return true;
      }

      if (this.tile != null) {
         for (int i = 0; i < this.tile.invPaintbrushes.getSlots(); i++) {
            if (brushMatchesLayer(this.tile.invPaintbrushes.getStackInSlot(i), layer)) {
               return true;
            }
         }

         if (brushMatchesLayer(this.tile.invInputPaintbrush.getStackInSlot(0), layer)) {
            return true;
         }

         if (brushMatchesLayer(this.tile.invOutputPaintbrush.getStackInSlot(0), layer)) {
            return true;
         }
      }

      return brushMatchesLayer(player.getMainHandItem(), layer) || brushMatchesLayer(player.getOffhandItem(), layer);
   }

   private static boolean brushMatchesLayer(ItemStack stack, int layer) {
      if (stack.isEmpty() || !(stack.getItem() instanceof ItemPaintbrush_BC8)) {
         return false;
      }

      DyeColor colour = stack.get(BCCore.BRUSH_COLOR);
      return colour != null && colour.getId() == layer;
   }

   private void pushLayersToClient() {
      if (this.tile == null) {
         return;
      }

      this.lastLayersVersion = this.tile.layersVersion;
      this.sendMessage(NET_LAYERS, buf -> {
         for (ZonePlan plan : this.tile.layers) {
            (plan == null ? new ZonePlan() : plan).writeToByteBuf(buf);
         }
      });
   }

   private void handleMapRequest(FriendlyByteBuf buffer) {
      int count = buffer.readVarInt();
      int cap = Math.min(Math.max(count, 0), buffer.readableBytes() / Long.BYTES);

      for (int i = 0; i < cap; i++) {
         long key = buffer.readLong();
         if (this.mapQueued.size() < MAP_QUEUE_LIMIT && this.mapQueued.add(key)) {
            this.mapQueue.enqueue(key);
         }
      }
   }

   private void serveMapQueue() {
      Level level = this.tile != null ? this.tile.getLevel() : null;
      if (level == null || level.isClientSide() || level.getServer() == null || this.mapQueue.isEmpty()) {
         return;
      }

      int viewDist = level.getServer().getPlayerList().getViewDistance();
      int centerX = this.tile.getBlockPos().getX() >> 4;
      int centerZ = this.tile.getBlockPos().getZ() >> 4;
      List<Long> okKeys = new ArrayList<>();
      List<int[]> cols = new ArrayList<>();
      List<int[]> heights = new ArrayList<>();
      List<Long> denied = new ArrayList<>();
      int dequeued = 0;

      while (!this.mapQueue.isEmpty() && okKeys.size() < MAP_CHUNKS_PER_TICK && dequeued++ < MAP_DEQUEUES_PER_TICK) {
         long key = this.mapQueue.dequeueLong();
         this.mapQueued.remove(key);
         int cx = ChunkPos.getX(key);
         int cz = ChunkPos.getZ(key);
         if (Math.max(Math.abs(cx - centerX), Math.abs(cz - centerZ)) > viewDist || !level.getChunkSource().hasChunk(cx, cz)) {
            denied.add(key);
            continue;
         }

         int[] col = new int[256];
         int[] height = new int[256];
         computeChunk(level, cx, cz, col, height);
         okKeys.add(key);
         cols.add(col);
         heights.add(height);
      }

      this.sendMessage(NET_MAP_DATA, buf -> {
         buf.writeVarInt(okKeys.size());

         for (int n = 0; n < okKeys.size(); n++) {
            buf.writeLong(okKeys.get(n));
            int[] c = cols.get(n);
            int[] h = heights.get(n);

            for (int k = 0; k < 256; k++) {
               buf.writeByte(c[k]);
               buf.writeShort(h[k]);
            }
         }

         buf.writeVarInt(denied.size());

         for (long key : denied) {
            buf.writeLong(key);
         }
      });
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
            int colourId;
            try {
               colourId = state.getMapColor(level, mpos).id;
            } catch (Throwable t) {
               colourId = 0;
            }

            heightOut[idx] = y;
            colOut[idx] = colourId;
         }
      }
   }

   private static int shadeByHeight(int rgb, Level level, int surfaceY) {
      int minY = level == null ? -64 : level.getMinY();
      int range = level == null ? 384 : level.getHeight();
      double norm = range <= 0 ? 0.5 : (surfaceY - minY) / (double)range;
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
      Slot slot = this.slots.get(slotIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         result = stack.copy();
         if (slotIndex < PLAYER_SLOTS_END) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOTS_END, MACHINE_SLOTS_END, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 0, PLAYER_SLOTS_END, true)) {
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
