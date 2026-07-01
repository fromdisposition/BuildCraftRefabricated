/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.compat;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
//? if >= 1.21.10 {
import net.minecraft.world.level.saveddata.SavedDataType;
//?}
//? if >= 26.1 {
import net.minecraft.resources.Identifier;
//?}

/**
 * Version-neutral SavedData registration. On 1.21.5+ a SavedData is registered through a codec-backed
 * SavedDataType (id is an Identifier on 26.1+, a plain String on 1.21.10/1.21.11); on 1.21.1 there is no
 * SavedDataType - registration uses SavedData.Factory(constructor, deserializer, dataFixType) keyed by a String
 * name, and each SavedData subclass must override save(CompoundTag, Provider). This wraps all three forms over a
 * single Codec, so call sites only build a BcSavedDataType and call getOrCreate/save.
 */
public final class BcSavedDataType<T extends SavedData> {
   public final String namespace;
   public final String path;
   public final Supplier<T> constructor;
   public final Codec<T> codec;
   public final DataFixTypes dataFixType;

   public BcSavedDataType(String namespace, String path, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType) {
      this.namespace = namespace;
      this.path = path;
      this.constructor = constructor;
      this.codec = codec;
      this.dataFixType = dataFixType;
   }

   public String name() {
      return this.namespace + "_" + this.path;
   }

   //? if >= 1.21.10 {
   public SavedDataType<T> savedDataType() {
      //? if >= 26.1 {
      return new SavedDataType<>(Identifier.fromNamespaceAndPath(this.namespace, this.path), this.constructor, this.codec, this.dataFixType);
      //?}
      //? if < 26.1 {
      /*return new SavedDataType<>(this.name(), this.constructor, this.codec, this.dataFixType);
      *///?}
   }
   //?}
   //? if < 1.21.10 {
   /*public SavedData.Factory<T> factory() {
      return new SavedData.Factory<>(
         this.constructor,
         (tag, provider) -> this.codec.parse(RegistryOps.create(NbtOps.INSTANCE, provider), tag).result().orElseGet(this.constructor),
         this.dataFixType
      );
   }
   *///?}

   /** Get-or-create the SavedData for the (server) level, returning a transient client-side instance off-server. */
   public T getOrCreate(Level level, Supplier<T> clientEmpty) {
      if (level.isClientSide()) {
         return clientEmpty.get();
      }
      //? if >= 1.21.10 {
      return ((ServerLevel) level).getDataStorage().computeIfAbsent(this.savedDataType());
      //?} else {
      /*return ((ServerLevel) level).getDataStorage().computeIfAbsent(this.factory(), this.name());
      *///?}
   }

   /** 1.21.1-style save: encode {@code data} through the codec into a CompoundTag (used by SavedData.save overrides). */
   public static <T extends SavedData> CompoundTag encode(Codec<T> codec, T data, CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
      Tag encoded = codec.encodeStart(RegistryOps.create(NbtOps.INSTANCE, provider), data).result().orElse(tag);
      return encoded instanceof CompoundTag ct ? ct : tag;
   }
}
