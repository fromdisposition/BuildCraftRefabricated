package buildcraft.builders.snapshot;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class JsonSelector {
   private final String base;
   @SerializedName("nbt")
   private final List<JsonSelector.Expression> expressions;
   public static final TypeAdapterFactory TYPE_ADAPTER_FACTORY = new TypeAdapterFactory() {
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
         if (type.getRawType() != JsonSelector.class) {
            return null;
         }

         final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
         return new TypeAdapter<T>() {
            public void write(JsonWriter out, T value) throws IOException {
               throw new UnsupportedOperationException();
            }

            public T read(JsonReader in) throws IOException {
               return (T)(in.peek() == JsonToken.STRING ? new JsonSelector(in.nextString(), Collections.emptyList()) : delegate.read(in));
            }
         };
      }
   };

   private JsonSelector(String base, List<JsonSelector.Expression> expressions) {
      this.base = base;
      this.expressions = expressions;
   }

   public boolean matches(Predicate<String> basePredicate, CompoundTag nbt) {
      return basePredicate.test(this.base)
         && this.expressions.stream().allMatch(expression -> expression.operation.compare(expression.key.get(nbt), expression.value));
   }

   private static class Expression {
      public final NbtPath key;
      public final EnumNbtCompareOperation operation;
      public final Tag value;

      public Expression(NbtPath key, EnumNbtCompareOperation operation, Tag value) {
         this.key = key;
         this.operation = operation;
         this.value = value;
      }
   }
}
