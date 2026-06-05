package buildcraft.fabric;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.core.BCCore;

public final class BCRegistries {
    private BCRegistries() {}

    public static Identifier id(String modid, String path) {
        return Identifier.fromNamespaceAndPath(modid, path);
    }

    public static <B extends Block> B registerBlock(
            String modid,
            String path,
            Function<BlockBehaviour.Properties, B> factory) {
        return registerBlock(modid, path, factory, UnaryOperator.identity());
    }

    public static <B extends Block> B registerBlock(
            String modid,
            String path,
            Function<BlockBehaviour.Properties, B> factory,
            UnaryOperator<BlockBehaviour.Properties> properties) {
        Identifier location = id(modid, path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, location);
        B block = factory.apply(properties.apply(BlockBehaviour.Properties.of()).setId(blockKey));
        return Registry.register(BuiltInRegistries.BLOCK, location, block);
    }

    public static <I extends Item> I registerItem(String modid, String path, Function<Item.Properties, I> factory) {
        return registerItem(modid, path, factory, UnaryOperator.identity());
    }

    public static <I extends Item> I registerItem(
            String modid,
            String path,
            Function<Item.Properties, I> factory,
            UnaryOperator<Item.Properties> properties) {
        Identifier location = id(modid, path);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, location);

        String nameKey = "item." + modid + "." + path;
        I item = factory.apply(properties.apply(new Item.Properties())
                .setId(itemKey)
                .component(DataComponents.ITEM_NAME, Component.translatable(nameKey)));
        return Registry.register(BuiltInRegistries.ITEM, location, item);
    }

    public static BlockItem registerBlockItem(String modid, String path, Block block) {
        return registerBlockItem(modid, path, block, UnaryOperator.identity());
    }

    public static BlockItem registerBlockItem(
            String modid,
            String path,
            Block block,
            UnaryOperator<Item.Properties> properties) {
        return registerItem(modid, path, props -> new BlockItem(block, props), properties);
    }

    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    public static <T> DataComponentType<T> registerDataComponent(
            String modid,
            String path,
            java.util.function.UnaryOperator<DataComponentType.Builder<T>> builder) {
        Identifier location = id(modid, path);
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, location, type);
    }

    public static <T extends net.minecraft.world.entity.Entity> EntityType<T> registerEntityType(
            String modid,
            String path,
            EntityType.Builder<T> builder) {
        Identifier location = id(modid, path);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                location,
                builder.build(ResourceKey.create(Registries.ENTITY_TYPE, location)));
    }

    @SafeVarargs
    public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String modid,
            String path,
            BlockEntityFactory<T> factory,
            Block... validBlocks) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id(modid, path),
                FabricBlockEntityTypeBuilder.<T>create(factory::create, validBlocks).build());
    }
}
