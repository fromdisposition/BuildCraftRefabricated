package buildcraft.silicon.integration.jei;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record AssemblyRecipeJei(String id, List<List<ItemStack>> inputSlots, List<ItemStack> outputs, long microJoules, int focusLinkInputIndex) {
   public AssemblyRecipeJei(String id, List<List<ItemStack>> inputSlots, List<ItemStack> outputs, long microJoules) {
      this(id, inputSlots, outputs, microJoules, -1);
   }
}
