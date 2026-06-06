package buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RequiredExtractorItemFromBlock extends RequiredExtractor {
   @Nonnull
   @Override
   public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
      return Collections.singletonList(new ItemStack(blockState.getBlock().asItem()));
   }
}
