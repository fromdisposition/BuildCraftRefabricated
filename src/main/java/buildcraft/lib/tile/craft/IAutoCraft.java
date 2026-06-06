package buildcraft.lib.tile.craft;

import buildcraft.lib.tile.ItemHandlerSimple;
import net.minecraft.world.item.ItemStack;

public interface IAutoCraft {
   ItemStack getCurrentRecipeOutput();

   ItemHandlerSimple getInvBlueprint();
}
