package buildcraft.api.schematics;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.resources.Identifier;

import buildcraft.api.core.BuildCraftAPI;

public class SchematicEntityFactoryRegistry {
    private static final Set<SchematicEntityFactory<?>> FACTORIES = new TreeSet<>();

    public static <S extends ISchematicEntity> void registerFactory(String name,
                                                                    int priority,
                                                                    Predicate<SchematicEntityContext> predicate,
                                                                    Supplier<S> supplier) {
        FACTORIES.add(new SchematicEntityFactory<>(
            BuildCraftAPI.nameToResourceLocation(name),
            priority,
            predicate,
            supplier
        ));
    }

    public static <S extends ISchematicEntity> void registerFactory(String name,
                                                                    int priority,
                                                                    List<Object> entities,
                                                                    Supplier<S> supplier) {
        registerFactory(
            name,
            priority,
            context -> entities.contains(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(context.entity.getType())),
            supplier
        );
    }

    public static List<SchematicEntityFactory<?>> getFactories() {
        return ImmutableList.copyOf(FACTORIES);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <S extends ISchematicEntity> SchematicEntityFactory<S> getFactoryByInstance(S instance) {
        return (SchematicEntityFactory<S>) FACTORIES.stream()
            .filter(schematicEntityFactory -> schematicEntityFactory.clazz == instance.getClass())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Didn't find a factory for " + instance.getClass()));
    }

    @Nullable
    public static SchematicEntityFactory<?> getFactoryByName(Object name) {
        Identifier id = name instanceof Identifier i ? i
                : name instanceof String s ? BuildCraftAPI.nameToResourceLocation(s)
                : null;
        if (id == null) return null;
        return FACTORIES.stream()
            .filter(schematicEntityFactory -> id.equals(schematicEntityFactory.name))
            .findFirst()
            .orElse(null);
    }
}
