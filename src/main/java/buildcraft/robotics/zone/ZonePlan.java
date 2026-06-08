/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import buildcraft.api.core.IZone;
import buildcraft.lib.misc.PositionUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public class ZonePlan implements IZone {
   private final HashMap<ChunkPos, ZoneChunk> chunkMapping = new HashMap<>();

   public ZonePlan() {
   }

   public ZonePlan(ZonePlan old) {
      for (Entry<ChunkPos, ZoneChunk> entry : old.chunkMapping.entrySet()) {
         this.chunkMapping.put(entry.getKey(), new ZoneChunk(entry.getValue()));
      }
   }

   public boolean get(int x, int z) {
      int xChunk = x >> 4;
      int zChunk = z >> 4;
      ChunkPos chunkId = new ChunkPos(xChunk, zChunk);
      if (!this.chunkMapping.containsKey(chunkId)) {
         return false;
      }

      ZoneChunk property = this.chunkMapping.get(chunkId);
      return property.get(x & 15, z & 15);
   }

   public void set(int x, int z, boolean val) {
      int xChunk = x >> 4;
      int zChunk = z >> 4;
      ChunkPos chunkId = new ChunkPos(xChunk, zChunk);
      ZoneChunk property;
      if (!this.chunkMapping.containsKey(chunkId)) {
         if (!val) {
            return;
         }

         property = new ZoneChunk();
         this.chunkMapping.put(chunkId, property);
      } else {
         property = this.chunkMapping.get(chunkId);
      }

      property.set(x & 15, z & 15, val);
      if (property.isEmpty()) {
         this.chunkMapping.remove(chunkId);
      }
   }

   public List<int[]> getAll() {
      List<int[]> result = new ArrayList<>();
      this.chunkMapping.forEach((chunkPos, zoneChunk) -> {
         List<int[]> zoneChunkAll = zoneChunk.getAll();
         int startX = chunkPos.getMinBlockX();
         int startZ = chunkPos.getMinBlockZ();

         for (int[] p : zoneChunkAll) {
            result.add(new int[]{p[0] + startX, p[1] + startZ});
         }
      });
      return result;
   }

   public ZonePlan getWithOffset(int offsetX, int offsetZ) {
      ZonePlan zonePlan = new ZonePlan();
      this.getAll().forEach(p -> zonePlan.set(p[0] + offsetX, p[1] + offsetZ, true));
      return zonePlan;
   }

   public boolean hasChunk(ChunkPos chunkPos) {
      return this.chunkMapping.containsKey(chunkPos);
   }

   public Set<ChunkPos> getChunkPoses() {
      return this.chunkMapping.keySet();
   }

   public HashMap<ChunkPos, ZoneChunk> getChunkMapping() {
      return this.chunkMapping;
   }

   public void writeToNBT(CompoundTag nbt) {
      ListTag list = new ListTag();

      for (Entry<ChunkPos, ZoneChunk> entry : this.chunkMapping.entrySet()) {
         CompoundTag zoneChunkTag = new CompoundTag();
         entry.getValue().writeToNBT(zoneChunkTag);
         zoneChunkTag.putInt("chunkX", PositionUtil.chunkX(entry.getKey()));
         zoneChunkTag.putInt("chunkZ", PositionUtil.chunkZ(entry.getKey()));
         list.add(zoneChunkTag);
      }

      nbt.put("chunkMapping", list);
   }

   public void readFromNBT(CompoundTag nbt) {
      this.chunkMapping.clear();
      nbt.getList("chunkMapping").ifPresent(list -> {
         for (int i = 0; i < list.size(); i++) {
            CompoundTag zoneChunkTag = list.getCompoundOrEmpty(i);
            ZoneChunk chunk = new ZoneChunk();
            chunk.readFromNBT(zoneChunkTag);
            this.chunkMapping.put(new ChunkPos(zoneChunkTag.getIntOr("chunkX", 0), zoneChunkTag.getIntOr("chunkZ", 0)), chunk);
         }
      });
   }

   @Override
   public double distanceTo(BlockPos pos) {
      return Math.sqrt(this.distanceToSquared(pos));
   }

   @Override
   public double distanceToSquared(BlockPos pos) {
      double maxSqrDistance = Double.MAX_VALUE;

      for (Entry<ChunkPos, ZoneChunk> e : this.chunkMapping.entrySet()) {
         double dx = (PositionUtil.chunkX(e.getKey()) << 4) + 8 - pos.getX();
         double dz = (PositionUtil.chunkZ(e.getKey()) << 4) + 8 - pos.getZ();
         double sqrDistance = dx * dx + dz * dz;
         if (sqrDistance < maxSqrDistance) {
            maxSqrDistance = sqrDistance;
         }
      }

      return maxSqrDistance;
   }

   @Override
   public boolean contains(Vec3 point) {
      int xBlock = (int)Math.floor(point.x);
      int zBlock = (int)Math.floor(point.z);
      return this.get(xBlock, zBlock);
   }

   @Override
   public BlockPos getRandomBlockPos(Random rand) {
      if (this.chunkMapping.isEmpty()) {
         return null;
      }

      int chunkId = rand.nextInt(this.chunkMapping.size());

      for (Entry<ChunkPos, ZoneChunk> e : this.chunkMapping.entrySet()) {
         if (chunkId == 0) {
            BlockPos i = e.getValue().getRandomBlockPos(rand);
            int x = (PositionUtil.chunkX(e.getKey()) << 4) + i.getX();
            int z = (PositionUtil.chunkZ(e.getKey()) << 4) + i.getZ();
            return new BlockPos(x, i.getY(), z);
         }

         chunkId--;
      }

      return null;
   }

   public ZonePlan readFromByteBuf(FriendlyByteBuf buf) {
      this.chunkMapping.clear();
      int size = buf.readInt();

      for (int i = 0; i < size; i++) {
         ChunkPos key = new ChunkPos(buf.readInt(), buf.readInt());
         ZoneChunk value = new ZoneChunk();
         value.readFromByteBuf(buf);
         this.chunkMapping.put(key, value);
      }

      return this;
   }

   public void writeToByteBuf(FriendlyByteBuf buf) {
      buf.writeInt(this.chunkMapping.size());

      for (Entry<ChunkPos, ZoneChunk> e : this.chunkMapping.entrySet()) {
         buf.writeInt(PositionUtil.chunkX(e.getKey()));
         buf.writeInt(PositionUtil.chunkZ(e.getKey()));
         e.getValue().writeToByteBuf(buf);
      }
   }
}
