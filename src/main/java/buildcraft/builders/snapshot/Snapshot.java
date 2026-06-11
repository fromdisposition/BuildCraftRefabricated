/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

public abstract class Snapshot {
   public Snapshot.Key key = new Snapshot.Key();
   public BlockPos size;
   public Direction facing;
   public BlockPos offset;

   public static Snapshot create(EnumSnapshotType type) {
      switch (type) {
         case TEMPLATE:
            return new Template();
         case BLUEPRINT:
            return new Blueprint();
         default:
            throw new UnsupportedOperationException();
      }
   }

   public static int posToIndex(int sizeX, int sizeY, int sizeZ, int x, int y, int z) {
      return (z * sizeY + y) * sizeX + x;
   }

   public static int posToIndex(BlockPos size, int x, int y, int z) {
      return posToIndex(size.getX(), size.getY(), size.getZ(), x, y, z);
   }

   public static int posToIndex(int sizeX, int sizeY, int sizeZ, BlockPos pos) {
      return posToIndex(sizeX, sizeY, sizeZ, pos.getX(), pos.getY(), pos.getZ());
   }

   public static int posToIndex(BlockPos size, BlockPos pos) {
      return posToIndex(size.getX(), size.getY(), size.getZ(), pos.getX(), pos.getY(), pos.getZ());
   }

   public int posToIndex(int x, int y, int z) {
      return posToIndex(this.size, x, y, z);
   }

   public int posToIndex(BlockPos pos) {
      return posToIndex(this.size, pos);
   }

   public static BlockPos indexToPos(int sizeX, int sizeY, int sizeZ, int i) {
      return new BlockPos(i % sizeX, i / sizeX % sizeY, i / (sizeY * sizeX));
   }

   public static BlockPos indexToPos(BlockPos size, int i) {
      return indexToPos(size.getX(), size.getY(), size.getZ(), i);
   }

   public BlockPos indexToPos(int i) {
      return indexToPos(this.size, i);
   }

   public static int getDataSize(int x, int y, int z) {
      return x * y * z;
   }

   public static int getDataSize(BlockPos size) {
      return getDataSize(size.getX(), size.getY(), size.getZ());
   }

