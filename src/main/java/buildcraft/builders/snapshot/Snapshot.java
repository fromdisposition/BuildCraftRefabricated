/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;

import net.minecraft.network.FriendlyByteBuf;

import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

public abstract class Snapshot {
    public Key key = new Key();
    public BlockPos size;
    public Direction facing;
    public BlockPos offset;

    public static Snapshot create(EnumSnapshotType type) {
        switch (type) {
            case TEMPLATE:
                return new Template();
            case BLUEPRINT:
                return new Blueprint();
        }
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int posToIndex(int sizeX, int sizeY, int sizeZ, int x, int y, int z) {
        return ((z * sizeY) + y) * sizeX + x;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int posToIndex(BlockPos size, int x, int y, int z) {
        return posToIndex(size.getX(), size.getY(), size.getZ(), x, y, z);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int posToIndex(int sizeX, int sizeY, int sizeZ, BlockPos pos) {
        return posToIndex(sizeX, sizeY, sizeZ, pos.getX(), pos.getY(), pos.getZ());
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int posToIndex(BlockPos size, BlockPos pos) {
        return posToIndex(size.getX(), size.getY(), size.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public int posToIndex(int x, int y, int z) {
        return posToIndex(size, x, y, z);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public int posToIndex(BlockPos pos) {
        return posToIndex(size, pos);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static BlockPos indexToPos(int sizeX, int sizeY, int sizeZ, int i) {
        return new BlockPos(
            i % sizeX,
            (i / sizeX) % sizeY,
            i / (sizeY * sizeX)
        );
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static BlockPos indexToPos(BlockPos size, int i) {
        return indexToPos(size.getX(), size.getY(), size.getZ(), i);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public BlockPos indexToPos(int i) {
        return indexToPos(size, i);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int getDataSize(int x, int y, int z) {
        return x * y * z;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int getDataSize(BlockPos size) {
        return getDataSize(size.getX(), size.getY(), size.getZ());
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public int getDataSize() {
        return getDataSize(size);
    }

    public static CompoundTag writeToNBT(Snapshot snapshot) {
        CompoundTag nbt = snapshot.serializeNBT();
        nbt.put("type", NBTUtilBC.writeEnum(snapshot.getType()));
        return nbt;
    }

    public static Snapshot readFromNBT(CompoundTag nbt) throws InvalidInputDataException {
        Tag tag = nbt.get("type");
        EnumSnapshotType type = NBTUtilBC.readEnum(tag, EnumSnapshotType.class);
        if (type == null) {
            throw new InvalidInputDataException("Unknown snapshot type " + tag);
        }
        Snapshot snapshot = Snapshot.create(type);
        snapshot.deserializeNBT(nbt);
        return snapshot;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("key", key.serializeNBT());
        nbt.put("size", NBTUtilBC.writeBlockPos(size));
        nbt.put("facing", NBTUtilBC.writeEnum(facing));
        nbt.put("offset", NBTUtilBC.writeBlockPos(offset));
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        key = new Key(nbt.getCompoundOrEmpty("key"));
        size = NBTUtilBC.readBlockPos(nbt.getCompoundOrEmpty("size"));
        facing = NBTUtilBC.readEnum(nbt.get("facing"), Direction.class);
        offset = NBTUtilBC.readBlockPos(nbt.getCompoundOrEmpty("offset"));
    }

    abstract public Snapshot copy();

    abstract public EnumSnapshotType getType();

    abstract public int countNonAirCells();

    public void computeKey() {
        CompoundTag nbt = writeToNBT(this);
        if (nbt.contains("key")) {
            nbt.remove("key");
        }
        key = new Key(key, HashUtil.computeHash(nbt));
    }

    @Override
    public String toString() {
        return "Snapshot{" +
            "key=" + key +
            ", size=" + (size != null ? size.getX() + "x" + size.getY() + "x" + size.getZ() : "null") +
            ", facing=" + facing +
            ", offset=" + offset +
            "}";
    }

    public static class Key {
        public final byte[] hash;
        @Nullable
        public final Header header;

        @SuppressWarnings("WeakerAccess")
        public Key() {
            this.hash = new byte[0];
            this.header = null;
        }

        @SuppressWarnings("WeakerAccess")
        public Key(Key oldKey, byte[] hash) {
            this.hash = hash;
            this.header = oldKey.header;
        }

        @SuppressWarnings("WeakerAccess")
        public Key(Key oldKey, @Nullable Header header) {
            this.hash = oldKey.hash;
            this.header = header;
        }

        @SuppressWarnings("WeakerAccess")
        public Key(CompoundTag nbt) {
            hash = nbt.getByteArray("hash").orElse(new byte[0]);
            header = nbt.contains("header") ? new Header(nbt.getCompoundOrEmpty("header")) : null;
        }

        public Key(FriendlyByteBuf buf) {
            this.hash = buf.readByteArray();
            this.header = (buf.readByte() != 0) ? new Header(buf) : null;
        }

        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putByteArray("hash", hash);
            if (header != null) {
                nbt.put("header", header.serializeNBT());
            }
            return nbt;
        }

        public void writeToByteBuf(FriendlyByteBuf buf) {
            buf.writeByteArray(hash);
            buf.writeByte(header != null ? 1 : 0);
            if (header != null) {
                header.writeToByteBuf(buf);
            }
        }

        @Override
        public boolean equals(Object o) {
            return this == o ||
                o != null &&
                    getClass() == o.getClass() &&
                    Arrays.equals(hash, ((Key) o).hash) &&
                    (header != null ? header.equals(((Key) o).header) : ((Key) o).header == null);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(hash);
        }

        @Override
        public String toString() {
            return HashUtil.convertHashToString(hash);
        }
    }

    public static class Header {
        public final Key key;
        public final UUID owner;
        public final Date created;
        public final String name;

        @SuppressWarnings("WeakerAccess")
        public Header(Key key, UUID owner, Date created, String name) {
            this.key = key;
            this.owner = owner;
            this.created = created;
            this.name = name;
        }

        @SuppressWarnings("WeakerAccess")
        public Header(CompoundTag nbt) {
            key = new Key(nbt.getCompoundOrEmpty("key"));
            owner = NBTUtilBC.getUUID(nbt, "owner");
            created = new Date(nbt.getLongOr("created", 0L));
            name = nbt.getStringOr("name", "");
        }

        public Header(FriendlyByteBuf buf) {
            key = new Key(buf);
            owner = buf.readUUID();
            created = new Date(buf.readLong());
            name = buf.readUtf();
        }

        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("key", key.serializeNBT());
            NBTUtilBC.putUUID(nbt, "owner", owner);
            nbt.putLong("created", created.getTime());
            nbt.putString("name", name);
            return nbt;
        }

        public void writeToByteBuf(FriendlyByteBuf buf) {
            key.writeToByteBuf(buf);
            buf.writeUUID(owner);
            buf.writeLong(created.getTime());
            buf.writeUtf(name);
        }

        public Player getOwnerPlayer(Level level) {
            return level.getPlayerByUUID(owner);
        }

        @Override
        public boolean equals(Object o) {
            return this == o ||
                o != null &&
                    getClass() == o.getClass() &&
                    key.equals(((Header) o).key) &&
                    owner.equals(((Header) o).owner) &&
                    created.equals(((Header) o).created) &&
                    name.equals(((Header) o).name);
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + owner.hashCode();
            result = 31 * result + created.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public abstract class BuildingInfo {
        public final BlockPos basePos;
        public final BlockPos offsetPos;
        public final Rotation rotation;
        public final Box box = new Box();

        @SuppressWarnings("this-escape")
        protected BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.offsetPos = basePos.offset(offset.rotate(rotation));
            this.rotation = rotation;
            this.box.extendToEncompass(toWorld(BlockPos.ZERO));
            this.box.extendToEncompass(toWorld(size.subtract(VecUtil.POS_ONE)));
        }

        public BlockPos toWorld(BlockPos blockPos) {
            return blockPos
                .rotate(rotation)
                .offset(offsetPos);
        }

        public BlockPos fromWorld(BlockPos blockPos) {
            return blockPos
                .subtract(offsetPos)
                .rotate(RotationUtil.invert(rotation));
        }

        public abstract Snapshot getSnapshot();
    }
}
