package buildcraft.fabric.fluid;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

interface BcFluidPhysicsHost {
   BcOilFluid.Holder holder();

   void spreadTo(LevelAccessor var1, BlockPos var2, BlockState var3, Direction var4, FluidState var5);

   FluidState getNewLiquid(ServerLevel var1, BlockPos var2, BlockState var3);

   Map<Direction, FluidState> getSpread(ServerLevel var1, BlockPos var2, BlockState var3);

   int getDropOff(LevelReader var1);

   FlowingFluid self();
}
