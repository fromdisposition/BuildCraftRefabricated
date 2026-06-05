/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.List;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import buildcraft.lib.misc.NBTUtilBC;

public class NbtPath {
    private final List<String> elements;

    private NbtPath(List<String> elements) {
        this.elements = elements;
    }

    public Tag get(ByteTag tag) {
        return elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL;
    }

    public Tag get(ShortTag tag) {
        return elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL;
    }

    public Tag get(IntTag tag) {
        return elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL;
    }

    public Tag get(LongTag tag) {
        return elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL;
    }

    public Tag get(FloatTag tag) {
        return elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL;
    }

    public Tag get(DoubleTag tag) {
        return elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL;
    }

    public Tag get(ByteArrayTag tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return NBTUtilBC.NBT_NULL;
            }
            byte[] bytes = tag.getAsByteArray();
            if (key >= 0 && key < bytes.length) {
                return ByteTag.valueOf(bytes[key]);
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public Tag get(StringTag tag) {
        return elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL;
    }

    public Tag get(ListTag tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return NBTUtilBC.NBT_NULL;
            }
            if (key >= 0 && key < tag.size()) {
                return new NbtPath(elements.subList(1, elements.size())).get(tag.get(key));
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public Tag get(CompoundTag tag) {
        if (!elements.isEmpty()) {
            String key = elements.get(0);
            if (tag.contains(key)) {
                return new NbtPath(elements.subList(1, elements.size())).get(tag.get(key));
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else {
            return tag;
        }
    }

    public Tag get(IntArrayTag tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return NBTUtilBC.NBT_NULL;
            }
            int[] ints = tag.getAsIntArray();
            if (key >= 0 && key < ints.length) {
                return IntTag.valueOf(ints[key]);
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public Tag get(Tag tag) {
        if (tag == null) return NBTUtilBC.NBT_NULL;
        return switch (tag.getId()) {
            case Tag.TAG_BYTE -> get((ByteTag) tag);
            case Tag.TAG_SHORT -> get((ShortTag) tag);
            case Tag.TAG_INT -> get((IntTag) tag);
            case Tag.TAG_LONG -> get((LongTag) tag);
            case Tag.TAG_FLOAT -> get((FloatTag) tag);
            case Tag.TAG_DOUBLE -> get((DoubleTag) tag);
            case Tag.TAG_BYTE_ARRAY -> get((ByteArrayTag) tag);
            case Tag.TAG_STRING -> get((StringTag) tag);
            case Tag.TAG_LIST -> get((ListTag) tag);
            case Tag.TAG_COMPOUND -> get((CompoundTag) tag);
            case Tag.TAG_INT_ARRAY -> get((IntArrayTag) tag);
            default -> NBTUtilBC.NBT_NULL;
        };
    }

    public void remove(CompoundTag root) {
        if (elements.isEmpty()) return;
        CompoundTag current = root;
        for (int i = 0; i < elements.size() - 1; i++) {
            Tag next = current.get(elements.get(i));
            if (!(next instanceof CompoundTag c)) return;
            current = c;
        }
        current.remove(elements.get(elements.size() - 1));
    }

    @Override
    public String toString() {
        return "NbtPath{" + elements + "}";
    }

    @SuppressWarnings("WeakerAccess")
    public static final JsonDeserializer<NbtPath> DESERIALIZER = (json, typeOfT, context) ->
        new NbtPath(
            context.deserialize(
                json,
                new TypeToken<List<String>>() {
                }.getType()
            )
        );
}
