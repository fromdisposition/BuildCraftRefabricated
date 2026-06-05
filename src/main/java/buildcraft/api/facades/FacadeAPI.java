package buildcraft.api.facades;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

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
        nbt.putString(NBT_CUSTOM_BLOCK_REG_KEY, net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        nbt.putInt(NBT_CUSTOM_BLOCK_META, 0);
        nbt.put(NBT_CUSTOM_ITEM_STACK, new net.minecraft.nbt.CompoundTag());
    }

    public static boolean isFacadeMessageId(String id) {
        return IMC_FACADE_CUSTOM.equals(id)
                || IMC_FACADE_DISABLE.equals(id);
    }
}
