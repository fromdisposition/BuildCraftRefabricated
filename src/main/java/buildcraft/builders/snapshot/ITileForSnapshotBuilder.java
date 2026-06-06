package buildcraft.builders.snapshot;

import buildcraft.api.core.IPlayerOwned;
import buildcraft.api.mj.MjBattery;
import buildcraft.lib.fluids.FluidStack;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public interface ITileForSnapshotBuilder extends IPlayerOwned {
   Level getWorldBC();

   MjBattery getBattery();

   BlockPos getBuilderPos();

   boolean canExcavate();

   SnapshotBuilder<?> getBuilder();

   default EnumFluidHandlingMode getFluidMode() {
      return EnumFluidHandlingMode.NO_REPLACE;
   }

   default EnumContainerContentsMode getContainerContentsMode() {
      return EnumContainerContentsMode.INCLUDE;
   }

   default ItemStack getBreakingTool() {
      return new ItemStack(Items.DIAMOND_PICKAXE);
   }

   default void onBlockBroken(BlockPos brokenPos, List<ItemStack> drops, int xp, FluidStack capturedFluid) {
   }
}
