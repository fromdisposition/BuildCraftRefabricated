package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import com.mojang.serialization.DataResult;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RequiredExtractorItemsList extends RequiredExtractor {
   private NbtPath path = null;

   private static ItemStack parseItemStack(CompoundTag ct) {
      DataResult<ItemStack> result = ItemStack.CODEC.parse(NbtOps.INSTANCE, ct);
      return result.resultOrPartial().orElse(ItemStack.EMPTY);
   }

   @Nonnull
   @Override
   public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
      if (tileNbt != null && this.path != null) {
         Tag result = this.path.get(tileNbt);
         return NBTUtilBC.readCompoundList(result)
            .map(RequiredExtractorItemsList::parseItemStack)
            .filter(stack -> !stack.isEmpty())
            .collect(Collectors.toList());
      } else {
         return Collections.emptyList();
      }
   }

   @Nonnull
   @Override
   public List<ItemStack> extractItemsFromEntity(@Nonnull CompoundTag entityNbt) {
      if (this.path == null) {
         return Collections.emptyList();
      }

      Tag result = this.path.get(entityNbt);
      return NBTUtilBC.readCompoundList(result).map(RequiredExtractorItemsList::parseItemStack).filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
   }

   @Override
   public void clearItemsFromBlock(@Nonnull CompoundTag tileNbt) {
      if (this.path != null) {
         this.path.remove(tileNbt);
      }
   }
}
