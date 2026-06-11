package buildcraft.fabric.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;

public class BcFluidBlock extends LiquidBlock {
   public BcFluidBlock(FlowingFluid fluid, Properties properties) {
      super(fluid, properties);
   }

   @Override
   protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
      if (!(level instanceof ServerLevel)) {
         return;
      }

      super.onPlace(state, level, pos, oldState, movedByPiston);
   }

   public SoundType getSoundType(BlockState state) {
      return SoundType.HONEY_BLOCK;
   }
}
