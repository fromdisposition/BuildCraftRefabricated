/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.lib.compat.BcSavedDataType;
import buildcraft.lib.nbt.BcAuth;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;

/**
 * Owner of each actively spreading water-gel block, keyed by position. The gel has no block entity to carry
 * the placing player, but every conversion of a water block must be attributable so the machine-break gate
 * (and through it any land-claim mod listening to the native Fabric break events) can allow or deny it.
 * Entries only live while a block is in a spreading stage; they are dropped once it starts gelling.
 */
public class GelOwnerSavedData extends SavedData {
   private static final Codec<GelOwnerSavedData.Entry> ENTRY_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.LONG.fieldOf("pos").forGetter(GelOwnerSavedData.Entry::pos),
            Codec.STRING.fieldOf("uuid").forGetter(GelOwnerSavedData.Entry::uuid),
            Codec.STRING.fieldOf("name").forGetter(GelOwnerSavedData.Entry::name)
         )
         .apply(instance, GelOwnerSavedData.Entry::new)
   );
   private static final Codec<GelOwnerSavedData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(Codec.list(ENTRY_CODEC).fieldOf("owners").forGetter(GelOwnerSavedData::entries))
         .apply(instance, GelOwnerSavedData::fromEntries)
   );
   public static final BcSavedDataType<GelOwnerSavedData> TYPE = new BcSavedDataType<>(
      "buildcraftfactory", "gel_owners", GelOwnerSavedData::new, CODEC, DataFixTypes.LEVEL
   );
   private final Long2ObjectOpenHashMap<GameProfile> owners = new Long2ObjectOpenHashMap<>();

   private GelOwnerSavedData() {
   }

   public static GelOwnerSavedData getOrCreate(Level level) {
      return TYPE.getOrCreate(level, GelOwnerSavedData::new);
   }

   private record Entry(long pos, String uuid, String name) {
   }

   private List<GelOwnerSavedData.Entry> entries() {
      List<GelOwnerSavedData.Entry> out = new ArrayList<>(this.owners.size());

      for (Long2ObjectOpenHashMap.Entry<GameProfile> e : this.owners.long2ObjectEntrySet()) {
         GameProfile profile = e.getValue();
         if (BcAuth.id(profile) != null) {
            out.add(new GelOwnerSavedData.Entry(e.getLongKey(), BcAuth.id(profile).toString(), BcAuth.name(profile) != null ? BcAuth.name(profile) : ""));
         }
      }

      return out;
   }

   private static GelOwnerSavedData fromEntries(List<GelOwnerSavedData.Entry> entries) {
      GelOwnerSavedData data = new GelOwnerSavedData();

      for (GelOwnerSavedData.Entry entry : entries) {
         try {
            data.owners.put(entry.pos(), new GameProfile(UUID.fromString(entry.uuid()), entry.name()));
         } catch (IllegalArgumentException ignored) {
            // Malformed uuid in old data: skip the entry; the gel then just fails closed at that position.
         }
      }

      return data;
   }

   public void setOwner(BlockPos pos, @Nullable GameProfile owner) {
      if (owner != null && BcAuth.id(owner) != null) {
         this.owners.put(pos.asLong(), owner);
         this.setDirty();
      }
   }

   @Nullable
   public GameProfile getOwner(BlockPos pos) {
      return this.owners.get(pos.asLong());
   }

   public void removeOwner(BlockPos pos) {
      if (this.owners.remove(pos.asLong()) != null) {
         this.setDirty();
      }
   }

   //? if < 1.21.10 {
   /*@Override
   public net.minecraft.nbt.CompoundTag save(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
      return BcSavedDataType.encode(CODEC, this, tag, provider);
   }
   *///?}
}
