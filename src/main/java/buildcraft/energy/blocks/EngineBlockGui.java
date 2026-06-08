package buildcraft.energy.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EngineBlockGui {
   private EngineBlockGui() {
   }

   public static <T extends BlockEntity & MenuProvider> InteractionResult open(Level level, BlockPos pos, Player player, Class<T> tileType) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      if (player instanceof ServerPlayer serverPlayer) {
         BlockEntity blockEntity = level.getBlockEntity(pos);
         if (tileType.isInstance(blockEntity) && blockEntity instanceof MenuProvider menu) {
            serverPlayer.openMenu(menu);
         }
      }

      return InteractionResult.SUCCESS;
   }
}
