package buildcraft.fabric;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType.Builder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Fluid;

public final class BCRegistries {
   private BCRegistries() {
   }

   public static Identifier id(String modid, String path) {
      return Identifier.fromNamespaceAndPath(modid, path);
   }

   public static ResourceKey<CreativeModeTab> creativeTabKey(String modid, String path) {
      return ResourceKey.create(Registries.CREATIVE_MODE_TAB, id(modid, path));
   }

   public static <T extends AbstractContainerMenu> MenuType<T> registerMenuType(String modid, String path, MenuType<T> type) {
      ResourceKey<MenuType<?>> key = ResourceKey.create(Registries.MENU, id(modid, path));
      return (MenuType<T>)Registry.register(BuiltInRegistries.MENU, key, type);
   }

   public static CreativeModeTab registerCreativeTab(String modid, String path, CreativeModeTab tab) {
      ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id(modid, path));
      return (CreativeModeTab)Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
   }

   public static <F extends Fluid> F registerFluid(String modid, String path, F fluid) {
      ResourceKey<Fluid> key = ResourceKey.create(Registries.FLUID, id(modid, path));
      return (F)Registry.register(BuiltInRegistries.FLUID, key, fluid);
   }

   public static <B extends Block> B registerBlock(String modid, String path, Function<Properties, B> factory) {
      return registerBlock(modid, path, factory, UnaryOperator.identity());
   }

   public static <B extends Block> B registerBlock(String modid, String path, Function<Properties, B> factory, UnaryOperator<Properties> properties) {
      ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id(modid, path));
      B block = (B)factory.apply(properties.apply(Properties.of()).setId(blockKey));
      return (B)Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
   }

   public static <I extends Item> I registerItem(String modid, String path, Function<net.minecraft.world.item.Item.Properties, I> factory) {
      return registerItem(modid, path, factory, UnaryOperator.identity());
   }

   public static <I extends Item> I registerItem(
      String modid,
      String path,
      Function<net.minecraft.world.item.Item.Properties, I> factory,
      UnaryOperator<net.minecraft.world.item.Item.Properties> properties
   ) {
      ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(modid, path));
      String nameKey = "item." + modid + "." + path;
      I item = (I)factory.apply(
         properties.apply(new net.minecraft.world.item.Item.Properties()).setId(itemKey).component(DataComponents.ITEM_NAME, Component.translatable(nameKey))
      );
      return (I)Registry.register(BuiltInRegistries.ITEM, itemKey, item);
   }

   public static BlockItem registerBlockItem(String modid, String path, Block block) {
      return registerBlockItem(modid, path, block, UnaryOperator.identity());
   }

   public static BlockItem registerBlockItem(String modid, String path, Block block, UnaryOperator<net.minecraft.world.item.Item.Properties> properties) {
      return registerItem(modid, path, props -> new BlockItem(block, props), properties);
   }

   public static <T> DataComponentType<T> registerDataComponent(String modid, String path, UnaryOperator<Builder<T>> builder) {
      ResourceKey<DataComponentType<?>> key = ResourceKey.create(Registries.DATA_COMPONENT_TYPE, id(modid, path));
      DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
      return (DataComponentType<T>)Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key, type);
   }

   public static <T extends Entity> EntityType<T> registerEntityType(String modid, String path, net.minecraft.world.entity.EntityType.Builder<T> builder) {
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id(modid, path));
      return (EntityType<T>)Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
   }

   @SafeVarargs
   public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
      String modid, String path, BCRegistries.BlockEntityFactory<T> factory, Block... validBlocks
   ) {
      ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id(modid, path));
      return (BlockEntityType<T>)Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE, key, FabricBlockEntityTypeBuilder.create(factory::create, validBlocks).build()
      );
   }

   @FunctionalInterface
   public interface BlockEntityFactory<T extends BlockEntity> {
      T create(BlockPos var1, BlockState var2);
   }
}
