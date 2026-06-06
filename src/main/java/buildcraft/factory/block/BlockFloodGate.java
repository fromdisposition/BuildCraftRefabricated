package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.lib.misc.BlockDropsUtil;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockFloodGate extends BaseEntityBlock {
   public static final MapCodec<BlockFloodGate> CODEC = simpleCodec(BlockFloodGate::new);
   public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = new HashMap<>(BuildCraftProperties.CONNECTED_MAP);

   public BlockFloodGate(Properties properties) {
      super(properties);
      BlockState defaultState = (BlockState)this.stateDefinition.any();

      for (Property<Boolean> prop : CONNECTED_MAP.values()) {
         defaultState = (BlockState)defaultState.setValue(prop, true);
      }

      this.registerDefaultState(defaultState);
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      for (Property<Boolean> prop : CONNECTED_MAP.values()) {
         builder.add(prop);
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileFloodGate(pos, state);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TileFloodGate floodGate) {
         floodGate.onPlacedBy(placer);
      }
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null : createTickerHelper(type, BCFactoryBlockEntities.FLOOD_GATE, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (!(stack.getItem() instanceof IToolWrench wrench)) {
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      } else {
         Direction side = hitResult.getDirection();
         if (side != Direction.UP && CONNECTED_MAP.containsKey(side)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileFloodGate floodGate) {
               boolean nowOpen;
               if (!floodGate.openSides.remove(side)) {
                  floodGate.openSides.add(side);
                  nowOpen = true;
               } else {
                  nowOpen = false;
               }

               floodGate.onSidesToggled();
               BlockState newState = state;

               for (Entry<Direction, Property<Boolean>> entry : CONNECTED_MAP.entrySet()) {
                  newState = (BlockState)newState.setValue(entry.getValue(), floodGate.openSides.contains(entry.getKey()));
               }

               level.setBlock(pos, newState, 2);
               floodGate.setChanged();
               level.playSound(
                  null,
                  pos,
                  nowOpen ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                  SoundSource.BLOCKS,
                  1.0F,
                  level.getRandom().nextFloat() * 0.1F + 0.9F
               );
            }

            wrench.wrenchUsed(player, hand, stack, hitResult);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof TileFloodGate floodGate) {
         BlockDropsUtil.dropFluidShard(level, pos, floodGate.fluidTank.getFluidStack());
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   static {
      CONNECTED_MAP.remove(Direction.UP);
   }
}
