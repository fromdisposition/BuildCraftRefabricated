package buildcraft.fabric.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class DeferredRegister<T> {
    protected final ResourceKey<? extends Registry<T>> registryKey;
    protected final String namespace;
    protected final List<Runnable> pending = new ArrayList<>();

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        return new DeferredRegister<>(registryKey, namespace);
    }

    public static Blocks createBlocks(String namespace) {
        return Blocks.create(namespace);
    }

    public static Items createItems(String namespace) {
        return Items.create(namespace);
    }

    @SuppressWarnings("unchecked")
    private Registry<T> registry() {
        if (registryKey.equals(net.minecraft.core.registries.Registries.BLOCK)) {
            return (Registry<T>) net.minecraft.core.registries.BuiltInRegistries.BLOCK;
        }
        if (registryKey.equals(net.minecraft.core.registries.Registries.ITEM)) {
            return (Registry<T>) net.minecraft.core.registries.BuiltInRegistries.ITEM;
        }
        if (registryKey.equals(net.minecraft.core.registries.Registries.FLUID)) {
            return (Registry<T>) net.minecraft.core.registries.BuiltInRegistries.FLUID;
        }
        if (registryKey.equals(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE)) {
            return (Registry<T>) net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE;
        }
        if (registryKey.equals(net.minecraft.core.registries.Registries.MENU)) {
            return (Registry<T>) net.minecraft.core.registries.BuiltInRegistries.MENU;
        }
        if (registryKey.equals(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE)) {
            return (Registry<T>) net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE;
        }
        throw new IllegalArgumentException("Unsupported registry: " + registryKey);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        DeferredHolder<T, I> holder = new DeferredHolder<>();
        Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
        pending.add(() -> {
            @SuppressWarnings("unchecked")
            I value = (I) Registry.register(registry(), id, sup.get());
            holder.bind(value);
        });
        return holder;
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Function<Identifier, ? extends I> factory) {
        return register(name, () -> factory.apply(Identifier.fromNamespaceAndPath(namespace, name)));
    }

    public void register() {
        pending.forEach(Runnable::run);
        pending.clear();
    }

    public static final class Blocks extends DeferredRegister<net.minecraft.world.level.block.Block> {
        private Blocks(String namespace) {
            super(net.minecraft.core.registries.Registries.BLOCK, namespace);
        }

        public static Blocks create(String namespace) {
            return new Blocks(namespace);
        }

        public <B extends net.minecraft.world.level.block.Block> DeferredBlock<B> registerBlock(
                String name,
                Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, B> factory,
                Supplier<net.minecraft.world.level.block.state.BlockBehaviour.Properties> props) {
            DeferredBlock<B> holder = new DeferredBlock<>();
            Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
            holder.pendingId = id;
            pending.add(() -> {
                ResourceKey<net.minecraft.world.level.block.Block> key =
                        ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, id);
                B value = factory.apply(props.get().setId(key));
                Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK, id, value);
                holder.bind(value, id);
            });
            return holder;
        }
    }

    public static final class Items extends DeferredRegister<net.minecraft.world.item.Item> {
        private Items(String namespace) {
            super(net.minecraft.core.registries.Registries.ITEM, namespace);
        }

        public static Items create(String namespace) {
            return new Items(namespace);
        }

        public DeferredItem<net.minecraft.world.item.BlockItem> registerSimpleBlockItem(DeferredBlock<?> block) {
            return registerSimpleBlockItem(block.getId().getPath(), block);
        }

        public DeferredItem<net.minecraft.world.item.BlockItem> registerSimpleBlockItem(
                String name,
                DeferredBlock<?> block) {
            return registerItem(name,
                    props -> new net.minecraft.world.item.BlockItem(block.get(), props),
                    () -> new net.minecraft.world.item.Item.Properties());
        }

        public <I extends net.minecraft.world.item.Item> DeferredItem<I> registerItem(
                String name,
                java.util.function.Function<net.minecraft.world.item.Item.Properties, I> factory,
                java.util.function.UnaryOperator<net.minecraft.world.item.Item.Properties> properties) {
            return registerItem(name, factory, () -> properties.apply(new net.minecraft.world.item.Item.Properties()));
        }

        public <I extends net.minecraft.world.item.Item> DeferredItem<I> registerItem(
                String name,
                Function<net.minecraft.world.item.Item.Properties, I> factory) {
            return registerItem(name, factory, () -> new net.minecraft.world.item.Item.Properties());
        }

        public <I extends net.minecraft.world.item.Item> DeferredItem<I> registerItem(
                String name,
                Function<net.minecraft.world.item.Item.Properties, I> factory,
                Supplier<net.minecraft.world.item.Item.Properties> props) {
            DeferredItem<I> holder = new DeferredItem<>();
            pending.add(() -> {
                ResourceKey<net.minecraft.world.item.Item> key =
                        ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                                Identifier.fromNamespaceAndPath(namespace, name));
                I value = factory.apply(props.get().setId(key));
                Registry.register(
                        net.minecraft.core.registries.BuiltInRegistries.ITEM,
                        Identifier.fromNamespaceAndPath(namespace, name),
                        value);
                holder.bind(value);
            });
            return holder;
        }

        public DeferredItem<net.minecraft.world.item.Item> registerSimpleItem(String name) {
            return registerItem(name, net.minecraft.world.item.Item::new, () -> new net.minecraft.world.item.Item.Properties());
        }
    }

    public static final class DataComponents extends DeferredRegister<net.minecraft.core.component.DataComponentType<?>> {
        private DataComponents(String namespace) {
            super(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, namespace);
        }

        public static DataComponents createDataComponents(
                net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<net.minecraft.core.component.DataComponentType<?>>> key,
                String namespace) {
            return new DataComponents(namespace);
        }

        @SuppressWarnings("unchecked")
        public <T> DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<T>> registerComponentType(
                String name,
                java.util.function.UnaryOperator<net.minecraft.core.component.DataComponentType.Builder<T>> builder) {
            DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<T>> holder =
                    new DeferredHolder<>();
            Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
            pending.add(() -> {
                net.minecraft.core.component.DataComponentType<T> type =
                        builder.apply(net.minecraft.core.component.DataComponentType.builder()).build();
                Registry.register(
                        net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE,
                        id,
                        type);
                holder.bind((net.minecraft.core.component.DataComponentType<T>) type);
            });
            return holder;
        }
    }
}
