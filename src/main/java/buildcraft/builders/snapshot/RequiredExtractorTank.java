package buildcraft.builders.snapshot;

import buildcraft.lib.fluids.FluidStack;
import com.mojang.serialization.DataResult;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.state.BlockState;

public class RequiredExtractorTank extends RequiredExtractor {
   private NbtPath path = null;

   private static FluidStack parseFluidStack(CompoundTag ct) {
      DataResult<FluidStack> result = FluidStack.CODEC.parse(NbtOps.INSTANCE, ct);
      return result.resultOrPartial().orElse(FluidStack.EMPTY);
   }

   @Nonnull
   @Override
   public List<FluidStack> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
      if (tileNbt != null && this.path != null) {
         if (this.path.get(tileNbt) instanceof CompoundTag ct && !ct.contains("Empty")) {
            FluidStack stack = parseFluidStack(ct);
            if (!stack.isEmpty()) {
               return Collections.singletonList(stack);
            }
         }

         return Collections.emptyList();
      } else {
         return Collections.emptyList();
      }
   }

   @Nonnull
   @Override
   public List<FluidStack> extractFluidsFromEntity(@Nonnull CompoundTag entityNbt) {
      if (this.path == null) {
         return Collections.emptyList();
      }

      if (this.path.get(entityNbt) instanceof CompoundTag ct && !ct.contains("Empty")) {
         FluidStack stack = parseFluidStack(ct);
         if (!stack.isEmpty()) {
            return Collections.singletonList(stack);
         }
      }

      return Collections.emptyList();
   }
}
