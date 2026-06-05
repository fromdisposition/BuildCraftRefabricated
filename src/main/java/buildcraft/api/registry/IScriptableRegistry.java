package buildcraft.api.registry;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public interface IScriptableRegistry<E> extends IReloadableRegistry<E> {

    String getEntryType();

    Map<String, Class<? extends E>> getScriptableTypes();

    Map<String, IEntryDeserializer<? extends E>> getCustomDeserializers();

    default void addSimpleType(String name, Class<? extends E> type) {
        getScriptableTypes().put(name, type);
    }

    default void addCustomType(String name, IEntryDeserializer<? extends E> deserializer) {
        getCustomDeserializers().put(name, deserializer);
    }

    Set<String> getSourceDomains();

    @FunctionalInterface
    public interface IEntryDeserializer<E> {

        OptionallyDisabled<E> deserialize(Object name, JsonObject obj, JsonDeserializationContext ctx)
            throws JsonSyntaxException;
    }

    @FunctionalInterface
    public interface ISimpleEntryDeserializer<E> extends IEntryDeserializer<E> {

        @Override
        default OptionallyDisabled<E> deserialize(Object name, JsonObject obj, JsonDeserializationContext ctx)
            throws JsonSyntaxException {
            return new OptionallyDisabled<>(deserializeConst(name, obj, ctx));
        }

        E deserializeConst(Object name, JsonObject obj, JsonDeserializationContext ctx)
            throws JsonSyntaxException;
    }

    public static final class OptionallyDisabled<E> {

        @Nullable
        private final E object;

        @Nullable
        private final String reason;

        public OptionallyDisabled(E object) {
            this.object = object;
            this.reason = null;
        }

        public OptionallyDisabled(String reason) {
            this.object = null;
            this.reason = reason;
        }

        public boolean isPresent() {
            return object != null;
        }

        @Nonnull
        public E get() {
            final E o = object;
            if (o != null) {
                return o;
            } else {
                throw new IllegalStateException("This object has been disabled! You must call isPresent() first!");
            }
        }

        @Nonnull
        public String getDisabledReason() {
            final String r = reason;
            if (r != null) {
                return r;
            } else {
                throw new IllegalStateException("This object has not been disabled! You must call isPresent() first!");
            }
        }
    }
}
