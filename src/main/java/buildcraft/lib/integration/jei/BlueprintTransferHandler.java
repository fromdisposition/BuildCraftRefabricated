package buildcraft.lib.integration.jei;

import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.misc.RegistryKeyUtil;
import java.util.Optional;
import javax.annotation.Nullable;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class BlueprintTransferHandler<C extends AbstractContainerMenu> implements IRecipeTransferHandler<C, RecipeHolder<CraftingRecipe>> {
   public static final int NET_JEI_RECIPE_TRANSFER = 100;
   private final Class<? extends C> containerClass;
   private final MenuType<C> menuType;

   public BlueprintTransferHandler(Class<? extends C> containerClass, MenuType<C> menuType) {
      this.containerClass = containerClass;
      this.menuType = menuType;
   }

   public Class<? extends C> getContainerClass() {
      return this.containerClass;
   }

   public Optional<MenuType<C>> getMenuType() {
      return Optional.of(this.menuType);
   }

   public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
      return RecipeTypes.CRAFTING;
   }

   @Nullable
   public IRecipeTransferError transferRecipe(
      C container, RecipeHolder<CraftingRecipe> recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer
   ) {
      if (!doTransfer) {
         return null;
      }

      if (container instanceof BcMenu bcContainer) {
         String recipeIdStr = RegistryKeyUtil.id(recipe.id()).toString();
         bcContainer.sendMessage(100, buf -> buf.writeUtf(recipeIdStr));
      }

      return null;
   }
}
