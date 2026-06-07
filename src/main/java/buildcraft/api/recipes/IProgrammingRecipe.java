package buildcraft.api.recipes;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface IProgrammingRecipe {
   String getId();

   List<ItemStack> getOptions(int width, int height);

   long getEnergyCostMj(ItemStack option);

   boolean canCraft(ItemStack input);

   ItemStack craft(ItemStack input, ItemStack option);
}
