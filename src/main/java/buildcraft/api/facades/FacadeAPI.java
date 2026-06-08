package buildcraft.api.facades;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Facade item/registry access, IMC identifiers, and helper methods for addon integration. */
public final class FacadeAPI {
   public static final String IMC_MOD_TARGET = "buildcraftrefabricated";
   public static final String IMC_FACADE_DISABLE = "facade_disable_block";
   public static final String IMC_FACADE_CUSTOM = "facade_custom_map_block_item";
   public static final String NBT_CUSTOM_BLOCK_REG_KEY = "block_registry_name";
   public static final String NBT_CUSTOM_BLOCK_META = "block_meta";
   public static final String NBT_CUSTOM_ITEM_STACK = "item_stack";
   @Nullable
   public static IFacadeItem facadeItem;
   @Nullable
   public static IFacadeRegistry registry;

   private FacadeAPI() {
   }

   public static void disableBlock(Block block) {
      if (registry != null) {
         registry.disableBlock(block, IMC_MOD_TARGET);
      }
   }

   public static void mapStateToStack(BlockState state, ItemStack stack) {
      if (registry != null) {
         registry.mapStateToStack(state, stack);
      }
   }

   public static boolean isFacadeMessageId(String id) {
      return "facade_custom_map_block_item".equals(id) || "facade_disable_block".equals(id);
   }
}
