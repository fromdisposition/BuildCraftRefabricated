package buildcraft.core.block;

import buildcraft.core.tile.TilePowerConsumerTester;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;

public class BlockPowerConsumerTester extends Block implements EntityBlock {
   public BlockPowerConsumerTester(Properties properties) {
      super(properties);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TilePowerConsumerTester(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null : (lvl, pos, st, be) -> {
         if (be instanceof TilePowerConsumerTester tester) {
            tester.serverTick();
         }
      };
   }
}
