package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

public class RecipeLookupHelper {
    public static final Map<String, IStackRecipes> handlerTypes = new HashMap<>();

    static {
        handlerTypes.put("smelting", GuideSmeltingRecipes.INSTANCE);
        handlerTypes.put("crafting", GuideCraftingRecipes.INSTANCE);
        handlerTypes.put("assembling", GuideAssemblyRecipes.INSTANCE);
    }

    public static List<GuidePartFactory> getAllUsages(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : handlerTypes.values()) {
            List<GuidePartFactory> recipes = handler.getUsages(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
        }
        return list;
    }

    public static List<GuidePartFactory> getAllRecipes(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : handlerTypes.values()) {
            List<GuidePartFactory> recipes = handler.getRecipes(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
        }
        return list;
    }
}
