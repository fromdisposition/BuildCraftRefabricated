package buildcraft.api.schematics;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.lib.fluids.FluidStack;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public interface ISchematicBlock {
   void init(SchematicBlockContext var1);

   default boolean isAir() {
      return false;
   }

   @Nonnull
   default Set<BlockPos> getRequiredBlockOffsets() {
      return Collections.emptySet();
   }

   @Nonnull
   default List<ItemStack> computeRequiredItems() {
      return Collections.emptyList();
   }

   @Nonnull
   default List<FluidStack> computeRequiredFluids() {
      return Collections.emptyList();
   }

   @Nullable
   default BlockState getBlockStateForRender() {
      return null;
   }

   @Nullable
   default CompoundTag getTileNbtForRender() {
      return null;
   }

   ISchematicBlock getRotated(Rotation var1);

   boolean canBuild(Level var1, BlockPos var2);

   default boolean isReadyToBuild(Level world, BlockPos blockPos) {
      return true;
   }

   boolean build(Level var1, BlockPos var2);

   boolean buildWithoutChecks(Level var1, BlockPos var2);

   boolean isBuilt(Level var1, BlockPos var2);

   CompoundTag serializeNBT();

   void deserializeNBT(CompoundTag var1) throws InvalidInputDataException;
}
