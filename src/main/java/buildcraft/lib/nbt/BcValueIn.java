/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
//? if >= 1.21.10 {
import net.minecraft.world.level.storage.ValueInput;
//?}

/**
 * Version-neutral read side of block-entity serialization. Mirror of {@link BcValueOut}. On 1.21.5+ it wraps
 * {@code ValueInput}; on 1.21.1 it wraps {@code CompoundTag} (+ registry provider for codecs) and emulates the
 * {@code getXOr} accessors (1.21.1 bare getters return the value directly, with no Optional/OrEmpty variants).
 * Method names mirror {@code ValueInput} so a tile's {@code readData} body is the same on every node.
 */
public final class BcValueIn {
   //? if >= 1.21.10 {
   public final ValueInput raw;

   public BcValueIn(ValueInput raw) {
      this.raw = raw;
   }

   public byte getByteOr(String key, byte def) { return raw.getByteOr(key, def); }
   public short getShortOr(String key, short def) { return (short) raw.getShortOr(key, def); } // ValueInput.getShortOr widens to int
   public int getIntOr(String key, int def) { return raw.getIntOr(key, def); }
   public long getLongOr(String key, long def) { return raw.getLongOr(key, def); }
   public float getFloatOr(String key, float def) { return raw.getFloatOr(key, def); }
   public double getDoubleOr(String key, double def) { return raw.getDoubleOr(key, def); }
   public boolean getBooleanOr(String key, boolean def) { return raw.getBooleanOr(key, def); }
   public String getStringOr(String key, String def) { return raw.getStringOr(key, def); }
   public int[] getIntArray(String key) { return raw.getIntArray(key).orElse(new int[0]); }

   public <T> Optional<T> read(String key, Codec<T> codec) { return raw.read(key, codec); }

   public Optional<BcValueIn> child(String key) { return raw.child(key).map(BcValueIn::new); }
   //?} else {
   /*public final CompoundTag raw;
   private final HolderLookup.Provider registries;

   public BcValueIn(CompoundTag raw, HolderLookup.Provider registries) {
      this.raw = raw;
      this.registries = registries;
   }

   public byte getByteOr(String key, byte def) { return raw.contains(key) ? raw.getByte(key) : def; }
   public short getShortOr(String key, short def) { return raw.contains(key) ? raw.getShort(key) : def; }
   public int getIntOr(String key, int def) { return raw.contains(key) ? raw.getInt(key) : def; }
   public long getLongOr(String key, long def) { return raw.contains(key) ? raw.getLong(key) : def; }
   public float getFloatOr(String key, float def) { return raw.contains(key) ? raw.getFloat(key) : def; }
   public double getDoubleOr(String key, double def) { return raw.contains(key) ? raw.getDouble(key) : def; }
   public boolean getBooleanOr(String key, boolean def) { return raw.contains(key) ? raw.getBoolean(key) : def; }
   public String getStringOr(String key, String def) { return raw.contains(key) ? raw.getString(key) : def; }
   public int[] getIntArray(String key) { return raw.getIntArray(key); }

   public <T> Optional<T> read(String key, Codec<T> codec) {
      if (!raw.contains(key)) {
         return Optional.empty();
      }
      RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
      return codec.parse(ops, raw.get(key)).result();
   }

   public Optional<BcValueIn> child(String key) {
      return raw.contains(key) ? Optional.of(new BcValueIn(raw.getCompound(key), registries)) : Optional.empty();
   }
   *///?}
}
