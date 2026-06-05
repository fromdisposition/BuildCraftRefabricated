/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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

public class JsonUtil {

    public static GsonBuilder registerNbtSerializersDeserializers(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return type.getRawType() == Tag.class ? new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        @SuppressWarnings("unchecked")
                        JsonElement element = ((JsonSerializer<T>) (JsonSerializer<Tag>) (src, typeOfSrc, context) -> {
                            if (src == NBTUtilBC.NBT_NULL) {
                                return JsonNull.INSTANCE;
                            }
                            switch (src.getId()) {
                                case Tag.TAG_BYTE: return context.serialize(src, ByteTag.class);
                                case Tag.TAG_SHORT: return context.serialize(src, ShortTag.class);
                                case Tag.TAG_INT: return context.serialize(src, IntTag.class);
                                case Tag.TAG_LONG: return context.serialize(src, LongTag.class);
                                case Tag.TAG_FLOAT: return context.serialize(src, FloatTag.class);
                                case Tag.TAG_DOUBLE: return context.serialize(src, DoubleTag.class);
                                case Tag.TAG_BYTE_ARRAY: return context.serialize(src, ByteArrayTag.class);
                                case Tag.TAG_STRING: return context.serialize(src, StringTag.class);
                                case Tag.TAG_LIST: return context.serialize(src, ListTag.class);
                                case Tag.TAG_COMPOUND: return context.serialize(src, CompoundTag.class);
                                case Tag.TAG_INT_ARRAY: return context.serialize(src, IntArrayTag.class);
                                default: throw new IllegalArgumentException(src.toString());
                            }
                        }).serialize(value, type.getType(), new JsonSerializationContext() {
                            @Override public JsonElement serialize(Object src) { return gson.toJsonTree(src); }
                            @Override public JsonElement serialize(Object src, Type typeOfSrc) { return gson.toJsonTree(src, typeOfSrc); }
                        });
                        Streams.write(element, out);
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public T read(JsonReader in) throws IOException {
                        return ((JsonDeserializer<T>) (json, typeOfT, context) -> {
                            if (json.isJsonNull()) {
                                return (T) NBTUtilBC.NBT_NULL;
                            }
                            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                                Number number = json.getAsJsonPrimitive().getAsNumber();
                                if (number instanceof BigInteger || number instanceof Long || number instanceof Integer
                                    || number instanceof Short || number instanceof Byte) {
                                    return context.deserialize(json, LongTag.class);
                                } else {
                                    return context.deserialize(json, DoubleTag.class);
                                }
                            }
                            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
                                return context.deserialize(
                                    new JsonPrimitive(json.getAsJsonPrimitive().getAsBoolean() ? (byte) 1 : (byte) 0),
                                    ByteTag.class);
                            }
                            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                                return context.deserialize(json, StringTag.class);
                            }
                            if (json.isJsonArray()) {
                                return context.deserialize(json, ListTag.class);
                            }
                            if (json.isJsonObject()) {
                                return context.deserialize(json, CompoundTag.class);
                            }
                            throw new IllegalArgumentException(json.toString());
                        }).deserialize(Streams.parse(in), type.getType(), gson::fromJson);
                    }
                } : null;
            }
        })

        .registerTypeAdapter(ByteTag.class,
            (JsonSerializer<ByteTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.value()))
        .registerTypeAdapter(ByteTag.class,
            (JsonDeserializer<ByteTag>) (json, typeOfT, context) -> ByteTag.valueOf(json.getAsJsonPrimitive().getAsByte()))

        .registerTypeAdapter(ShortTag.class,
            (JsonSerializer<ShortTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.value()))
        .registerTypeAdapter(ShortTag.class,
            (JsonDeserializer<ShortTag>) (json, typeOfT, context) -> ShortTag.valueOf(json.getAsJsonPrimitive().getAsShort()))

        .registerTypeAdapter(IntTag.class,
            (JsonSerializer<IntTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.value()))
        .registerTypeAdapter(IntTag.class,
            (JsonDeserializer<IntTag>) (json, typeOfT, context) -> IntTag.valueOf(json.getAsJsonPrimitive().getAsInt()))

        .registerTypeAdapter(LongTag.class,
            (JsonSerializer<LongTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.value()))
        .registerTypeAdapter(LongTag.class,
            (JsonDeserializer<LongTag>) (json, typeOfT, context) -> LongTag.valueOf(json.getAsJsonPrimitive().getAsLong()))

        .registerTypeAdapter(FloatTag.class,
            (JsonSerializer<FloatTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.value()))
        .registerTypeAdapter(FloatTag.class,
            (JsonDeserializer<FloatTag>) (json, typeOfT, context) -> FloatTag.valueOf(json.getAsJsonPrimitive().getAsFloat()))

        .registerTypeAdapter(DoubleTag.class,
            (JsonSerializer<DoubleTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.value()))
        .registerTypeAdapter(DoubleTag.class,
            (JsonDeserializer<DoubleTag>) (json, typeOfT, context) -> DoubleTag.valueOf(json.getAsJsonPrimitive().getAsDouble()))

        .registerTypeAdapter(ByteArrayTag.class, (JsonSerializer<ByteArrayTag>) (src, typeOfSrc, context) -> {
            JsonArray jsonArray = new JsonArray();
            for (byte element : src.getAsByteArray()) {
                jsonArray.add(new JsonPrimitive(element));
            }
            return jsonArray;
        })
        .registerTypeAdapter(ByteArrayTag.class,
            (JsonDeserializer<ByteArrayTag>) (json, typeOfT, context) -> {
                JsonArray arr = json.getAsJsonArray();
                byte[] bytes = new byte[arr.size()];
                for (int i = 0; i < arr.size(); i++) bytes[i] = arr.get(i).getAsByte();
                return new ByteArrayTag(bytes);
            })

        .registerTypeAdapter(StringTag.class,
            (JsonSerializer<StringTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.value()))
        .registerTypeAdapter(StringTag.class,
            (JsonDeserializer<StringTag>) (json, typeOfT, context) -> StringTag.valueOf(json.getAsJsonPrimitive().getAsString()))

        .registerTypeAdapter(ListTag.class, (JsonSerializer<ListTag>) (src, typeOfSrc, context) -> {
            JsonArray jsonArray = new JsonArray();
            for (int i = 0; i < src.size(); i++) {
                jsonArray.add(context.serialize(src.get(i), Tag.class));
            }
            return jsonArray;
        })
        .registerTypeAdapter(ListTag.class, (JsonDeserializer<ListTag>) (json, typeOfT, context) -> {
            ListTag nbtTagList = new ListTag();
            StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                .map(element -> context.<Tag>deserialize(element, Tag.class))
                .forEach(nbtTagList::add);
            return nbtTagList;
        })

        .registerTypeAdapter(CompoundTag.class, (JsonSerializer<CompoundTag>) (src, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();
            for (String key : src.keySet()) {
                jsonObject.add(key, context.serialize(src.get(key), Tag.class));
            }
            return jsonObject;
        })
        .registerTypeAdapter(CompoundTag.class, (JsonDeserializer<CompoundTag>) (json, typeOfT, context) -> {
            CompoundTag nbtTagCompound = new CompoundTag();
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                nbtTagCompound.put(entry.getKey(), context.deserialize(entry.getValue(), Tag.class));
            }
            return nbtTagCompound;
        })

        .registerTypeAdapter(IntArrayTag.class, (JsonSerializer<IntArrayTag>) (src, typeOfSrc, context) -> {
            JsonArray jsonArray = new JsonArray();
            for (int element : src.getAsIntArray()) {
                jsonArray.add(new JsonPrimitive(element));
            }
            return jsonArray;
        })
        .registerTypeAdapter(IntArrayTag.class,
            (JsonDeserializer<IntArrayTag>) (json, typeOfT, context) -> {
                JsonArray arr = json.getAsJsonArray();
                int[] ints = new int[arr.size()];
                for (int i = 0; i < arr.size(); i++) ints[i] = arr.get(i).getAsInt();
                return new IntArrayTag(ints);
            });
    }

    public static GsonBuilder registerTypeAdaptors(GsonBuilder builder) {
        return registerNbtSerializersDeserializers(builder);
    }

    public static JsonObject inheritTags(JsonObject parent, JsonObject child) {
        JsonObject result = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : parent.entrySet()) {
            result.add(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, JsonElement> entry : child.entrySet()) {
            result.add(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static float[] getSubAsFloatArray(JsonObject obj, String member) {
        if (!obj.has(member)) {
            throw new com.google.gson.JsonSyntaxException("Required member '" + member + "' in '" + obj + "'");
        }
        JsonElement elem = obj.get(member);
        if (elem.isJsonArray()) {
            JsonArray arr = elem.getAsJsonArray();
            float[] result = new float[arr.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = arr.get(i).getAsFloat();
            }
            return result;
        } else {
            throw new com.google.gson.JsonSyntaxException("Expected an array for '" + member + "', got " + elem);
        }
    }

    public static String[] getSubAsStringArray(JsonObject obj, String member) {
        if (!obj.has(member)) {
            throw new com.google.gson.JsonSyntaxException("Required member '" + member + "' in '" + obj + "'");
        }
        JsonElement elem = obj.get(member);
        if (elem.isJsonArray()) {
            JsonArray arr = elem.getAsJsonArray();
            String[] result = new String[arr.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = arr.get(i).getAsString();
            }
            return result;
        } else {
            throw new com.google.gson.JsonSyntaxException("Expected an array for '" + member + "', got " + elem);
        }
    }

    public static <T extends Map<?, ?>> T getSubAsImmutableMap(JsonObject obj, String member, TypeToken<T> token) {
        if (!obj.has(member)) {
            @SuppressWarnings("unchecked")
            T empty = (T) java.util.Collections.emptyMap();
            return empty;
        }
        return new Gson().fromJson(obj.get(member), token.getType());
    }

    public static String getAsString(JsonElement elem) {
        if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
            return elem.getAsString();
        }
        return elem.toString();
    }

    public static JsonObject inlineCustom(JsonObject obj) {

        return obj;
    }
}
