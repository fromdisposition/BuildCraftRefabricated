package buildcraft.fabric.fluid;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class BcFluidEntityEffects {
   private BcFluidEntityEffects() {
   }

   static void apply(BcOilFluid.Holder holder, Entity entity) {
      if (!holder.props.gaseous() && holder.props.sticky()) {
         BlockState blockState = holder.block.defaultBlockState();
         entity.makeStuckInBlock(blockState, new Vec3(0.25, 0.05, 0.25));
      }
   }
}
