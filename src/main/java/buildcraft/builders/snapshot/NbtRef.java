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
import java.lang.reflect.Type;
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
   public static final TypeAdapterFactory TYPE_ADAPTER_FACTORY = new NbtRefTypeAdapterFactory();

   private NbtRef(NbtRef.EnumType type, NbtPath path, N value) {
      this.type = type;
      this.path = path;
      this.value = value;
   }

   public Optional<N> get(Tag nbt) {
      if (this.type == NbtRef.EnumType.BY_PATH) {
         N result = this.path.getTyped(nbt);
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
            return new NbtRef<>(this, path, null);
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

   private static final class NbtRefTypeAdapterFactory implements TypeAdapterFactory {
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
         if (type.getRawType() != NbtRef.class) {
            return null;
         }

         Type javaType = type.getType();
         if (!(javaType instanceof ParameterizedType parameterizedType)) {
            return null;
         }

         Type[] args = parameterizedType.getActualTypeArguments();
         if (args.length != 1 || !(args[0] instanceof Class<?> tagClass) || !Tag.class.isAssignableFrom(tagClass)) {
            return null;
         }

         @SuppressWarnings("unchecked")
         Class<? extends Tag> typedTagClass = (Class<? extends Tag>) tagClass;
         @SuppressWarnings("unchecked")
         TypeAdapter<T> adapter = (TypeAdapter<T>) createAdapter(gson, typedTagClass);
         return adapter;
      }

      private static <N extends Tag> TypeAdapter<NbtRef<N>> createAdapter(Gson gson, Class<N> tagClass) {
         boolean structured = tagClass == ByteArrayTag.class || tagClass == IntArrayTag.class || tagClass == ListTag.class;
         return new TypeAdapter<NbtRef<N>>() {
            @Override
            public void write(JsonWriter out, NbtRef<N> value) {
               throw new UnsupportedOperationException();
            }

            @Override
            public NbtRef<N> read(JsonReader in) throws IOException {
               if (structured) {
                  if (in.peek() == JsonToken.BEGIN_ARRAY) {
                     return new NbtRef<>(NbtRef.EnumType.BY_VALUE, null, gson.fromJson(in, tagClass));
                  }

                  Map<String, NbtPath> refMap = gson.fromJson(in, TypeToken.getParameterized(Map.class, String.class, NbtPath.class).getType());
                  NbtPath path = refMap.get("ref");
                  return new NbtRef<>(NbtRef.EnumType.BY_PATH, path, null);
               }

               if (in.peek() == JsonToken.BEGIN_ARRAY) {
                  NbtPath path = gson.fromJson(in, NbtPath.class);
                  return new NbtRef<>(NbtRef.EnumType.BY_PATH, path, null);
               }

               return new NbtRef<>(NbtRef.EnumType.BY_VALUE, null, gson.fromJson(in, tagClass));
            }
         };
      }
   }
}