   public int getDataSize() {
      return getDataSize(this.size);
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

      Snapshot snapshot = create(type);
      snapshot.deserializeNBT(nbt);
      return snapshot;
   }

   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.put("key", this.key.serializeNBT());
      nbt.put("size", NBTUtilBC.writeBlockPos(this.size));
      nbt.put("facing", NBTUtilBC.writeEnum(this.facing));
      nbt.put("offset", NBTUtilBC.writeBlockPos(this.offset));
      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
      this.key = new Snapshot.Key(nbt.getCompoundOrEmpty("key"));
      this.size = NBTUtilBC.readBlockPos(nbt.getCompoundOrEmpty("size"));
      this.facing = NBTUtilBC.readEnum(nbt.get("facing"), Direction.class);
      this.offset = NBTUtilBC.readBlockPos(nbt.getCompoundOrEmpty("offset"));
   }

   public abstract Snapshot copy();

   public abstract EnumSnapshotType getType();

   public abstract int countNonAirCells();

   public void computeKey() {
      CompoundTag nbt = writeToNBT(this);
      if (nbt.contains("key")) {
         nbt.remove("key");
      }

      this.key = new Snapshot.Key(this.key, HashUtil.computeHash(nbt));
   }

   @Override
   public String toString() {
      return "Snapshot{key="
         + this.key
         + ", size="
         + (this.size != null ? this.size.getX() + "x" + this.size.getY() + "x" + this.size.getZ() : "null")
         + ", facing="
         + this.facing
         + ", offset="
         + this.offset
         + "}";
   }

   public abstract class BuildingInfo {
      public final BlockPos basePos;
      public final BlockPos offsetPos;
      public final Rotation rotation;
      public final Box box = new Box();

      protected BuildingInfo(BlockPos basePos, Rotation rotation) {
         this.basePos = basePos;
         this.offsetPos = basePos.offset(Snapshot.this.offset.rotate(rotation));
         this.rotation = rotation;
         this.box.extendToEncompass(this.toWorld(BlockPos.ZERO));
         this.box.extendToEncompass(this.toWorld(Snapshot.this.size.subtract(VecUtil.POS_ONE)));
      }

      public BlockPos toWorld(BlockPos blockPos) {
         return blockPos.rotate(this.rotation).offset(this.offsetPos);
      }

      public BlockPos fromWorld(BlockPos blockPos) {
         return blockPos.subtract(this.offsetPos).rotate(RotationUtil.invert(this.rotation));
      }

      public abstract Snapshot getSnapshot();
   }

   public static class Header {
      public final Snapshot.Key key;
      public final UUID owner;
      public final Date created;
      public final String name;

      public Header(Snapshot.Key key, UUID owner, Date created, String name) {
         this.key = key;
         this.owner = owner;
         this.created = created;
         this.name = name;
      }

      public Header(CompoundTag nbt) {
         this.key = new Snapshot.Key(nbt.getCompoundOrEmpty("key"));
         this.owner = NBTUtilBC.getUUID(nbt, "owner");
         this.created = new Date(nbt.getLongOr("created", 0L));
         this.name = nbt.getStringOr("name", "");
      }

      public Header(FriendlyByteBuf buf) {
         this.key = new Snapshot.Key(buf);
         this.owner = buf.readUUID();
         this.created = new Date(buf.readLong());
         this.name = buf.readUtf();
      }

      public CompoundTag serializeNBT() {
         CompoundTag nbt = new CompoundTag();
         nbt.put("key", this.key.serializeNBT());
         NBTUtilBC.putUUID(nbt, "owner", this.owner);
         nbt.putLong("created", this.created.getTime());
         nbt.putString("name", this.name);
         return nbt;
      }

      public void writeToByteBuf(FriendlyByteBuf buf) {
         this.key.writeToByteBuf(buf);
         buf.writeUUID(this.owner);
         buf.writeLong(this.created.getTime());
         buf.writeUtf(this.name);
      }

      public Player getOwnerPlayer(Level level) {
         return level.getPlayerByUUID(this.owner);
      }

      @Override
      public boolean equals(Object o) {
         return this == o
            || o != null
               && this.getClass() == o.getClass()
               && this.key.equals(((Snapshot.Header)o).key)
               && this.owner.equals(((Snapshot.Header)o).owner)
               && this.created.equals(((Snapshot.Header)o).created)
               && this.name.equals(((Snapshot.Header)o).name);
      }

      @Override
      public int hashCode() {
         int result = this.key.hashCode();
         result = 31 * result + this.owner.hashCode();
         result = 31 * result + this.created.hashCode();
         return 31 * result + this.name.hashCode();
      }

      @Override
      public String toString() {
         return this.name;
      }
   }

   public static class Key {
      public final byte[] hash;
      public final Snapshot.@Nullable Header header;

      public Key() {
         this.hash = new byte[0];
         this.header = null;
      }

      public Key(Snapshot.Key oldKey, byte[] hash) {
         this.hash = hash;
         this.header = oldKey.header;
      }

      public Key(Snapshot.Key oldKey, Snapshot.@Nullable Header header) {
         this.hash = oldKey.hash;
         this.header = header;
      }

      public Key(CompoundTag nbt) {
         this.hash = nbt.getByteArray("hash").orElse(new byte[0]);
         this.header = nbt.contains("header") ? new Snapshot.Header(nbt.getCompoundOrEmpty("header")) : null;
      }

      public Key(FriendlyByteBuf buf) {
         this.hash = buf.readByteArray();
         this.header = buf.readByte() != 0 ? new Snapshot.Header(buf) : null;
      }

      public CompoundTag serializeNBT() {
         CompoundTag nbt = new CompoundTag();
         nbt.putByteArray("hash", this.hash);
         if (this.header != null) {
            nbt.put("header", this.header.serializeNBT());
         }

         return nbt;
      }

      public void writeToByteBuf(FriendlyByteBuf buf) {
         buf.writeByteArray(this.hash);
         buf.writeByte(this.header != null ? 1 : 0);
         if (this.header != null) {
            this.header.writeToByteBuf(buf);
         }
      }

      @Override
      public boolean equals(Object o) {
         return this == o
            || o != null
               && this.getClass() == o.getClass()
               && Arrays.equals(this.hash, ((Snapshot.Key)o).hash)
               && (this.header != null ? this.header.equals(((Snapshot.Key)o).header) : ((Snapshot.Key)o).header == null);
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode(this.hash);
      }

      @Override
      public String toString() {
         return HashUtil.convertHashToString(this.hash);
      }
   }
}
