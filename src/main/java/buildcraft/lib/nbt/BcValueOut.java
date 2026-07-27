/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
//? if >= 1.21.10 {
import net.minecraft.world.level.storage.ValueOutput;
//?}

/**
 * Version-neutral write side of block-entity serialization. 1.21.5 replaced the direct {@code CompoundTag}
 * save API with {@code ValueOutput}; on 1.21.1 that type does not exist, so this wraps a {@code CompoundTag}
 * (plus the registry provider needed for codec stores). BuildCraft tiles override the neutral
 * {@code writeData(BcValueOut)} and use this uniform API, so their bodies are identical on every node.
 */
public final class BcValueOut {
   //? if >= 1.21.10 {
   public final ValueOutput raw;

   public BcValueOut(ValueOutput raw) {
      this.raw = raw;
   }

   public void putByte(String key, byte value) { raw.putByte(key, value); }
   public void putShort(String key, short value) { raw.putShort(key, value); }
   public void putInt(String key, int value) { raw.putInt(key, value); }
   public void putLong(String key, long value) { raw.putLong(key, value); }
   public void putFloat(String key, float value) { raw.putFloat(key, value); }
   public void putDouble(String key, double value) { raw.putDouble(key, value); }
   public void putBoolean(String key, boolean value) { raw.putBoolean(key, value); }
   public void putString(String key, String value) { raw.putString(key, value); }
   public void putIntArray(String key, int[] value) { raw.putIntArray(key, value); }

   public <T> void store(String key, Codec<T> codec, T value) { raw.store(key, codec, value); }

   public <T> void storeNullable(String key, Codec<T> codec, T value) { raw.storeNullable(key, codec, value); }

   public BcValueOut child(String key) { return new BcValueOut(raw.child(key)); }
   //?} else {
   /*public final CompoundTag raw;
   private final HolderLookup.Provider registries;

   public BcValueOut(CompoundTag raw, HolderLookup.Provider registries) {
      this.raw = raw;
      this.registries = registries;
   }

   public void putByte(String key, byte value) { raw.putByte(key, value); }
   public void putShort(String key, short value) { raw.putShort(key, value); }
   public void putInt(String key, int value) { raw.putInt(key, value); }
   public void putLong(String key, long value) { raw.putLong(key, value); }
   public void putFloat(String key, float value) { raw.putFloat(key, value); }
   public void putDouble(String key, double value) { raw.putDouble(key, value); }
   public void putBoolean(String key, boolean value) { raw.putBoolean(key, value); }
   public void putString(String key, String value) { raw.putString(key, value); }
   public void putIntArray(String key, int[] value) { raw.putIntArray(key, value); }

   public <T> void store(String key, Codec<T> codec, T value) {
      RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
      raw.put(key, codec.encodeStart(ops, value).getOrThrow());
   }

   public <T> void storeNullable(String key, Codec<T> codec, T value) {
      if (value != null) {
         this.store(key, codec, value);
      }
   }

   public BcValueOut child(String key) {
      CompoundTag sub = new CompoundTag();
      raw.put(key, sub);
      return new BcValueOut(sub, registries);
   }
   *///?}
}
