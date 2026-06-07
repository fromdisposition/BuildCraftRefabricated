package buildcraft.robotics;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.lib.misc.NBTUtilBC;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class BoardProgrammingRecipe implements IProgrammingRecipe {
   @Override
   public String getId() {
      return "buildcraft:redstone_board";
   }

   @Override
   public List<ItemStack> getOptions(int width, int height) {
      List<ItemStack> options = new ArrayList<>(width * height);

      for (RedstoneBoardNBT<?> nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
         ItemStack stack = ItemRedstoneBoard.createStack(nbt);
         options.add(stack);
      }

      options.sort(Comparator.comparingLong((ItemStack stack) -> this.getEnergyCostMj(stack)).thenComparing(stack -> ItemRedstoneBoard.getBoardNBT(stack).getID()));
      return options;
   }

   @Override
   public long getEnergyCostMj(ItemStack option) {
      return RedstoneBoardRegistry.instance.getPowerCost(RedstoneBoardRegistry.instance.getRedstoneBoard(NBTUtilBC.getItemData(option)));
   }

   @Override
   public boolean canCraft(ItemStack input) {
      return input.getItem() instanceof ItemRedstoneBoard;
   }

   @Override
   public ItemStack craft(ItemStack input, ItemStack option) {
      return option.copy();
   }
}
