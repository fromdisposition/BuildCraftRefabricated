package buildcraft.api.facades;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class FacadeAPI {
   public static final String IMC_MOD_TARGET = "buildcraftrefabricated";
   public static final String IMC_FACADE_DISABLE = "facade_disable_block";
   public static final String IMC_FACADE_CUSTOM = "facade_custom_map_block_item";
   public static final String NBT_CUSTOM_BLOCK_REG_KEY = "block_registry_name";
   public static final String NBT_CUSTOM_BLOCK_META = "block_meta";
   public static final String NBT_CUSTOM_ITEM_STACK = "item_stack";
   public static IFacadeItem facadeItem;
   public static IFacadeRegistry registry;

   private FacadeAPI() {
   }

   public static void disableBlock(Block block) {
   }

   public static void mapStateToStack(BlockState state, ItemStack stack) {
      CompoundTag nbt = new CompoundTag();
      nbt.putString("block_registry_name", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
      nbt.putInt("block_meta", 0);
      nbt.put("item_stack", new CompoundTag());
   }

   public static boolean isFacadeMessageId(String id) {
      return "facade_custom_map_block_item".equals(id) || "facade_disable_block".equals(id);
   }
}
