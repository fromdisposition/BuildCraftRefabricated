package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NbtRef<N extends Tag> {
   private final NbtRef.EnumType type;
   private final NbtPath path;
   private final N value;
   public static final TypeAdapterFactory TYPE_ADAPTER_FACTORY = new TypeAdapterFactory() {
      public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
         if (type.getRawType() != NbtRef.class) {
            return null;
         }

         final Class<? extends Tag> nClass = (Class<? extends Tag>)((ParameterizedType)type.getType()).getActualTypeArguments()[0];
         return nClass != ByteArrayTag.class && nClass != IntArrayTag.class && nClass != ListTag.class
            ? new TypeAdapter<T>() {
               public void write(JsonWriter out, T value) throws IOException {
                  throw new UnsupportedOperationException();
               }

               public T read(JsonReader in) throws IOException {
                  return (T)(in.peek() == JsonToken.BEGIN_ARRAY
                     ? NbtRef.EnumType.BY_PATH.create((NbtPath)gson.fromJson(in, NbtPath.class))
                     : NbtRef.EnumType.BY_VALUE.create((Tag)gson.fromJson(in, nClass)));
               }
            }
            : new TypeAdapter<T>() {
               public void write(JsonWriter out, T value) throws IOException {
                  throw new UnsupportedOperationException();
               }

               public T read(JsonReader in) throws IOException {
                  return (T)(in.peek() != JsonToken.BEGIN_ARRAY
                     ? NbtRef.EnumType.BY_PATH.create((NbtPath)((Map)gson.fromJson(in, (new TypeToken<Map<String, NbtPath>>() {}).getType())).get("ref"))
                     : NbtRef.EnumType.BY_VALUE.create((Tag)gson.fromJson(in, nClass)));
               }
            };
      }
   };

   private NbtRef(NbtRef.EnumType type, NbtPath path, N value) {
      this.type = type;
      this.path = path;
      this.value = value;
   }

   public Optional<N> get(Tag nbt) {
      if (this.type == NbtRef.EnumType.BY_PATH) {
         N result = (N)this.path.get(nbt);
         return NBTUtilBC.toOptional(result);
      } else if (this.type == NbtRef.EnumType.BY_VALUE) {
         return NBTUtilBC.toOptional(this.value);
      } else {
         throw new IllegalStateException();
      }
   }

   @Override
   public String toString() {
      if (this.type == NbtRef.EnumType.BY_PATH) {
         return "NbtRef{path=" + this.path + "}";
      } else if (this.type == NbtRef.EnumType.BY_VALUE) {
         return "NbtRef{value=" + this.value + "}";
      } else {
         throw new IllegalStateException();
      }
   }

   public enum EnumType {
      BY_PATH {
         @Override
         public NbtRef<?> create(NbtPath path) {
            return new NbtRef(this, path, null);
         }
      },
      BY_VALUE {
         @Override
         public <N extends Tag> NbtRef<N> create(N value) {
            return new NbtRef<>(this, null, value);
         }
      };

      public NbtRef<?> create(NbtPath path) {
         throw new UnsupportedOperationException();
      }

      public <N extends Tag> NbtRef<N> create(N value) {
         throw new UnsupportedOperationException();
      }
   }
}
