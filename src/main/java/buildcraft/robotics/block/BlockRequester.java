package buildcraft.robotics.block;

import buildcraft.lib.misc.BlockDropsUtil;
import buildcraft.robotics.BCRoboticsBlockEntities;
import buildcraft.robotics.container.ContainerRequester;
import buildcraft.robotics.tile.TileRequester;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockRequester extends BaseEntityBlock {
   public static final MapCodec<BlockRequester> CODEC = simpleCodec(BlockRequester::new);

   public BlockRequester(Properties properties) {
      super(properties);
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileRequester(pos, state);
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, final BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      final BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof TileRequester requester && player instanceof ServerPlayer serverPlayer) {
         serverPlayer.openMenu(new ExtendedMenuProvider<BlockPos>() {
            public BlockPos getScreenOpeningData(ServerPlayer player) {
               return pos;
            }

            public Component getDisplayName() {
               return be.getBlockState().getBlock().getName();
            }

            public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player p) {
               return new ContainerRequester(containerId, inv, requester);
            }
         });
         return InteractionResult.SUCCESS;
      }

      return InteractionResult.PASS;
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof TileRequester requester) {
         BlockDropsUtil.dropItems(level, pos, requester.invItems);
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   @Nullable
   public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type
   ) {
      return null;
   }
}
