package buildcraft.builders.block;

import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.items.FluidItemDrops;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.tile.TileBuilder;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockBuilder extends HorizontalDirectionalBlock implements EntityBlock {
   public static final MapCodec<BlockBuilder> CODEC = simpleCodec(BlockBuilder::new);
   public static final EnumProperty<EnumOptionalSnapshotType> SNAPSHOT_TYPE = EnumProperty.create("snapshot_type", EnumOptionalSnapshotType.class);

   public BlockBuilder(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
            .setValue(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE)
      );
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, SNAPSHOT_TYPE});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()))
         .setValue(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileBuilder(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return blockEntityType != BCBuildersBlockEntities.BUILDER ? null : (lvl, pos, st, be) -> {
         if (be instanceof TileBuilder builder) {
            builder.tick();
         }
      };
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileBuilder builder) {
         player.openMenu(builder);
      }

      return InteractionResult.SUCCESS;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TileBuilder builder) {
         builder.onPlacedBy(placer, stack);
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileBuilder builder) {
         ItemStack snapshot = builder.getSnapshot();
         if (!snapshot.isEmpty()) {
            Block.popResource(level, pos, snapshot);
         }

         for (int i = 0; i < 27; i++) {
            ItemStack stack = builder.getResource(i);
            if (!stack.isEmpty()) {
               Block.popResource(level, pos, stack);
            }
         }

         NonNullList<ItemStack> fluidDrops = NonNullList.create();
         FluidItemDrops.addFluidDrops(fluidDrops, builder.getTank(0), builder.getTank(1), builder.getTank(2), builder.getTank(3));

         for (ItemStack drop : fluidDrops) {
            Block.popResource(level, pos, drop);
         }
      }

      return super.playerWillDestroy(level, pos, state, player);
   }
}
