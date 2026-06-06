package buildcraft.factory.integration.jei;

import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.container.ContainerDistiller;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.integration.jei.JeiTransferUtil;
import java.util.Optional;
import javax.annotation.Nullable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DistillerTransferHandler implements IRecipeTransferHandler<ContainerDistiller, IRefineryRecipeManager.IDistillationRecipe> {
   private final IRecipeTransferHandlerHelper helper;

   public DistillerTransferHandler(IRecipeTransferHandlerHelper helper) {
      this.helper = helper;
   }

   public Class<? extends ContainerDistiller> getContainerClass() {
      return ContainerDistiller.class;
   }

   public Optional<MenuType<ContainerDistiller>> getMenuType() {
      return Optional.of(BCFactoryMenuTypes.DISTILLER);
   }

   public IRecipeType<IRefineryRecipeManager.IDistillationRecipe> getRecipeType() {
      return DistillerRecipeTypes.DISTILLER;
   }

   @Nullable
   public IRecipeTransferError transferRecipe(
      ContainerDistiller container,
      IRefineryRecipeManager.IDistillationRecipe recipe,
      IRecipeSlotsView recipeSlots,
      Player player,
      boolean maxTransfer,
      boolean doTransfer
   ) {
      FluidStack in = recipe.in();
      Item bucket = in != null && !in.isEmpty() ? in.getFluid().getBucket() : Items.AIR;
      if (bucket != Items.AIR && JeiTransferUtil.countMatching(player.getInventory(), new ItemStack(bucket)) >= 1) {
         if (doTransfer) {
            String bucketId = BuiltInRegistries.ITEM.getKey(bucket).toString();
            container.sendMessage(103, buf -> {
               buf.writeVarInt(1);
               buf.writeVarInt(0);
               buf.writeUtf(bucketId);
            });
         }

         return null;
      } else {
         return this.helper.createUserErrorWithTooltip(Component.translatable("gui.jei.transfer.buildcraft.missing"));
      }
   }
}
