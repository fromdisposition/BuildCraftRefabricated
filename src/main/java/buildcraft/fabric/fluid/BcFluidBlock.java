package buildcraft.fabric.fluid;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;

public class BcFluidBlock extends LiquidBlock {
   public BcFluidBlock(FlowingFluid fluid, Properties properties) {
      super(fluid, properties);
   }

   public SoundType getSoundType(BlockState state) {
      return SoundType.HONEY_BLOCK;
   }
}
