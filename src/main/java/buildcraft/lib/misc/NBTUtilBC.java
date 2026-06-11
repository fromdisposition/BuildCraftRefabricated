/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.fabric.Mc26Compat;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NBTUtilBC {
   public static final Tag NBT_NULL = EndTag.INSTANCE;

   public static DynamicOps<Tag> registryAwareOps() {
      return Mc26Compat.registryAwareOps();
   }

   public static DynamicOps<Tag> registryAwareOps(Level level) {
      return Mc26Compat.registryAwareOps(level);
   }

   public static <T extends Tag> Optional<T> toOptional(@Nullable T tag) {
      return tag != null && tag != NBT_NULL && tag.getId() != 0 ? Optional.of(tag) : Optional.empty();
   }

   @Nonnull
   public static CompoundTag getItemData(@Nonnull ItemStack stack) {
      if (stack.isEmpty()) {
         return new CompoundTag();
      }

      CustomData customData = (CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
      return customData.copyTag();
   }

   public static void setItemData(@Nonnull ItemStack stack, CompoundTag tag) {
      if (tag.isEmpty()) {
         stack.remove(DataComponents.CUSTOM_DATA);
      } else {
         stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      }
   }

   public static CompoundTag writeBlockPos(BlockPos pos) {
      CompoundTag nbt = new CompoundTag();
      nbt.putInt("X", pos.getX());
      nbt.putInt("Y", pos.getY());
      nbt.putInt("Z", pos.getZ());
      return nbt;
   }

   public static BlockPos readBlockPos(CompoundTag nbt) {
      return new BlockPos(nbt.getIntOr("X", 0), nbt.getIntOr("Y", 0), nbt.getIntOr("Z", 0));
   }

   public static void putUUID(CompoundTag nbt, String key, UUID uuid) {
      nbt.putLong(key + "Most", uuid.getMostSignificantBits());
      nbt.putLong(key + "Least", uuid.getLeastSignificantBits());
   }

   public static UUID getUUID(CompoundTag nbt, String key) {
      return new UUID(nbt.getLongOr(key + "Most", 0L), nbt.getLongOr(key + "Least", 0L));
   }

   public static StringTag writeEnum(Enum<?> value) {
      return StringTag.valueOf(value.name());
   }

   @Nullable
   public static <E extends Enum<E>> E readEnum(Tag tag, Class<E> clazz) {
      if (tag instanceof StringTag stringTag) {
         try {
            return Enum.valueOf(clazz, stringTag.value());
         } catch (IllegalArgumentException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static ListTag writeCompoundList(Stream<CompoundTag> stream) {
      ListTag list = new ListTag();
      stream.forEach(list::add);
      return list;
   }

   public static Stream<CompoundTag> readCompoundList(Tag tag) {
      return tag instanceof ListTag listTag
         ? IntStream.range(0, listTag.size()).mapToObj(i -> listTag.get(i) instanceof CompoundTag ct ? ct : new CompoundTag())
         : Stream.empty();
   }

   public static ListTag writeVec3(Vec3 vec) {
      ListTag list = new ListTag();
      list.add(DoubleTag.valueOf(vec.x));
      list.add(DoubleTag.valueOf(vec.y));
      list.add(DoubleTag.valueOf(vec.z));
      return list;
   }

   @Nullable
   public static Vec3 readVec3(@Nullable Tag tag) {
      return tag instanceof ListTag listTag && listTag.size() >= 3
         ? new Vec3(listTag.getDoubleOr(0, 0.0), listTag.getDoubleOr(1, 0.0), listTag.getDoubleOr(2, 0.0))
         : null;
   }

   public static ListTag writeStringList(Stream<String> stream) {
      ListTag list = new ListTag();
      stream.map(StringTag::valueOf).forEach(list::add);
      return list;
   }

   public static Stream<String> readStringList(@Nullable Tag tag) {
      return tag instanceof ListTag listTag
         ? IntStream.range(0, listTag.size()).mapToObj(i -> listTag.get(i) instanceof StringTag st ? st.value() : "")
         : Stream.empty();
   }

   @Nonnull
   public static CompoundTag itemStackToNBT(@Nonnull ItemStack stack) {
      if (stack.isEmpty()) {
         return new CompoundTag();
      }

      CompoundTag nbt = new CompoundTag();
      Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
      nbt.putString("id", itemId.toString());
      nbt.putInt("count", stack.getCount());
      return nbt;
   }

   @Nonnull
   public static ItemStack itemStackFromNBT(@Nonnull CompoundTag nbt) {
      if (nbt.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         String idStr = nbt.getStringOr("id", "");
         if (idStr.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            Identifier itemId = Identifier.parse(idStr);
            Item item = Mc26Compat.getItem(itemId);
            if (item != null && item != Items.AIR) {
               int count = nbt.getIntOr("count", 1);
               return new ItemStack(item, count);
            } else {
               return ItemStack.EMPTY;
            }
         }
      }
   }
}
