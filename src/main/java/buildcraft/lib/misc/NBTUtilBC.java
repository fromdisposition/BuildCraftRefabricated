/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.misc;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.serialization.DynamicOps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.phys.Vec3;

public class NBTUtilBC {

    public static final Tag NBT_NULL = EndTag.INSTANCE;

    public static DynamicOps<Tag> registryAwareOps() {
        return buildcraft.lib.fabric.Mc26Compat.registryAwareOps();
    }

    public static DynamicOps<Tag> registryAwareOps(net.minecraft.world.level.Level level) {
        return buildcraft.lib.fabric.Mc26Compat.registryAwareOps(level);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Tag> Optional<T> toOptional(@Nullable T tag) {
        if (tag == null || tag == NBT_NULL || tag.getId() == Tag.TAG_END) {
            return Optional.empty();
        }
        return Optional.of(tag);
    }

    @Nonnull
    public static CompoundTag getItemData(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return new CompoundTag();
        }
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
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
        return new BlockPos(
            nbt.getIntOr("X", 0),
            nbt.getIntOr("Y", 0),
            nbt.getIntOr("Z", 0)
        );
    }

    public static void putUUID(CompoundTag nbt, String key, UUID uuid) {
        nbt.putLong(key + "Most", uuid.getMostSignificantBits());
        nbt.putLong(key + "Least", uuid.getLeastSignificantBits());
    }

    public static UUID getUUID(CompoundTag nbt, String key) {
        return new UUID(
            nbt.getLongOr(key + "Most", 0L),
            nbt.getLongOr(key + "Least", 0L)
        );
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
        }
        return null;
    }

    public static ListTag writeCompoundList(Stream<CompoundTag> stream) {
        ListTag list = new ListTag();
        stream.forEach(list::add);
        return list;
    }

    public static Stream<CompoundTag> readCompoundList(Tag tag) {
        if (tag instanceof ListTag listTag) {
            return IntStream.range(0, listTag.size())
                    .mapToObj(i -> {
                        Tag element = listTag.get(i);
                        return element instanceof CompoundTag ct ? ct : new CompoundTag();
                    });
        }
        return Stream.empty();
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
        if (tag instanceof ListTag listTag && listTag.size() >= 3) {
            return new Vec3(
                listTag.getDoubleOr(0, 0.0),
                listTag.getDoubleOr(1, 0.0),
                listTag.getDoubleOr(2, 0.0)
            );
        }
        return null;
    }

    public static ListTag writeStringList(Stream<String> stream) {
        ListTag list = new ListTag();
        stream.map(StringTag::valueOf).forEach(list::add);
        return list;
    }

    public static Stream<String> readStringList(@Nullable Tag tag) {
        if (tag instanceof ListTag listTag) {
            return IntStream.range(0, listTag.size())
                    .mapToObj(i -> {
                        Tag element = listTag.get(i);
                        return element instanceof StringTag st ? st.value() : "";
                    });
        }
        return Stream.empty();
    }

    @Nonnull
    public static CompoundTag itemStackToNBT(@Nonnull net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) {
            return new CompoundTag();
        }
        CompoundTag nbt = new CompoundTag();
        net.minecraft.resources.Identifier itemId =
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        nbt.putString("id", itemId.toString());
        nbt.putInt("count", stack.getCount());
        return nbt;
    }

    @Nonnull
    public static net.minecraft.world.item.ItemStack itemStackFromNBT(@Nonnull CompoundTag nbt) {
        if (nbt.isEmpty()) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        String idStr = nbt.getStringOr("id", "");
        if (idStr.isEmpty()) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        net.minecraft.resources.Identifier itemId = net.minecraft.resources.Identifier.parse(idStr);
        net.minecraft.world.item.Item item =
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(itemId);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        int count = nbt.getIntOr("count", 1);
        return new net.minecraft.world.item.ItemStack(item, count);
    }
}
