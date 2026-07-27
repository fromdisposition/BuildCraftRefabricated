package buildcraft.fabric;

import buildcraft.fabric.config.BCObjectsConfig;
import com.mojang.logging.LogUtils;
import java.util.function.Function;
import java.util.function.UnaryOperator;
//? if >= 1.21.10 {
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
//?}
import org.slf4j.Logger;
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
   private static final Logger LOGGER = LogUtils.getLogger();

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
      if (!BCObjectsConfig.isBlockEnabled(modid, path)) {
         LOGGER.info("Skipping disabled block {}:{}", modid, path);
         return null;
      }

      ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id(modid, path));
      //? if >= 1.21.10 {
      B block = (B)factory.apply(properties.apply(Properties.of()).setId(blockKey));
      //?} else {
      /*B block = (B)factory.apply(properties.apply(Properties.of()));
      *///?}
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
      return registerItem(modid, path, factory, properties, false);
   }

   public static <I extends Item> I registerItemDynamicName(String modid, String path, Function<net.minecraft.world.item.Item.Properties, I> factory) {
      return registerItem(modid, path, factory, UnaryOperator.identity(), true);
   }

   /**
    * Like {@link #registerItem} but for items that build their display name in {@code getName(ItemStack)} (wire
    * colour, fluid shard, gate/lens/facade variants, redstone board, paintbrush). On 1.21.1
    * {@code ItemStack.getHoverName()} returns the ITEM_NAME component BEFORE consulting {@code Item.getName(stack)},
    * so a generic ITEM_NAME ("item.modid.path") short-circuits the dynamic name (the wire showed the raw key, the
    * shard lost its fluid). We therefore omit ITEM_NAME for these on 1.21.1, letting getHoverName fall through to
    * getName. On >= 1.21.10 getName is reached regardless, so ITEM_NAME is still set there (no behaviour change).
    */
   public static <I extends Item> I registerItemDynamicName(
      String modid,
      String path,
      Function<net.minecraft.world.item.Item.Properties, I> factory,
      UnaryOperator<net.minecraft.world.item.Item.Properties> properties
   ) {
      return registerItem(modid, path, factory, properties, true);
   }

   private static <I extends Item> I registerItem(
      String modid,
      String path,
      Function<net.minecraft.world.item.Item.Properties, I> factory,
      UnaryOperator<net.minecraft.world.item.Item.Properties> properties,
      boolean dynamicName
   ) {
      if (!BCObjectsConfig.isItemEnabled(modid, path)) {
         LOGGER.info("Skipping disabled item {}:{}", modid, path);
         return null;
      }

      ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(modid, path));
      String nameKey = "item." + modid + "." + path;
      net.minecraft.world.item.Item.Properties props = properties.apply(new net.minecraft.world.item.Item.Properties());
      //? if >= 1.21.10 {
      props.setId(itemKey);
      //?}
      boolean setItemName = true;
      //? if < 1.21.10 {
      /*setItemName = !dynamicName;
      *///?}
      if (setItemName) {
         props.component(DataComponents.ITEM_NAME, Component.translatable(nameKey));
      }

      I item = (I)factory.apply(props);
      return (I)Registry.register(BuiltInRegistries.ITEM, itemKey, item);
   }

   public static BlockItem registerBlockItem(String modid, String path, Block block) {
      return registerBlockItem(modid, path, block, UnaryOperator.identity());
   }

   public static BlockItem registerBlockItem(String modid, String path, Block block, UnaryOperator<net.minecraft.world.item.Item.Properties> properties) {
      if (block == null) {
         return null;
      }

      return registerItem(modid, path, props -> new BlockItem(block, props), properties);
   }

   public static <T> DataComponentType<T> registerDataComponent(String modid, String path, UnaryOperator<Builder<T>> builder) {
      ResourceKey<DataComponentType<?>> key = ResourceKey.create(Registries.DATA_COMPONENT_TYPE, id(modid, path));
      DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
      return (DataComponentType<T>)Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key, type);
   }

   public static <T extends Entity> EntityType<T> registerEntityType(String modid, String path, net.minecraft.world.entity.EntityType.Builder<T> builder) {
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id(modid, path));
      //? if >= 1.21.10 {
      return (EntityType<T>)Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
      //?} else {
      /*return (EntityType<T>)Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(id(modid, path).toString()));
      *///?}
   }

   @SafeVarargs
   public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
      String modid, String path, BCRegistries.BlockEntityFactory<T> factory, Block... validBlocks
   ) {
      ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id(modid, path));
      //? if >= 1.21.10 {
      return (BlockEntityType<T>)Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE, key, FabricBlockEntityTypeBuilder.create(factory::create, validBlocks).build()
      );
      //?} else {
      /*// Fabric deprecated FabricBlockEntityTypeBuilder once vanilla's BlockEntityType.Builder was patched to
      // accept modded blocks; use the vanilla builder directly (build(null) = no datafixer type).
      return (BlockEntityType<T>)Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE, key, BlockEntityType.Builder.of(factory::create, validBlocks).build(null)
      );
      *///?}
   }

   @FunctionalInterface
   public interface BlockEntityFactory<T extends BlockEntity> {
      T create(BlockPos var1, BlockState var2);
   }
}
