package buildcraft.lib.client.guide.parts.recipe;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

public interface IStackRecipes {
    List<GuidePartFactory> getUsages(@Nonnull ItemStack stack);

    List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack);
}
