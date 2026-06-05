package buildcraft.lib.tile.craft;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.tile.item.ItemHandlerSimple;

public interface IAutoCraft {
    ItemStack getCurrentRecipeOutput();

    ItemHandlerSimple getInvBlueprint();
}
