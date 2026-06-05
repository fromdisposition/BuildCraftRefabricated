package buildcraft.lib.registry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class LegacyAliases {
    private static final Logger LOGGER = LogUtils.getLogger();

    public record Mapping(Registry<?> registry, Identifier from, Identifier to) {}

    public static final List<Mapping> MAPPINGS = new ArrayList<>();
    private static final Set<String> SEEN = new LinkedHashSet<>();

    private LegacyAliases() {}

    public static void init() {

        alias(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath("buildcraftcore", "fragile_fluid_shard"),
                Identifier.fromNamespaceAndPath("buildcraftcore", "fragile_fluid_container"));
        alias(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath("buildcraftfactory", "gel"),
                Identifier.fromNamespaceAndPath("buildcraftfactory", "gelled_water"));
        alias(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "redstone_red_chipset"),
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "chipset_redstone"));
        alias(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "redstone_iron_chipset"),
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "chipset_iron"));
        alias(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "redstone_gold_chipset"),
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "chipset_gold"));
        alias(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "redstone_quartz_chipset"),
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "chipset_quartz"));
        alias(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "redstone_diamond_chipset"),
                Identifier.fromNamespaceAndPath("buildcraftsilicon", "chipset_diamond"));
    }

    private static <T> void alias(Registry<T> registry, Identifier from, Identifier to) {
        if (from.equals(to)) {
            return;
        }
        String dedupe = registry.key().toString() + "|" + from;
        if (!SEEN.add(dedupe)) {
            return;
        }
        if (tryRegisterAlias(registry, from, to)) {
            MAPPINGS.add(new Mapping(registry, from, to));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean tryRegisterAlias(Registry<?> registry, Identifier from, Identifier to) {
        try {
            Method staticRegisterAlias = Registry.class.getMethod("registerAlias", Registry.class, Identifier.class, Identifier.class);
            staticRegisterAlias.invoke(null, registry, from, to);
            return true;
        } catch (ReflectiveOperationException ignored) {}
        try {
            Method addAlias = registry.getClass().getMethod("addAlias", Identifier.class, Identifier.class);
            addAlias.invoke(registry, from, to);
            return true;
        } catch (ReflectiveOperationException ignored) {}
        try {
            Method registerAlias = registry.getClass().getMethod("registerAlias", Identifier.class, Identifier.class);
            registerAlias.invoke(registry, from, to);
            return true;
        } catch (ReflectiveOperationException ignored) {}
        LOGGER.debug("Alias API not found for registry {}: {} -> {}", registry.key(), from, to);
        return false;
    }
}
