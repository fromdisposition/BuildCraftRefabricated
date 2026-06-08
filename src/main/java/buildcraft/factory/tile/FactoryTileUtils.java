package buildcraft.factory.tile;

import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fluids.FluidStack;
import net.minecraft.world.level.storage.ValueInput;

public final class FactoryTileUtils {
   private FactoryTileUtils() {
   }

   public static void loadTank(SingleFluidTank tank, ValueInput input, String key) {
      FluidStack fluid = input.read(key, FluidStack.CODEC).orElse(FluidStack.EMPTY);
      if (fluid.isEmpty()) {
         tank.setContents(FluidStack.EMPTY);
      } else {
         tank.setContents(fluid);
      }
   }
}
