package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public interface IStackRecipes {
   List<GuidePartFactory> getUsages(@Nonnull ItemStack var1);

   List<GuidePartFactory> getRecipes(@Nonnull ItemStack var1);
}
