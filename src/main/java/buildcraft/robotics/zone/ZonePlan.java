/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.core.IZone;

public class ZonePlan implements IZone {
    private final HashMap<ChunkPos, ZoneChunk> chunkMapping = new HashMap<>();

    public ZonePlan() {}

    public ZonePlan(ZonePlan old) {
        for (Map.Entry<ChunkPos, ZoneChunk> entry : old.chunkMapping.entrySet()) {
            chunkMapping.put(entry.getKey(), new ZoneChunk(entry.getValue()));
        }
    }

    public boolean get(int x, int z) {
        int xChunk = x >> 4;
        int zChunk = z >> 4;
        ChunkPos chunkId = new ChunkPos(xChunk, zChunk);
        ZoneChunk property;

        if (!chunkMapping.containsKey(chunkId)) {
            return false;
        } else {
            property = chunkMapping.get(chunkId);
            return property.get(x & 0xF, z & 0xF);
        }
    }

    public void set(int x, int z, boolean val) {
        int xChunk = x >> 4;
        int zChunk = z >> 4;
        ChunkPos chunkId = new ChunkPos(xChunk, zChunk);
        ZoneChunk property;

        if (!chunkMapping.containsKey(chunkId)) {
            if (val) {
                property = new ZoneChunk();
                chunkMapping.put(chunkId, property);
            } else {
                return;
            }
        } else {
            property = chunkMapping.get(chunkId);
        }

        property.set(x & 0xF, z & 0xF, val);

        if (property.isEmpty()) {
            chunkMapping.remove(chunkId);
        }
    }

    public List<int[]> getAll() {
        List<int[]> result = new ArrayList<>();
        chunkMapping.forEach((chunkPos, zoneChunk) -> {
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
        getAll().forEach(p -> zonePlan.set(p[0] + offsetX, p[1] + offsetZ, true));
        return zonePlan;
    }

    public boolean hasChunk(ChunkPos chunkPos) {
        return chunkMapping.containsKey(chunkPos);
    }

    public Set<ChunkPos> getChunkPoses() {
        return chunkMapping.keySet();
    }

    public HashMap<ChunkPos, ZoneChunk> getChunkMapping() {
        return chunkMapping;
    }

    public void writeToNBT(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (Map.Entry<ChunkPos, ZoneChunk> entry : chunkMapping.entrySet()) {
            CompoundTag zoneChunkTag = new CompoundTag();
            entry.getValue().writeToNBT(zoneChunkTag);
            zoneChunkTag.putInt("chunkX", buildcraft.lib.misc.PositionUtil.chunkX(entry.getKey()));
            zoneChunkTag.putInt("chunkZ", buildcraft.lib.misc.PositionUtil.chunkZ(entry.getKey()));
            list.add(zoneChunkTag);
        }
        nbt.put("chunkMapping", list);
    }

    public void readFromNBT(CompoundTag nbt) {
        chunkMapping.clear();
        nbt.getList("chunkMapping").ifPresent(list -> {
            for (int i = 0; i < list.size(); i++) {
                CompoundTag zoneChunkTag = list.getCompoundOrEmpty(i);
                ZoneChunk chunk = new ZoneChunk();
                chunk.readFromNBT(zoneChunkTag);
                chunkMapping.put(
                    new ChunkPos(
                        zoneChunkTag.getIntOr("chunkX", 0),
                        zoneChunkTag.getIntOr("chunkZ", 0)
                    ),
                    chunk
                );
            }
        });
    }

    @Override
    public double distanceTo(BlockPos pos) {
        return Math.sqrt(distanceToSquared(pos));
    }

    @Override
    public double distanceToSquared(BlockPos pos) {
        double maxSqrDistance = Double.MAX_VALUE;

        for (Map.Entry<ChunkPos, ZoneChunk> e : chunkMapping.entrySet()) {
            double dx = (buildcraft.lib.misc.PositionUtil.chunkX(e.getKey()) << 4) + 8 - pos.getX();
            double dz = (buildcraft.lib.misc.PositionUtil.chunkZ(e.getKey()) << 4) + 8 - pos.getZ();

            double sqrDistance = dx * dx + dz * dz;

            if (sqrDistance < maxSqrDistance) {
                maxSqrDistance = sqrDistance;
            }
        }

        return maxSqrDistance;
    }

    @Override
    public boolean contains(Vec3 point) {
        int xBlock = (int) Math.floor(point.x);
        int zBlock = (int) Math.floor(point.z);

        return get(xBlock, zBlock);
    }

    @Override
    public BlockPos getRandomBlockPos(Random rand) {
        if (chunkMapping.isEmpty()) {
            return null;
        }

        int chunkId = rand.nextInt(chunkMapping.size());

        for (Map.Entry<ChunkPos, ZoneChunk> e : chunkMapping.entrySet()) {
            if (chunkId == 0) {
                BlockPos i = e.getValue().getRandomBlockPos(rand);
                int x = (buildcraft.lib.misc.PositionUtil.chunkX(e.getKey()) << 4) + i.getX();
                int z = (buildcraft.lib.misc.PositionUtil.chunkZ(e.getKey()) << 4) + i.getZ();

                return new BlockPos(x, i.getY(), z);
            }

            chunkId--;
        }

        return null;
    }

    public ZonePlan readFromByteBuf(FriendlyByteBuf buf) {
        chunkMapping.clear();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            ChunkPos key = new ChunkPos(buf.readInt(), buf.readInt());
            ZoneChunk value = new ZoneChunk();
            value.readFromByteBuf(buf);
            chunkMapping.put(key, value);
        }
        return this;
    }

    public void writeToByteBuf(FriendlyByteBuf buf) {
        buf.writeInt(chunkMapping.size());
        for (Map.Entry<ChunkPos, ZoneChunk> e : chunkMapping.entrySet()) {
            buf.writeInt(buildcraft.lib.misc.PositionUtil.chunkX(e.getKey()));
            buf.writeInt(buildcraft.lib.misc.PositionUtil.chunkZ(e.getKey()));
            e.getValue().writeToByteBuf(buf);
        }
    }
}
